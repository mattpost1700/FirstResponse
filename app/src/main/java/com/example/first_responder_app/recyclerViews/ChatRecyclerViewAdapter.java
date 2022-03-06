package com.example.first_responder_app.recyclerViews;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.messaging.Message;

import java.util.HashMap;
import java.util.List;

public class ChatRecyclerViewAdapter extends RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>{

    private List<Message> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ActiveUser activeUser;
    private UsersDataModel user;
    private String userId;
    private HashMap<String, String> mMembers;

    public ChatRecyclerViewAdapter(Context context, List<Message> data, List<String> members){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        mMembers = new HashMap<>();

        for (String s : members) {
            String[] memberAndId = s.split("/");
            mMembers.put(memberAndId[1], memberAndId[0]);
        }



        userId = "";
        activeUser = (ActiveUser)context;
        if (activeUser != null) {
            user = activeUser.getActive();
            if (user != null ) {
                userId = user.getDocumentId();
            }
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == 5001) {
            // self message
            view = mInflater.inflate(R.layout.my_message, parent, false);
        } else if(viewType == 5002) {
            // others message
            view = mInflater.inflate(R.layout.their_message, parent, false);
        } else {
            view = mInflater.inflate(R.layout.row_layout_chatgroup, parent, false);
        }
        return new ViewHolder(view);
    }

    public void addData(Message data) {
        mData.add(data);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message message = mData.get(position);

        if (getItemViewType(position)==5001) {
            holder.myMsg.setText(message.getMessageText());
        } else {
            String senderId = message.getSender();
            String senderName = mMembers.get(senderId);
            holder.theirName.setText(senderName);
            holder.theirMsg.setText(message.getMessageText());
        }



    }

    // total number of rows
    @Override
    public int getItemCount() {

        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView theirMsg;
        TextView myMsg;
        TextView theirName;
        Message data;

        ViewHolder(View itemView) {
            super(itemView);

            theirMsg = itemView.findViewById(R.id.their_message_body);
            theirName = itemView.findViewById(R.id.their_name);
            myMsg = itemView.findViewById(R.id.my_message_body);
            //itemView.setOnClickListener(this);
        }

        /*
        @Override
        public void onClick(View view) {
            //passing data to chatGroup
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), data);
        }

         */
    }

    public Message getItem(int id) {
        return mData.get(id);
    }

    @Override
    public int getItemViewType(int position) {
        String ID = mData.get(position).getSender();
        if (ID.equals(userId)) {
            return 5001;
        } else {
            return 5002;
        }

    }



    // allows clicks messages to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click messages
    public interface ItemClickListener {
        void onItemClick(View view, int position, Message data);
    }
}
