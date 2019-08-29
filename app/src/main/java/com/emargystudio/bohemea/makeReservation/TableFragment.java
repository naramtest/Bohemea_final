package com.emargystudio.bohemea.makeReservation;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.devs.vectorchildfinder.VectorChildFinder;
import com.devs.vectorchildfinder.VectorDrawableCompat;
import com.emargystudio.bohemea.localDataBases.AppDatabase;
import com.emargystudio.bohemea.localDataBases.AppExecutors;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.menu.MenuActivity;
import com.emargystudio.bohemea.model.FoodOrder;
import com.emargystudio.bohemea.model.Reservation;
import com.emargystudio.bohemea.model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.CommonReservation;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.emargystudio.bohemea.helperClasses.Common.isOrdered;
import static com.emargystudio.bohemea.helperClasses.Common.total;
import static com.emargystudio.bohemea.helperClasses.Common.res_id;

public class TableFragment extends Fragment {


    private ImageView imageView;
    private TabLayout tabLayout;
    private ImageButton backBtn;
    private FloatingActionButton sendBtn;


    //var
    private Reservation reservation;
    private ArrayList<Integer> tableArray;
    private User user;
    private AppDatabase mDb;
    private List<FoodOrder> foodList;
    private int tablesNumber = 13; //should be changed when a table is add
    private String selectedTable = "0";

    String lang = Locale.getDefault().getLanguage();

    boolean oldSdk;
    private VectorChildFinder vector;


    public TableFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_table, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {


        initVIews(view);

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP){
            oldSdk = true;
        }

        tableArray = new ArrayList<>();
        foodList = new ArrayList<>();
        mDb = AppDatabase.getInstance(getContext());

