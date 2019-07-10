package com.emargystudio.bohemea.Login;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class UpdatePasswordFragment extends Fragment {


    //widget
    TextInputLayout newPasswordLayout;
    EditText newPasswordEdt , codeEdt;
    Button updatePasswordBtn;
    ProgressBar progressBar;
    TextView resendCodeTxt;

    //sharedPreference and dataBase constant
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "new_password";
    private static final String KEY_CODE = "code";
    private static final String KEY_DATE = "date";
    private static final String KEY_EMPTY = "";


    //objects instance
    SharedPreferenceManger sharedPreferenceManger;
    java.text.SimpleDateFormat sdf ;


    public UpdatePasswordFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());
        int diff = calculateDiff(sharedPreferenceManger.getRequestDate().getDate());
        if (diff<0 || diff > 24){
            deleteRequest();
            Toast.makeText(getContext(), getString(R.string.f_update_code_old), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        return  inflater.inflate(R.layout.fragment_update_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //init widget + simpleDateFormat + sharedPreferenceManger
        initViewsAndObjects(view);

        //change the hint of newPasswordEdt int realTime
        newPasswordEdt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                newPasswordLayout.setHint(getString(R.string.f_update_new_pass__small_hint));
            }
        });


        /*when user pressed on th update password button check to see if date and email saved in sp
         and validate inputs for code and password
         and then check if the diff between the saved date and current date is less than 24 hour*/
        updatePasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferenceManger.getRequestDate()!=null){
                    String date = sharedPreferenceManger.getRequestDate().getDate();
                    String email = sharedPreferenceManger.getRequestDate().getEmail();
                    String password = newPasswordEdt.getText().toString().trim();
                    String code = codeEdt.getText().toString().trim();
                    if (validateInputs(code,password)) {
                        if (calculateDiff(date) >= 0 && calculateDiff(date) < 24) {
                            updatePassword(email, password, code, date);
                        }
                    }
                }

            }
        });

        //go back to ResetRequestFragment and delete data from sp
        resendCodeTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteRequest();
            }
        });

    }

    private void deleteRequest() {
        sharedPreferenceManger.deletePasswordRequestData();
        if (getActivity()!=null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, new ResetRequestFragment(), getString(R.string.update_password_fragment));
            ft.commit();
        }
    }

    //init widget + simpleDateFormat + sharedPreferenceManger
    private void initViewsAndObjects(@NonNull View view) {
        newPasswordLayout = view.findViewById(R.id.new_password_container);
        newPasswordEdt = view.findViewById(R.id.new_password);
        updatePasswordBtn = view.findViewById(R.id.update_password);
        progressBar = view.findViewById(R.id.progressBar2);
        resendCodeTxt =view.findViewById(R.id.resend_code);
        codeEdt = view.findViewById(R.id.code);
        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
    }


    //sand request to update user password
    private void updatePassword(String email, String password, String code, String date){

        progressBar.setVisibility(View.VISIBLE);
        final JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_EMAIL, email);
            request.put(KEY_PASSWORD, password);
            request.put(KEY_CODE, code);
            request.put(KEY_DATE, date);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, URLS.update_password, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        progressBar.setVisibility(View.GONE);
                        try {
                            //error = 0 : successfully updated
                            //error = 1 : code and password is correct but something want wrong on the server side
                            //error = 2 : wrong code or password
                            //error = 3 : missing some request var
                            if (response.getInt("error")==0){

                                Toast.makeText(getContext(), getString(R.string.f_update_successful), Toast.LENGTH_SHORT).show();
                                sharedPreferenceManger.deletePasswordRequestData();
                                Intent intent = new Intent(getContext(), LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                                
                            }else if (response.getInt("error")==1){

                                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                                
                            }else if (response.getInt("error")==2){

                                Toast.makeText(getContext(), getString(R.string.f_update_wrong_code), Toast.LENGTH_SHORT).show();

                            }else {
                                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                                
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        //Display error message whenever an error occurs
                        Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();


                    }
                });



        // Access the RequestQueue through your VolleyHandler class.
        VolleyHandler.getInstance(getContext()).addRequetToQueue(jsArrayRequest);

    }

    //calculate and return the diff between to dates current date and the date passed to the method
    private int calculateDiff(String date){
        int diffHours =0;
        try {
            Date date1 = sdf.parse(date);
            Date date2 = Calendar.getInstance().getTime();
            long diff = date1.getTime() - date2.getTime();
            diffHours = (int) (diff / (60 * 60 * 1000));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return diffHours;
    }

    //to change the text of codeEdt if this fragment is opened from email link
    @Override
    public void onResume() {
        super.onResume();
            codeEdt.setText(Common.code);
    }

    //return false if  one of this conditions is false
    private boolean validateInputs(String code , String password) {
        if(KEY_EMPTY.equals(code)){
            codeEdt.setError(getString(R.string.phone_number_empty));
            codeEdt.requestFocus();
            return false;
        }
        if(KEY_EMPTY.equals(password)){
            newPasswordEdt.setError(getString(R.string.phone_number_empty));
            newPasswordEdt.requestFocus();
            return false;
        }

        if (password.length()<5){
            newPasswordEdt.setError(getString(R.string.a_register_password_short));
            newPasswordEdt.requestFocus();
            return false;
        }
        return true;
    }
}
