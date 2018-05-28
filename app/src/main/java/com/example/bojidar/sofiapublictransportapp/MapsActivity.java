package com.example.bojidar.sofiapublictransportapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.example.bojidar.sofiapublictransportapp.dialogs.TimingDialog;
import com.example.bojidar.sofiapublictransportapp.fragments.CustomMapFragment;
import com.example.bojidar.sofiapublictransportapp.fragments.Favourites;
import com.example.bojidar.sofiapublictransportapp.model.Stop;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.example.bojidar.sofiapublictransportapp.StopsManager.favStopsList;

public class MapsActivity extends FragmentActivity implements TimingDialog.onTiminigDialogIteractionListener {

    public static final int REQUEST_LOCATION_CODE = 99;

    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    private Location lastLocation;
    private Marker currentLocationMarker;

    private AutoCompleteTextView stopSearchTV;
    private Button clearTextButton;

    private List<Stop> stops;

    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        stops = new ArrayList<>();

        stopSearchTV = (AutoCompleteTextView) findViewById(R.id.search_auto_tv);
        clearTextButton = (Button) findViewById(R.id.clear_text_button);

        //reads all fav stops written in the shared prefs
        StopsManager.getInstance().initFavStopsListFromSharePrefs(this);

        String stringJSON =  loadJSONFromAsset();

        String stopNameTemp = "";
        int stopIDTemp;
        double latTemp;
        double lngTemp;


        try {
            JSONArray rootJSONArray = new JSONArray(stringJSON);

            for(int position=0;position<rootJSONArray.length();position++){
                List<Integer> typesTransportTemp = new ArrayList<>();

                stopNameTemp = rootJSONArray.getJSONObject(position).getString("stopName");
                stopIDTemp = rootJSONArray.getJSONObject(position).getInt("stopCode");
                String stopIDStringTemp = ""+stopIDTemp;
                while(stopIDStringTemp.length()<4){
                    stopIDStringTemp = "0"+stopIDStringTemp;
                }
                stopIDTemp = Integer.parseInt(stopIDStringTemp);
                for(int pos=0;pos<rootJSONArray.getJSONObject(position).getJSONArray("lineTypes").length();pos++){
                    typesTransportTemp.add(rootJSONArray.getJSONObject(position).getJSONArray("lineTypes").getInt(pos));
                }
                latTemp = rootJSONArray.getJSONObject(position).getJSONArray("coordinates").getDouble(0);
                lngTemp = rootJSONArray.getJSONObject(position).getJSONArray("coordinates").getDouble(1);

                Stop stop = new Stop(stopNameTemp,stopIDStringTemp,latTemp,lngTemp,typesTransportTemp);
                stops.add(stop);
            }

        } catch (JSONException e) {
            Log.e("",e.getMessage());
        }

        viewPager = (ViewPager) findViewById(R.id.pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Add Fragments to adapter one by one
        Bundle bundle = new Bundle();
        bundle.putSerializable("Stops",(Serializable)stops);
        CustomMapFragment customMapFragment = new CustomMapFragment();
        customMapFragment.setArguments(bundle);
        adapter.addFragment(customMapFragment, "Карта");
        adapter.addFragment(new Favourites(), "Любими");
        viewPager.setAdapter(adapter);

//       viewPager.setCurrentItem(2);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        final List<String> stopsName = new ArrayList<>();
        for(int i=0;i<stops.size();i++){
            stopsName.add(stops.get(i).getStopName()+" - "+stops.get(i).getStopID());
        }

        ArrayAdapter arrayAdapter = new ArrayAdapter(this,R.layout.dropdown,stopsName);
        stopSearchTV.setAdapter(arrayAdapter);
        stopSearchTV.setThreshold(2);

        stopSearchTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString();
                if (TextUtils.isEmpty(input)) {
                    clearTextButton.setBackgroundResource(R.drawable.search_icon);
                } else {
                    clearTextButton.setBackgroundResource(android.R.drawable.ic_menu_close_clear_cancel);
                }
            }
        });

        stopSearchTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Fragment page = getSupportFragmentManager().findFragmentByTag("android:switcher:" + R.id.pager + ":" + viewPager.getCurrentItem());

                // based on the current position you can then cast the page to the correct
                // class and call the method:
                String stopNameCode = (String)parent.getItemAtPosition(position);
                String[] arrStringStopNameCode = stopNameCode.split("-");
                String stopCode = arrStringStopNameCode[arrStringStopNameCode.length-1].trim();
//                if (viewPager.getCurrentItem() == 0 && page != null) {
//                    ((CustomMapFragment)page).callStopArrivalsFromSearch(stopCode,stopNameCode);
//                }

                TimingDialog timingDialog = new TimingDialog();
                timingDialog.setLinesArrivalsListener(stopCode, stopNameCode,MapsActivity.this);
                timingDialog.show(getFragmentManager(), "TIMING DIALOG");

                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

            }
        });

        clearTextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isEmpty=TextUtils.isEmpty(stopSearchTV.getText().toString());
                if(!isEmpty){
                    stopSearchTV.setText("");
                    stopSearchTV.setError(null);
                    stopSearchTV.requestFocus();
                    stopSearchTV.dismissDropDown();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(stopSearchTV, InputMethodManager.SHOW_IMPLICIT);
                }

            }
        });

    }

    @Override
    public void interact() {
        Favourites favFragment = (Favourites) adapter.getItem(1);
        favFragment.updateFavListInRecView();
    }

    // Adapter for the viewpager using FragmentPagerAdapter
    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    public String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("coordinates.json");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
            is.close();
            json = out.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    protected void onPause() {
        StopsManager.getInstance().saveFavStopsInSharedPref(this);
        super.onPause();

    }
}
