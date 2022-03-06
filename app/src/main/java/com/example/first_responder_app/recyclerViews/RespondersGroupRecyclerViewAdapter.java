package com.example.first_responder_app.recyclerViews;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.AppUtil;
import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.IncidentDataModel;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.auth.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @apiNote ref: https://stackoverflow.com/questions/4602902/how-to-set-the-text-color-of-textview-in-code
 */
public class RespondersGroupRecyclerViewAdapter extends RecyclerView.Adapter<RespondersGroupRecyclerViewAdapter.ViewHolder> {

    private List<UsersDataModel> responderList;
    private List<IncidentDataModel> incidentList;
    private LayoutInflater inflater;
    private ResponderClickListener responderClickListener;

    public RespondersGroupRecyclerViewAdapter(Context context, List<UsersDataModel> responderList, List<IncidentDataModel> incidents) {
        this.inflater = LayoutInflater.from(context);
        this.responderList = responderList;
        this.incidentList = incidents;
    }

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.responders_group_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsersDataModel responder = responderList.get(position);
        holder.responderNameTextView.setText(responder.getFirst_name() + " " + responder.getLast_name());
        setResponseLocation(holder, responder);
        setEta(holder, responder);
    }

    @Override
    public int getItemCount() {
        return responderList.size();
    }


    /**
     * Set the responding location field on the card
     *
     * @param holder The ViewHolder object
     * @param user The user who is responding
     */
    public void setResponseLocation(ViewHolder holder, UsersDataModel user){
        StringBuilder responding = new StringBuilder();
        Log.d(TAG, "setResponseLocation: ");
        for(int i = 0; i < incidentList.size(); i++) {
            IncidentDataModel incident = incidentList.get(i);
            Map<String, String> status = incident.getStatus();

            List<String> responses = user.getResponses();
            if (responses != null && responses.size() > 0 && responses.get(responses.size() - 1).equals(incident.getDocumentId())) {
                if (status != null && status.containsKey(user.getDocumentId()) && status.get(user.getDocumentId()).equals("Station")) {
                    holder.responderRankTextView.setText("Station");
                }else if(status != null && status.containsKey(user.getDocumentId()) && status.get(user.getDocumentId()).equals("Unavailable")){
                    holder.responderRankTextView.setText("Unavailable");
                }else{
                    holder.responderRankTextView.setText(incident.getLocation());
                }
                return;
            }
        }
    }

    public void setEta(ViewHolder holder, UsersDataModel user){
        for(int i = 0; i < incidentList.size(); i++) {
            IncidentDataModel incident = incidentList.get(i);
            Map<String, String> etas = incident.getEta();

            List<String> responses = user.getResponses();
            if (responses != null && responses.size() > 0 && responses.get(responses.size() - 1).equals(incident.getDocumentId())) {
                if(etas != null && etas.containsKey(user.getDocumentId())){
                    String eta = "ETA: " + etas.get(user.getDocumentId());
                    holder.responderEtaTextView.setText(eta);
                    return;
                }
            }
        }
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
