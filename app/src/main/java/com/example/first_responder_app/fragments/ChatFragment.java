package com.example.first_responder_app.fragments;

import static android.content.ContentValues.TAG;

import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.ChatFragmentBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.messaging.Chat;
import com.example.first_responder_app.messaging.Message;
import com.example.first_responder_app.recyclerViews.ChatRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.ChatViewModel;
import com.example.first_responder_app.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatFragment extends Fragment {

    private ChatViewModel mViewModel;
    private Message messageInfo;
    private FirestoreDatabase firestoreDatabase;
    private FirebaseFirestore db;
    private ActiveUser activeUser;
    private UsersDataModel user;
    private List<Message> listOfMessages;
    private List<String> listOfMembers;
    private ChatRecyclerViewAdapter chatRecyclerViewAdapter;
    private Chat c;
    private RecyclerView chatRecyclerView;
    private ListenerRegistration chatListener;

    // ref: https://github.com/ScaleDrone/android-chat-tutorial/blob/master/app/src/main/res/layout/activity_main.xml
    public static ChatFragment newInstance() {
        return new ChatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        ChatFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_fragment, container, false);
        NavHostFragment navHostFragment =
                (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();

        firestoreDatabase = new FirestoreDatabase();
        db = FirebaseFirestore.getInstance();

        activeUser = (ActiveUser)getActivity();
        if(activeUser != null){
            user = activeUser.getActive();
        }

        mViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        c = mViewModel.getChatDetail();
        listOfMembers = c.getMembers();
        listOfMessages = mViewModel.getListOfMessages();
        populateMessageList();


        //getting data from chat
        mViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);
        messageInfo = mViewModel.getMessageDetail();

        chatRecyclerView = binding.chatRecyclerView;
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerViewAdapter = new ChatRecyclerViewAdapter(getContext(), listOfMessages, listOfMembers);
        //chatRecyclerViewAdapter.setClickListener(chatClickListener);
        chatRecyclerView.setAdapter(chatRecyclerViewAdapter);




        chatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                Log.d(TAG, "onLayoutChange: scroll " + listOfMessages.size());
                chatRecyclerView.removeOnLayoutChangeListener(this);
                chatRecyclerView.scrollToPosition(chatRecyclerViewAdapter.getItemCount() - 1);
            }
        });



        binding.sendButton.setOnClickListener(v -> {
            String userName = "";
            if (user != null) {
                userName = user.getFirst_name() + " " + user.getLast_name();
            }
            String msg = binding.editText.getText().toString();
            binding.editText.setText("");

            //hide keyboard
            View view = getActivity().getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            binding.editText.clearFocus();

            if (!msg.equals("") && user != null) {
                firestoreDatabase.addMessage(c.getId(), msg, user.getDocumentId(), chatRecyclerViewAdapter, mViewModel);
            }

        });

        binding.leaveChat.setOnClickListener(v -> {

            new AlertDialog.Builder(getActivity())
                    .setTitle("Leave Chat?")
                    .setMessage("Are you sure you want to leave this chat?")
                    .setPositiveButton("Yes", (DialogInterface.OnClickListener) (dialogInterface, i) -> {
                        firestoreDatabase.removeUserFromChat(user.getDocumentId(), c.getId(), listOfMembers);
                        NavDirections action = ChatFragmentDirections.actionChatFragmentToChatGroupFragment();
                        Navigation.findNavController(binding.getRoot()).navigate(action);
                        firestoreDatabase.addMessage(c.getId(), user.getFull_name() + " has left the chat", user.getDocumentId(), chatRecyclerViewAdapter, mViewModel);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        // TODO: Use the ViewModel
    }

    private void populateMessageList() {
        String chatId = c.getId();
        chatListener = db.collection("chat").document(chatId).collection("messages")
                .addSnapshotListener((snapshot, e) -> {
                    if (e == null) {
                        ArrayList<Message> temp = new ArrayList<>();


                        for (QueryDocumentSnapshot doc : snapshot){
                            Timestamp t = (Timestamp) doc.get("time_sent");;
                            Message m = new Message(doc.getId(), (String) doc.get("message_text"), (String) doc.get("sender"), t);
                            temp.add(m);
                        }
                        listOfMessages.clear();
                        listOfMessages.addAll(temp);
                        Collections.sort(listOfMessages);
                        mViewModel.setListOfMessages(listOfMessages);
                        chatRecyclerView.scrollToPosition(chatRecyclerViewAdapter.getItemCount() - 1);
                        chatRecyclerViewAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "db get failed in chat page " + e);
                    }
                });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(chatListener != null){
            chatListener.remove();
        }
    }
}