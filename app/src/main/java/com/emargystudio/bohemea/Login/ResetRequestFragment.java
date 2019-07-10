package com.emargystudio.bohemea.Login;


import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.Model.PasswordRequest;

import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;


import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class ResetRequestFragment extends Fragment {

    //widget
    EditText emailEdt;
    Button resetRequestBtn;
    ProgressBar progressBar;


    java.text.SimpleDateFormat sdf ;
    SharedPreferenceManger sharedPreferenceManger;




    public ResetRequestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // make a instance  of SharedPreferenceManger to use it to save and get email and date value
        sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());

        //make a instance  of SimpleDateFormat to work with dates
        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());

        /* check if there is already a code saved within 24 hour in sharedPreference
        and if it's true switch fragment to updatePassword Fragment */
        if (!sharedPreferenceManger.getRequestDate().getDate().isEmpty()){
           int differ = checkDateDifference(sharedPreferenceManger.getRequestDate().getDate());
            if (differ>=0 && differ<24){
               toTheNextFragment();
            }else {
                //delete Password Request from sharedPreference if the diff is more than 24 hours
                sharedPreferenceManger.deletePasswordRequestData();
            }
        }


    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_request, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        emailEdt = view.findViewById(R.id.email);
        resetRequestBtn = view.findViewById(R.id.reset_request);
        progressBar = view.findViewById(R.id.progressBar);

        // call sendRequest method if emailEdt is not null
        resetRequestBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!emailEdt.getText().toString().isEmpty()){
                    //get email value from the user input
                    String email = emailEdt.getText().toString();
                    //get the current date to save it into the dataBase
                    Date date = Calendar.getInstance().getTime();
                    String currentTime = sdf.format(date);

                    sendResetRequest(email,currentTime);
                }else {
                    emailEdt.setError(getString(R.string.f_reset_emailEdt_error_msg));
                    emailEdt.requestFocus();
                }
            }
        });


    }

    //send reset request to the php API and store result in sharedPreference
    //and if the response was  successful go to updatePasswordFragment
    public void  sendResetRequest(final String email , final String date){
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.reset_password_request,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        progressBar.setVisibility(View.GONE);

                        if (response.contains("datebase")){


                            Toast.makeText(getContext(), getString(R.string.f_reset_request_cheack_email_toast), Toast.LENGTH_SHORT).show();
                            sharedPreferenceManger.passwordRequestDate(new PasswordRequest(email,date));

                            toTheNextFragment();

                        }else if (response.contains("exists")){

                            Toast.makeText(getContext(), getString(R.string.f_reset_request_wrong_email), Toast.LENGTH_SHORT).show();

                        }else {
                            Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);

                        Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                    }
                }
        ){

            @Override
            protected Map<String, String> getParams() {
                Map<String,String> userData = new HashMap<>();
                userData.put("email",email);
                userData.put("date",date);
                return  userData;
            }
        };//end of string Request


        //increase request timeout
        int x=2;// retry count
        stringRequest.setRetryPolicy(new DefaultRetryPolicy(DefaultRetryPolicy.DEFAULT_TIMEOUT_MS * 48,
                x, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);




    }

    private void toTheNextFragment() {
        UpdatePasswordFragment updatePasswordFragment = new UpdatePasswordFragment();
        if (getActivity()!=null) {
            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, updatePasswordFragment, getString(R.string.update_password_fragment));
            ft.commit();
        }
    }


    //check the difference between the current date and database date in hours
    public int checkDateDifference(String date){

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


}
