package com.jimmy.skripsi.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.activities.AdminChatActivity;
import com.jimmy.skripsi.adapters.ListUserAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserChatFragment extends Fragment {

    @BindView(R.id.listUser)
    RecyclerView listUser;
    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;
    private ListUserAdapter adapter;

    public static UserChatFragment newInstance() {
        UserChatFragment fragment = new UserChatFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.list_chat_fragment, container, false);
        ButterKnife.bind(this, view);
        adapter = new ListUserAdapter(getContext());
        listUser.setLayoutManager(new LinearLayoutManager(getContext()));
        listUser.setAdapter(adapter);
        adapter.setOnItemClickListener((user, room, position) -> AdminChatActivity.to(getContext(), room, user));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chat");
        databaseReference.keepSynced(true);
        databaseReference.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String room = dataSnapshot.getKey();
                adapter.add(room);
                adapter.notifyDataSetChanged();
                listUser.scrollToPosition(adapter.getItemCount() - 1);

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        return view;
    }

}
