package com.jimmy.skripsi.models;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

public class PengingatModel {
    private String id;
    private String nama;

    public PengingatModel(){}

    public static List<PengingatModel> dataPengingat(){
        return Arrays.asList(
                new PengingatModel("0","Waktu Pengingat Agenda"),
                new PengingatModel("1","3 menit"),
                new PengingatModel("2","30 menit"),
                new PengingatModel("3","1 jam"),
                new PengingatModel("4","3 jam"),
                new PengingatModel("5","6 jam"),
                new PengingatModel("6","12 jam"),
                new PengingatModel("7","24 jam")
        );
    }

    public PengingatModel(String id, String nama){
        this.id = id;
        this.nama = nama;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return nama;
    }

    public void setName(String name) {
        this.nama = name;
    }

    @NonNull
    @Override
    public String toString() {
        return nama;
    }
}
