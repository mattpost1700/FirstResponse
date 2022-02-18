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
import com.example.first_responder_app.dataModels.UsersDataModel;

import java.util.List;

/**
 * @apiNote ref: https://stackoverflow.com/questions/4602902/how-to-set-the-text-color-of-textview-in-code
 */
public class RespondersRecyclerViewAdapter extends RecyclerView.Adapter<RespondersRecyclerViewAdapter.ViewHolder> {

    private List<UsersDataModel> responderList;
    private LayoutInflater inflater;
    private ResponderClickListener responderClickListener;

    public RespondersRecyclerViewAdapter(Context context, List<UsersDataModel> responderList) {
        this.inflater = LayoutInflater.from(context);
        this.responderList = responderList;
    }

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.responder_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsersDataModel responder = responderList.get(position);
        holder.responderNameTextView.setText(responder.getFirst_name() + " " + responder.getLast_name());
        holder.responderRankTextView.setText(responder.getRank());
        holder.responderEtaTextView.setText("Temp ETA" + " Mins");
    }

    @Override
    public int getItemCount() {
        return responderList.size();
    }



    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView responderNameTextView;
        TextView responderRankTextView;
        TextView responderEtaTextView;

        ViewHolder(View itemView) {
            super(itemView);
            responderNameTextView = itemView.findViewById(R.id.responders_name_text_view);
            responderRankTextView = itemView.findViewById(R.id.responders_rank_text_view);
            responderEtaTextView = itemView.findViewById(R.id.live_responder_eta);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (responderClickListener != null) responderClickListener.onResponderItemClick(view, getAdapterPosition());
        }
    }



    public UsersDataModel getItem(int idx) {
        return responderList.get(idx);
    }

    public void setResponderClickListener(ResponderClickListener responderClickListener) {
        this.responderClickListener = responderClickListener;
    }

    public interface ResponderClickListener {
        void onResponderItemClick(View view, int position);
    }
}
