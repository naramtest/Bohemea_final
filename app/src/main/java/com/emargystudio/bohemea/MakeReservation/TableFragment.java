package com.emargystudio.bohemea.MakeReservation;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.LocalDataBases.AppDatabase;
import com.emargystudio.bohemea.LocalDataBases.AppExecutors;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.Menu.MenuActivity;
import com.emargystudio.bohemea.Model.FoodOrder;
import com.emargystudio.bohemea.Model.Reservation;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.CommonReservation;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.emargystudio.bohemea.helperClasses.Common.isOrdered;
import static com.emargystudio.bohemea.helperClasses.Common.total;
import static com.emargystudio.bohemea.helperClasses.Common.res_id;

public class TableFragment extends Fragment {

    private TabLayout tabLayout;


    //var
    private Reservation reservation;
    private ArrayList<Integer> tableArray;
    private User user;
    private AppDatabase mDb;
    private List<FoodOrder> foodList;


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

        TextView textView = view.findViewById(R.id.textView);
        tabLayout = view.findViewById(R.id.tableLayout);

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

        if (getActivity() != null) {
            Typeface face = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Kabrio_Regular.ttf");
            textView.setTypeface(face);
        }
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
                                    Log.i("jsonObjectSingleRes", jsonObjectSingleRes.toString());

                                    double startHour = jsonObjectSingleRes.getDouble("hours");
                                    double endHour = jsonObjectSingleRes.getDouble("end_hour");
                                    if (reservation.getStartHour() >= startHour && reservation.getStartHour() <= endHour) {
                                        tableArray.add(jsonObjectSingleRes.getInt("table_id"));
                                    }
                                }

                                initTabLayout();

                            } else {
                                Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                    }
                }

        );


        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }


    private void initTabLayout() {

        for (int i = 0; i < 10; i++) {
            if (!tableArray.contains(i + 1)) {
                tabLayout.addTab(tabLayout.newTab().setText(String.valueOf(i + 1)));
            }
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                alertSend(user.getUserId(), reservation, Objects.requireNonNull(tab.getText()).toString(), "No movie");
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void alertSend(final int user_id, final Reservation reservation, final String table_id, final String movie_name) {
        if (getContext() != null) {

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setTitle(String.format(getString(R.string.table_frag_alert_send_table_number), table_id));
            alert.setMessage(R.string.table_frag_alert_send_message);
            alert.setPositiveButton(R.string.table_frag_alert_send_yesBtn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sendReservation(user_id, reservation, table_id, movie_name);
                }
            });
            alert.setNegativeButton(R.string.table_frag_alert_send_cancel_btn, new DialogInterface.OnClickListener() {
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
                                sendNotification(user_id,jsonObjectUser.getInt("res_id"),"reservation_channel");

                            } else {
                                Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                            }

                        } catch (JSONException e) {
                            Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), Objects.requireNonNull(getContext()).getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
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

    private void sendNotification(int user_id, int res_id, String channel) {

        JSONObject data = new JSONObject();
        JSONObject notification_data = new JSONObject();
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            //Populate the request parameters
            String hour = CommonReservation.changeHourFormat(reservation.getStartHour());
            data.put("title", "Bohemea Art Cafe");
            data.put("message", "user "+user.getUserName()+" Reservation date "+reservation.getMonth()+"/"+reservation.getDay()+"/"+hour);
            data.put("android_channel_id",channel);
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

            if (isOrdered) {
                menuTxt.setVisibility(View.GONE);
            }
            alert.setView(alertLayout);
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(), MenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Objects.requireNonNull(getContext()).startActivity(intent);
                }
            });
            alert.setNegativeButton(R.string.table_frag_alert_send_cancel_btn, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(getContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    Objects.requireNonNull(getContext()).startActivity(intent);
                }
            });
            AlertDialog dialog = alert.create();
            dialog.show();

        }


    }
}
