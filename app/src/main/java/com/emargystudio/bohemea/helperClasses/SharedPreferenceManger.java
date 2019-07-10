package com.emargystudio.bohemea.helperClasses;

import android.content.Context;

import android.content.SharedPreferences;

import com.emargystudio.bohemea.Model.PasswordRequest;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.Model.UserTokens;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class SharedPreferenceManger {

    private static final String FILENAME = "BOHEMEAARTCAFE";
    private static final String PASSWORD_DATA = "PASSWORDDATA";
    private static final String USER_TOKENS = "USERTOKENS";
    private static final String DEVICE_TOKENS = "DEVICETOKENS";

    private static final String USERNAME = "username";
    private static final String EMAIL = "email";
    private static final String PASSWORD_EMAIL = "password_email";
    private static final String IMAGE = "image";
    private static final String ID = "id";
    private static final String IS_FACEBOOK = "is_facebook";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String PASSWORD_DATE = "password_date";


    private static final String TOKENS = "tokens";
    private static final String DEVICE_TOKEN ="device_token";


    private static SharedPreferenceManger mSharedPreferenceManger;
    private static Context mContext;

    public SharedPreferenceManger(Context context) {
        this.mContext = context;
    }

    public static synchronized SharedPreferenceManger getInstance(Context context){

        if(mSharedPreferenceManger == null){
            mSharedPreferenceManger = new SharedPreferenceManger(context);
        }
        return mSharedPreferenceManger;
    }

    public void storeUserData(User user){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERNAME,user.getUserName());
        editor.putString(EMAIL,user.getUserEmail());
        editor.putString(IMAGE,user.getUserPhoto());
        editor.putInt(ID,user.getUserId());
        editor.putInt(PHONE_NUMBER,user.getUserPhoneNumber());
        editor.putInt(IS_FACEBOOK,user.getIs_facebook());
        editor.apply();

    }

    public void storeUserTokens(int userId , ArrayList<String> tokens){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER_TOKENS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            sb.append(tokens.get(i)).append(",");
        }
        editor.putString(TOKENS,sb.toString());
        editor.putInt(ID,userId);
        editor.apply();
    }

    public UserTokens getUserTokens(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER_TOKENS, Context.MODE_PRIVATE);
        String tokenString= sharedPreferences.getString(TOKENS,null);
        ArrayList<String> tokens = new ArrayList<>();
        if (tokenString != null){
            String[] tokenStringList = tokenString.split(",");
            tokens.addAll(Arrays.asList(tokenStringList));
        }
        return new UserTokens(sharedPreferences.getInt(ID,-1),tokens);

    }

    public void storeDeviceToken(String token){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(DEVICE_TOKENS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DEVICE_TOKEN,token);
        editor.apply();
    }

    public String getDeviceToken(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(DEVICE_TOKENS, Context.MODE_PRIVATE);
        return sharedPreferences.getString(DEVICE_TOKEN,null);
    }


    public User getUserData(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        return new User(sharedPreferences.getInt(ID,-1),sharedPreferences.getString(USERNAME,null),
                sharedPreferences.getString(EMAIL,null),sharedPreferences.getString(IMAGE,null),
                sharedPreferences.getInt(PHONE_NUMBER,0),sharedPreferences.getInt(IS_FACEBOOK,3));
    }

    public void logUserOut(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

    public void logUserDeleteTokens(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(USER_TOKENS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

    }

    public boolean isUserLogggedIn(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);

        if(sharedPreferences.getString(USERNAME,null) != null){
            return true;
        }

        return false;
    }

    public void updatPhoneNumber(int phone_number){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(FILENAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(PHONE_NUMBER,phone_number);
        editor.apply();


    }

    public void passwordRequestDate(PasswordRequest passwordRequest){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PASSWORD_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PASSWORD_DATE,passwordRequest.getDate());
        editor.putString(PASSWORD_EMAIL,passwordRequest.getEmail());
        editor.apply();
    }

    public PasswordRequest getRequestDate(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PASSWORD_DATA, Context.MODE_PRIVATE);
        return new PasswordRequest(sharedPreferences.getString(PASSWORD_EMAIL,""),
                sharedPreferences.getString(PASSWORD_DATE,""));
    }

    public void deletePasswordRequestData(){
        SharedPreferences sharedPreferences = mContext.getSharedPreferences(PASSWORD_DATA, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }



}
