package com.jimmy.skripsi.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.LocationTrack;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.AgendaModel;
import com.jimmy.skripsi.models.PengingatModel;

import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.android.volley.VolleyLog.TAG;
import static com.jimmy.skripsi.activities.PlacePickerActivity.REQUEST_PLACE_PICKER;

public class FormAgendaActivity extends AppCompatActivity implements OnMapReadyCallback {

    @BindView(R.id.edNamaAgenda) EditText edNamaAgenda;
    @BindView(R.id.edDeskripsi) EditText edDeskripsi;
    @BindView(R.id.edHasilRapat) EditText edHasilRapat;
    @BindView(R.id.edTanggal) TextView edTanggal;
    @BindView(R.id.edWaktu) TextView edWaktu;
    @BindView(R.id.tvAlamat) TextView tvAlamat;
    @BindView(R.id.spWaktuPengingat) Spinner spWaktuPengingat;
    @BindView(R.id.btnTambah) Button btnTambah;
    @BindView(R.id.btnPilihLokasi) Button btnPickLokasi;
    @BindView(R.id.lytHasilRapat) View lytHasilRapat;

    private GoogleMap googleMap;
    private LocationTrack gps;
    private LatLng currentLocation;
    private ArrayAdapter<PengingatModel> adapterPengingat;
    private String id_pengingat;
    private AgendaModel detailAgenda;
    private AgendaModel resAlamat = new AgendaModel();
    private int lastIdAgenda;
    public static final String refAgenda = "Agenda";
    private Query lastId;
    ProgressDialog progressDialog;
    private boolean valEdit;

    public static void to(Context context){
        Intent i = new Intent(context, FormAgendaActivity.class);
        context.startActivity(i);
    }

