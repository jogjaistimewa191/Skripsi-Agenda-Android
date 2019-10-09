package com.jimmy.skripsi.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.core.view.GravityCompat;
import androidx.appcompat.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.fragments.DaftarAgendaFragment;
import com.jimmy.skripsi.fragments.UserChatFragment;
import com.jimmy.skripsi.helpers.PermissionsManager;
import com.jimmy.skripsi.helpers.PrefManager;
import com.jimmy.skripsi.models.UserModel;

import java.util.List;
import java.util.Objects;

import butterknife.ButterKnife;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, PermissionsManager.PermissionsListener {

    private PermissionsManager permissionsManager;
    private TextView tvName;
    private String mUID;
    private String name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(PrefManager.isAdmin())
            setContentView(R.layout.activity_home_admin);
        else
            setContentView(R.layout.activity_home);
        ButterKnife.bind(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        tvName = navigationView.getHeaderView(0).findViewById(R.id.tvName);
        navigationView.getMenu().getItem(0).setChecked(true);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        checkPermission();
        getProfile();

    }

    @SuppressLint("SetTextI18n")
    private void getProfile() {
        if(PrefManager.isAdmin()){
            FirebaseMessaging.getInstance().subscribeToTopic("agenda-admin");
            tvName.setText("Admin");
            return;
        }
        FirebaseMessaging.getInstance().unsubscribeFromTopic("agenda-admin");
        DatabaseReference profileRef = FirebaseDatabase.getInstance().getReference();
        profileRef.child("users").child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                .addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                UserModel mProfile = dataSnapshot.getValue(UserModel.class);
                assert mProfile != null;
                tvName.setText(mProfile.getNama());
                name = mProfile.getNama();
                mUID = mProfile.getUID();

                PrefManager.setName(name);
                PrefManager.setUID(mUID);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    private void checkPermission() {
        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }else {
            open(DaftarAgendaFragment.newInstance(), "List Agenda");
        }
    }

    public void open(Fragment f, String title) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Anda yakin ingin keluar dari aplikasi?")
                    .setCancelable(false)
                    .setPositiveButton("Ya", (dialog, id) ->finish())
                    .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_add) {
            FormAgendaActivity.to(this);
        }else if (id == R.id.nav_list_agenda) {
            open(DaftarAgendaFragment.newInstance(), "List Agenda");
        }else if(id == R.id.nav_list_chat){
            open(UserChatFragment.newInstance(), "List User");
        }else if(id == R.id.nav_list_bantuan){
        }else if(id == R.id.nav_list_logout){
            new AlertDialog.Builder(this)
                    .setTitle("Yakin Ingin Logout ?")
                    .setPositiveButton("Ya", (dialog, which) -> {
                        if(PrefManager.isAdmin())
                            PrefManager.clear();
                        else
                            FirebaseAuth.getInstance().signOut();
                        PrefManager.setLogin(false);
                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Tidak", null)
                    .show();
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(!granted){
            Toast.makeText(this, "Lokasi harus diaktifkan", Toast.LENGTH_SHORT).show();
            finish();
        }else {
            open(DaftarAgendaFragment.newInstance(), "List Agenda");
        }
    }

}
