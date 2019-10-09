package com.jimmy.skripsi.models;

public class AgendaModel {
    private String id_acara;
    private String nama;
    private String deskripsi;
    private String waktu;
    private String tanggal;
    private String pengingat;
    private String alamat;
    private String latitude;
    private String longitude;

    public String getId_acara() {
        return id_acara;
    }

    public void setId_acara(String id_acara) {
        this.id_acara = id_acara;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public String getWaktu() {
        return waktu;
    }

    public void setWaktu(String waktu) {
        this.waktu = waktu;
    }

    public String getTanggal() {
        return tanggal;
    }

    public void setTanggal(String tanggal) {
        this.tanggal = tanggal;
    }

    public String getPengingat() {
        return pengingat;
    }

    public void setPengingat(String pengingat) {
        this.pengingat = pengingat;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
