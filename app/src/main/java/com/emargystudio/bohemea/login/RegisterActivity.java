package com.emargystudio.bohemea.login;

import android.content.Intent;

import com.facebook.accountkit.Account;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitCallback;
import com.facebook.accountkit.AccountKitError;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.PhoneNumber;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.google.android.material.textfield.TextInputLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {



    TextInputLayout usernameContainer ,emailContainer , passwordContainer ;
    EditText usernameEdt , emailEdt , passwordEdt ;
    Button signUpBtn;
    ProgressBar progressBar;


    //var
    private static final String KEY_EMPTY = "";
    private static final int APP_REQUEST_CODE = 99;


    String username ;
    String email;
    String password;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        usernameContainer = findViewById(R.id.username_container);
        emailContainer = findViewById(R.id.email_container);
        passwordContainer = findViewById(R.id.password_container);


        usernameEdt = findViewById(R.id.username);
        emailEdt = findViewById(R.id.email);
        passwordEdt = findViewById(R.id.password);
        signUpBtn = findViewById(R.id.sign_up);
        progressBar = findViewById(R.id.progressBar2);

        Typeface face = Typeface.createFromAsset(RegisterActivity.this.getAssets(),"fonts/Cairo-Regular.ttf");
        Typeface face_book = Typeface.createFromAsset(RegisterActivity.this.getAssets(),"fonts/Cairo-Bold.ttf");

        usernameContainer.setTypeface(face);
        usernameEdt.setTypeface(face);
        emailEdt.setTypeface(face);
        passwordEdt.setTypeface(face);
        signUpBtn.setTypeface(face_book);
        emailContainer.setTypeface(face);
        passwordContainer.setTypeface(face);


        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    username = usernameEdt.getText().toString();
                    email = emailEdt.getText().toString();
                    password = passwordEdt.getText().toString();


                    if (validateInputs(username,email,password)){
                        foodQuery(email);
                    }
            }
        });


        usernameContainer.setHint(getString(R.string.a_register_username));
        usernameEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                usernameContainer.setHint(getString(R.string.a_register_username));
            }
        });
    }

    private void registerUser(final String username, final String email, final String password, final String phone) {

        progressBar.setVisibility(View.VISIBLE);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.register_self_user,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            progressBar.setVisibility(View.GONE);
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (!jsonObject.getBoolean("error")) {
                                    JSONObject jsonObjectUser = jsonObject.getJSONObject("user");
                                    User user = new User(jsonObjectUser.getInt("id"),
                                            jsonObjectUser.getString("user_name"),
                                            jsonObjectUser.getString("user_email"),
                                            jsonObjectUser.getString("user_photo"),
                                            jsonObjectUser.getInt("user_phone_number"),
                                            jsonObjectUser.getInt("is_facebook"),
                                            jsonObjectUser.getInt("is_blocked"));
                                    //store user data inside sharedPreferences
                                    SharedPreferenceManger.getInstance(getApplicationContext()).storeUserData(user);
                                    Toast.makeText(RegisterActivity.this, getString(R.string.a_register_successfully), Toast.LENGTH_SHORT).show();
                                    mainActivityIntent();
                                } else {

                                    String message = jsonObject.getString("message");
                                    if (message.contains("facebook")) {

                                        Toast.makeText(RegisterActivity.this, getString(R.string.a_register_email_facebook_used_error), Toast.LENGTH_LONG).show();

                                    } else if (message.contains("user already  exists")) {

                                        Toast.makeText(RegisterActivity.this, getString(R.string.a_register_email_used_error), Toast.LENGTH_LONG).show();

                                    } else {

                                        Toast.makeText(RegisterActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                                    }

                                }


                            } catch (JSONException e) {
                                Toast.makeText(RegisterActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(RegisterActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
            ) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> userData = new HashMap<>();
                    userData.put("username", username);
                    userData.put("email", email);
                    userData.put("phone", phone);
                    userData.put("password", password);
                    userData.put("user_photo", URLS.user_default_photo);
                    userData.put("is_facebook", "2");
                    userData.put("is_blocked", "0");
                    return userData;
                }
            };//end of string Request

            VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);




    }

    //this method send user to main activity after logging in
    private void mainActivityIntent(){
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }


    //check edit text validation
    private boolean validateInputs(String username, String email, String password) {
        if (KEY_EMPTY.equals(email)) {
            emailEdt.setError(getString(R.string.phone_number_empty));
            emailEdt.requestFocus();
            return false;


        }
        if (KEY_EMPTY.equals(username)) {
            usernameEdt.setError(getString(R.string.phone_number_empty));
            usernameEdt.requestFocus();
            return false;
        }
        if (KEY_EMPTY.equals(password)) {
            passwordEdt.setError(getString(R.string.phone_number_empty));
            passwordEdt.requestFocus();
            return false;
        }


        if (password.length()<= 5){
            passwordEdt.setError(getString(R.string.a_register_password_short));
            passwordEdt.requestFocus();
            return false;
        }
        if (!isEmailValid(email)){
            emailEdt.setError(getString(R.string.a_register_invalid_email_form));
            emailEdt.requestFocus();
            return false;
        }

        return true;
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void phoneLogin() {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(
                        LoginType.PHONE,
                        AccountKitActivity.ResponseType.TOKEN); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
                AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
                configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }



    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
            } else if (loginResult.wasCancelled()) {
                toastMessage = getString(R.string.error);
            } else {

                toastMessage = getString(R.string.alert_phone_done_btn);

                // If you have an authorization code, retrieve it from
                // loginResult.getAuthorizationCode()
                // and pass it to your server and exchange it for an access token.

                // Success! Start your next activity...
                AccountKit.getCurrentAccount(new AccountKitCallback<Account>() {
                    @Override
                    public void onSuccess(final Account account) {

                        // Get phone number
                        PhoneNumber phoneNumber = account.getPhoneNumber();
                        if (phoneNumber != null) {
                            String phoneNumberString = phoneNumber.toString();
                            registerUser(username, email, password, phoneNumberString);

                        }

                        // Get email

                    }

                    @Override
                    public void onError(final AccountKitError error) {
                        // Handle Error
                    }
                });
            }

            // Surface the result to your user in an appropriate way.
            Toast.makeText(
                    this,
                    toastMessage,
                    Toast.LENGTH_LONG)
                    .show();
        }
    }


    private void foodQuery(final String email){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.checkEmail,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                JSONArray jsonObjectCategory =  jsonObject.getJSONArray("emails");
                                boolean isExists = false;
                                for (int i =0; i<jsonObjectCategory.length();i++){
                                    JSONObject jsonObject1 = jsonObjectCategory.getJSONObject(i);
                                    if (jsonObject1.getString("user_email").equals(email)){
                                        isExists = true;
                                        if (jsonObject1.getInt("is_facebook")==1){

                                            Toast.makeText(RegisterActivity.this, getString(R.string.a_register_email_facebook_used_error), Toast.LENGTH_LONG).show();

                                        }else {
                                            Toast.makeText(RegisterActivity.this, getString(R.string.a_register_email_used_error), Toast.LENGTH_LONG).show();

                                        }
                                    }
                                }

                                if (!isExists){
                                    phoneLogin();
                                }


                            }else{
                                Toast.makeText(RegisterActivity.this,getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(RegisterActivity.this,getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                    }
                }
        );
        VolleyHandler.getInstance(RegisterActivity.this).addRequetToQueue(stringRequest);

    }
}
