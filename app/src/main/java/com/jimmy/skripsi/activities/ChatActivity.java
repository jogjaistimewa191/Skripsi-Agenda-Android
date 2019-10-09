package com.jimmy.skripsi.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.adapters.ChatAdapter;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.MyDate;
import com.jimmy.skripsi.helpers.PrefManager;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.ChatModel;
import com.jimmy.skripsi.models.UserModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ChatActivity extends AppCompatActivity {

    private static final String TAG = ChatActivity.class.getSimpleName();
    @BindView(R.id.listChat)
    RecyclerView listChat;
    @BindView(R.id.progress_bar)
    ProgressBar progress_bar;
    @BindView(R.id.et_message)
    EditText edMsg;
    @BindView(R.id.icSend)
    ImageView icSend;
    private DatabaseReference roomRef;
    private ChatAdapter chatAdapter;
    private Query mRef;
    private String idRoom = "admin-0";
    private RequestQueue mRequestQue;
    private String name;
    private String mUID;

    public static void to(Context _context){
        Intent i = new Intent(_context, ChatActivity.class);
        _context.startActivity(i);
    }

    public static Intent fromNotif(Context _context, String dataNotif) {
        Intent i = new Intent(_context, ChatActivity.class);
        i.putExtra("dataNotif", dataNotif);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        _context.startActivity(i);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_fragment);
        ButterKnife.bind(this);
        mRequestQue = Volley.newRequestQueue(this);
        if(getIntent().hasExtra("dataNotif")){
            getProfile();
            idRoom = name+"-"+mUID;
        }else {
            idRoom = PrefManager.getName() +"-"+PrefManager.getUID();
            loadView();
        }
        getSupportActionBar().setTitle("Chat Admin");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void loadView(){
        chatAdapter = new ChatAdapter(this);
        listChat.setLayoutManager(new LinearLayoutManager(this));
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
    }

    private void getProfile() {
        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference();
        profileRef.child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        UserModel mProfile = dataSnapshot.getValue(UserModel.class);
                        name = mProfile.getNama();
                        mUID = mProfile.getUID();
                        loadView();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    private void addMessage() {
        ChatModel mData = new ChatModel();
        mData.setName(idRoom.substring(0, idRoom.indexOf("-")));
        mData.setMessage(edMsg.getText().toString());
        mData.setTime(MyDate.getCurDateTime());
        mData.setStatus("user");
        roomRef.push().setValue(mData);
        edMsg.setText("");
        Util.hideSoftKeyboard(this);
        sendNotification(mData);
    }

    private void isTyping(boolean typing) {
        icSend.setColorFilter(typing ? ContextCompat.getColor(this, R.color.colorPrimary) : ContextCompat.getColor(this, R.color.colorDarkGray), android.graphics.PorterDuff.Mode.MULTIPLY);
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
                    header.put("authorization","key=AIzaSyCpTqpx3SscUvw7W9zHTfzojhjXBW9NBuQ");
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
