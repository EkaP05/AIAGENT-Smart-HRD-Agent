package com.hragent.data;

import com.hragent.domain.*;
import java.util.*;


 // DataStore: In-memory storage untuk semua data HR

public class DataStore {
    private final Map<Integer, Employee> employees;
    private final Map<Integer, List<LeaveBalance>> leaveBalances;
    private final Map<Integer, List<LeaveRequest>> leaveRequests;
    private final Map<Integer, List<PerformanceReview>> performanceReviews;
    
    // Index untuk pencarian cepat by nama
    private final Map<String, Integer> nameToIdIndex;

    public DataStore() {
        this.employees = new HashMap<>();
        this.leaveBalances = new HashMap<>();
        this.leaveRequests = new HashMap<>();
        this.performanceReviews = new HashMap<>();
        this.nameToIdIndex = new HashMap<>();
    }

    // Add methods
    public void addEmployee(Employee emp) {
        employees.put(emp.getId(), emp);
        // Index nama (lowercase untuk case-insensitive search)
        nameToIdIndex.put(emp.getNama().toLowerCase(), emp.getId());
    }

    public void addLeaveBalance(LeaveBalance lb) {
        leaveBalances.computeIfAbsent(lb.getIdKaryawan(), k -> new ArrayList<>()).add(lb);
    }

    public void addLeaveRequest(LeaveRequest lr) {
        leaveRequests.computeIfAbsent(lr.getIdKaryawan(), k -> new ArrayList<>()).add(lr);
    }

    public void addPerformanceReview(PerformanceReview pr) {
        performanceReviews.computeIfAbsent(pr.getIdKaryawan(), k -> new ArrayList<>()).add(pr);
    }

    // Query methods
    public Employee getEmployeeById(int id) {
        return employees.get(id);
    }

    public Employee getEmployeeByName(String nama) {
        Integer id = nameToIdIndex.get(nama.toLowerCase().trim());
        return id != null ? employees.get(id) : null;
    }

    public List<LeaveBalance> getLeaveBalances(int idKaryawan) {
        return leaveBalances.getOrDefault(idKaryawan, Collections.emptyList());
    }

    public LeaveBalance getLeaveBalance(int idKaryawan, String tipeCuti) {
        return leaveBalances.getOrDefault(idKaryawan, Collections.emptyList())
                .stream()
                .filter(lb -> lb.getTipeCuti().equalsIgnoreCase(tipeCuti))
                .findFirst()
                .orElse(null);
    }

    public List<LeaveRequest> getLeaveRequests(int idKaryawan) {
        return leaveRequests.getOrDefault(idKaryawan, Collections.emptyList());
    }

    public LeaveRequest getLatestLeaveRequest(int idKaryawan) {
        List<LeaveRequest> requests = leaveRequests.get(idKaryawan);
        if (requests == null || requests.isEmpty()) return null;
        return requests.get(requests.size() - 1); // Ambil yang terakhir
    }

    public List<PerformanceReview> getPerformanceReviews(int idKaryawan) {
        return performanceReviews.getOrDefault(idKaryawan, Collections.emptyList());
    }

    public Employee getManagerOf(Employee employee) {
        if (employee.getIdManajer() == null) return null;
        return employees.get(employee.getIdManajer());
    }

    public int getTotalEmployees() {
        return employees.size();
    }

    public Collection<Employee> getAllEmployees() {
        return employees.values();
    }
}
