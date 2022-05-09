package pwr.edu.app.parkinsonsdisease.entity;

public class Patient {
    private String doctorId;
    private String email;
    private String role;

    public Patient() {
    }

    public Patient(String doctorId, String email, String role) {
        this.doctorId = doctorId;
        this.email = email;
        this.role = role;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
