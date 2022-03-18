package com.example.first_responder_app.recyclerViews;

import static android.content.ContentValues.TAG;

import android.animation.ValueAnimator;
import android.content.Context;
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

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AnnouncementRecyclerViewAdapter extends RecyclerView.Adapter<AnnouncementRecyclerViewAdapter.ViewHolder>{

    private List<AnnouncementsDataModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    public AnnouncementRecyclerViewAdapter(Context context, List<AnnouncementsDataModel> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout_announcement, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AnnouncementsDataModel announ = mData.get(position);
        holder.title.setText(announ.getTitle());
        holder.des.setText(announ.getDescription());
        String dateString = new SimpleDateFormat("MM/dd h:mm aa", Locale.getDefault()).format(announ.getCreated_at().toDate());
        holder.annoucementTime.setText(dateString);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView title;
        TextView des;
        TextView author;
        TextView annoucementTime;
        LinearLayout layout;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.rowlayout_announcement_announTitle);
            des = itemView.findViewById(R.id.rowlayout_announcement_announDes);
            author = itemView.findViewById(R.id.rowlayout_announcement_author);
            annoucementTime = itemView.findViewById(R.id.announcement_time);
            layout = (LinearLayout) itemView;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());

            if(author.getText().toString().equals("") && mData.get(getAdapterPosition()).getUser_created_id() != null) {
                FirestoreDatabase.getInstance().getDb().collection("users").document(mData.get(getAdapterPosition()).getUser_created_id()).get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String first = (String) task.getResult().get("first_name");
                        String last = (String) task.getResult().get("last_name");
                        author.setText("-" + first + " " + last);
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
            }

            if (des.getVisibility()== View.GONE){
                des.setVisibility(View.VISIBLE);
                author.setVisibility(View.VISIBLE);
            } else {

                int height = layout.getHeight();
                ValueAnimator va = ValueAnimator.ofInt(height, 0);
                va.setDuration(350);
                va.addUpdateListener(animation -> {
                    Integer value = (Integer) animation.getAnimatedValue();
                    layout.setMinimumHeight(value);
                    layout.requestLayout();
                });
                va.start();


                des.setVisibility(View.GONE);
                author.setVisibility(View.GONE);
            }
        }
    }

    // convenience method for getting data at click position
    public AnnouncementsDataModel getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}
