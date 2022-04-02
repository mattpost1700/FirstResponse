package com.example.first_responder_app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.first_responder_app.dataModels.UsersDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SearchUserAdapter extends BaseAdapter {

    // Declare Variables

    private Context mContext;
    private LayoutInflater inflater;
    private List<UsersDataModel> usersList;

    private List<UsersDataModel> origUserList;

    // TODO: Make work correctly pls
    public SearchUserAdapter(Context context, List<UsersDataModel> usersList) {
        this.mContext = context;
        this.usersList = usersList;
        this.inflater = LayoutInflater.from(mContext);
        this.origUserList = new ArrayList<>();
        this.origUserList.addAll(usersList);
    }

    public class ViewHolder {
        TextView listItemFullNameTextView;
    }

    @Override
    public void notifyDataSetChanged() {
        origUserList.clear();
        origUserList.addAll(usersList);
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return usersList.size();
    }

    @Override
    public UsersDataModel getItem(int position) {
        return usersList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public View getView(final int position, View view, ViewGroup parent) {
        final ViewHolder holder;
        if (view == null) {
            holder = new ViewHolder();
            view = inflater.inflate(R.layout.search_user_list_item, null);

            // Locate the TextViews in listview_item.xml
            holder.listItemFullNameTextView = (TextView) view.findViewById(R.id.searchUserListItemFullName);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        // Set the results into TextViews
        holder.listItemFullNameTextView.setText(usersList.get(position).getFull_name());
        return view;
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        usersList.clear();
        if (charText.length() == 0) {
            usersList.addAll(origUserList);
        } else {
            for (UsersDataModel mUser : origUserList) {
                if (mUser.getFull_name().toLowerCase(Locale.getDefault()).contains(charText)) {
                    usersList.add(mUser);
                }
            }
        }
        notifyDataSetChanged();
    }
}