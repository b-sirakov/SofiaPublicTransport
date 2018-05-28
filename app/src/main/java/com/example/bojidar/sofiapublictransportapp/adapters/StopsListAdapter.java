package com.example.bojidar.sofiapublictransportapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.bojidar.sofiapublictransportapp.R;
import com.example.bojidar.sofiapublictransportapp.StopsManager;
import com.example.bojidar.sofiapublictransportapp.dialogs.TimingDialog;

import java.util.List;

/**
 * Created by Bozhidar.Sirakov on 8/24/2017.
 */

public class StopsListAdapter extends RecyclerView.Adapter {

    public class HistoryListVH extends RecyclerView.ViewHolder{

        TextView favStop;
        SwipeRevealLayout swipeRevealLayout;
        LinearLayout secondSwipeLayout;


        public HistoryListVH(View row) {
            super(row);
            favStop= (TextView) row.findViewById(R.id.fav_stop);
            swipeRevealLayout = (SwipeRevealLayout) row.findViewById(R.id.swipe_reveal_layout);
            secondSwipeLayout = (LinearLayout) row.findViewById(R.id.second_swipe_layout_stop_row);
        }
    }

    private final ViewBinderHelper viewBinderHelper = new ViewBinderHelper();

    private Activity context;
    private List<String> stopsList;

    public StopsListAdapter(Activity context, List<String> locationsList) {
        this.context = context;
        this.stopsList = locationsList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.stop_row, parent, false);
        HistoryListVH vh=new HistoryListVH(row);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        viewBinderHelper.bind(((HistoryListVH)holder).swipeRevealLayout, stopsList.get(position));

        ((HistoryListVH)holder).favStop.setText(stopsList.get(position));
        ((HistoryListVH)holder).favStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] arrStringStopNameCode = stopsList.get(position).split("-");
                String stopCode = arrStringStopNameCode[arrStringStopNameCode.length-1].trim();

                TimingDialog timingDialog = new TimingDialog();
                timingDialog.setLinesArrivalsListener(stopCode, stopsList.get(position),(TimingDialog.onTiminigDialogIteractionListener)context);
                timingDialog.show(context.getFragmentManager(), "TIMING DIALOG");
            }
        });
        ((HistoryListVH)holder).secondSwipeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StopsManager.getInstance().removeStopFromFav(position);
                notifyDataSetChanged();
            }
        });

    }

    @Override
    public int getItemCount() {
        return stopsList.size();
    }
}
