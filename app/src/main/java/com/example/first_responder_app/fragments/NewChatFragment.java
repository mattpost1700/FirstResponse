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
import com.example.first_responder_app.databinding.ChatFragmentBinding;
import com.example.first_responder_app.databinding.ChatNewFragmentBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.messaging.Chat;
import com.example.first_responder_app.messaging.Message;
import com.example.first_responder_app.recyclerViews.ChatGroupRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.ChatRecyclerViewAdapter;
import com.example.first_responder_app.recyclerViews.NewChatRecyclerViewAdapter;
import com.example.first_responder_app.viewModels.ChatViewModel;
import com.firebase.ui.auth.data.model.User;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class NewChatFragment extends Fragment {

    private ChatViewModel mViewModel;
    private Message messageInfo;
    private FirestoreDatabase firestoreDatabase;
    private FirebaseFirestore db;
    private ActiveUser activeUser;
    private UsersDataModel user;
    private List<UsersDataModel> listOfNewUsers;
    private List<UsersDataModel> listOfUsers;
    private List<String> listOfMembers;
    private NewChatRecyclerViewAdapter newChatRecyclerViewAdapter;
    private Chat c;

    // ref: https://github.com/ScaleDrone/android-chat-tutorial/blob/master/app/src/main/res/layout/activity_main.xml
    public static NewChatFragment newInstance() {
        return new NewChatFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        //binding fragment with nav_map by using navHostFragment, throw this block of code in there and that allows you to switch to other fragments
        ChatNewFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.chat_new_fragment, container, false);
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

        listOfUsers = new ArrayList<>();
        populateUserList();
        listOfNewUsers = new ArrayList<>();

        NewChatRecyclerViewAdapter.ItemClickListener userClickListener = ((view, position, data) -> {
            //remove user
            listOfNewUsers.remove(data);
            newChatRecyclerViewAdapter.notifyDataSetChanged();
        });

        RecyclerView newChatRecyclerView = binding.newChatRecyclerView;
        newChatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        newChatRecyclerViewAdapter = new NewChatRecyclerViewAdapter(getContext(), listOfNewUsers);
        newChatRecyclerViewAdapter.setClickListener(userClickListener);
        newChatRecyclerView.setAdapter(newChatRecyclerViewAdapter);


        newChatRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                newChatRecyclerView.removeOnLayoutChangeListener(this);
                //newChatRecyclerView.scrollToPosition(newChatRecyclerView.getAdapter().getItemCount() - 1);
            }
        });

        binding.newChatAddUserFab.setOnClickListener(v -> {

            String newUserName = binding.newChatUsersEditText.getText().toString();
            UsersDataModel result = listOfUsers.stream()
                    .filter(u -> newUserName.equals(u.getFull_name()))
                    .findAny()
                    .orElse(null);

            //If user exists, is not the current user, and has not already been added
            if (result != null && !result.getDocumentId().equals(user.getDocumentId()) && !listOfNewUsers.contains(result)) {
                binding.newChatErrorMsg.setText("");
                listOfNewUsers.add(result);
                newChatRecyclerViewAdapter.notifyDataSetChanged();
            } else {
                binding.newChatErrorMsg.setText("User not found");
            }

        });

        binding.newChatCreateConfirm.setOnClickListener(v -> {
            String chatName = binding.newChatChatNameEditText.getText().toString();

            if (listOfNewUsers.size() == 0) {
                binding.newChatErrorMsg.setText("Must add users to the chat");
            } else if (chatName.length() == 0) {
                binding.newChatErrorMsg.setText("Chat name must not be blank");
            }else {
                listOfNewUsers.add(user);
                firestoreDatabase.addChat(chatName, listOfNewUsers);
            }
            NavDirections action = NewChatFragmentDirections.actionNewChatFragmentToChatGroupFragment();
            Navigation.findNavController(binding.getRoot()).navigate(action);

        });

        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        // TODO: Use the ViewModel
    }


    private void populateUserList() {
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                for (QueryDocumentSnapshot doc : task.getResult()){
                    UsersDataModel u = new UsersDataModel(doc.getId(), (String) doc.get("first_name"), (String) doc.get("last_name"));
                    listOfUsers.add(u);
                }
                UsersDataModel currentU = new UsersDataModel(user.getDocumentId(), user.getFirst_name(), user.getLast_name());
                listOfUsers.add(currentU);
            } else {
                Log.d(TAG, "db get failed in new chat page " + task.getException());
            }
        });
    }




}