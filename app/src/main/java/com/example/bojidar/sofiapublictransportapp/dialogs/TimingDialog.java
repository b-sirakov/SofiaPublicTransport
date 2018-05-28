package com.example.bojidar.sofiapublictransportapp.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bojidar.sofiapublictransportapp.R;
import com.example.bojidar.sofiapublictransportapp.StopsManager;
import com.example.bojidar.sofiapublictransportapp.asynctasks.ApiCallAsyncTask;
import com.example.bojidar.sofiapublictransportapp.model.Line;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.w3c.dom.Text;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Bojidar on 2/22/2018.
 */

public class TimingDialog extends DialogFragment {

    private List<Line> linesArrivals;
    private String stopNameCode;
    private String stopCode;
    private boolean isRefreshbuttonclicked;
    private onTiminigDialogIteractionListener mCallBack;

    private TextView stopNameCodeTV;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView loadingMessageTextView;
    private ImageButton addToFavButton;
    private Button refreshButton;
    private Button closeButton;
    private ProgressBar refreshProgressBar;
//    private TextView problemMessageTV;

    

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.timing_dialog, null);
        stopNameCodeTV = (TextView) rootView.findViewById(R.id.stop_name_code_tv);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.rows_in_timing_rv);
        progressBar = (ProgressBar) rootView.findViewById(R.id.progress_bar);
        loadingMessageTextView = (TextView) rootView.findViewById(R.id.loading_message_tv);


        addToFavButton = (ImageButton) rootView.findViewById(R.id.add_to_favourite_button);
        refreshButton = (Button) rootView.findViewById(R.id.refresh_button);
        closeButton = (Button) rootView.findViewById(R.id.close_button);
        refreshProgressBar = (ProgressBar) rootView.findViewById(R.id.refresh_progress_bar);
//        problemMessageTV = (TextView) rootView.findViewById(R.id.problem_message_tv);

        linesArrivals = new ArrayList<>();

//        DisplayMetrics displayMetrics = new DisplayMetrics();
//        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//        int widthLcl = (int) (displayMetrics.widthPixels*0.9f);
//        int heightLcl = (int) (displayMetrics.heightPixels*0.9f);
//
//        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(widthLcl,heightLcl);
//
//        rootView.setLayoutParams(params);


        recyclerView.setAdapter(new RecyclerView.Adapter() {

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                View row = inflater.inflate(R.layout.row_in_timing_dialog, parent, false);
                MyRecyclerViewHolder vh = new MyRecyclerViewHolder(row);
                return vh;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((MyRecyclerViewHolder) holder).lineNameTV.setText(linesArrivals.get(position).getLineNumber());
                ((MyRecyclerViewHolder) holder).timingTV.setText(linesArrivals.get(position).getArrivals());
            }

            @Override
            public int getItemCount() {
                return linesArrivals.size();
            }

            class MyRecyclerViewHolder extends RecyclerView.ViewHolder {

                TextView lineNameTV;
                TextView timingTV;

                public MyRecyclerViewHolder(View row) {
                    super(row);
                    lineNameTV = (TextView) row.findViewById(R.id.text_view_transport);
                    timingTV = (TextView) row.findViewById(R.id.text_view_timing);
                }
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));

        builder.setView(rootView);

        stopNameCodeTV.setText(stopNameCode);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(stopCode)){

                    isRefreshbuttonclicked = true;

                    ApiCallAsyncTask apiCallAsyncTask = new ApiCallAsyncTask();
                    apiCallAsyncTask.setIResult(new ResultFromAsyncTask());
                    refreshProgressBar.setVisibility(View.VISIBLE);

                    apiCallAsyncTask.execute(stopCode);
                }
            }
        });
        
        addToFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Toast.makeText(getActivity(), "Не работи все още", Toast.LENGTH_SHORT).show();
                if(StopsManager.getInstance().getFavStops().contains(TimingDialog.this.stopNameCode)){
                    addToFavButton.setImageResource(R.drawable.star1);
                    StopsManager.getInstance().removeStopFromFav(stopNameCode);
                    callCenteredToast("Премахнато от любими");
                }else{
                    //logic is reversed
                    addToFavButton.setImageResource(R.drawable.star2);
                    StopsManager.getInstance().addStopInFav(stopNameCode);
                    callCenteredToast("добавено в любими");
                }

                mCallBack.interact();
            }
        });

        if(StopsManager.getInstance().getFavStops().contains(this.stopNameCode)){
            addToFavButton.setImageResource(R.drawable.star2);
        }else{
            addToFavButton.setImageResource(R.drawable.star1);
        }

        ApiCallAsyncTask apiCallAsyncTask = new ApiCallAsyncTask();
        apiCallAsyncTask.setIResult(new ResultFromAsyncTask());

        recyclerView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        loadingMessageTextView.setVisibility(View.VISIBLE);

        apiCallAsyncTask.execute(stopCode);

        // Create the AlertDialog object and return it
        return builder.create();
    }

    public List<Line> getLinesArrivals() {
        return linesArrivals;
    }

    public void setLinesArrivalsListener(String stopCode,String stopNameCode,onTiminigDialogIteractionListener mCallBack) {
        this.stopNameCode = stopNameCode;
        this.stopCode = stopCode;
        this.mCallBack = mCallBack;
    }



    class ResultFromAsyncTask implements ApiCallAsyncTask.IResult{

        @Override
        public void onResult( List<Line> result) {
            if(result!=null) {
//                problemMessageTV.setVisibility(View.GONE);
                linesArrivals=result;
                recyclerView.getAdapter().notifyDataSetChanged();
            }else{
//              dismiss();
//                problemMessageTV.setVisibility(View.VISIBLE);

                callCenteredToast("Проблем при извличането на данните");
            }
            recyclerView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            refreshProgressBar.setVisibility(View.GONE);
            loadingMessageTextView.setVisibility(View.GONE);
        }

        @Override
        public NetworkInfo networkConnectivityInfo() {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager)  getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo;
        }

        @Override
        public void noInternetConnectionToast(boolean flag) {
            if(flag){
                if(!isRefreshbuttonclicked) {
                    dismiss();
                }
            Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
            isRefreshbuttonclicked=false;
        }


    }

    public interface onTiminigDialogIteractionListener {
        public abstract void interact();
    }

    public void callCenteredToast(String text){
        Toast toast = Toast.makeText(getActivity(),  text, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }
}
