package com.jimmy.skripsi.fragments;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

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
import com.jimmy.skripsi.activities.HomeActivity;
import com.jimmy.skripsi.activities.PlacePickerActivity;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.LocationTrack;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.AgendaModel;
import com.jimmy.skripsi.models.PengingatModel;


import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.android.volley.VolleyLog.TAG;
import static com.jimmy.skripsi.activities.PlacePickerActivity.REQUEST_PLACE_PICKER;

public class TambahAgendaFragment extends Fragment implements OnMapReadyCallback {

    @BindView(R.id.edNamaAgenda) EditText edNamaAgenda;
    @BindView(R.id.edDeskripsi) EditText edDeskripsi;
    @BindView(R.id.edTanggal) TextView edTanggal;
    @BindView(R.id.edWaktu) TextView edWaktu;
    @BindView(R.id.tvAlamat) TextView tvAlamat;
    @BindView(R.id.spWaktuPengingat) Spinner spWaktuPengingat;
    @BindView(R.id.btnTambah) Button btnTambah;
    @BindView(R.id.btnPilihLokasi) Button btnPickLokasi;

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

    public static TambahAgendaFragment newInstance() {
        TambahAgendaFragment fragment = new TambahAgendaFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public static TambahAgendaFragment newInstance(String data, boolean edit) {
        TambahAgendaFragment fragment = new TambahAgendaFragment();
        Bundle args = new Bundle();
        args.putString("data", data);
        args.putBoolean("edit", edit);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.tambah_agenda_fragment, container, false);
        ButterKnife.bind(this,view);
        gps = new LocationTrack(getContext());
        currentLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        getLastID();
        if(getArguments()!=null){
            detailAgenda = Gxon.from(getArguments().getString("data"), AgendaModel.class);
            valEdit = getArguments().getBoolean("edit", false);
            if(detailAgenda!=null)tampilData(valEdit);
        }
        adapterPengingat = new ArrayAdapter<>(getActivity(), R.layout.spinner_item, PengingatModel.dataPengingat());
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
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) -> {
                edTanggal.setText(String.format(Locale.US, "%d-%d-%d", dayOfMonth, month + 1, year));
            },
                    Calendar.getInstance().get(Calendar.YEAR),
                    Calendar.getInstance().get(Calendar.MONTH),
                    Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
            datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
            datePickerDialog.show();
        });
        edWaktu.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view12, hourOfDay, minute) -> {
                edWaktu.setText(String.format(Locale.US, "%02d:%02d", hourOfDay, minute));
            },
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE),
                    DateFormat.is24HourFormat(getActivity()));
            timePickerDialog.show();
        });
        btnPickLokasi.setOnClickListener(v -> {
            Intent i = new Intent();
            i.setClass(getActivity(), PlacePickerActivity.class);
            startActivityForResult(i, REQUEST_PLACE_PICKER);
        });
        btnTambah.setOnClickListener(v -> {
            if(edNamaAgenda.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(getActivity(), "Nama agenda belum benar");
            else if(edDeskripsi.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(getActivity(), "Deskripsi belum benar");
            else if(edTanggal.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(getActivity(), "Tanggal belum benar");
            else if(edWaktu.getText().toString().trim().equalsIgnoreCase(""))
                Util.showToast(getActivity(), "Waktu belum benar");
            else if(tvAlamat == null
                    || resAlamat.getLatitude()==null || resAlamat.getLongitude()==null)
                Util.showToast(getActivity(), "Lokasi belum benar");
            else if(id_pengingat == null || id_pengingat.equalsIgnoreCase("0"))
                Util.showToast(getActivity(), "Pengingat belum benar");
            else
                tambahAgenda();

        });
        return view;
    }

    private void tampilData(boolean valEdit) {
        edNamaAgenda.setText(detailAgenda.getNama());
        edDeskripsi.setText(detailAgenda.getDeskripsi());
        edTanggal.setText(detailAgenda.getTanggal());
        edWaktu.setText(detailAgenda.getWaktu());
        tvAlamat.setText(detailAgenda.getAlamat());
        resAlamat.setLatitude(detailAgenda.getLatitude());
        resAlamat.setLongitude(detailAgenda.getLongitude());
        spWaktuPengingat.setEnabled(valEdit);
        //addMarker(detailAgenda);

        edNamaAgenda.setEnabled(valEdit);
        edDeskripsi.setEnabled(valEdit);
        edTanggal.setEnabled(valEdit);
        edWaktu.setEnabled(valEdit);
        tvAlamat.setEnabled(valEdit);
        btnPickLokasi.setVisibility(valEdit ? View.VISIBLE:View.GONE);
        btnTambah.setVisibility(valEdit ? View.VISIBLE:View.GONE);
        btnTambah.setText("Edit");

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

    private void tambahAgenda() {
        progressDialog = Util.showProgressDialog(getActivity(), "Silahkan tunggu...");
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
        addToDatabase(dataInsert, valEdit ? Integer.parseInt(detailAgenda.getId_acara()):lastIdAgenda+1);
    }

    private void addToDatabase(AgendaModel dataInsert, int idAgenda){
        dataInsert.setId_acara(String.valueOf(idAgenda));
        FirebaseDatabase.getInstance().getReference().child(refAgenda)
                .child(String.valueOf(idAgenda))
                .setValue(dataInsert).addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        progressDialog.dismiss();
                        Toast.makeText(getActivity(), valEdit ? "Berhasil diperbaharui":"Berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), HomeActivity.class));
                        getActivity().finish();
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if(detailAgenda!=null){
            addMarker(detailAgenda);
            return;
        }

        // For showing a move to my location button
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PLACE_PICKER && resultCode == RESULT_OK) {
            resAlamat = Gxon.from(data.getStringExtra("resAlamat"), AgendaModel.class);
            tvAlamat.setText(resAlamat.getAlamat());
            addMarker(resAlamat);
        }
    }

    private void addMarker(AgendaModel data){
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude())));
        markerOptions.title(data.getAlamat());
        googleMap.clear();
        googleMap.addMarker(markerOptions);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(data.getLatitude()), Double.parseDouble(data.getLongitude())), 12));
    }
}
