package com.emargystudio.bohemea.Model;

import java.util.ArrayList;

public class UserTokens {

    private int userID;
    private ArrayList<String> tokens;

    public UserTokens(int userID, ArrayList<String> tokens) {
        this.userID = userID;
        this.tokens = tokens;
    }


    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public ArrayList<String> getTokens() {
        return tokens;
    }

    public void setTokens(ArrayList<String> tokens) {
        this.tokens = tokens;
    }
}
