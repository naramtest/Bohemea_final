package com.emargystudio.bohemea.Model;

public class User {
    private int userId;
    private String userName;
    private String userEmail;
    private String userPhoto;
    private int userPhoneNumber;
    private int is_facebook; //1=register via facebook account ;2 = register via email
    private int is_blocked;  //1= blocked ; 0=active


    public User(int userId, String userName, String userEmail, String userPhoto, int userPhoneNumber, int is_facebook, int is_blocked) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.userPhoto = userPhoto;
        this.userPhoneNumber = userPhoneNumber;
        this.is_facebook = is_facebook;
        this.is_blocked = is_blocked;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public int getUserPhoneNumber() {
        return userPhoneNumber;
    }

    public void setUserPhoneNumber(int userPhoneNumber) {
        this.userPhoneNumber = userPhoneNumber;
    }

    public int getIs_facebook() {
        return is_facebook;
    }

    public void setIs_facebook(int is_facebook) {
        this.is_facebook = is_facebook;
    }

    public int getIs_blocked() {
        return is_blocked;
    }

    public void setIs_blocked(int is_blocked) {
        this.is_blocked = is_blocked;
    }
}
