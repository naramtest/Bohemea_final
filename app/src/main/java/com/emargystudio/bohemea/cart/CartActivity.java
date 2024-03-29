package com.emargystudio.bohemea.cart;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.emargystudio.bohemea.history.HistoryActivity;
import com.emargystudio.bohemea.localDataBases.AppDatabase;
import com.emargystudio.bohemea.localDataBases.AppExecutors;
import com.emargystudio.bohemea.localDataBases.MainViewModel;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.makeReservation.ReservationActivity;
import com.emargystudio.bohemea.menu.MenuActivity;
import com.emargystudio.bohemea.model.FastOrder;
import com.emargystudio.bohemea.model.FoodOrder;
import com.emargystudio.bohemea.model.Reservation;
import com.emargystudio.bohemea.model.User;
import com.emargystudio.bohemea.profile.ProfileActivity;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.viewHolders.CartAdapter;
import com.emargystudio.bohemea.viewHolders.NewReservationAdapter;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import static com.emargystudio.bohemea.helperClasses.Common.total;

public class CartActivity extends AppCompatActivity {


    //bottom navigation
    SpaceNavigationView spaceNavigationView;

    private List<FoodOrder> foodOrders;
    private ArrayList<Reservation> reservations = new ArrayList<>();
    private List<FastOrder> fastOrders = new ArrayList<>();

    // adapter for food item list on the cart activity
    private CartAdapter cartAdapter;
    // adapter for list of reservation when pressing on for later button
    private NewReservationAdapter newReservationAdapter;

    // to calculate the total price of all the item on the cart list
    private int total1;

    Typeface face_Regular ;
    Typeface face_ExtraBold;
    Typeface face_Bold ;
    Typeface face_light;


    private AppDatabase mDb;
    private User user;
    private SharedPreferenceManger sharedPreferenceManger;

    String lang = Locale.getDefault().getLanguage();

    //widgets
    TextView empty_cart_text, total_text;
    RelativeLayout emptyCart;
    LinearLayout fullCart;
    Button addMoreBtn , emptyCartBtn , orderNowBtn ,orderForLaterBtn ;
    RecyclerView recyclerView;
    private ConstraintLayout coordinatorLayout;


    //for later list of reservation on click listener
    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            sendOrder(reservations.get(position).getRes_id());

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        foodOrders = new ArrayList<>();

        //add on click lister for the edit and delete item on every item on the cart list
        cartAdapter = new CartAdapter(CartActivity.this, new CartAdapter.DetailsAdapterListener() {
            @Override
            public void deleteCartItem(RecyclerView.ViewHolder v, int position) {
                String name = foodOrders.get(position).getFood_name();

                // backup of removed item for undo purpose
                final FoodOrder deletedItem = foodOrders.get(position);
                final int deletedIndex = position;

                // remove the item from recycler view
                cartAdapter.removeItem(position);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.orderDao().deleteFood(deletedItem);
                    }
                });


