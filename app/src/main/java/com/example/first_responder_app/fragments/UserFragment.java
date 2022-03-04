package com.example.first_responder_app.fragments;

import android.os.Bundle;
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

import com.example.first_responder_app.FirestoreDatabase;
import com.example.first_responder_app.R;
import com.example.first_responder_app.dataModels.UsersDataModel;
import com.example.first_responder_app.databinding.FragmentUserBinding;
import com.example.first_responder_app.interfaces.ActiveUser;
import com.example.first_responder_app.viewModels.IncidentViewModel;
import com.example.first_responder_app.viewModels.UserViewModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;


public class UserFragment extends Fragment {

    private FirestoreDatabase firestoreDatabase;
    private FirebaseFirestore db;
    private UsersDataModel activeUser;
    private UsersDataModel user;

    private UserViewModel mViewModel;

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentUserBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_user, container, false);
        NavHostFragment navHostFragment = (NavHostFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        mViewModel = new ViewModelProvider(requireActivity()).get(UserViewModel.class);
        user = mViewModel.getUserDataModel();


        //Get active user id
        ActiveUser active = (ActiveUser)getActivity();
        if(active != null){
            activeUser = active.getActive();
        }


        // TODO: navCont created for side bar(still need to be implemented)
        NavController navController = navHostFragment.getNavController();
//        firestoreDatabase = new FirestoreDatabase();
//        db = firestoreDatabase.getDb();

        boolean THIS_IS_CURRENT_USER = user != null && activeUser != null &&
                                        activeUser.getDocumentId().equals(user.getDocumentId());
        if(THIS_IS_CURRENT_USER) {
            binding.userSendMessageFab.setVisibility(View.GONE);
            binding.userEditFab.setOnClickListener(v -> {
                NavDirections action = UserFragmentDirections.actionUserFragmentToEditUserFragment2();
                Navigation.findNavController(binding.getRoot()).navigate(action);
            });
        } else {
            binding.userEditFab.setVisibility(View.GONE);
            binding.userSendMessageFab.setOnClickListener(v -> {
                Snackbar.make(getView(), "Send msg!", Snackbar.LENGTH_SHORT).show();
                // TODO: Send message
            });
        }

        setText(binding);

        // TODO: GetRank should be in AppUtil or Firestore db

        // TODO: Add db/storage query

        return binding.getRoot();
    }


    private void setText(FragmentUserBinding binding){
        binding.userFullNameTv.setText(user.getFull_name());
        binding.userRankTv.setText(HomeFragment.getRank(user.getRankId()) == null ? "Unable to get rank" : HomeFragment.getRank(user.getRankId()).getRank_name());
        binding.userPhoneNumberTv.setText("" + user.getPhone_number());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = new ViewModelProvider(this).get(UserViewModel.class);
        // TODO: Use the ViewModel
    }


}