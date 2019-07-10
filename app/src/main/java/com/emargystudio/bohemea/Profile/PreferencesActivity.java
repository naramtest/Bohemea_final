package com.emargystudio.bohemea.Profile;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.Login.LoginActivity;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class PreferencesActivity extends AppCompatActivity {

    private static final String TAG = "PreferencesActivity";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PASSWORD = "new_password";

    ImageView user_image ;
    TextView user_name;
    TextView header1, header2;
    TextInputEditText editPhone , editPassword , editEmail;
    LinearLayout phoneLayout, passwordLayout , emailLayout;
    ImageView phoneBtn , emailBtn , passwordBtn;
    ProgressBar progressBar;

    //dialog views
    ImageView  upload_image ;
    FrameLayout rotateBtn ;
    Button upload_btn ,cancel_btn;


    SharedPreferenceManger sharedPreferenceManger;
    User user;

    boolean OkToUpload;
    final int GALLARY_PICK = 2;
    Bitmap bitmap;
    String imageToString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);

        sharedPreferenceManger = SharedPreferenceManger.getInstance(PreferencesActivity.this);
        user = sharedPreferenceManger.getUserData();

        OkToUpload = false;


        setupLayout();

        phoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = editPhone.getText().toString();
                if (!phone.isEmpty() || !phone.equals("")){

                    updatePhone(user.getUserId(),phone);
                }
            }
        });

        emailBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString();
                if(!email.isEmpty() || !email.equals("")){
                    updateEmail(user.getUserId(),user.getUserEmail(),email);
                }
            }
        });

        passwordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = editPassword.getText().toString();
                if (!password.isEmpty() || !password.equals("")){

                    updatePasswordDialog(password,user.getUserEmail());
               }

            }
        });

        user_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select Image"), GALLARY_PICK);
            }
        });



    }

    private void updatePhone(int userId, String phone) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.update_phone+userId+"&user_phone_number="+phone,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                int user_phone = jsonObject.getInt("user_phone_number");
                                user.setUserPhoneNumber(user_phone);
                                sharedPreferenceManger.logUserOut();
                                sharedPreferenceManger.storeUserData(user);
                                editPhone.setText(getString(R.string.update_phone_done));


                            }else{

                                    Toast.makeText(PreferencesActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                            }
                        }catch (JSONException e){

                                Toast.makeText(PreferencesActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        Toast.makeText(PreferencesActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        VolleyHandler.getInstance(PreferencesActivity.this).addRequetToQueue(stringRequest);

    }

    public void updateEmail(final int userId, final String userEmail , final String newUserEmail){

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.update_email,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")){

                                String user_email = jsonObject.getString("user_email");
                                user.setUserEmail(user_email);
                                sharedPreferenceManger.logUserOut();
                                sharedPreferenceManger.storeUserData(user);
                                editEmail.setText(R.string.update_phone_done);

                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(PreferencesActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }
        ){

            @Override
            protected Map<String, String> getParams() {
                Map<String,String> userData = new HashMap<>();
                userData.put("id",String.valueOf(userId));
                userData.put("user_email",userEmail);
                userData.put("user_new_email",newUserEmail);
                return  userData;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);




    }

    public void updatePasswordDialog(final String password , final String userEmail ){
        final AlertDialog.Builder alert = new AlertDialog.Builder(PreferencesActivity.this);
        alert.setTitle(R.string.alert_confirm_password_title);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View alertLayout = li.inflate(R.layout.alert_confirm_password, null);
        final EditText confirmPasswordEdt = alertLayout.findViewById(R.id.confirm_password_edt);
        TextView doneBtn = alertLayout.findViewById(R.id.done);

        final TextInputLayout layout = alertLayout.findViewById(R.id.confirm_password_layout);

        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String password_confirm = confirmPasswordEdt.getText().toString();
               if (password_confirm.equals(password)){

                   updatePassword(userEmail,password);
               }
            }
        });




    }

    //sand request to update user password
    private void updatePassword(String email, String password){


        final JSONObject request = new JSONObject();
        try {
            //Populate the request parameters
            request.put(KEY_EMAIL, email);
            request.put(KEY_PASSWORD, password);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, URLS.prefrence_update_password, request, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {


                        try {
                            //error = 0 : successfully updated
                            //error = 1 : code and password is correct but something want wrong on the server side
                            //error = 2 : wrong code or password
                            //error = 3 : missing some request var
                            if (response.getInt("error")==0){

                                Toast.makeText(PreferencesActivity.this, getString(R.string.f_update_successful), Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PreferencesActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                            }else if (response.getInt("error")==1){

                                Toast.makeText(PreferencesActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }else {
                                Toast.makeText(PreferencesActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }
                        } catch (JSONException e) {
                            Toast.makeText(PreferencesActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        //Display error message whenever an error occurs
                        Toast.makeText(PreferencesActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();


                    }
                });



        // Access the RequestQueue through your VolleyHandler class.
        VolleyHandler.getInstance(PreferencesActivity.this).addRequetToQueue(jsArrayRequest);

    }

    private void setupLayout() {
        user_image = findViewById(R.id.circleImageView);
        user_name  = findViewById(R.id.user_name);
        progressBar = findViewById(R.id.progressBar3);
        header1    = findViewById(R.id.text_header1);
        header2    = findViewById(R.id.text_header2);
        editEmail   = findViewById(R.id.edt_email);
        editPassword = findViewById(R.id.edt_password);
        editPhone    = findViewById(R.id.edt_phone);
        phoneLayout = findViewById(R.id.phone_container);
        passwordLayout = findViewById(R.id.password_container);
        emailLayout    = findViewById(R.id.email_container);
        phoneBtn =    findViewById(R.id.phoneBtn);
        emailBtn = findViewById(R.id.emailBtn);
        passwordBtn = findViewById(R.id.passwordBtn);


        Typeface face_Bold = Typeface.createFromAsset(PreferencesActivity.this.getAssets(),"fonts/Kabrio-Bold.ttf");
        Typeface face_Light = Typeface.createFromAsset(PreferencesActivity.this.getAssets(),"fonts/Kabrio-Light.ttf");
        Typeface akrobat_Regular = Typeface.createFromAsset(PreferencesActivity.this.getAssets(),"fonts/Akrobat-Regular.otf");

        header1.setTypeface(face_Bold);
        user_name.setTypeface(face_Bold);
        header2.setTypeface(face_Light);
        editEmail.setTypeface(akrobat_Regular);
        editPassword.setTypeface(akrobat_Regular);
        editPhone.setTypeface(akrobat_Regular);


        Picasso.get().load(user.getUserPhoto()).into(user_image);
        user_name.setText(user.getUserName());


        if(user.getIs_facebook()== 1){
            passwordLayout.setVisibility(View.GONE);
            emailLayout.setVisibility(View.GONE);
        }
    }


    private void uploadStory(){


        progressBar.setVisibility(View.VISIBLE);
        if(!OkToUpload){
            Toast.makeText(PreferencesActivity.this,"There is no image to upload!",Toast.LENGTH_LONG).show();

            return;
        }

        final int user_id = user.getUserId();
        imageToString = convertImageToString();
        final String image_name = String.valueOf(dateOfImage());

        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.change_profile_pics,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.GONE);

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                String image_url  = jsonObject.getString("image");
                                Toast.makeText(PreferencesActivity.this,"Image uploaded successfully!",Toast.LENGTH_LONG).show();
                                Log.d(TAG, "onResponse: "+image_url);
                                user.setUserPhoto(image_url);
                                sharedPreferenceManger.logUserOut();
                                //Picasso.get().load(user.getUserPhoto()).into(user_image);
                                sharedPreferenceManger.storeUserData(user);

                            }else{

                                Toast.makeText(PreferencesActivity.this,jsonObject.getString("message"),Toast.LENGTH_LONG).show();
                            }
                            Picasso.get().load(user.getUserPhoto()).into(user_image);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(PreferencesActivity.this,error.getMessage(),Toast.LENGTH_LONG).show();

                    }
                }


        ){

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> imageMap = new HashMap<>();
                imageMap.put("image_encoded",imageToString);
                imageMap.put("user_id",String.valueOf(user_id));
                imageMap.put("image_name",image_name);
                return  imageMap;
            }
        };//end of string Request

        VolleyHandler.getInstance(PreferencesActivity.this).addRequetToQueue(stringRequest);

        OkToUpload = false;


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLARY_PICK){

            if(resultCode == RESULT_OK){


                OkToUpload = true;
                Uri uri =   data.getData();
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
                    if(bitmap != null){
                        uploadImageDialog();
                    }else{
                        Toast.makeText(PreferencesActivity.this,"Couldn't get bitmap of image",Toast.LENGTH_LONG).show();
                    } //String path = getPath(PreferencesActivity.this,uri);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                // captured_iv.setImageBitmap(bitmap);

            }


        }
    }

    public void uploadImageDialog(){
        final Dialog dialog = new Dialog(PreferencesActivity.this);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.setContentView(R.layout.update_image_dialog);


        initDialogViews(dialog);
        changeTxtViewFonts();

        upload_image.setImageBitmap(bitmap);


        rotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bitmap = rotateBitmap(bitmap,90);
                upload_image.setImageBitmap(bitmap);
            }
        });
        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        upload_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                uploadStory();
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void changeTxtViewFonts() {
        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Akrobat-Bold.otf");
        Typeface faceLight = Typeface.createFromAsset(getAssets(),"fonts/Akrobat-Light.otf");
        cancel_btn.setTypeface(faceLight);
        upload_btn.setTypeface(face);
    }

    private void initDialogViews(Dialog dialog) {

        rotateBtn = dialog.findViewById(R.id.rotate);
        upload_image = dialog.findViewById(R.id.update_image);
        upload_btn    = dialog.findViewById(R.id.upload_btn);
        cancel_btn = dialog.findViewById(R.id.cancel_btn);
    }

    private String convertImageToString(){


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,50,baos);
        byte[] imageByteArray = baos.toByteArray();
        String result =  Base64.encodeToString(imageByteArray,Base64.DEFAULT);

        return result;

    }

    public static Bitmap rotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Long dateOfImage(){
        Date date = new Date();


        return date.getTime();

    }

}
