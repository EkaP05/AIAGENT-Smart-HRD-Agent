package com.hragent.domain;

import java.time.LocalDate;

public class PerformanceReview {
    private final String idReview;
    private final int idKaryawan;
    private final int idReviewer;
    private final LocalDate tanggalReview;
    private final int skorPerforma;
    private final String statusReview;

    public PerformanceReview(String idReview, int idKaryawan, int idReviewer,
                            LocalDate tanggalReview, int skorPerforma, 
                            String statusReview) {
        this.idReview = idReview;
        this.idKaryawan = idKaryawan;
        this.idReviewer = idReviewer;
        this.tanggalReview = tanggalReview;
        this.skorPerforma = skorPerforma;
        this.statusReview = statusReview;
    }

    public String getIdReview() { return idReview; }
    public int getIdKaryawan() { return idKaryawan; }
    public int getIdReviewer() { return idReviewer; }
    public LocalDate getTanggalReview() { return tanggalReview; }
    public int getSkorPerforma() { return skorPerforma; }
    public String getStatusReview() { return statusReview; }

    @Override
    public String toString() {
        return String.format("PerformanceReview[id=%s, karyawan=%d, status=%s]", 
                            idReview, idKaryawan, statusReview);
    }
}
