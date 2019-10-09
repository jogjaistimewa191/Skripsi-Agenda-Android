package com.jimmy.skripsi.activities;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.jimmy.skripsi.R;

import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED;
import static com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_HIDDEN;

public class CurrentBottomSheet extends CoordinatorLayout {

    private BottomSheetBehavior bottomSheetBehavior;
    private CoordinatorLayout rootView;
    private TextView placeNameTextView;
    private ProgressBar placeProgressBar;

    public CurrentBottomSheet(Context context) {
        this(context, null);
    }

    public CurrentBottomSheet(Context context, AttributeSet attrs) {
        this(context, attrs, -1);
    }

    public CurrentBottomSheet(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    private void initialize(Context context) {
        rootView = (CoordinatorLayout) inflate(context, R.layout.bottom_sheet_container, this);
        bottomSheetBehavior = BottomSheetBehavior.from(rootView.findViewById(R.id.root_bottom_sheet));
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(STATE_HIDDEN);
        bindViews();
    }

    private void bindViews() {
        placeNameTextView = findViewById(R.id.text_view_place_name);
        placeProgressBar = findViewById(R.id.progress_bar_place);
    }

    public void setPlaceDetails(@Nullable String address) {
        if (!isShowing()) {
            toggleBottomSheet();
        }
        if (address == null) {
            placeNameTextView.setText("");
            placeProgressBar.setVisibility(VISIBLE);
            return;
        }
        placeProgressBar.setVisibility(INVISIBLE);

        placeNameTextView.setText(address);
    }

    public void dismissPlaceDetails() {
        toggleBottomSheet();
    }

    public boolean isShowing() {
        return bottomSheetBehavior.getState() != STATE_HIDDEN;
    }

    private void toggleBottomSheet() {
        bottomSheetBehavior.setPeekHeight(rootView.findViewById(R.id.bottom_sheet_header).getHeight());
        bottomSheetBehavior.setHideable(isShowing());
        bottomSheetBehavior.setState(isShowing() ? STATE_HIDDEN : STATE_COLLAPSED);
    }
}
