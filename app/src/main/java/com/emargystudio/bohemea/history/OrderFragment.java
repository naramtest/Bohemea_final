package com.emargystudio.bohemea.history;


import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.model.FoodOrder;
import com.emargystudio.bohemea.model.Reservation;
import com.emargystudio.bohemea.model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.viewHolders.OrderHistoryAdapter;
import com.emargystudio.bohemea.helperClasses.CommonReservation;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class OrderFragment extends Fragment implements OrderHistoryAdapter.EventHandler {


    private OrderHistoryAdapter orderHistoryAdapter;
    private ArrayList<FoodOrder> foodOrders = new ArrayList<>();
    private Reservation reservation;
    private User user;


    //widgets
    private TextView date;
    private TextView hour;
    private TextView table_number;
    private TextView reservation_for;
    private TextView name;
    private TextView totalTxt;
    private RecyclerView recyclerView;
    private TextView noOrder;

    private ProgressBar progressBar;
    private Button submit,cancel;

    private String lang = Locale.getDefault().getLanguage();



    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SharedPreferenceManger sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());
        user = sharedPreferenceManger.getUserData();
        reservation = getReservationFromBundle();

        initViews(view);

        if (reservation!=null){
            if (reservation.getTotal()==0){
                recyclerView.setVisibility(View.GONE);
            }else {
                noOrder.setVisibility(View.GONE);
                initRecyclerView();
                orderQuery();
            }
        }



        setUpView();
        cancelReservation();
        submitOrder();
    }

    //init methods
    private void initViews(@NonNull View view) {
        date = view.findViewById(R.id.date);
        hour = view.findViewById(R.id.hour);
        table_number = view.findViewById(R.id.table_number);
        reservation_for = view.findViewById(R.id.reservation_for);
        name = view.findViewById(R.id.name);
        totalTxt = view.findViewById(R.id.total);
        recyclerView = view.findViewById(R.id.order_history);
        progressBar = view.findViewById(R.id.progress_bar);
        noOrder = view.findViewById(R.id.noOrder);
        submit = view.findViewById(R.id.submit);
        cancel = view.findViewById(R.id.cancel);
        TextView name_tag_txt = view.findViewById(R.id.name_tag_txt);
        TextView total_tag_txt = view.findViewById(R.id.total_tag_txt);
        TextView summaryTxt = view.findViewById(R.id.summaryTxt);
        if (getActivity()!=null){

            Typeface face;
            Typeface face_bold;

            if (lang.equals("ar")){
                face = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Cairo-SemiBold.ttf");
                face_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Cairo-Regular.ttf");
            }else {
                face = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Akrobat-ExtraBold.otf");
                face_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Akrobat-Bold.otf");
            }



            date.setTypeface(face_bold);
            summaryTxt.setTypeface(face);
            hour.setTypeface(face_bold);
            table_number.setTypeface(face_bold);
            reservation_for.setTypeface(face_bold);
            name.setTypeface(face_bold);
            totalTxt.setTypeface(face_bold);
            name_tag_txt.setTypeface(face_bold);
            total_tag_txt.setTypeface(face_bold);
            submit.setTypeface(face_bold);
            noOrder.setTypeface(face_bold);
        }

    }

    private void orderQuery(){
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.order_query_id+reservation.getRes_id()+"&is_fast=0",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                JSONArray orderArrayJson =  jsonObject.getJSONArray("orders");

                                for(int i = 0 ; i<orderArrayJson.length(); i++){
                                    JSONObject orderJson = orderArrayJson.getJSONObject(i);
                                    foodOrders.add(new FoodOrder(orderJson.getInt("id"),
                                            orderJson.getInt("res_id"),
                                            orderJson.getInt("food_id"),
                                            orderJson.getString("food_name"),
                                            orderJson.getInt("quantity"),
                                            orderJson.getInt("price"),
                                            " ",
                                            ""));

                                }

                                progressBar.setVisibility(View.GONE);
                                orderHistoryAdapter.notifyDataSetChanged();

                            }else{
                                progressBar.setVisibility(View.GONE);
                                if (getActivity()!=null)
                                    Toast.makeText(getContext(), getActivity().getString(R.string.error), Toast.LENGTH_SHORT).show();

                            }
                        }catch (JSONException e){
                            if (getActivity()!=null)
                                Toast.makeText(getContext(), getActivity().getString(R.string.error), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(View.GONE);
                        if (getActivity()!=null)
                            Toast.makeText(getContext(), getActivity().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                    }
                }

        );


        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }


    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        orderHistoryAdapter = new OrderHistoryAdapter(getContext(), foodOrders,this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(orderHistoryAdapter);
    }


    private Reservation getReservationFromBundle(){

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.reservation_bundle));
        }else{
            return null;
        }
    }


    private void setUpView(){
        if (reservation!=null){
            //date
            int year = reservation.getYear();
            int month = reservation.getMonth();
            int day = reservation.getDay();

            String lang = Locale.getDefault().getLanguage();
            String dateString;
            if(lang.equals("ar")){
                NumberFormat nf = NumberFormat.getInstance(new Locale("ar","LB")); //or "nb","No" - for Norway
                String sYear = nf.format(year);
                String yearString = sYear.replace("٬", "");
                String monthString = nf.format(month);
                String dayString = nf.format(day);
                dateString = "التاريخ: "+yearString+"/"+monthString+"/"+dayString;
            }else {
                dateString = "Date: "+year+"/"+month+"/"+day;
            }
            date.setText(dateString);

            //hour
            double dHour = reservation.getStartHour();
            String sHour = CommonReservation.changeHourFormat(getContext(),dHour);
            hour.setText(sHour);

            String table = String.format(getString(R.string.order_his_table_number),reservation.getTable_id());
            table_number.setText(table);

            String forNumber = String.format(getString(R.string.order_his_for_number),reservation.getChairNumber());
            reservation_for.setText(forNumber);

            //totalTxt
            String total = String.format(getString(R.string.order_his_f_totalTxt),reservation.getTotal());
            totalTxt.setText(total);

            //name and phone number
            if (user!=null){
                name.setText(user.getUserName());
            }

        }

    }


    //adapter listener
    @Override
    public void handle(int position) {
        submit.setVisibility(View.VISIBLE);
    }

    //cancel reservation
    private void cancelReservation(){
        final double diff = reservation.getStartHour() - Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (diff < 2){

                    showCancelDialog();
                }else {
                    cancelAlert();
                }
            }
        });
    }
    private void cancelAlert(){
        if (getContext()!=null){


        final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setMessage(R.string.order_his_cancel_dialog_message);
        alert.setNegativeButton(R.string.order_his_cancel_dialog_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setPositiveButton(R.string.order_his_cancel_dialog_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.cancelReservation+reservation.getRes_id()+"&status="+2,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);

                                    if(!jsonObject.getBoolean("error")){
                                        if (getActivity()!=null) {
                                            Toast.makeText(getContext(), getActivity().getString(R.string.order_his_cancel_dialog_done), Toast.LENGTH_SHORT).show();
                                            getActivity().onBackPressed();
                                        }

                                        sendNotification(user.getUserId(),reservation.getRes_id());
                                    }else{
                                        if (getActivity()!=null)
                                            Toast.makeText(getContext(), getActivity().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                                    }
                                }catch (JSONException e){
                                    if (getActivity()!=null)
                                        Toast.makeText(getContext(), getActivity().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                if (getActivity()!=null)
                                    Toast.makeText(getContext(), getActivity().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                            }
                        }
                );

                VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
            }
        });

        AlertDialog dialog = alert.create();
        dialog.show();
        }
    }

    private void showCancelDialog(){
        if (getContext()!=null) {
            final AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            View alertLayout = View.inflate(getContext(),R.layout.can_not_cancel_dialog, null);
            TextView number = alertLayout.findViewById(R.id.number1);

            TextView header = alertLayout.findViewById(R.id.dialog_header);
            TextView first = alertLayout.findViewById(R.id.first_b);
            TextView second = alertLayout.findViewById(R.id.second_b);

            Typeface regular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Regular.ttf");
            Typeface extraBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Bold.ttf");

            header.setTypeface(extraBold);
            first.setTypeface(regular);
            second.setTypeface(regular);

            Linkify.addLinks(number, Linkify.PHONE_NUMBERS);
            number.setLinkTextColor(Color.parseColor("#3498db"));


            // this is set the view from XML inside AlertDialog
            alert.setView(alertLayout);
            final AlertDialog dialog = alert.create();
            dialog.show();


        }


    }


    //edit order
    private void submitOrder(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrder();
            }
        });
    }
    private void updateOrder(){
        Gson gson=new Gson();
        ArrayList<FoodOrder> newFoodOrders = orderHistoryAdapter.getFoodOrders();
        final int total = getTotal(newFoodOrders);
        final String newDataArray=gson.toJson(newFoodOrders);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.edit_order,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        if (getActivity()!=null) {
                            Toast.makeText(getContext(), getActivity().getString(R.string.order_his_f_update_done), Toast.LENGTH_SHORT).show();
                            totalTxt.setText(String.format(getActivity().getString(R.string.order_his_f_totalTxt), total));
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (getActivity()!=null)
                            Toast.makeText(getContext(), getActivity().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                    }
                }
        ){

            @Override
            protected Map<String, String> getParams()  {
                Map<String,String> param=new HashMap<>();
                param.put("array",newDataArray);
                param.put("total", String.valueOf(total));
                param.put("res_id",String.valueOf(reservation.getRes_id()));// array is key which we will use on server side

                return param;
            }
        };//end of string Request

        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);
    }

    private int getTotal(ArrayList<FoodOrder> foodOrders){
        int total = 0;
        for (int i = 0 ; i<foodOrders.size();i++){
            total+= foodOrders.get(i).getPrice()*foodOrders.get(i).getQuantity();
        }
        return total;
    }



    private void sendNotification(int user_id, int res_id) {

        JSONObject data = new JSONObject();
        JSONObject notification_data = new JSONObject();
        String url = "https://fcm.googleapis.com/fcm/send";
        try {
            //Populate the request parameters
            data.put("title", "Bohemea Art Cafe");
            data.put("message", user.getUserName()+" Canceled His Reservation");
            data.put("android_channel_id", "cancel_reservation_channel");
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

    //fragment restore in landscape mod
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (reservation!=null) {
            Bundle args = new Bundle();
            args.putParcelable(getString(R.string.reservation_bundle), reservation);
            outState.putBundle("args", args);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            Bundle bundle = savedInstanceState.getBundle("args");
            if (bundle != null) {
                reservation = bundle.getParcelable(getString(R.string.reservation_bundle));
                setUpView();
            }
        }
        super.onActivityCreated(savedInstanceState);
    }
}