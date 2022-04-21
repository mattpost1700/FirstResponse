package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.ChatGroupFragmentBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.messaging.Chat;
import com.example.first_responder_app.messaging.Message;
import com.example.first_responder_app.recyclerViews.ChatGroupRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.ChatViewModel;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatGroupFragment extends Fragment {

    private ChatViewModel mViewModel;
    FirestoreDatabase firestoreDatabase;
    FirebaseFirestore db;
    ActiveUser activeUser;
    UsersDataModel user;
    private List<Chat> listOfChats;
    private ChatGroupRecyclerViewAdapter chatGroupRecyclerViewAdapter;

    // ref: https://github.com/ScaleDrone/android-chat-tutorial/blob/master/app/src/main/res/layout/activity_main.xml
    public static ChatGroupFragment newInstance() {
        return new ChatGroupFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        ChatGroupFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_group_fragment, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();

        firestoreDatabase = new FirestoreDatabase();
        db = firestoreDatabase.getDb();

        activeUser = (ActiveUser)getActivity();
        if(activeUser != null){
            user = activeUser.getActive();
        }

        listOfChats = new ArrayList<>();
        populateChatList();

        ChatGroupRecyclerViewAdapter.ItemClickListener chatClickListener = ((view, position, data) -> {
            //passing data to chat
            mViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
            mViewModel.setChatDetail(data);
            List<Message> temp = new ArrayList<>();
            mViewModel.setListOfMessages(temp);
            NavDirections action = ChatGroupFragmentDirections.actionChatGroupFragmentToChatFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        RecyclerView chatGroupRecyclerView = binding.chatGroupRecyclerView;
        chatGroupRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatGroupRecyclerViewAdapter = new ChatGroupRecyclerViewAdapter(getContext(), listOfChats);
        chatGroupRecyclerViewAdapter.setClickListener(chatClickListener);
        chatGroupRecyclerView.setAdapter(chatGroupRecyclerViewAdapter);

        binding.chatTextView.setOnClickListener(v -> {
            NavDirections action = ChatGroupFragmentDirections.actionChatGroupFragmentToChatFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });

        binding.addChatTextView.setOnClickListener(v -> {
            NavDirections action = ChatGroupFragmentDirections.actionChatGroupFragmentToNewChatFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);
        });


        binding.chatGroupSwipeRefreshLayout.setOnRefreshListener(() -> {
            populateChatList();
            binding.chatGroupSwipeRefreshLayout.setRefreshing(false);
        });


        return binding.getRoot();

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        // TODO: Use the ViewModel
    }

    private void populateChatList() {
        db.collection("chat").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ArrayList<Chat> temp = new ArrayList<>();

                for (QueryDocumentSnapshot doc : task.getResult()){
                    ArrayList<String> members = (ArrayList<String>)  doc.get("members");

                    if (user != null) {
                        String memberName = user.getDocumentId();
                        if (includes(members, memberName)) {
                            Timestamp t = (Timestamp) doc.get("most_recent_message_time");
                            Chat chat = new Chat(doc.getId(), (String) doc.get("most_recent_message"), members, (String) doc.get("chat_name"), t);
                            temp.add(chat);
                        }
                    }



                }
                listOfChats.clear();
                listOfChats.addAll(temp);
                Collections.sort(listOfChats);
                Collections.reverse(listOfChats);

                chatGroupRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                Log.d(TAG, "db get failed in chat group page " + task.getException());
            }
        });
    }


    boolean includes(ArrayList<String> list, String s){
        if(list == null) return false;
        for(int i = 0; i < list.size(); i++){
            if(list.get(i).contains(s)){
                return true;
            }
        }
        return false;
    }
}