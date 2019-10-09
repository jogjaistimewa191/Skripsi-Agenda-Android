package com.jimmy.skripsi.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.activities.HomeActivity;
import com.jimmy.skripsi.adapters.ChatAdapter;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.MyDate;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.ChatModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatFragment extends Fragment {

    @BindView(R.id.listChat)
    RecyclerView listChat;
    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;
    @BindView(R.id.et_message)
    EditText edMsg;
    @BindView(R.id.icSend)
    ImageView icSend;

    private DatabaseReference roomRef;
    private HomeActivity homeActivty;
    private ChatAdapter chatAdapter;
    private Query mRef;
    private String idRoom = "admin-0";
    private RequestQueue mRequestQue;
    private final String TAG = ChatFragment.class.getSimpleName();


    public static ChatFragment newInstance(String roomID) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putString("roomID", roomID);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.chat_fragment, container, false);
        ButterKnife.bind(this, view);
        homeActivty = (HomeActivity) getActivity();
        mRequestQue = Volley.newRequestQueue(homeActivty);
        if (getArguments() != null) {
            idRoom = getArguments().getString("roomID");
        }
        chatAdapter = new ChatAdapter(getContext());
        listChat.setLayoutManager(new LinearLayoutManager(getContext()));
        listChat.setAdapter(chatAdapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("chat");
        roomRef = databaseReference.child(idRoom);
        mRef = roomRef.orderByKey().limitToLast(50);
        mRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatModel messages = dataSnapshot.getValue(ChatModel.class);
                chatAdapter.add(messages);
                chatAdapter.notifyDataSetChanged();
                listChat.scrollToPosition(chatAdapter.getItemCount() - 1);

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
        isTyping(false);
        edMsg.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().isEmpty() && s.length() > 0 && !s.equals("")) {
                    isTyping(true);
                } else {
                    isTyping(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        icSend.setOnClickListener(v -> {
            if (!edMsg.getText().toString().trim().isEmpty()) addMessage();
        });

        return view;
    }

    private void addMessage() {
        ChatModel mData = new ChatModel();
        mData.setName(idRoom.substring(0, idRoom.indexOf("-")));
        mData.setMessage(edMsg.getText().toString());
        mData.setTime(MyDate.getCurDateTime());
        mData.setStatus("user");
        roomRef.push().setValue(mData);
        edMsg.setText("");
        Util.hideSoftKeyboard(homeActivty);
        sendNotification(mData);

    }

    private void isTyping(boolean typing) {
        icSend.setColorFilter(typing ? ContextCompat.getColor(homeActivty, R.color.colorPrimary) : ContextCompat.getColor(homeActivty, R.color.colorDarkGray), android.graphics.PorterDuff.Mode.MULTIPLY);
        icSend.setEnabled(typing);
    }

    private void sendNotification(ChatModel mData) {
        JSONObject json = new JSONObject();
        mData.setRoomId(idRoom);
        try {
            json.put("to","/topics/"+"agenda-admin");
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("data", Gxon.to(mData));
            json.put("data",notificationObj);

            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, "https://fcm.googleapis.com/fcm/send",
                    json,
                    response -> Log.d(TAG, "onResponse: ")
                    , error -> Log.d(TAG, "onError: "+error.networkResponse)
            ){
                @Override
                public Map<String, String> getHeaders() {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AIzaSyBIR01SRBmK_dYqTx4gH-HG78A1RPMApCY");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }



}
