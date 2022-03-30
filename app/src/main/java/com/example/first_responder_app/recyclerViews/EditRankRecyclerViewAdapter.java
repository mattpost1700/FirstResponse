package com.example.first_responder_app.recyclerViews;

import static android.content.ContentValues.TAG;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.AnnouncementsDataModel;
import com.example.first_responder_app.dataModels.RanksDataModel;
import com.example.first_responder_app.viewModels.EditRankViewModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class EditRankRecyclerViewAdapter extends RecyclerView.Adapter<EditRankRecyclerViewAdapter.ViewHolder>{

    private List<RanksDataModel> mData;
    private LayoutInflater mInflater;
    private EditRankRecyclerViewAdapter.ItemClickListener mClickListener;

    // data is passed into the constructor
    public EditRankRecyclerViewAdapter(Context context, List<RanksDataModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public EditRankRecyclerViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout_edit_rank, parent, false);
        return new EditRankRecyclerViewAdapter.ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(EditRankRecyclerViewAdapter.ViewHolder holder, int position) {
        RanksDataModel rank = mData.get(position);
        holder.title.setText(rank.getRank_name());
        holder.data = rank;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        RanksDataModel data;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowlayout_edit_rank_title);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), data);

        }
    }

    // convenience method for getting data at click position
    public RanksDataModel getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(EditRankRecyclerViewAdapter.ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position, RanksDataModel data);
    }
}
