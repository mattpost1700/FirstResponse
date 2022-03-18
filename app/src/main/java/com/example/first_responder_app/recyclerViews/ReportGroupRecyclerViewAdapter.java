package com.example.first_responder_app.recyclerViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.ReportDataModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportGroupRecyclerViewAdapter extends RecyclerView.Adapter<ReportGroupRecyclerViewAdapter.ViewHolder> {

    private List<ReportDataModel> reportList;
    private LayoutInflater inflater;
    private ReportGroupRecyclerViewAdapter.ReportClickListener reportClickListener;
    private ReportGroupRecyclerViewAdapter.ReportLongClickListener reportLongClickListener;

    public ReportGroupRecyclerViewAdapter(Context context, List<ReportDataModel> reportList) {
        this.inflater = LayoutInflater.from(context);
        this.reportList = reportList;
    }

    public ReportGroupRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.row_layout_reportgroup, parent, false);
        return new ReportGroupRecyclerViewAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ReportGroupRecyclerViewAdapter.ViewHolder holder, int position) {
        ReportDataModel report = reportList.get(position);
        Date date = report.getCreated_at().toDate();
        String dateString = new SimpleDateFormat("MM/dd/yy", Locale.getDefault()).format(date);

        holder.reportAddressTextView.setText(report.getAddress());
        holder.reportDateTextView.setText(dateString);

    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView reportAddressTextView;
        TextView reportDateTextView;

        ViewHolder(View itemView) {
            super(itemView);
            reportAddressTextView = itemView.findViewById(R.id.rowlayout_reportgroup_title);
            reportDateTextView = itemView.findViewById(R.id.reportgroup_time);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (reportClickListener != null) reportClickListener.onReportItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            if(reportLongClickListener != null) reportLongClickListener.onReportItemClick(view, getAdapterPosition());
            return true;
        }
    }

    public ReportDataModel getItem(int idx) {
        return reportList.get(idx);
    }

    public void setReportClickListener(ReportGroupRecyclerViewAdapter.ReportClickListener reportClickListener) {
        this.reportClickListener = reportClickListener;
    }

    public void setReportLongClickListener(ReportGroupRecyclerViewAdapter.ReportLongClickListener reportLongClickListener) {
        this.reportLongClickListener = reportLongClickListener;
    }


    public interface ReportClickListener {
        void onReportItemClick(View view, int position);
    }
    public interface ReportLongClickListener {
        void onReportItemClick(View view, int position);
    }
}
