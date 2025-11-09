package com.hragent.domain;

public class LeaveBalance {
    private final int idKaryawan;
    private final String tipeCuti;
    private final int sisaHari;

    public LeaveBalance(int idKaryawan, String tipeCuti, int sisaHari) {
        this.idKaryawan = idKaryawan;
        this.tipeCuti = tipeCuti;
        this.sisaHari = sisaHari;
    }

    public int getIdKaryawan() { return idKaryawan; }
    public String getTipeCuti() { return tipeCuti; }
    public int getSisaHari() { return sisaHari; }

    @Override
    public String toString() {
        return String.format("LeaveBalance[id=%d, tipe=%s, sisa=%d hari]", 
                            idKaryawan, tipeCuti, sisaHari);
    }
}
