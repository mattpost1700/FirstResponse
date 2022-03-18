package com.example.first_responder_app.recyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.EventsDataModel;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class EventGroupRecyclerViewAdapter extends RecyclerView.Adapter<EventGroupRecyclerViewAdapter.ViewHolder>{

    private List<EventsDataModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    public EventGroupRecyclerViewAdapter(Context context, List<EventsDataModel> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout_eventgroup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        EventsDataModel events = mData.get(position);
        holder.title.setText(events.getTitle());
        if(events.getEvent_time() != null) {
            Date date = events.getEvent_time().toDate();
            String time = new SimpleDateFormat("MM/dd/yy\nh:mm aa", Locale.getDefault()).format(date);
            holder.date.setText(time);
        }
        holder.location.setText(events.getLocation());
        holder.data = events;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView date;
        TextView location;
        EventsDataModel data;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowlayout_eventGroup_title);
            date = itemView.findViewById(R.id.row_layout_event_time);
            location = itemView.findViewById(R.id.row_layout_event_location);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing data to eventGroup
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), data);
        }
    }

    public EventsDataModel getItem(int id) {
        return mData.get(id);
    }



    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, EventsDataModel data);
    }
}
