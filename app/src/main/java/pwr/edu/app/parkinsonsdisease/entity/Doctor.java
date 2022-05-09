package pwr.edu.app.parkinsonsdisease.entity;

public class Doctor {
    private String doctorEmail;
    private String role;

    public Doctor() {
    }

    public Doctor(String doctorEmail, String role) {
        this.doctorEmail = doctorEmail;
        this.role = role;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
