package com.example.first_responder_app.recyclerViews;

import static org.mockito.Mockito.mock;

import androidx.fragment.app.Fragment;

import com.example.first_responder_app.dataModels.UsersDataModel;
import com.google.common.truth.Truth;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.core.View;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

import java.util.List;

class RespondersRecyclerViewAdapterTest {
    private RespondersRecyclerViewAdapter adapter;
    private RespondersRecyclerViewAdapter.ViewHolder holder;
    private View mockView;
    private Fragment mockFragment;
    private List<UsersDataModel> listOfResponders;

    private UsersDataModel user1;
    private UsersDataModel user2;

    @BeforeEach
    void setUp() {
        adapter = new RespondersRecyclerViewAdapter(mockFragment.getContext(), listOfResponders);
        mockView = mock(View.class);
        mockFragment = mock(Fragment.class);

        user1 = new UsersDataModel("address1", "first_name1", "last_name1", "password1", 1234567890L, "rank1", "username1", Timestamp.now(), "remote_path_to_profile_picture1");
        user2 = new UsersDataModel("address2", "first_name2", "last_name2", "password2", 1234567890L, "rank2", "username2", Timestamp.now(), "remote_path_to_profile_picture2");
    }

    @Test
    void getItemCount_returnsTrue() {
        listOfResponders.add(user1);
        listOfResponders.add(user2);
        adapter.notifyDataSetChanged();

        Truth.assertThat(adapter.getItemCount()).isEqualTo(2);
    }

    @Test
    void getItemShouldReturnSameItemInList_returnsTrue() {
        listOfResponders.add(user1);
        listOfResponders.add(user2);

        UsersDataModel adapterUser = adapter.getItem(2);
        UsersDataModel listUser = listOfResponders.get(2);

        Truth.assertThat(adapterUser == listUser).isTrue();
    }

    @Test
    void getItemShouldReturnSameItemInListAtDifferentIndex_returnsTrue() {
        listOfResponders.add(user1);
        listOfResponders.add(user1);

        UsersDataModel adapterUser = adapter.getItem(1);
        UsersDataModel listUser = listOfResponders.get(2);

        Truth.assertThat(adapterUser == listUser).isTrue();
    }

    @org.junit.jupiter.api.Test
    void setResponderClickListenerDoesNotCrash_returnsTrue() {
        RespondersRecyclerViewAdapter.ResponderClickListener listener = (view, position) -> {
            // Stub
        };
        adapter.setResponderClickListener(listener);

        RespondersRecyclerViewAdapter.ResponderClickListener listener2 = (view, position) -> {
            // Stub
        };
        adapter.setResponderClickListener(listener2);

        Truth.assertThat(true).isTrue();
    }
}