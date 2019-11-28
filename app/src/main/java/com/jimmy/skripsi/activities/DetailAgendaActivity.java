package com.jimmy.skripsi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.PrefManager;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.AgendaModel;
import com.jimmy.skripsi.models.PengingatModel;
import com.jimmy.skripsi.service.AlarmReciever;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailAgendaActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.tvDeskripsi)
    TextView tvDeskripsi;
    @BindView(R.id.tvTgl)
    TextView tvTgl;
    @BindView(R.id.tvJam)
    TextView tvJam;
    @BindView(R.id.tvAlamat)
    TextView tvAlamat;
    @BindView(R.id.tvPengingat)
    TextView tvPengingat;
    @BindView(R.id.tvAlarm)
    TextView tvAlarm;
    @BindView(R.id.tvHasilRapat)
    TextView tvHasilRapat;
    @BindView(R.id.btnPengingat)
    Button btnPengingat;
    @BindView(R.id.btnChat)
    Button btnChatAdmin;
    @BindView(R.id.btnNav)
    Button btnNav;
    private AgendaModel mDetail;
    private GoogleMap gMap;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;

    public static void to(Context _context, String detail){
        Intent i = new Intent(_context, DetailAgendaActivity.class);
        i.putExtra("detail", detail);
        _context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_agenda);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        calendar = Calendar.getInstance();
        if(getIntent().hasExtra("detail")){
            mDetail = Gxon.from(getIntent().getStringExtra("detail"), AgendaModel.class);
            tvDeskripsi.setText(mDetail.getDeskripsi());
            tvTgl.setText(mDetail.getTanggal());
            tvJam.setText(mDetail.getWaktu());
            tvAlamat.setText(mDetail.getAlamat());
            tvHasilRapat.setText(mDetail.getHasilRapat());

            for(int i = 0;i < PengingatModel.dataPengingat().size();i++){
                if(PengingatModel.dataPengingat().get(i).getId().equals(mDetail.getPengingat()))
                    tvPengingat.setText(PengingatModel.dataPengingat().get(i).getName());
            }
            getSupportActionBar().setTitle(mDetail.getNama());
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            if(PrefManager.isAdmin()){
                tvAlarm.setVisibility(View.GONE);
                btnPengingat.setVisibility(View.GONE);
                btnChatAdmin.setVisibility(View.GONE);
            }

        }
        btnChatAdmin.setOnClickListener(v -> {
            ChatActivity.to(this);
        });
        btnPengingat.setOnClickListener(v -> {
            if(!Util.isValid(mDetail)){
                Toast.makeText(this, "Acara sudah lewat", Toast.LENGTH_SHORT).show();
                return;
            }
            aturPengingat();
        });
    }

    private void addMarker(AgendaModel data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude())));
        markerOptions.title(data.getAlamat());
        gMap.clear();
        gMap.addMarker(markerOptions);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude())), 12));
    }

    private void aturPengingat(){
        String dateTime = Util.ConvertDate(mDetail.getTanggal()+" "+mDetail.getWaktu(), "dd-MM-yyyy HH:mm", "yyyy MM dd HH:mm");
        Date timeAlarm = Util.toDate(dateTime, "yyyy MM dd HH:mm");
        Date current = calendar.getTime();

        long setInterval = 0L;
        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;
        List<Long> data = Arrays.asList(
                minutesInMilli * 3,//3 menit
                minutesInMilli * 30,// 30 menit
                hoursInMilli,// 1jam
                hoursInMilli * 3,// 3 jam
                hoursInMilli * 6,// 6 jam
                hoursInMilli * 12,// 12 jam
                daysInMilli// 24 jam
        );
        long finalInterval;
        int tipe = 0;

        for (int i =0;i < data.size();i++){
            if(mDetail.getPengingat().equals(String.valueOf(i))){
                setInterval = data.get(i);
                tipe = i;
            }
        }
        if(current.getTime() > timeAlarm.getTime() - data.get(1)){
            finalInterval = timeAlarm.getTime() - data.get(0);
            setAlarm(finalInterval);
        }else {
            long d = timeAlarm.getTime() - data.get(tipe+1);
            finalInterval = d;
            setAlarm(finalInterval);
        }

    }

    private void setAlarm(long iVal) {
        tvAlarm.setText(Util.getDate(iVal, "dd-MM-yyyy HH:mm"));
        String dF = Util.ConvertDate(tvAlarm.getText().toString(), "dd-MM-yyyy HH:mm", "yyyy MM dd HH:mm");
        Date resAlarm = Util.toDate(dF, "yyyy MM dd HH:mm");

        Log.d("DetailAgenda", "Alarm On");

        calendar.setTime(resAlarm);
        Intent myIntent = new Intent(this, AlarmReciever.class);
        pendingIntent = PendingIntent.getBroadcast(DetailAgendaActivity.this, 0, myIntent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);

        Toast.makeText(getApplicationContext(), "Pengingat telah dibuat !", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;
        // For showing a move to my location button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        gMap.getUiSettings()
                .setZoomControlsEnabled(true);
        gMap.setMyLocationEnabled(true);

        if(mDetail!=null)
            addMarker(mDetail);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
