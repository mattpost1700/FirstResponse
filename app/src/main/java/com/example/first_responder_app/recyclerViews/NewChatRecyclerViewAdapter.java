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
import com.example.first_responder_app.fragments.UserFragmentDirections;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.messaging.Message;
import com.firebase.ui.auth.data.model.User;

import java.util.HashMap;
import java.util.List;

public class NewChatRecyclerViewAdapter extends RecyclerView.Adapter<NewChatRecyclerViewAdapter.ViewHolder>{

    private List<UsersDataModel> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private ActiveUser activeUser;
    private UsersDataModel user;
    private String userId;


    public NewChatRecyclerViewAdapter(Context context, List<UsersDataModel> data){
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;


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
        View view = mInflater.inflate(R.layout.chat_user, parent, false);
        return new NewChatRecyclerViewAdapter.ViewHolder(view);
    }

    public void addData(UsersDataModel data) {
        mData.add(data);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UsersDataModel u = mData.get(position);
        holder.newUser.setText("Remove " + u.getFull_name());
        holder.data = u;

    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView newUser;
        UsersDataModel data;

        ViewHolder(View itemView) {
            super(itemView);

            newUser = itemView.findViewById(R.id.new_user_name);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            //passing data to new chat fragment
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition(), data);
        }


    }

    public UsersDataModel getItem(int id) {
        return mData.get(id);
    }


    // allows clicks users to be caught
    public void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click users
    public interface ItemClickListener {
        void onItemClick(View view, int position, UsersDataModel data);
    }
}