    public static void edit(Context context, String data){
        Intent i = new Intent(context, FormAgendaActivity.class);
        i.putExtra("data", data);
        context.startActivity(i);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form_agenda);
        ButterKnife.bind(this);
        gps = new LocationTrack(this);
        currentLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLastID();
        if(getIntent().hasExtra("data") && getIntent().getStringExtra("data") != null){
            getSupportActionBar().setTitle("Ubah Agenda");
            detailAgenda = Gxon.from(getIntent().getStringExtra("data"), AgendaModel.class);
            tampilData(true);
            valEdit = true;
        }
        initView();
    }

    private void initView(){
        adapterPengingat = new ArrayAdapter<>(this, R.layout.spinner_item, PengingatModel.dataPengingat());
        spWaktuPengingat.setAdapter(adapterPengingat);
        spWaktuPengingat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
                id_pengingat = PengingatModel.dataPengingat().get(pos).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        if(detailAgenda!=null){
            for (int i = 0; i < PengingatModel.dataPengingat().size();i++){
                if(PengingatModel.dataPengingat().get(i).getId().equals(detailAgenda.getPengingat())){
                    spWaktuPengingat.setSelection(i);
                }
            }
        }
        edTanggal.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view1, year, month, dayOfMonth) -> {
                edTanggal.setText(String.format(Locale.US, "%d-%d-%d", dayOfMonth, month + 1, year));
            },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
        edWaktu.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view12, hourOfDay, minute) -> {
                edWaktu.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            },
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(this));
            timePickerDialog.show();
        });
        btnPickLokasi.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setClass(this, PlacePickerActivity.class);
            startActivityForResult(i, REQUEST_PLACE_PICKER);
        });
        btnTambah.setOnClickListener(v -> {
            if(edNamaAgenda.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(this, "Nama agenda belum benar");
            else if(edDeskripsi.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(this, "Deskripsi belum benar");
            else if(edTanggal.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(this, "Tanggal belum benar");
            else if(edWaktu.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(this, "Waktu belum benar");
            else if(tvAlamat == null
                    || resAlamat.getLatitude()==null || resAlamat.getLongitude()==null)
                Util.showToast(this, "Lokasi belum benar");
            else if(id_pengingat == null || id_pengingat.equalsIgnoreCase("0"))
                Util.showToast(this, "Pengingat belum benar");
            else
                tambahAgenda();

        });
    }


    private void tampilData(boolean valEdit) {
        edNamaAgenda.setText(detailAgenda.getNama());
        edDeskripsi.setText(detailAgenda.getDeskripsi());
        edTanggal.setText(detailAgenda.getTanggal());
        edWaktu.setText(detailAgenda.getWaktu());
        tvAlamat.setText(detailAgenda.getAlamat());
        if(detailAgenda.getHasilRapat()!=null)edHasilRapat.setText(detailAgenda.getHasilRapat());
        resAlamat.setLatitude(detailAgenda.getLatitude());
        resAlamat.setLongitude(detailAgenda.getLongitude());
        spWaktuPengingat.setEnabled(valEdit);
        //addMarker(detailAgenda);

        edNamaAgenda.setEnabled(valEdit);
        edDeskripsi.setEnabled(valEdit);
        edTanggal.setEnabled(valEdit);
        edWaktu.setEnabled(valEdit);
        tvAlamat.setEnabled(valEdit);
        edHasilRapat.setEnabled(valEdit);
        btnPickLokasi.setVisibility(valEdit ? View.VISIBLE:View.GONE);
        btnTambah.setVisibility(valEdit ? View.VISIBLE:View.GONE);
        lytHasilRapat.setVisibility(Util.isValid(detailAgenda) ? View.GONE:View.VISIBLE);
        btnTambah.setText("Edit");
        this.valEdit = valEdit;

    }

    private void tambahAgenda() {
        progressDialog = Util.showProgressDialog(this, "Silahkan tunggu...");
        progressDialog.show();
        AgendaModel dataInsert = new AgendaModel();
        dataInsert.setNama(edNamaAgenda.getText().toString());
        dataInsert.setDeskripsi(edDeskripsi.getText().toString());
        dataInsert.setTanggal(edTanggal.getText().toString());
        dataInsert.setWaktu(edWaktu.getText().toString());
        dataInsert.setAlamat(tvAlamat.getText().toString());
        dataInsert.setLatitude(resAlamat.getLatitude());
        dataInsert.setLongitude(resAlamat.getLongitude());
        dataInsert.setPengingat(id_pengingat);
        dataInsert.setHasilRapat(edHasilRapat.getText().toString().trim());

        addToDatabase(dataInsert, valEdit ? Integer.parseInt(detailAgenda.getId_acara()):lastIdAgenda+1);
    }

    private void addToDatabase(AgendaModel dataInsert, int idAgenda){
        dataInsert.setId_acara(String.valueOf(idAgenda));

        FirebaseDatabase.getInstance().getReference().child(refAgenda)
                .child(String.valueOf(idAgenda))
                .setValue(dataInsert).addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                progressDialog.dismiss();
                Util.showToast(this, valEdit ? "Berhasil diperbaharui":"Berhasil ditambahkan");
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        });
    }

    private void getLastID() {
        final DatabaseReference idDatabaseRef = FirebaseDatabase.getInstance()
                .getReference(refAgenda);
        lastId = idDatabaseRef.orderByKey().limitToLast(1);
        lastId.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    lastIdAgenda = Integer.parseInt(childSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Timber.tag(TAG).w(databaseError.toException(), "onCancelled");
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        // For showing a move to my location button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.getUiSettings()
                .setZoomControlsEnabled(true);
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        if(detailAgenda!=null)addMarker(detailAgenda);

    }

    private void addMarker(AgendaModel data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude())));
        markerOptions.title(data.getAlamat());
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude())), 12));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PLACE_PICKER && resultCode == RESULT_OK) {
            resAlamat = Gxon.from(data.getStringExtra("resAlamat"), AgendaModel.class);
            tvAlamat.setText(resAlamat.getAlamat());
            addMarker(resAlamat);
        }
    }
}