                // showing snack bar with Undo option
                Snackbar snackbar = Snackbar
                        .make(coordinatorLayout, name +" "+getString(R.string.cart_snackbar_remove), Snackbar.LENGTH_LONG);
                snackbar.setAction(R.string.cart_snackbar_undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // undo is selected, restore the deleted item
                        cartAdapter.restoreItem(deletedItem, deletedIndex);
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.orderDao().insertFood(deletedItem);
                            }
                        });
                    }
                });
                snackbar.setActionTextColor(Color.YELLOW);
                snackbar.show();

            }


            @Override
            public void editCartItem(RecyclerView.ViewHolder v, int position) {

                showDialog(foodOrders.get(position),position);

            }
        });
        newReservationAdapter = new NewReservationAdapter(reservations,CartActivity.this);



        // init setup for the activity
        initViewAndTypeFaces();
        bottomNavigationInit(savedInstanceState, CartActivity.this);
        sharedPreferenceManger = SharedPreferenceManger.getInstance(CartActivity.this);
        mDb = AppDatabase.getInstance(CartActivity.this);
        user= sharedPreferenceManger.getUserData();

        //setup rv

        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        recyclerView.setAdapter(cartAdapter);
        recyclerView.setHasFixedSize(true);


        loadListFood();
        checkEmpty();



        //listener for all the button
        addMoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, MenuActivity.class));
            }
        });

        emptyCartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, MenuActivity.class));
            }
        });

        orderNowBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (sharedPreferenceManger.getFirstTime() == 0){
                    alertFirstTime();
                }else {

                    for (FoodOrder foodOrder : foodOrders){
                        fastOrders.add(new FastOrder(foodOrder.getFood_id(),foodOrder.getFood_name(),foodOrder.getQuantity(),foodOrder.getPrice()));
                    }
                    if (fastOrders!=null){
                        if (!fastOrders.isEmpty()){
                            sendFastOrder();
                        }
                    }
                }



            }
        });
        orderForLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                laterOrderDialog();
            }
        });


    }

    //init views in the activity
    private void initViewAndTypeFaces() {
        spaceNavigationView = findViewById(R.id.space);
        recyclerView = findViewById(R.id.cart_rv);
        recyclerView.setHasFixedSize(true);
        empty_cart_text = findViewById(R.id.empty_cart_text);
        emptyCart = findViewById(R.id.empty_cart_layout);
        fullCart = findViewById(R.id.cart_full);
        addMoreBtn = findViewById(R.id.add_more_cart_btn);
        orderForLaterBtn = findViewById(R.id.order_for_later_btn);
        orderNowBtn      = findViewById(R.id.order_now_btn);
        total_text = findViewById(R.id.cart_total_text);
        emptyCartBtn = findViewById(R.id.empty_cart_btn);
        coordinatorLayout = findViewById(R.id.coordinator_layout);



        if (lang.equals("ar")){
            face_Regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Regular.ttf");
            face_ExtraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Bold.ttf");
            face_Bold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-SemiBold.ttf");
            face_light = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Light.ttf");

            recyclerView.setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            total_text.setTextSize(16);
        }else{
            face_Regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-Regular.otf");
            face_ExtraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-ExtraBold.otf");
            face_Bold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-Bold.otf");
            face_light = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Light.ttf");

        }


        empty_cart_text.setTypeface(face_Regular);
        total_text.setTypeface(face_ExtraBold);
        addMoreBtn.setTypeface(face_Bold);
        orderNowBtn.setTypeface(face_Bold);
        orderForLaterBtn.setTypeface(face_Bold);
        emptyCartBtn.setTypeface(face_Bold);

    }


    //use view model with mysqLite to auto load all the item on the list
    private void loadListFood() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        viewModel.getTasks().observe(this, new Observer<List<FoodOrder>>() {
            @Override
            public void onChanged(@Nullable List<FoodOrder> foodOrders1) {

                total1 = 0;
                foodOrders = foodOrders1;
                cartAdapter.setTasks(foodOrders);
                cartAdapter.notifyDataSetChanged();
                for (int i = 0; i < foodOrders.size(); i++) {
                    total1 += foodOrders.get(i).getPrice() * foodOrders.get(i).getQuantity();
                }
                total_text.setText(String.format(getString(R.string.total_cart_text), total1));
                Common.isOrdered = true;
                checkEmpty();

            }
        });


    }

    private void sendNotification(int res_id) {

        JSONObject data = new JSONObject();
        JSONObject notification_data = new JSONObject();
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            //Populate the request parameters

            data.put("title", "Bohemea Art Cafe");
            data.put("message", "Fast order by "+user.getUserName());
            data.put("android_channel_id", "fast_order_channel");
            data.put("res_id",String.valueOf(res_id));

            notification_data.put("data", data);
            notification_data.put("to","/topics/reservation");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsArrayRequest = new JsonObjectRequest
                (Request.Method.POST, url, notification_data, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
            @Override
            public Map<String, String> getHeaders() {
                String api_key_header_value = "Key=AAAAMsoSJbQ:APA91bEiYfFYyjyCcgbOvepKtd111o4S_QbZW5yGoZJzXkUJFYQen7-by5lcUGTYP02lVFMuNIyzUUy1oOeGOYHzz6cdqHqixXNbTApdqw7SY4t5B5qwhIwvIXrO-ht1BzKslbq7_O2x";
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", api_key_header_value);
                return headers;
            }
        };

        // Access the RequestQueue through your VolleyHandler class.
        VolleyHandler.getInstance(CartActivity.this).addRequetToQueue(jsArrayRequest);

    }


    public void checkEmpty() {
        if (foodOrders != null) {
            if (foodOrders.isEmpty()) {
                Common.isOrdered = false;
                emptyCart.setVisibility(View.VISIBLE);
                fullCart.setVisibility(View.GONE);
                orderNowBtn.setVisibility(View.GONE);
                orderForLaterBtn.setVisibility(View.GONE);

            } else {
                emptyCart.setVisibility(View.GONE);
                fullCart.setVisibility(View.VISIBLE);
                orderNowBtn.setVisibility(View.VISIBLE);
                orderForLaterBtn.setVisibility(View.VISIBLE);

            }
        }
    }


    //this dialog is showing when send pre order from for later button
    public void alertDone() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        View alertLayout = View.inflate(CartActivity.this,R.layout.alert_done, null);

        TextView header = alertLayout.findViewById(R.id.dialog_header);
        TextView first = alertLayout.findViewById(R.id.first_b);
        TextView second = alertLayout.findViewById(R.id.second_b);

        Typeface regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Regular.ttf");
        Typeface extraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Bold.ttf");

        header.setTypeface(extraBold);
        first.setTypeface(regular);
        second.setTypeface(regular);

        alert.setView(alertLayout);
        alert.setNegativeButton(R.string.cart_done_dailog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    //this dialog shows only the first time this app opened
    public void alertFirstTime() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        View alertLayout = View.inflate(CartActivity.this,R.layout.alert_first_time, null);
        CheckBox checkBox = alertLayout.findViewById(R.id.dont_show_checkBox);

        TextView header = alertLayout.findViewById(R.id.dialog_header);
        TextView first = alertLayout.findViewById(R.id.first_b);
        TextView second = alertLayout.findViewById(R.id.second_b);

        Typeface regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Regular.ttf");
        Typeface extraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Bold.ttf");

        header.setTypeface(extraBold);
        first.setTypeface(regular);
        second.setTypeface(regular);


        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    sharedPreferenceManger.storeFirstUse(1);
                }
            }
        });
        alert.setView(alertLayout);

        alert.setNegativeButton(R.string.cart_done_dailog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();


            }
        });

        alert.setPositiveButton(getString(R.string.a_cart_order_now_btn), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                for (FoodOrder foodOrder : foodOrders){
                    fastOrders.add(new FastOrder(foodOrder.getFood_id(),foodOrder.getFood_name(),foodOrder.getQuantity(),foodOrder.getPrice()));
                }
                if (fastOrders!=null){
                    if (!fastOrders.isEmpty()){
                        sendFastOrder();
                    }
                }
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }


    //when sending order from the order now button
    public void alertFastOrderDone() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        View alertLayout = View.inflate(CartActivity.this,R.layout.alert_fast_order_done, null);

        TextView header = alertLayout.findViewById(R.id.dialog_header);
        TextView first = alertLayout.findViewById(R.id.first_b);


        Typeface regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Regular.ttf");
        Typeface extraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Cairo-Bold.ttf");

        header.setTypeface(extraBold);
        first.setTypeface(regular);

        alert.setView(alertLayout);
        alert.setNegativeButton(R.string.cart_done_dailog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    //this dialog show when pressing on edit button on the list
    //allow user to edit his order
    private void showDialog(final FoodOrder foodOrder , final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        View alertLayout = View.inflate(CartActivity.this,R.layout.alert_update, null);
        final TextView txt_price = alertLayout.findViewById(R.id.price_txt);
        final ElegantNumberButton numberButton = alertLayout.findViewById(R.id.number_button);
        TextView updateBtn = alertLayout.findViewById(R.id.update_txt);
        TextView cancelBtn = alertLayout.findViewById(R.id.cancel_txt);
        TextView headerTxt = alertLayout.findViewById(R.id.edit_cart_header);



        headerTxt.setTypeface(face_Bold);
        txt_price.setTypeface(face_Regular);
        updateBtn.setTypeface(face_Bold);
        cancelBtn.setTypeface(face_Bold);



        int total = foodOrder.getPrice()*foodOrder.getQuantity();
        txt_price.setText(String.format(getString(R.string.food_menu_adapter_price_text), total));
        numberButton.setNumber(String.valueOf(foodOrder.getQuantity()));
        numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                int total = foodOrder.getPrice()*newValue;
                Common.total = Common.total+total;
                txt_price.setText(String.format(getString(R.string.food_menu_adapter_price_text), total));            }
        });



        alert.setView(alertLayout);

        final AlertDialog dialog = alert.create();
        dialog.show();


        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();

            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                foodOrder.setQuantity(Integer.parseInt(numberButton.getNumber()));
                cartAdapter.notifyItemChanged(position);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.orderDao().updateFood(foodOrder);
                    }
                });
                dialog.dismiss();
            }
        });
    }


    //send fast order request to dataBase
    private void sendFastOrder(){
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getDefault());
        Date date = Calendar.getInstance().getTime();
        final String currentTime = sdf.format(date);

        Gson gson = new Gson();
        final String newDataArray = gson.toJson(fastOrders);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.fast_order,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        total = 0;
                        total_text.setText(String.format(getString(R.string.total_cart_text), total1));
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.orderDao().deleteAllFood();
                            }
                        });

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int res_id = jsonObject.getInt("res_id");
                            sendNotification(res_id);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        alertFastOrderDone();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        error.getMessage();
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("array", newDataArray);
                param.put("total", String.valueOf(total));
                param.put("is_fast","1");
                param.put("date",currentTime);
                param.put("status","0");//0 waiting ; 1=approved 2 = canceled
                param.put("user_id", String.valueOf(user.getUserId()));// array is key which we will use on server side

                return param;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);

    }

    //send pre order request to dataBase
    private void sendOrder(final int resId) {
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(foodOrders);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.pre_order,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        total = 0;
                        total_text.setText(String.format(getString(R.string.total_cart_text), total1));
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.orderDao().deleteAllFood();
                            }
                        });

                        alertDone();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        error.getMessage();
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> param = new HashMap<>();
                param.put("array", newDataArray);
                param.put("total", String.valueOf(total));
                param.put("is_fast", "0"); //0 = full reservation ;1=fast order at the restaurant
                param.put("res_id", String.valueOf(resId));// array is key which we will use on server side

                return param;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


    }





    //show dialog contain list of the user reservation and an option to make a new one
    //this dialog showing when the user press on FOR LATER button
    private void laterOrderDialog(){
        reservationQuery(user.getUserId());
        final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);

        // find views
        View alertLayout = View.inflate(CartActivity.this,R.layout.choose_reservation_dialog, null);
        RecyclerView dialogRv = alertLayout.findViewById(R.id.new_reservation_rv);
        TextView addNew = alertLayout.findViewById(R.id.add_new_reservation_btn);
        TextView header = alertLayout.findViewById(R.id.new_reservation_header);

        addNew.setTypeface(face_Regular);
        header.setTypeface(face_Bold);

        //rv setup
        dialogRv.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        dialogRv.setAdapter(newReservationAdapter);
        newReservationAdapter.setOnItemClickListener(onItemClickListener);

        //add new reservation button listener
        addNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(CartActivity.this, ReservationActivity.class));
            }
        });

        //dialog setup
        alert.setView(alertLayout);
        AlertDialog dialog = alert.create();
        dialog.show();
    }


    //get list of user reservation if he have a reservation in waiting status to display it in
    // for later recycler view
    private void reservationQuery(int userId){
        if (!reservations.isEmpty()){
            reservations.clear();
        }
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.user_reservation + userId + "&status=0",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {


                                JSONArray jsonArrayReservation = jsonObject.getJSONArray("reservations");

                                Calendar calendar =Calendar.getInstance() ;

                                final int currentYear = calendar.get(Calendar.YEAR);
                                final int currentMonth = calendar.get(Calendar.MONTH)+1;
                                final int currentDay   = calendar.get(Calendar.DAY_OF_MONTH);
                                final int currentHour = calendar.get(Calendar.HOUR_OF_DAY)-2;
                                final int currentMinute = calendar.get(Calendar.MINUTE);
                                final double currentTime = formatStartingHour(currentHour, currentMinute);
                                int year ;
                                int month ;
                                int day  ;
                                int hours ;



                                for (int i = 0; i < jsonArrayReservation.length(); i++) {
                                    JSONObject jsonObjectSingleRes = jsonArrayReservation.getJSONObject(i);
                                    boolean readyToAdd = true;
                                    if (jsonObjectSingleRes.getInt("status") == 0 ||
                                            jsonObjectSingleRes.getInt("status")==1) {

                                        year = jsonObjectSingleRes.getInt("year");
                                        month = jsonObjectSingleRes.getInt("month");
                                        day = jsonObjectSingleRes.getInt("day");
                                        hours = jsonObjectSingleRes.getInt("hours");


                                        if (currentYear == year){
                                            if (currentMonth == month && currentDay == day){
                                                if (currentTime > hours){
                                                    readyToAdd = false;
                                                }
                                            }else if (currentMonth == month && currentDay > day){
                                                readyToAdd = false;
                                            }else if (currentMonth>month){
                                                readyToAdd = false;
                                            }
                                        }else if (currentYear>year){
                                            readyToAdd = false;
                                        }


                                        if (readyToAdd) {
                                            reservations.add(new Reservation(jsonObjectSingleRes.getInt("res_id"),
                                                    jsonObjectSingleRes.getInt("user_id"),
                                                    jsonObjectSingleRes.getInt("table_id"),
                                                    jsonObjectSingleRes.getInt("year"),
                                                    jsonObjectSingleRes.getInt("month"),
                                                    jsonObjectSingleRes.getInt("day"),
                                                    jsonObjectSingleRes.getDouble("hours"),
                                                    jsonObjectSingleRes.getDouble("end_hour"),
                                                    jsonObjectSingleRes.getInt("chairNumber"),
                                                    jsonObjectSingleRes.getInt("status"),
                                                    jsonObjectSingleRes.getInt("total"),
                                                    jsonObjectSingleRes.getString("movie_name")));

                                        }
                                    }
                                }

                                newReservationAdapter.notifyDataSetChanged();


                            }
                        } catch (JSONException e) {
                            Toast.makeText(CartActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CartActivity.this, getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }

        );


        VolleyHandler.getInstance(CartActivity.this).addRequetToQueue(stringRequest);
    }


    //setup bottom navigation
    //to make bottom navigation always in the right position
    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(1);
    }
    private void bottomNavigationInit(Bundle savedInstanceState, final Activity activityA) {
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("CART", R.drawable.ic_shopping_cart));
        spaceNavigationView.addSpaceItem(new SpaceItem("HISTORY", R.drawable.ic_history));
        spaceNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_man_user));
        spaceNavigationView.showIconOnly();
        spaceNavigationView.shouldShowFullBadgeText(true);
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.changeCurrentItem(1);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                spaceNavigationView.setActiveSpaceItemColor(ContextCompat.getColor(CartActivity.this, R.color.inactive_color));
                spaceNavigationView.changeCurrentItem(-1);
                startActivity(new Intent(activityA, MenuActivity.class));

            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {

                switch (itemIndex) {
                    case 0:
                        startActivity(new Intent(activityA, MainActivity.class));
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
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }


    // format time to look like this HH:mm with only tow number after decimal point
    private double formatStartingHour(int hour, int minute){
        double d = minute /100.00;
        double h = hour+d;
        return round(h);
    }
    private static double round(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(CartActivity.this,MainActivity.class));
        finish();
    }
}
