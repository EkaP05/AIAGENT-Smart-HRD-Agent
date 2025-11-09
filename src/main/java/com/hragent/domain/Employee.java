package com.hragent.domain;

import java.time.LocalDate;

public class Employee {
    private final int id;
    private final String nama;
    private final String email;
    private final String jabatan;
    private final String departemen;
    private final Integer idManajer;
    private final LocalDate tanggalBergabung;
    private final String statusKaryawan;

    public Employee(int id, String nama, String email, String jabatan, 
                    String departemen, Integer idManajer, 
                    LocalDate tanggalBergabung, String statusKaryawan) {
        this.id = id;
        this.nama = nama;
        this.email = email;
        this.jabatan = jabatan;
        this.departemen = departemen;
        this.idManajer = idManajer;
        this.tanggalBergabung = tanggalBergabung;
        this.statusKaryawan = statusKaryawan;
    }

    // Getters
    public int getId() { return id; }
    public String getNama() { return nama; }
    public String getEmail() { return email; }
    public String getJabatan() { return jabatan; }
    public String getDepartemen() { return departemen; }
    public Integer getIdManajer() { return idManajer; }
    public LocalDate getTanggalBergabung() { return tanggalBergabung; }
    public String getStatusKaryawan() { return statusKaryawan; }

    @Override
    public String toString() {
        return String.format("Employee[id=%d, nama=%s, jabatan=%s, departemen=%s]", 
                            id, nama, jabatan, departemen);
    }
}


