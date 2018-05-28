package com.example.bojidar.sofiapublictransportapp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.bojidar.sofiapublictransportapp.R;
import com.example.bojidar.sofiapublictransportapp.asynctasks.ApiCallAsyncTask;
import com.example.bojidar.sofiapublictransportapp.dialogs.TimingDialog;
import com.example.bojidar.sofiapublictransportapp.model.Line;
import com.example.bojidar.sofiapublictransportapp.model.Stop;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CustomMapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the factory method to
 * create an instance of this fragment.
 */
public class CustomMapFragment extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {


    private static final int REQUEST_LOCATION_CODE = 99;
    private OnFragmentInteractionListener mListener;
    private MapView mMapView;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;

    private List<Stop> stops;
    private Set<LatLng> displayedStopsPosition;
    private Marker clickedMarker;

    public CustomMapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_custom_map, container, false);

        mMapView = (MapView) rootView.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        stops = new ArrayList<>();

        Bundle bundle = getArguments();
        if(bundle!=null&&bundle.size()>0){
            stops = (ArrayList<Stop>) bundle.getSerializable("Stops");
        }

        displayedStopsPosition = new HashSet<>();



        return rootView;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng sofia = new LatLng(42.701725, 23.323174);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sofia, 13.4f));

        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);

        final Set<Stop> stopsTemp = new HashSet<>();

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng point) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15.8f),500,null);

                double latPoint = point.latitude;
                double longPoint = point.longitude;

                for(int pos=0;pos< stops.size();pos++){
                    if(stops.get(pos).getLat()<=(latPoint+0.005) && stops.get(pos).getLat()>=(latPoint-0.005) &&
                            stops.get(pos).getLng()<=(longPoint+0.005) && stops.get(pos).getLng()>=(longPoint-0.005)){

                        LatLng latLngTemp = new LatLng(stops.get(pos).getLat(),stops.get(pos).getLng());
                        if(!displayedStopsPosition.contains(latLngTemp)) {
                            stopsTemp.add(stops.get(pos));
                            displayedStopsPosition.add(latLngTemp);
                        }
                    }
                }

                for(Stop stop: stopsTemp){
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(stop.getLat(),stop.getLng());
                    markerOptions.position(latLng);
                    markerOptions.title(stop.getStopName());
                    markerOptions.snippet(""+stop.getStopID());
                    //call method for correct stop icon
                    markerOptions.icon(giveCorrectStopIcon(stop));
                    Marker marker = mMap.addMarker(markerOptions);

                }
                stopsTemp.clear();
            }
        });

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                if(clickedMarker!=null) {

                        String stopNameCode = clickedMarker.getTitle() + " - " + clickedMarker.getSnippet();

                        TimingDialog timingDialog = new TimingDialog();
                        timingDialog.setLinesArrivalsListener(clickedMarker.getSnippet(), stopNameCode,
                                (TimingDialog.onTiminigDialogIteractionListener)getActivity());
                        timingDialog.show(getActivity().getFragmentManager(), "TIMING DIALOG");

                    clickedMarker = null;
                }
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                float zoom = 15.8f;
                if(mMap.getCameraPosition().zoom>zoom){
                    zoom = mMap.getCameraPosition().zoom;
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), zoom),500,null);
                clickedMarker = marker;

                return true;
            }
        });

    }

    public void callStopArrivalsFromSearch(String stopCode,String stopNameCode){
        TimingDialog timingDialog = new TimingDialog();
        timingDialog.setLinesArrivalsListener(stopCode, stopNameCode,(TimingDialog.onTiminigDialogIteractionListener)getActivity());
        timingDialog.show(getActivity().getFragmentManager(), "TIMING DIALOG");
    }

    private BitmapDescriptor giveCorrectStopIcon(Stop stop){
        List<Integer> linesType = stop.getTypesTransport();
        Bitmap icon =null;

        if(linesType.size()==1){
            switch(linesType.get(0)){
                case 0:
                    return BitmapDescriptorFactory.fromResource(R.drawable.b);
                case 1:
                    return BitmapDescriptorFactory.fromResource(R.drawable.a);
                case 2:
                    return BitmapDescriptorFactory.fromResource(R.drawable.c);
            }
        }
        // Never mind
        if(linesType.size()==2){
            if(linesType.contains(0)){
                if(linesType.contains(1)){
                    return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                }else{
                    return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                }
            }else{
                    return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }
        }
        if(linesType.size()==3){
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
        }

        return BitmapDescriptorFactory.fromBitmap(icon);
    }

    protected synchronized void buildGoogleApiClient(){
        client = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }


    @Override
    public void onLocationChanged(Location location) {
        lastLocation = location;

        if(currentLocationMarker!=null){
            currentLocationMarker.remove();
        }



//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("ZDR");
//        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

//        currentLocationMarker = mMap.addMarker(markerOptions);


//        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        mMap.animateCamera(CameraUpdateFactory.zoomBy(150));


        if(client != null){
            LocationServices.FusedLocationApi.removeLocationUpdates(client,this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
        }
    }

    public boolean checkLocationPermission() {
        if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }else{
                ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
            }
            return false;
        }else{
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_LOCATION_CODE:
                if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    //permission granted
                    if(ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                        if(client!=null){
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }else{ //not granted
                        Toast.makeText(getActivity(), "Permission for current location denied!", Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
