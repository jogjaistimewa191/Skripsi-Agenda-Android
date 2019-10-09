package com.jimmy.skripsi.activities;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.UserModel;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity {

    @BindView(R.id.edNama) TextView edNama;
    @BindView(R.id.edEmail) TextView edEmail;
    @BindView(R.id.edPass) TextView edPass;
    @BindView(R.id.btnRegister) Button btnRegister;
    @BindView(R.id.progress_bar) ProgressBar progressBar;

    private FirebaseAuth mAuth;
    private String mToken = "a-z";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(this);
        mAuth = FirebaseAuth.getInstance();
        btnRegister.setOnClickListener(v -> {
            String nama = edNama.getText().toString().trim();
            String email = edEmail.getText().toString().trim();
            String password = edPass.getText().toString().trim();
            if(nama.isEmpty()){
                edNama.setError("Nama harus diisi");
                edNama.requestFocus();
                return;
            }
            if(email.isEmpty()){
                edEmail.setError("Email harus diisi");
                edEmail.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edEmail.setError("Masukkan email dengan benar");
                edEmail.requestFocus();
                return;
            }
            if(password.isEmpty()){
                edPass.setError("Password harus diisi");
                edPass.requestFocus();
                return;
            }
            if(password.length() < 6){
                edPass.setError("Minimal 6 karakter");
                edPass.requestFocus();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            actRegister(nama, email, password);
        });
    }

    private void actRegister(String nama, String email, String pass){
        Util.hideSoftKeyboard(this);
        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                if(task.getResult()==null){
                    Util.showToast(this, "Registrasi belum berhasil");
                    return;
                }
                String uid = task.getResult().getUser().getUid();
                FirebaseInstanceId.getInstance().getInstanceId()
                        .addOnCompleteListener(task1 -> {
                            mToken = task1.getResult().getToken();
                            DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
                            UserModel mData = new UserModel();
                            mData.setUID(uid);
                            mData.setNama(nama);
                            mData.setEmail(email);
                            mData.setPassword(pass);
                            mData.setTokenFcm(mToken);
                            dbRef.child("users").child(uid).setValue(mData).addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                Util.showToast(this, "Pendaftaran berhasil");
                                finish();
                            });
                        });
            } else {
                progressBar.setVisibility(View.GONE);
                if(task.getException()instanceof FirebaseAuthUserCollisionException) {
                    Toast.makeText(getApplicationContext(), "Email ini sudah terdaftar !", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
