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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.emargystudio.bohemea.Cinema.CinemaActivity;
import com.emargystudio.bohemea.LocalDataBases.AppDatabase;
import com.emargystudio.bohemea.LocalDataBases.AppExecutors;
import com.emargystudio.bohemea.LocalDataBases.MainViewModel;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.MakeReservation.ReservationActivity;
import com.emargystudio.bohemea.Menu.MenuActivity;
import com.emargystudio.bohemea.Model.FoodCategory;
import com.emargystudio.bohemea.Model.FoodOrder;
import com.emargystudio.bohemea.Profile.ProfileActivity;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.ViewHolders.CartAdapter;
import com.emargystudio.bohemea.ViewHolders.FoodCategoryAdapter;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.emargystudio.bohemea.helperClasses.Common.total;

public class CartActivity extends AppCompatActivity {


    SpaceNavigationView spaceNavigationView;

    private List<FoodOrder> foodOrders;
    private CartAdapter cartAdapter;
    int total1;

    private AppDatabase mDb;

    //widgets
    TextView empty_cart_text, total_text;
    RelativeLayout emptyCart;
    LinearLayout fullCart;
    Button addMoreBtn, continueBtn, emptyCartBtn;
    RecyclerView recyclerView;
    private ConstraintLayout coordinatorLayout;


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

                showDialog(foodOrders.get(position));

            }
        });


        mDb = AppDatabase.getInstance(CartActivity.this);
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


    }

    private void initViewAndTypeFaces() {
        spaceNavigationView = findViewById(R.id.space);
        recyclerView = findViewById(R.id.cart_rv);
        recyclerView.setHasFixedSize(true);
        empty_cart_text = findViewById(R.id.empty_cart_text);
        emptyCart = findViewById(R.id.empty_cart_layout);
        fullCart = findViewById(R.id.cart_full);
        addMoreBtn = findViewById(R.id.add_more_cart_btn);
        continueBtn = findViewById(R.id.continue_cart_btn);
        total_text = findViewById(R.id.cart_total_text);
        emptyCartBtn = findViewById(R.id.empty_cart_btn);
        coordinatorLayout = findViewById(R.id.coordinator_layout);

        Typeface face_Regular = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-Regular.otf");
        Typeface face_ExtraBold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-ExtraBold.otf");
        Typeface face_Bold = Typeface.createFromAsset(CartActivity.this.getAssets(), "fonts/Akrobat-Bold.otf");

        empty_cart_text.setTypeface(face_Regular);
        total_text.setTypeface(face_ExtraBold);
        addMoreBtn.setTypeface(face_Bold);
        continueBtn.setTypeface(face_Bold);
        emptyCartBtn.setTypeface(face_Bold);

        if (Common.res_id == 0) {
            continueBtn.setText(getString(R.string.cart_act_btn_make_res_first));
        }
    }


    //setup bottom navigation
    private void bottomNavigationInit(Bundle savedInstanceState, final Activity activityA) {
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("CINEMA", R.drawable.ic_clapperboard));
        spaceNavigationView.addSpaceItem(new SpaceItem("CART", R.drawable.ic_shopping_cart));
        spaceNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_man_user));
        spaceNavigationView.showIconOnly();
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.changeCurrentItem(2);
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

                    case 1:
                        startActivity(new Intent(activityA, CinemaActivity.class));
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


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Common.res_id != 0) {
                    sendOrder();
                } else {
                    Intent intent = new Intent(CartActivity.this, ReservationActivity.class);
                    startActivity(intent);
                }
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


    private void sendOrder() {
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
                param.put("res_id", String.valueOf(Common.res_id));// array is key which we will use on server side

                return param;
            }
        };//end of string Request

        VolleyHandler.getInstance(getApplicationContext()).addRequetToQueue(stringRequest);


    }


    private void showDialog(final FoodOrder foodOrder) {
        final AlertDialog.Builder alert = new AlertDialog.Builder(CartActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View alertLayout = li.inflate(R.layout.alert_update, null);
        final TextView txt_price = alertLayout.findViewById(R.id.price_txt);
        final ElegantNumberButton numberButton = alertLayout.findViewById(R.id.number_button);

        if (foodOrder.getPrice() != 0) {
            txt_price.setText(String.format(getString(R.string.food_menu_adapter_price_text), foodOrder.getPrice()));
        }
        if (foodOrder.getQuantity() != 0) {
            numberButton.setNumber(String.valueOf(foodOrder.getQuantity()));
        }

        alert.setView(alertLayout);
        alert.setNegativeButton(R.string.cart_update_dialog_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setPositiveButton(R.string.cart_update_dialog_update, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                foodOrder.setQuantity(Integer.parseInt(numberButton.getNumber()));
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.orderDao().updateFood(foodOrder);
                    }
                });

            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(2);
    }
}
