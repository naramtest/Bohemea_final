package com.emargystudio.bohemea.model;

public class PasswordRequest {

    private String email;
    private String date;

    public PasswordRequest(String email, String date) {
        this.email = email;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
