package com.jimmy.skripsi.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Window;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.helpers.CurrentBottomSheet;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.LocationTrack;
import com.jimmy.skripsi.helpers.Util;
import com.jimmy.skripsi.models.AgendaModel;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


public class PlacePickerActivity extends AppCompatActivity implements
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback {

    public static final int REQUEST_PLACE_PICKER = 100;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 200;

    @BindView(R.id.picker_bottom_sheet)
    CurrentBottomSheet bottomSheet;
    @BindView(R.id.image_view_marker)
    ImageView imgMarker;
    @BindView(R.id.place_chosen_button)
    FloatingActionButton btnPick;

    private LocationTrack gps;
    private LatLng currentLocation;
    private GoogleMap map;
    private LatLng pickLoc;
    private String resAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        Mapbox.getInstance(this, getString(R.string.token_mapbox));
        setContentView(R.layout.activity_place_picker);
        ButterKnife.bind(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_view);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        gps = new LocationTrack(this);
        currentLocation = new LatLng(gps.getLatitude(), gps.getLongitude());
        btnPick.setOnClickListener(v -> {
            Intent returningIntent = new Intent();
            AgendaModel data = new AgendaModel();
            data.setAlamat(resAddress);
            data.setLatitude(String.valueOf(pickLoc.latitude));
            data.setLongitude(String.valueOf(pickLoc.longitude));
            returningIntent.putExtra("resAlamat", Gxon.to(data));
            setResult(AppCompatActivity.RESULT_OK, returningIntent);
            finish();
        });


    }

    @OnClick(R.id.place_search)
    public void doSearch() {
        Intent intent = new PlaceAutocomplete.IntentBuilder()
                .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.token_mapbox))
                .placeOptions(PlaceOptions.builder()
                        .backgroundColor(Color.parseColor("#EEEEEE"))
                        .limit(10)
                        //.addInjectedFeature(home)
                        //.addInjectedFeature(work)
                        .build(PlaceOptions.MODE_CARDS))
                .build(PlacePickerActivity.this);
        startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // For showing a move to my location button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 14));
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraIdleListener(this);
    }

    @Override
    public void onCameraIdle() {
        Timber.v("Map camera is now idling.");
        imgMarker.animate().translationY(0)
                .setInterpolator(new OvershootInterpolator()).setDuration(250).start();
        reversetoAddress();

    }

    private void reversetoAddress() {
        pickLoc = map.getCameraPosition().target;
        if (pickLoc != null) {
            resAddress = Util.getAddressFromLocation(this, map.getCameraPosition().target.latitude, map.getCameraPosition().target.longitude);
            bottomSheet.setPlaceDetails(resAddress);
        }
    }

    @Override
    public void onCameraMoveStarted(int i) {
        Timber.v("Map camera has begun moving.");
        if (imgMarker.getTranslationY() == 0) {
            imgMarker.animate().translationY(-75)
                    .setInterpolator(new OvershootInterpolator()).setDuration(250).start();
            bottomSheet.setPlaceDetails(null);
//            if (bottomSheet.isShowing()) {
//                bottomSheet.dismissPlaceDetails();
//            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            map.animateCamera(CameraUpdateFactory.newCameraPosition(
                    new CameraPosition.Builder()
                            .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                    ((Point) selectedCarmenFeature.geometry()).longitude()))
                            .zoom(18)
                            .build()));
            reversetoAddress();
        }



    }
}
