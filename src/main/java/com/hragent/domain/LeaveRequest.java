package com.hragent.domain;

import java.time.LocalDate;

public class LeaveRequest {
    private final String idRequest;
    private final int idKaryawan;
    private final String tipeCuti;
    private final LocalDate tanggalMulai;
    private final LocalDate tanggalSelesai;
    private final String statusRequest;
    private String statusCuti;
    private String idCuti;
    private String jenisCuti;

    public LeaveRequest(String idRequest, int idKaryawan, String tipeCuti,
                       LocalDate tanggalMulai, LocalDate tanggalSelesai, 
                       String statusRequest, String idCuti, String jenisCuti, String statusCuti) {
        this.idRequest = idRequest;
        this.idKaryawan = idKaryawan;
        this.tipeCuti = tipeCuti;
        this.tanggalMulai = tanggalMulai;
        this.tanggalSelesai = tanggalSelesai;
        this.statusRequest = statusRequest;
        this.idCuti = idCuti;
        this.jenisCuti = jenisCuti;
        this.statusCuti = statusCuti;
    }

    public String getIdRequest() { return idRequest; }
    public int getIdKaryawan() { return idKaryawan; }
    public String getTipeCuti() { return tipeCuti; }
    public LocalDate getTanggalMulai() { return tanggalMulai; }
    public LocalDate getTanggalSelesai() { return tanggalSelesai; }
    public String getStatusRequest() { return statusRequest; }
    public String getIdCuti() { return idCuti; }
    public String getJenisCuti() { return jenisCuti; }
    public String getStatusCuti() { return statusCuti; }

    @Override
    public String toString() {
        return String.format("LeaveRequest[id=%s, karyawan=%d, tipe=%s, status=%s]", 
                            idRequest, idKaryawan, tipeCuti, statusRequest);
    }
}
