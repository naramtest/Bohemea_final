package com.emargystudio.bohemea.login;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;

import com.emargystudio.bohemea.helperClasses.Common;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;



public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_EMPTY = "";

    //facebook var
    CallbackManager callbackManager;

    //widgets
    Button facebookLoginBtn , mySqlLoginBtn;
    EditText emailEdt , passwordEdt;
    TextInputLayout textInputLayoutEmail , passwordContainer;
    TextView forget_password ,registerNewUserTxt;
    ProgressBar progressBar;
    String lang = Locale.getDefault().getLanguage();


    ArrayList<String> tokens;
    SharedPreferenceManger sharedPreferenceManger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //init widgets
        initWidgets();


        tokens = new ArrayList<>();
        sharedPreferenceManger = new SharedPreferenceManger(LoginActivity.this);



        registerNewUserTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,RegisterActivity.class));
            }
        });

        mySqlLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEdt.getText().toString();
                String password = passwordEdt.getText().toString();

                if (validateInputs(email,password)){
                    loginUserViaMysqlRegistration( email, password);
                }
            }
        });

        forget_password.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this,ForgetPasswordActivity.class));
            }
        });


        facebookLoginCallBack();
        layoutStrings();

    }

    private void initWidgets() {
        facebookLoginBtn = findViewById(R.id.facbook);
        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);
        forget_password = findViewById(R.id.forgetPassword);
        textInputLayoutEmail = findViewById(R.id.email_container);
        registerNewUserTxt = findViewById(R.id.register_new_user);
        mySqlLoginBtn = findViewById(R.id.sign_in);
        progressBar = findViewById(R.id.progressBar2);
        passwordContainer = findViewById(R.id.password_container);

        Typeface face = Typeface.createFromAsset(LoginActivity.this.getAssets(),"fonts/Cairo-Regular.ttf");
        Typeface face_book = Typeface.createFromAsset(LoginActivity.this.getAssets(),"fonts/Cairo-Bold.ttf");

        emailEdt.setTypeface(face);
        passwordEdt.setTypeface(face);
        forget_password.setTypeface(face_book);
        textInputLayoutEmail.setTypeface(face);
        registerNewUserTxt.setTypeface(face);
        mySqlLoginBtn.setTypeface(face_book);
        passwordContainer.setTypeface(face);
        facebookLoginBtn.setTypeface(face_book);
    }

    /*when the user is first time logged in via facebook account this method get called and make sure to add
     all of his data to the app database*/
    public void registerUserViaFacebook(final String name, final String email , final String profile_image , final String phone_number){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.register_api,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")){
                                JSONObject jsonObjectUser =  jsonObject.getJSONObject("user");

                                User user = new User(jsonObjectUser.getInt("id"),jsonObjectUser.getString("user_name"),
                                        jsonObjectUser.getString("user_email")
                                        ,jsonObjectUser.getString("user_photo"),jsonObjectUser.getInt("user_phone_number"),
                                        jsonObjectUser.getInt("is_facebook"),jsonObjectUser.getInt("is_blocked"));

                                //store user data inside sharedPreferences
                                sharedPreferenceManger.storeUserData(user);
                                sharedPreferenceManger.storeUserStatus(jsonObjectUser.getInt("is_blocked"));
                                mainActivityIntent();
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }
        ){

            @Override
            protected Map<String, String> getParams() {
                Map<String,String> userData = new HashMap<>();
                userData.put("user_name",name);
                userData.put("user_email",email);
                userData.put("user_photo",profile_image);
                userData.put("user_phone_number",phone_number);
                userData.put("is_facebook","1");
                userData.put("is_blocked","0");
                return  userData;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);




    }

    /*this method get called when the user info is already register in the database to add his info to
     sharedPreference  */
    public void loginUserViaFacebook(final String name, final String email , final String profile_image ){
        tokens.clear();
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.login_user+email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {
                                JSONObject jsonObjectUser = jsonObject.getJSONObject("user");

                                if (jsonObjectUser.isNull("id")){
                                    addYourPhoneNumberDialog(name,email,profile_image);
                                }else {

                                    User user = new User(jsonObjectUser.getInt("id"),
                                            jsonObjectUser.getString("user_name"),
                                            jsonObjectUser.getString("user_email"),
                                            jsonObjectUser.getString("user_photo"),
                                            jsonObjectUser.getInt("user_phone_number"),
                                            jsonObjectUser.getInt("is_facebook"),
                                            jsonObjectUser.getInt("is_blocked"));

                                    //store user data inside sharedPreferences
                                    JSONArray tokensJsonArray = jsonObject.getJSONArray("tokens");
                                    for (int i=0 ; i<tokensJsonArray.length();i++){
                                        JSONObject tokenJSONObject = tokensJsonArray.getJSONObject(i);
                                        String token = tokenJSONObject.getString("token");
                                        tokens.add(token);
                                    }
                                    if (tokens.size()>0){

                                        sharedPreferenceManger.storeUserTokens(user.getUserId(),tokens);

                                    }
                                    sharedPreferenceManger.storeUserData(user);
                                    sharedPreferenceManger.storeUserStatus(jsonObjectUser.getInt("is_blocked"));
                                    if (sharedPreferenceManger.getDeviceToken()!=null){
                                        if (!Common.isNewToken){
                                            if (!tokens.contains(sharedPreferenceManger.getDeviceToken())){
                                                registerToken(user.getUserId(),sharedPreferenceManger.getDeviceToken());
                                            }
                                        }
                                    }
                                    mainActivityIntent();

                                }
                            }


                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this,getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(LoginActivity.this,getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                    }
                }

        );



        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);
    }

    public void loginUserViaMysqlRegistration(String email , String password){
        tokens.clear();
        progressBar.setVisibility(View.VISIBLE);
        JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_EMAIL, email);
            request.put(KEY_PASSWORD, password);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, URLS.login_self_user, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {



                        progressBar.setVisibility(View.GONE);

                        try {
                            if (response.getInt("error")==0) {
                                JSONObject jsonObjectUser = response.getJSONObject("user");


                                    User user = new User(jsonObjectUser.getInt("id"),
                                            jsonObjectUser.getString("user_name"),
                                            jsonObjectUser.getString("user_email"),
                                            jsonObjectUser.getString("user_photo"),
                                            jsonObjectUser.getInt("user_phone_number"),
                                            jsonObjectUser.getInt("is_facebook"),
                                            jsonObjectUser.getInt("is_blocked"));

                                    JSONArray tokensJsonArray = response.getJSONArray("tokens");
                                    for (int i=0 ; i<tokensJsonArray.length();i++){
                                        JSONObject tokenJSONObject = tokensJsonArray.getJSONObject(i);
                                        String token = tokenJSONObject.getString("token");
                                        tokens.add(token);
                                    }
                                    if (tokens.size()>0){
                                        sharedPreferenceManger.storeUserTokens(user.getUserId(),tokens);
                                    }

                                    Toast.makeText(LoginActivity.this, getString(R.string.a_login_wc_back)+" "+user.getUserName(), Toast.LENGTH_SHORT).show();
                                    //store user data inside sharedPreferences
                                    sharedPreferenceManger.storeUserData(user);
                                    sharedPreferenceManger.storeUserStatus(jsonObjectUser.getInt("is_blocked"));
                                    if (sharedPreferenceManger.getDeviceToken()!=null){
                                        if (!Common.isNewToken){
                                            if (!tokens.contains(sharedPreferenceManger.getDeviceToken())){
                                                registerToken(user.getUserId(),sharedPreferenceManger.getDeviceToken());
                                            }
                                        }
                                    }
                                    mainActivityIntent();


                            }else if (response.getInt("error")==1){
                                Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                            }else if (response.getInt("error")==2){
                                Toast.makeText(LoginActivity.this, getString(R.string.a_login_invaled_password), Toast.LENGTH_SHORT).show();
                            }else{
                                Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        //Display error message whenever an error occurs
                        Toast.makeText(LoginActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();


                    }
                });

        // Access the RequestQueue through your VolleyHandler class.
        VolleyHandler.getInstance(this).addRequetToQueue(jsArrayRequest);

    }

    public void registerToken(final int userId , final String token){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.register_token,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")){


                                String token = jsonObject.getString("fcmToken");
                                tokens.add(token);
                                sharedPreferenceManger.storeUserTokens(userId,tokens);

                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }
        ){

            @Override
            protected Map<String, String> getParams() {
                Map<String,String> userData = new HashMap<>();
                userData.put("user_id",String.valueOf(userId));
                userData.put("token",token);

                return  userData;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


    }

    //part of facebook sdk
    private void facebookLoginCallBack() {
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {


                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {


                        getUserFacebookData(object);
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields","id,name,email,picture.type(large)");
                request.setParameters(parameters);
                request.executeAsync();

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {

            }
        });
        facebookLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList("public_profile","email"));
            }
        });
    }

    //get user name,email,profilePics from facebook
    private void getUserFacebookData(JSONObject object) {

        try {
            final String mImageUrl = ("https://graph.facebook.com/" + object.getString("id") + "/picture?width=500&height=500");
            String name = object.getString("name");
            String email = object.getString("email");

            loginUserViaFacebook(name,email,mImageUrl);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /*when the user logged in via facebook for the first time this dialog is showing and ask
     him to add his phone number and add this to the app database and prevent him from continue to the app before
     completing this step
     */
    public void addYourPhoneNumberDialog(final String name, final String email , final String profile_image ){
        final AlertDialog.Builder alert = new AlertDialog.Builder(LoginActivity.this);
        alert.setTitle(R.string.alert_phone_title);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View alertLayout = li.inflate(R.layout.alert_phone, null);
        final EditText edtPhone = alertLayout.findViewById(R.id.edtPhone);
        TextView loginBtn = alertLayout.findViewById(R.id.login);

        final TextInputLayout layout = alertLayout.findViewById(R.id.edtPhoneLayout);

        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone_number = edtPhone.getText().toString();
                if (!phone_number.isEmpty()){
                    registerUserViaFacebook(name,email,profile_image,phone_number);
                    dialog.dismiss();
                }else {
                    layout.setError(getString(R.string.phone_number_empty));
                }
            }
        });




    }

    //this method send user to main activity after logging in
    private void mainActivityIntent(){
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    //sit dynamic hint for email and split forget password in to strings
    //with different colors
    private void layoutStrings() {
        textInputLayoutEmail.setHint(getString(R.string.a_login_username_email));

        emailEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                textInputLayoutEmail.setHint(getString(R.string.a_login_email_hint));
            }
        });

        String firstPart = getString(R.string.a_login_forgot_password);
        String secondPart = getString(R.string.a_login_press_here);
        String fullString = firstPart+" "+secondPart;
        Spannable spannable = new SpannableString(fullString);
        spannable.setSpan(new ForegroundColorSpan(Color.parseColor("#ffbb00")), firstPart.length(), fullString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        forget_password.setText(spannable, TextView.BufferType.SPANNABLE);
    }

    private boolean validateInputs(String email , String password) {
        if(KEY_EMPTY.equals(email)){
            emailEdt.setError(getString(R.string.phone_number_empty));
            emailEdt.requestFocus();
            return false;
        }
        if(KEY_EMPTY.equals(password)){
            passwordEdt.setError(getString(R.string.phone_number_empty));
            passwordEdt.requestFocus();
            return false;
        }
        return true;
    }

   //get result for facebook login system
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
