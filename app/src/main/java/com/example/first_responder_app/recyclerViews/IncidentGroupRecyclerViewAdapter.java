package com.example.first_responder_app.recyclerViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class IncidentGroupRecyclerViewAdapter extends RecyclerView.Adapter<IncidentGroupRecyclerViewAdapter.ViewHolder> {

    private List<IncidentDataModel> incidentList;
    private LayoutInflater inflater;
    private IncidentGroupRecyclerViewAdapter.IncidentClickListener incidentClickListener;

    public IncidentGroupRecyclerViewAdapter(Context context, List<IncidentDataModel> incidentList) {
        this.inflater = LayoutInflater.from(context);
        this.incidentList = incidentList;
    }

    public IncidentGroupRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.incident_group_row_layout, parent, false);
        return new IncidentGroupRecyclerViewAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull IncidentGroupRecyclerViewAdapter.ViewHolder holder, int position) {
        IncidentDataModel incident = incidentList.get(position);
        Date date = incident.getReceived_time().toDate();
        String dateString = new SimpleDateFormat("h:mm aa", Locale.getDefault()).format(date);

        holder.incidentAddressTextView.setText(incident.getLocation());
        holder.incidentTimeTextView.setText(dateString);

    }

    @Override
    public int getItemCount() {
        return incidentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView incidentAddressTextView;
        TextView incidentTimeTextView;

        ViewHolder(View itemView) {
            super(itemView);
            incidentAddressTextView = itemView.findViewById(R.id.incident_address_text_view);
            incidentTimeTextView = itemView.findViewById(R.id.incident_time_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (incidentClickListener != null) incidentClickListener.onIncidentItemClick(view, getAdapterPosition());
        }
    }

    public IncidentDataModel getItem(int idx) {
        return incidentList.get(idx);
    }

    public void setIncidentClickListener(IncidentGroupRecyclerViewAdapter.IncidentClickListener incidentClickListener) {
        this.incidentClickListener = incidentClickListener;
    }

    public interface IncidentClickListener {
        void onIncidentItemClick(View view, int position);
    }
}
