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
import com.example.first_responder_app.messaging.Chat;

import java.util.ArrayList;
import java.util.List;

public class ChatGroupRecyclerViewAdapter extends RecyclerView.Adapter<ChatGroupRecyclerViewAdapter.ViewHolder>{

    private List<Chat> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ActiveUser activeUser;
    private UsersDataModel user;

    public ChatGroupRecyclerViewAdapter(Context context, List<Chat> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_layout_chatgroup, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chat chat = mData.get(position);

        Context context = holder.itemView.getContext();

        String userId = "";
        ActiveUser activeUser = (ActiveUser)context;
        if (activeUser != null) {
            UsersDataModel user = activeUser.getActive();
            if (user != null ) {
                userId = user.getDocumentId();
            }
        }


        ArrayList<String> members = chat.getMembers();

        String a = holder.name.getText().toString();
        String chatName = "";
        // If it is direct message, set chat name to user's name, otherwise set chat name to the name stored in db
        if (members.size() == 2) {
            String[] memberAndId0 = members.get(0).split("/");
            String[] memberAndId1 = members.get(1).split("/");
            if (memberAndId0[1].equals(userId)) {
                holder.name.setText(memberAndId1[0]);
            } else {
                holder.name.setText(memberAndId0[0]);
            }
        } else {
            holder.name.setText(chat.getChatName());

            a = holder.name.getText().toString();
        }
        holder.recentMsg.setText(chat.getMostRecentMessage());
        holder.data = chat;
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView name;
        TextView recentMsg;
        Chat data;

        ViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.row_layout_chatGroup_title);
            recentMsg = itemView.findViewById(R.id.row_layout_chat_msg);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing data to chatGroup
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), data);
        }
    }

    public Chat getItem(int id) {
        return mData.get(id);
    }



    // allows clicks chats to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click chats
    public interface ItemClickListener {
        void onItemClick(View view, int position, Chat data);
    }
}
