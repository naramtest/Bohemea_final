package com.emargystudio.bohemea;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.cart.CartActivity;
import com.emargystudio.bohemea.history.HistoryActivity;
import com.emargystudio.bohemea.localDataBases.AppDatabase;
import com.emargystudio.bohemea.localDataBases.AppExecutors;
import com.emargystudio.bohemea.login.LoginActivity;
import com.emargystudio.bohemea.makeReservation.ReservationActivity;
import com.emargystudio.bohemea.menu.MenuActivity;

import com.emargystudio.bohemea.profile.ProfileActivity;
import com.emargystudio.bohemea.viewHolders.SliderAdapterExample;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.smarteist.autoimageslider.IndicatorAnimations;
import com.smarteist.autoimageslider.SliderAnimations;
import com.smarteist.autoimageslider.SliderView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {




    Button  makeReservationBtn;
    SliderView sliderView;
    SliderAdapterExample sliderAdapterExample;
    TextView address;
    private ArrayList<String> images = new ArrayList<>();

    String lang = Locale.getDefault().getLanguage();
    double versionName;
    int isImportant ; // 1 = true 0 = false



    SpaceNavigationView spaceNavigationView;
    SharedPreferenceManger sharedPreferenceManger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //views init
        viewsInit();
        needUpdate();

        //go to the reservation activity to make a reservation
        makeReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ReservationActivity.class));
            }
        });



        //call the setup method for init bottom navigation view
        bottomNavigationInit(savedInstanceState,MainActivity.this);


        //allow only logged in users to enter app
        //and if its not send him to LoginActivity
        sharedPreferenceManger = SharedPreferenceManger.getInstance(MainActivity.this);
        if (!sharedPreferenceManger.isUserLogggedIn()){
           startActivity(new Intent(MainActivity.this, LoginActivity.class));
           finish();
        }

        //check if the user is blocked and if its true switch to white activity
        //1= blocked ; 0=active
        checkUserStatus(sharedPreferenceManger.getUserData().getUserId());
        final AppDatabase mdb = AppDatabase.getInstance(this);
        if (!Common.isOrdered&&Common.total==0) {
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mdb.orderDao().deleteAllFood();
                }
            });
        }

        //call image query method
        imageQuery();

    }


    private void checkUserStatus(final int user_id) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.send_reservation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {

                                int status = jsonObject.getInt("is_blocked");
                                if (status == 1){
                                    sharedPreferenceManger.logUserOut();
                                    sharedPreferenceManger.logUserDeleteTokens();
                                    Common.isNewToken =false;
                                    Toast.makeText(MainActivity.this, getString(R.string.blocked_account), Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, WhiteActivity.class);
                                    intent.putExtra("type","block");
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                }

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> ReservationData = new HashMap<>();
                ReservationData.put("id", String.valueOf(user_id));


                return ReservationData;
            }
        };
        VolleyHandler.getInstance(MainActivity.this).addRequetToQueue(stringRequest);
    }


    private void viewsInit() {
        spaceNavigationView =  findViewById(R.id.space);
        makeReservationBtn = findViewById(R.id.make_reservation_btn);
        sliderView         = findViewById(R.id.imageSlider);
        address =findViewById(R.id.textView);

        Typeface face ;
        Typeface face_book;
        if (lang.equals("ar")){
            face = Typeface.createFromAsset(MainActivity.this.getAssets(),"fonts/Cairo-Regular.ttf");
            face_book = Typeface.createFromAsset(MainActivity.this.getAssets(),"fonts/Cairo-Bold.ttf");
            address.setTextSize(16);
        }else {
            face = Typeface.createFromAsset(MainActivity.this.getAssets(),"fonts/Kabrio_Regular.ttf");
            face_book = Typeface.createFromAsset(MainActivity.this.getAssets(),"fonts/Akrobat-Bold.otf");
        }

        makeReservationBtn.setTypeface(face);
        address.setTypeface(face_book);
    }

    //this part is responsible for showing image and switch between them automatically
    private void setupSlider() {
        sliderAdapterExample = new SliderAdapterExample(images);
        sliderView.setSliderAdapter(sliderAdapterExample);
        sliderView.startAutoCycle();
        sliderView.setIndicatorAnimation(IndicatorAnimations.WORM);
        sliderView.setSliderTransformAnimation(SliderAnimations.SIMPLETRANSFORMATION);
    }

    //get home images from dataBase and add it to sliderView adapter
    public void imageQuery(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.home_images,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){
                                JSONArray jsonObjectTables =  jsonObject.getJSONArray("images");
                                for(int i = 0 ; i<jsonObjectTables.length(); i++){
                                    JSONObject jsonimage = jsonObjectTables.getJSONObject(i);

                                    images.add(jsonimage.getString("image_url"));
                                }
                                setupSlider();
                            }else{
                                setupSlider();
                            }
                        }catch (JSONException e){
                            setupSlider();
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setupSlider();
                    }
                }
        );
        VolleyHandler.getInstance(MainActivity.this).addRequetToQueue(stringRequest);
    }

    public void needUpdate(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.check_for_update,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String runningVersionString;
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){
                                JSONArray jsonArray = jsonObject.getJSONArray("appUpdate");
                                for(int i = 0 ; i<jsonArray.length(); i++){
                                    JSONObject jsonUpdate = jsonArray.getJSONObject(i);
                                    versionName = jsonUpdate.getDouble("versionNumber");
                                    isImportant = jsonUpdate.getInt("is_important");
                                }

                                if (isImportant == 1){
                                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                                    runningVersionString = pInfo.versionName;
                                    double runningVersionName = Double.parseDouble(runningVersionString);
                                    if (versionName>runningVersionName){
                                        Intent intent = new Intent(MainActivity.this, WhiteActivity.class);
                                        intent.putExtra("type","update");
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            }
                        }catch (JSONException e){
                            setupSlider();
                            e.printStackTrace();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        setupSlider();
                    }
                }
        );
        VolleyHandler.getInstance(MainActivity.this).addRequetToQueue(stringRequest);


    }

    //setup bottom navigation
    private void bottomNavigationInit(Bundle savedInstanceState , final Activity activityA) {
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("CART", R.drawable.ic_shopping_cart));
        spaceNavigationView.addSpaceItem(new SpaceItem("HISTORY", R.drawable.ic_history));
        spaceNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_man_user));
        spaceNavigationView.showIconOnly();
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.changeCurrentItem(0);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                spaceNavigationView.setActiveSpaceItemColor(ContextCompat.getColor(MainActivity.this,R.color.inactive_color));
                spaceNavigationView.changeCurrentItem(-1);
                startActivity(new Intent(activityA, MenuActivity.class));
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                switch (itemIndex){

                    case 1:
                        startActivity(new Intent(activityA, CartActivity.class));
                        break;

                    case 2:
                        startActivity(new Intent(activityA, HistoryActivity.class));
                        break;



                    case 3:
                        startActivity(new Intent(activityA, ProfileActivity.class));
                        break;
                }

            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {
            }
        });
    }


    //its part of bottom navigation setup
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }

    //also part of bottom navigation setup
    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(0);
    }

}
