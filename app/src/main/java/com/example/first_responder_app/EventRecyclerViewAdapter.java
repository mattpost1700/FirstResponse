package com.example.first_responder_app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.first_responder_app.dataModels.UsersDataModel;

import java.util.List;

public class EventRecyclerViewAdapter extends RecyclerView.Adapter<EventRecyclerViewAdapter.ViewHolder>{
    private List<UsersDataModel> mData;
    private LayoutInflater mInflater;
    private EventRecyclerViewAdapter.ItemClickListener mClickListener;
    private boolean isAnyParticipants;

    public EventRecyclerViewAdapter(Context context, List<UsersDataModel> data, boolean isAnyParticipants){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.isAnyParticipants = isAnyParticipants;
    }

    @NonNull
    @Override
    public EventRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout_event, parent, false);
        return new EventRecyclerViewAdapter.ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull EventRecyclerViewAdapter.ViewHolder holder, int position) {
        if (isAnyParticipants) {
            holder.participantName.setText(mData.get(position).getFull_name());
            holder.participantID = mData.get(position).getDocumentId();
        }
        else {
            holder.participantName.setText("No participant for now");
        }
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView participantName;
        String participantID;

        ViewHolder(View itemView) {
            super(itemView);
            participantName = itemView.findViewById(R.id.rowlayout_event_participants);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // allows clicks events to be caught
    public void setClickListener(EventRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
