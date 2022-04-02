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
import com.example.first_responder_app.dataModels.GroupDataModel;

import java.util.List;

public class AdminGroupsRecyclerViewAdapter extends RecyclerView.Adapter<AdminGroupsRecyclerViewAdapter.ViewHolder> {

    private List<GroupDataModel> groupDataModelList;
    private LayoutInflater inflater;
    private GroupClickListener groupClickListener;

    public AdminGroupsRecyclerViewAdapter(Context context, List<GroupDataModel> groupDataModelList) {
        this.inflater = LayoutInflater.from(context);
        this.groupDataModelList = groupDataModelList;
    }

    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.responder_row_layout, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupDataModel groupDataModel = groupDataModelList.get(position);
        holder.groupNameTextView.setText(groupDataModel.getName());
    }

    @Override
    public int getItemCount() {
        return groupDataModelList == null ? 0 : groupDataModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView groupNameTextView;

        ViewHolder(View itemView) {
            super(itemView);
            groupNameTextView = itemView.findViewById(R.id.responders_name_text_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (groupClickListener != null) groupClickListener.onGroupItemClick(view, getAdapterPosition());
        }
    }

    public GroupDataModel getItem(int idx) {
        return groupDataModelList.get(idx);
    }

    public void setGroupClickListener(GroupClickListener groupClickListener) {
        this.groupClickListener = groupClickListener;
    }

    public interface GroupClickListener {
        void onGroupItemClick(View view, int position);
    }
}
