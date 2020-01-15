package com.jimmy.skripsi.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.PrefManager;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.AdminModel;

import java.util.HashMap;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.edEmail) EditText edEmail;
    @BindView(R.id.edPass) EditText edPass;
    @BindView(R.id.edUser) EditText edUser;
    @BindView(R.id.edPassAdmin) EditText edPassAdmin;
    @BindView(R.id.tvDeskripsi) TextView tvDeskripsi;
    @BindView(R.id.btnAdmin) TextView btnAdmin;
    @BindView(R.id.btnUser) TextView btnUser;
    @BindView(R.id.btnLogin) Button btnLogin;
    @BindView(R.id.progress_bar) ProgressBar progressBar;
    @BindView(R.id.lytInputUser) View lytInput;
    @BindView(R.id.lytInputAdmin) View lytInputAdmin;


    private FirebaseAuth mAuth;
    private HashMap<Integer, View> viewHashMap = new HashMap<>();
    private boolean isAdmin = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        changeButton(btnAdmin);
        changeForm(true);
        btnLogin.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            if(isAdmin)
                preLoginAdmin();
            else
                preLoginUser();

        });

        tvDeskripsi.setOnClickListener(v -> startActivity(new Intent(this, RegisterActivity.class)));
    }
    @OnClick(R.id.btnUser)
    public void asLoginUser(){
        changeButton(btnUser);
        changeForm(false);
    }
    @OnClick(R.id.btnAdmin)
    public void asLoginAdmin(){
        changeButton(btnAdmin);
        changeForm(true);
    }

    private void changeButton(TextView bet) {
        if (viewHashMap.size() == 0) viewHashMap.put(0, bet);
        TextView textView = (TextView) viewHashMap.get(0);
        textView.setBackgroundResource(R.drawable.btn_round);
        textView.setTextColor(getResources().getColor(R.color.colorDeactive));

        bet.setBackgroundResource(R.drawable.gray_button_background);
        bet.setTextColor(getResources().getColor(R.color.colorPrimary));
        viewHashMap.put(0, bet);
    }

    private void changeForm(boolean isAdmin){
        lytInputAdmin.setVisibility(isAdmin ? View.VISIBLE:View.GONE);
        lytInput.setVisibility(isAdmin ? View.GONE:View.VISIBLE);
        tvDeskripsi.setVisibility(isAdmin ? View.GONE:View.VISIBLE);
        this.isAdmin = isAdmin;
    }

    private void actLoginAdmin(String user, String pass){
        DatabaseReference tbAdmin = FirebaseDatabase.getInstance().getReference();
        Query query = tbAdmin.child("admin").orderByChild("username").equalTo(user);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot admin : dataSnapshot.getChildren()) {
                        AdminModel adminData = admin.getValue(AdminModel.class);
                        if (adminData.getPassword().equals(pass)) {
                            PrefManager.setAdmin(true);
                            PrefManager.setName(adminData.getUsername());
                            moveToMain();
                            Toast.makeText(LoginActivity.this, "Anda login sebagai admin", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(LoginActivity.this, "Username/Password salah", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                } else {
                    Toast.makeText(LoginActivity.this, "Admin tidak ditemukan", Toast.LENGTH_LONG).show();
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void preLoginAdmin(){
        String email = edUser.getText().toString().trim();
        String password = edPassAdmin.getText().toString().trim();
        if(email.isEmpty()){
            edUser.setError("User harus diisi");
            edUser.requestFocus();
            return;
        }
        if(password.isEmpty()){
            edPassAdmin.setError("Password harus diisi");
            edPassAdmin.requestFocus();
            return;
        }
        actLoginAdmin(email, password);
    }

    private void preLoginUser(){
        String email = edEmail.getText().toString().trim();
        String password = edPass.getText().toString().trim();
        if(email.isEmpty()){
            edEmail.setError("Email harus diisi");
            edEmail.requestFocus();
            return;
        }
        if(password.isEmpty()){
            edPass.setError("Password harus diisi");
            edPass.requestFocus();
            return;
        }
        actLogin(email, password);
    }

    private void actLogin(String email, String password) {
        Util.hideSoftKeyboard(this);
        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if(task.isSuccessful()) {
                String uid = task.getResult().getUser().getUid();
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(task1 -> {
                            String mToken = task1.getResult().getToken();
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                            dbRef.child("users").child(uid).child("tokenFcm").setValue(mToken).addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Util.showToast(this, "Login berhasil");
                                moveToMain();
                                finish();
                            });
                        });

            } else {
                String error;
                try {
                    throw Objects.requireNonNull(task.getException());
                }catch(FirebaseAuthInvalidCredentialsException e) {
                    error = "Password salah";
                }catch(Exception e) {
                    error = "Kesalahan/email belum terdaftar";
                }
                Util.showToast(LoginActivity.this, error);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    private void moveToMain(){
        PrefManager.setLogin(true);
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(PrefManager.isLogin()){
            if(PrefManager.isAdmin() || mAuth.getCurrentUser() !=null)
                moveToMain();
        }


    }
}
