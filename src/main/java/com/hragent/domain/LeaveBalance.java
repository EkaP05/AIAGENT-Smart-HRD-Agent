package com.hragent.domain;

public class LeaveBalance {
    private final int idKaryawan;
    private final String tipeCuti;
    private final int sisaHari;
    private int sisaCutiTahunan;
    private int sisaCutiSakit;
    private int sisaCutiMelahirkan;

    public LeaveBalance(int idKaryawan, String tipeCuti, int sisaHari, int sisaCutiTahunan, int sisaCutiSakit, int sisaCutiMelahirkan) {
        this.idKaryawan = idKaryawan;
        this.tipeCuti = tipeCuti;
        this.sisaHari = sisaHari;
        this.sisaCutiTahunan = sisaCutiTahunan;
        this.sisaCutiSakit = sisaCutiSakit;
        this.sisaCutiMelahirkan = sisaCutiMelahirkan;
    }

    // Getters untuk semua fields
    public int getIdKaryawan() { return idKaryawan; }
    public String getTipeCuti() { return tipeCuti; }
    public int getSisaHari() { return sisaHari; }
    public int getSisaCutiTahunan() { return sisaCutiTahunan; }
    public int getSisaCutiSakit() { return sisaCutiSakit; }
    public int getSisaCutiMelahirkan() { return sisaCutiMelahirkan; }

    @Override
    public String toString() {
        return String.format("LeaveBalance[id=%d, tipe=%s, sisa=%d hari, tahunan=%d, sakit=%d, melahirkan=%d]", 
                            idKaryawan, tipeCuti, sisaHari, sisaCutiTahunan, sisaCutiSakit, sisaCutiMelahirkan);
    }
}
