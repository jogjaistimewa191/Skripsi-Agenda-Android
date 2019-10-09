package com.jimmy.skripsi.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimmy.skripsi.activities.DetailAgendaActivity;
import com.jimmy.skripsi.activities.FormAgendaActivity;
import com.jimmy.skripsi.helpers.PrefManager;
import com.jimmy.skripsi.models.AgendaModel;
import com.jimmy.skripsi.R;
import com.jimmy.skripsi.activities.HomeActivity;
import com.jimmy.skripsi.adapters.AgendaAdapter;
import com.jimmy.skripsi.helpers.Gxon;
import com.jimmy.skripsi.helpers.Util;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DaftarAgendaFragment extends Fragment {

    @BindView(R.id.listAgenda) RecyclerView listAgenda;
    @BindView(R.id.progress_bar) ProgressBar progress_bar;


    public static final String aksiLihat = "AKSI_LIHAT";
    public static final String aksiEdit = "AKSI_LIHAT";

    private AgendaAdapter agendaAdapter;
    private DatabaseReference databaseReference;
    private HomeActivity homeActivty;
    private String refAgenda = "Agenda";

    public static DaftarAgendaFragment newInstance() {
        DaftarAgendaFragment fragment = new DaftarAgendaFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args) ;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.daftar_agenda_fragment, container, false);
        ButterKnife.bind(this,view);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        homeActivty = (HomeActivity) getActivity();
        agendaAdapter = new AgendaAdapter(getContext(), PrefManager.isAdmin());
        listAgenda.setLayoutManager(new LinearLayoutManager(getContext()));
        listAgenda.setAdapter(agendaAdapter);
        agendaAdapter.setOnItemClickListener(new AgendaAdapter.OnItemClickListener() {
            @Override
            public void onLihat(View view, AgendaModel obj, int position) {
                DetailAgendaActivity.to(getContext(), Gxon.to(obj));
            }

            @Override
            public void onHapus(View view, AgendaModel obj, int position) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Anda yakin ingin menghapus agenda "+obj.getNama()+" ?")
                        .setCancelable(false)
                        .setPositiveButton("Ya", (dialog, id) ->hapus(obj.getId_acara(), position))
                        .setNegativeButton("Tidak", (dialog, id) -> dialog.cancel());
                AlertDialog alert = builder.create();
                alert.show();
            }

            @Override
            public void onEdit(View view, AgendaModel obj, int position) {
                FormAgendaActivity.edit(getContext(), Gxon.to(obj));
            }
        });
        tampil();
        return view;
    }

//    private void open(Fragment f, String title) {
//        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.container, f).commit();
//        getActivity().getActionBar().setTitle(title);
//    }

    private void tampil(){
        progress_bar.setVisibility(View.VISIBLE);
        databaseReference.child(refAgenda).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<AgendaModel> eventsList = new ArrayList<AgendaModel>();
                for (DataSnapshot eventSnapshot: dataSnapshot.getChildren()) {
                    eventsList.add(eventSnapshot.getValue(AgendaModel.class));
                }
                agendaAdapter.addItem(eventsList);
                progress_bar.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(),databaseError.getMessage(),
                        Toast.LENGTH_SHORT).show();
                progress_bar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void hapus(String idAgenda, int position){
        FirebaseDatabase.getInstance().getReference()
                .child(refAgenda).child(idAgenda).removeValue()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        agendaAdapter.removeItem(position);
                        Util.showToast(getActivity(), "Agenda berhasil dihapus");
                    } else {
                        Util.showToast(getActivity(), "gagal hapus agenda");
                    }
                });
    }

}
