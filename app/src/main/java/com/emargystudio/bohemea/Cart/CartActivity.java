package com.emargystudio.bohemea.Cart;

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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.emargystudio.bohemea.History.HistoryActivity;
import com.emargystudio.bohemea.LocalDataBases.AppDatabase;
import com.emargystudio.bohemea.LocalDataBases.AppExecutors;
import com.emargystudio.bohemea.LocalDataBases.MainViewModel;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.MakeReservation.ReservationActivity;
import com.emargystudio.bohemea.Menu.MenuActivity;
import com.emargystudio.bohemea.Model.FastOrder;
import com.emargystudio.bohemea.Model.FoodOrder;
import com.emargystudio.bohemea.Model.Reservation;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.Profile.ProfileActivity;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.ViewHolders.CartAdapter;
import com.emargystudio.bohemea.ViewHolders.NewReservationAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.emargystudio.bohemea.helperClasses.Common.total;

public class CartActivity extends AppCompatActivity {


    SpaceNavigationView spaceNavigationView;

    private List<FoodOrder> foodOrders;
    private ArrayList<Reservation> reservations = new ArrayList<>();
    private List<FastOrder> fastOrders = new ArrayList<>();
    private CartAdapter cartAdapter;
    private NewReservationAdapter newReservationAdapter;
    int total1;

    private AppDatabase mDb;
    private User user;
    private SharedPreferenceManger sharedPreferenceManger;

    //widgets
    TextView empty_cart_text, total_text;
    RelativeLayout emptyCart;
    LinearLayout fullCart;
    Button addMoreBtn , emptyCartBtn , orderNowBtn ,orderForLaterBtn ;
    RecyclerView recyclerView;
    private ConstraintLayout coordinatorLayout;

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
                        .make(coordinatorLayout, name + getString(R.string.cart_snackbar_remove), Snackbar.LENGTH_LONG);
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


        sharedPreferenceManger = SharedPreferenceManger.getInstance(CartActivity.this);
        mDb = AppDatabase.getInstance(CartActivity.this);
        user= sharedPreferenceManger.getUserData();
        initViewAndTypeFaces();


        bottomNavigationInit(savedInstanceState, CartActivity.this);

        recyclerView.setLayoutManager(new LinearLayoutManager(CartActivity.this));
        recyclerView.setAdapter(cartAdapter);


        loadListFood();
        checkEmpty();


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

        orderForLaterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                laterOrderDialog();
            }
        });




    }

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

        Typeface face_Regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-Regular.otf");
        Typeface face_ExtraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-ExtraBold.otf");
        Typeface face_Bold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-Bold.otf");

        empty_cart_text.setTypeface(face_Regular);
        total_text.setTypeface(face_ExtraBold);
        addMoreBtn.setTypeface(face_Bold);
        orderNowBtn.setTypeface(face_Bold);
        orderForLaterBtn.setTypeface(face_Bold);
        emptyCartBtn.setTypeface(face_Bold);

    }





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


    public void checkEmpty() {
        if (foodOrders != null) {
            if (foodOrders.isEmpty()) {
                Common.isOrdered = false;
                emptyCart.setVisibility(View.VISIBLE);
                fullCart.setVisibility(View.GONE);

            } else {
                emptyCart.setVisibility(View.GONE);
                fullCart.setVisibility(View.VISIBLE);
                if (sharedPreferenceManger.getFirstTime() == 0){
                    alertFirstTime();
                    sharedPreferenceManger.storeFirstUse(1);
                }
            }
        }
    }


    public void alertDone() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = li.inflate(R.layout.alert_done, null);
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

    public void alertFirstTime() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = li.inflate(R.layout.alert_first_time, null);
        alert.setView(alertLayout);
        alert.setNegativeButton(R.string.cart_done_dailog_done, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

    public void alertFastOrderDone() {
        AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = li.inflate(R.layout.alert_fast_order_done, null);
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


    private void sendFastOrder(){
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
                param.put("user_id", String.valueOf(user.getUserId()));// array is key which we will use on server side

                return param;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);

    }

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


    private void showDialog(final FoodOrder foodOrder , final int position) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = li.inflate(R.layout.alert_update, null);
        final TextView txt_price = alertLayout.findViewById(R.id.price_txt);
        final ElegantNumberButton numberButton = alertLayout.findViewById(R.id.number_button);
        TextView updateBtn = alertLayout.findViewById(R.id.update_txt);
        TextView cancelBtn = alertLayout.findViewById(R.id.cancel_txt);
        TextView headerTxt = alertLayout.findViewById(R.id.edit_cart_header);


        Typeface face = Typeface.createFromAsset(getAssets(),"fonts/Akrobat-ExtraBold.otf");
        Typeface face_light = Typeface.createFromAsset(getAssets(),"fonts/Akrobat-Regular.otf");
        Typeface face_bold = Typeface.createFromAsset(getAssets(),"fonts/Kabrio-Bold.ttf");

        headerTxt.setTypeface(face);
        txt_price.setTypeface(face_light);
        updateBtn.setTypeface(face_bold);
        cancelBtn.setTypeface(face_bold);



        int total = foodOrder.getPrice()*foodOrder.getQuantity();
        txt_price.setText(String.format(getString(R.string.food_menu_adapter_price_text), total));
        numberButton.setNumber(String.valueOf(foodOrder.getQuantity()));
        numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                int total = foodOrder.getPrice()*newValue;
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


    //show dialog contain list of the user reservation and an option to make a new one
    //this dialog showing when the user press on FOR LATER button
    private void laterOrderDialog(){
        reservationQuery(user.getUserId());
        final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // find views
        View alertLayout = li.inflate(R.layout.choose_reservation_dialog, null);
        RecyclerView dialogRv = alertLayout.findViewById(R.id.new_reservation_rv);
        TextView addNew = alertLayout.findViewById(R.id.add_new_reservation_btn);

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

                                for (int i = 0; i < jsonArrayReservation.length(); i++) {
                                    JSONObject jsonObjectSingleRes = jsonArrayReservation.getJSONObject(i);

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

                                newReservationAdapter.notifyDataSetChanged();


                            }
                        } catch (JSONException e) {
                            Log.i("cart", "onResponse: "+e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("cart", "onResponse: "+error.getMessage());

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
}
