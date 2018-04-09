package com.AlForce.android.runvolution.history;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.AlForce.android.runvolution.history.HistoryItem;
import com.AlForce.android.runvolution.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import com.AlForce.android.runvolution.R;
import com.AlForce.android.runvolution.utils.DatabaseOpenHelper;

/**
 * Created by iqbal on 16/02/18.
 */

public class HistoryAdapter extends Adapter<HistoryAdapter.ViewHolder> {

    private HistoryDAO historyDAO;
    private HistoryStatistics statistics;
    private Context context;

    public HistoryAdapter(Context context, DatabaseOpenHelper db) {
        this.context = context;
        this.historyDAO = new HistoryDAO(db);
        this.statistics = new HistoryStatistics(historyDAO);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View historyItemView = LayoutInflater.from(context)
                .inflate(R.layout.history_item, parent, false);
        return new ViewHolder(historyItemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        HistoryItem currentItem;
        currentItem = historyDAO.query(position);

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String date = context.getString(R.string.date) + dateFormat.format(currentItem.getDate()).toString();
        String steps = context.getString(R.string.steps) + Integer.toString(currentItem.getSteps());
        String distance = context.getString(R.string.distance) + Float.toString(currentItem.getDistance());
        holder.dateTextView.setText(date);
        holder.stepsTextView.setText(steps);
        holder.distanceTextView.setText(distance);
    }

    @Override
    public int getItemCount() {
        return (int) historyDAO.getQueryCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView dateTextView;
        public TextView stepsTextView;
        public TextView distanceTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            dateTextView = (TextView) itemView.findViewById(R.id.date);
            stepsTextView = (TextView) itemView.findViewById(R.id.steps);
            distanceTextView = (TextView) itemView.findViewById(R.id.distance);
        }
    }
}