        try {
            reservation = getReservationFromBundle();
            user = SharedPreferenceManger.getInstance(getContext()).getUserData();
            reservationQuery();

        } catch (NullPointerException e) {
            if (getContext() != null) {
                Toast.makeText(getContext(), getContext().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
            }
        }

        loadListFood();


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFragmentManager().popBackStack();
            }
        });

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedTable.equals("0")){
                    alertSend(user.getUserId(), reservation, selectedTable);
                }else {
                    Toast.makeText(getContext(), getString(R.string.f_table_choose_table_first), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void initVIews(@NonNull View view) {
        tabLayout = view.findViewById(R.id.tabLayout);
        imageView = view.findViewById(R.id.imageView);
        TextView headerTxt1 = view.findViewById(R.id.header1);
        TextView headerTxt2 = view.findViewById(R.id.header2);
        TextView taken = view.findViewById(R.id.taken_txt);
        TextView available = view.findViewById(R.id.available_txt);
        TextView selected = view.findViewById(R.id.selected_txt);
        backBtn  = view.findViewById(R.id.backBtn);
        sendBtn  = view.findViewById(R.id.send_btn);

        Typeface face_light;
        Typeface face_book;
        Typeface face_bold ;
        if (lang.equals("ar")){
            sendBtn.setRotation(180);
            backBtn.setRotation(0);
            face_light = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-Light.ttf");
            face_book = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-SemiBold.ttf");
            face_bold = Typeface.createFromAsset(getContext().getAssets(),"fonts/Cairo-Bold.ttf");
        }else {
            face_light = Typeface.createFromAsset(getContext().getAssets(),"fonts/Kabrio-Light.ttf");
            face_book = Typeface.createFromAsset(getContext().getAssets(),"fonts/Kabrio-Book.ttf");
            face_bold = Typeface.createFromAsset(getContext().getAssets(),"fonts/Kabrio-Bold.ttf");
        }



        headerTxt1.setTypeface(face_light);
        headerTxt2.setTypeface(face_book);
        taken.setTypeface(face_bold);
        available.setTypeface(face_bold);
        selected.setTypeface(face_bold);
    }


    private Reservation getReservationFromBundle() {

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            return bundle.getParcelable(getString(R.string.reservation_bundle));
        } else {
            return null;
        }
    }

    private void reservationQuery() {
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.reservation_query + reservation.getYear() + "&month=" + reservation.getMonth() + "&day=" + reservation.getDay(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if (!jsonObject.getBoolean("error")) {

                                JSONArray jsonArrayReservation = jsonObject.getJSONArray("reservations");

                                for (int i = 0; i < jsonArrayReservation.length(); i++) {
                                    JSONObject jsonObjectSingleRes = jsonArrayReservation.getJSONObject(i);

                                    if (jsonObjectSingleRes.getInt("status") == 0 ||jsonObjectSingleRes.getInt("status") == 1) {
                                        double startHour = jsonObjectSingleRes.getDouble("hours");
                                        double endHour = jsonObjectSingleRes.getDouble("end_hour");
                                        if (reservation.getStartHour() >= startHour && reservation.getStartHour() <= endHour) {
                                            tableArray.add(jsonObjectSingleRes.getInt("table_id"));
                                        }
                                    }
                                }

                                initTabLayout();

                            } else {
                                Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }

        );


        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }

    private void initTabLayout() {

        if (!oldSdk){
            vector = new VectorChildFinder(getContext(), R.drawable.ic_group_192, imageView);
            for (int i = 0; i < tablesNumber; i++) {
                if (!tableArray.contains(i + 1)) {
                    tabLayout.addTab(tabLayout.newTab().setText(String.valueOf(i + 1)));

                }else {
                    int pathNumber = i+1;
                    String pathName  = "path"+pathNumber;

                    VectorDrawableCompat.VFullPath path1 = vector.findPathByName(pathName);
                    path1.setFillColor(Color.RED);

                    imageView.invalidate();
                }
            }
        }else {
            imageView.setImageDrawable(getResources().getDrawable(R.drawable.table));
            for (int i = 0; i < tablesNumber; i++) {
                if (!tableArray.contains(i + 1)) {
                    tabLayout.addTab(tabLayout.newTab().setText(String.valueOf(i + 1)));

                }
            }
        }


        LinearLayout linearLayout = (LinearLayout)tabLayout.getChildAt(0);
        linearLayout.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(Color.BLACK);
        drawable.setSize(1, 1);
        linearLayout.setDividerPadding(10);
        linearLayout.setDividerDrawable(drawable);




        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {



                selectedTable=tab.getText().toString();
                if (!oldSdk) {
                    String pathName = "path" + tab.getText().toString();
                    VectorDrawableCompat.VFullPath path1 = vector.findPathByName(pathName);
                    path1.setFillColor(Color.GREEN);
                    imageView.invalidate();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (!oldSdk) {
                    String pathName = "path" + tab.getText().toString();
                    VectorDrawableCompat.VFullPath path1 = vector.findPathByName(pathName);
                    path1.setFillColor(Color.WHITE);
                    imageView.invalidate();
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                if (tab.getPosition()==0){
                    selectedTable=tab.getText().toString();
                    if (!oldSdk){
                        String pathName  = "path"+tab.getText().toString();
                        VectorDrawableCompat.VFullPath path1 = vector.findPathByName(pathName);
                        path1.setFillColor(Color.GREEN);
                        imageView.invalidate();
                    }

                }
                //alertSend(user.getUserId(), reservation, Objects.requireNonNull(tab.getText()).toString(), "No movie");

            }
        });
    }

    private void alertSend(final int user_id, final Reservation reservation, final String table_id) {
        if (getContext() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(String.format(getString(R.string.table_frag_alert_send_table_number), table_id));
            alert.setMessage(R.string.table_frag_alert_send_message);

            alert.setPositiveButton(R.string.table_frag_alert_send_yesBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendReservation(user_id, reservation, table_id, "no movie");
                }
            });
            alert.setNegativeButton(R.string.cart_update_dialog_cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();

        }
    }

    private void sendReservation(final int user_id, final Reservation reservation, final String table_id, final String movie_name) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.send_reservation,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            if (!jsonObject.getBoolean("error")) {

                                JSONObject jsonObjectUser = jsonObject.getJSONObject("reservation");
                                Common.res_id = jsonObjectUser.getInt("res_id");

                                if (isOrdered) {
                                    sendOrder();
                                }
                                alertDone();
                                sendNotification(user_id,jsonObjectUser.getInt("res_id"));

                            } else {
                                if (jsonObject.getInt("error_type")==1){
                                    Toast.makeText(getContext(), getString(R.string.reservation_at_same_time), Toast.LENGTH_SHORT).show();
                                }else {
                                    Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                                }
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }
        ) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> ReservationData = new HashMap<>();
                ReservationData.put("user_id", String.valueOf(user_id));
                ReservationData.put("table_id", table_id);
                ReservationData.put("year", String.valueOf(reservation.getYear()));
                ReservationData.put("month", String.valueOf(reservation.getMonth()));
                ReservationData.put("day", String.valueOf(reservation.getDay()));
                ReservationData.put("hours", String.valueOf(reservation.getStartHour()));
                ReservationData.put("end_hour", String.valueOf(reservation.getEnd_hour()));
                ReservationData.put("chairNumber", String.valueOf(reservation.getChairNumber()));
                ReservationData.put("movie_name", movie_name);

                return ReservationData;
            }
        };
        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }

    private void sendNotification(int user_id, int res_id) {

        JSONObject data = new JSONObject();
        JSONObject notification_data = new JSONObject();
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            //Populate the request parameters
            String hour = CommonReservation.changeHourFormat(getContext(),reservation.getStartHour());
            data.put("title", "Bohemea Art Cafe");
            data.put("message", "User "+user.getUserName()+" Reservation date "+reservation.getMonth()+"/"+reservation.getDay()+"/"+hour);
            data.put("android_channel_id", "reservation_channel");
            data.put("user_id",String.valueOf(user_id));
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
        VolleyHandler.getInstance(getContext()).addRequetToQueue(jsArrayRequest);

    }

    private void loadListFood() {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                foodList = mDb.orderDao().loadAllFoodsAdapter();
                if (!foodList.isEmpty()) {
                    isOrdered = true;
                }
            }
        });
    }

    private void sendOrder() {
        Gson gson = new Gson();
        final String newDataArray = gson.toJson(foodList);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.pre_order,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Common.clearCommon();
                        Common.isSended = true;
                        AppExecutors.getInstance().diskIO().execute(new Runnable() {
                            @Override
                            public void run() {
                                mDb.orderDao().deleteAllFood();
                            }
                        });
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
                param.put("res_id", String.valueOf(res_id));
                return param;
            }
        };
        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }

    private void alertDone() {
        if (getContext() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            LayoutInflater li = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View alertLayout = li.inflate(R.layout.alert_reser_done, null);
            TextView menu = alertLayout.findViewById(R.id.menu_container);
            RelativeLayout menuTxt = alertLayout.findViewById(R.id.menu);

            TextView header = alertLayout.findViewById(R.id.dialog_header);
            TextView first = alertLayout.findViewById(R.id.first_b);
            TextView second = alertLayout.findViewById(R.id.second_b);
            TextView third = alertLayout.findViewById(R.id.third_b);

            Typeface regular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Regular.ttf");
            Typeface extraBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Bold.ttf");

            header.setTypeface(extraBold);
            first.setTypeface(regular);
            second.setTypeface(regular);
            third.setTypeface(regular);

            if (isOrdered) {
                menuTxt.setVisibility(View.GONE);
            }
            alert.setView(alertLayout);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            alert.setNegativeButton(R.string.table_frag_alert_send_cancel_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    getContext().startActivity(intent);
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();

        }


    }
}
