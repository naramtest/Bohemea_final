package com.emargystudio.bohemea.History;


import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
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
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.Model.FoodOrder;
import com.emargystudio.bohemea.Model.Reservation;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.ViewHolders.OrderHistoryAdapter;
import com.emargystudio.bohemea.helperClasses.CommonReservation;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class OrderFragment extends Fragment implements OrderHistoryAdapter.EventHandler {


    private OrderHistoryAdapter orderHistoryAdapter;
    private ArrayList<FoodOrder> foodOrders = new ArrayList<>();
    private ArrayList<FoodOrder> newFoodOrders = new ArrayList<>();
    private Reservation reservation;
    private User user;
    private SharedPreferenceManger sharedPreferenceManger;


    //widgets
    private TextView date,hour,table_number ,reservation_for,name, totalTxt , total_tag_txt , name_tag_txt;
    private RecyclerView recyclerView;
    private TextView noOrder;

    private ProgressBar progressBar;
    private Button submit,cancel;


    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());
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
        name_tag_txt = view.findViewById(R.id.name_tag_txt);
        total_tag_txt = view.findViewById(R.id.total_tag_txt);
        TextView summaryTxt = view.findViewById(R.id.summaryTxt);
        if (getActivity()!=null){
            Typeface face = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Akrobat-ExtraBold.otf");
            Typeface face_bold = Typeface.createFromAsset(getActivity().getAssets(),"fonts/Akrobat-Bold.otf");
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
        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.order_query_id+reservation.getRes_id(),
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
            String dateString = "Date: "+year+"/"+month+"/"+day;
            date.setText(dateString);

            //hour
            double dHour = reservation.getStartHour();
            String sHour = CommonReservation.changeHourFormat(dHour);
            hour.setText(sHour);

            //table and chair number
            if (reservation.getTable_id() == -1){
                table_number.setText(String.valueOf(reservation.getMovie_name()));
            }else {
                table_number.setText("Table Number: "+reservation.getTable_id());
            }
            reservation_for.setText("For: "+reservation.getChairNumber());

            //totalTxt
            totalTxt.setText(reservation.getTotal()+" S.P");

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
    public void cancelReservation(){
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelAlert();
            }
        });
    }
    public void cancelAlert(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
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


    //edit order
    public void submitOrder(){
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateOrder();
            }
        });
    }
    public void updateOrder(){
        Gson gson=new Gson();
        newFoodOrders = orderHistoryAdapter.getFoodOrders();
        final int total = getTotal(newFoodOrders);
        final String newDataArray=gson.toJson(newFoodOrders);
        Log.d("ORder", "updateOrder: "+newDataArray);
        Log.d("ORder", "total: "+total);
        Log.d("ORder", "res_id: "+reservation.getRes_id());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URLS.edit_order,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {


                        Log.d("ORder", "onResponse: "+response);
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

    public int getTotal(ArrayList<FoodOrder> foodOrders){
        int total = 0;
        for (int i = 0 ; i<foodOrders.size();i++){
            total+= foodOrders.get(i).getPrice()*foodOrders.get(i).getQuantity();
        }
        return total;
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