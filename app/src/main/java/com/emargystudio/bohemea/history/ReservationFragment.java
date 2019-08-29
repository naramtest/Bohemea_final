package com.emargystudio.bohemea.history;


import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.emargystudio.bohemea.makeReservation.ReservationActivity;
import com.emargystudio.bohemea.model.Reservation;
import com.emargystudio.bohemea.model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.viewHolders.ResHistoryAdapter;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


public class ReservationFragment extends Fragment {

    private User user;
    private ArrayList<Reservation> reservations = new ArrayList<>();
    private ResHistoryAdapter resHistoryAdapter;

    private String lang = Locale.getDefault().getLanguage();


    private ProgressBar progressBar;
    private RelativeLayout emptyView;
    private RecyclerView recyclerView;


    public ReservationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reservation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        SharedPreferenceManger sharedPreferenceManger = SharedPreferenceManger.getInstance(getContext());
        user = sharedPreferenceManger.getUserData();


        initViews(view);
        initRecyclerView();
        reservationQuery();



    }

    private void initViews(@NonNull View view) {
        progressBar = view.findViewById(R.id.progress_bar);
        recyclerView = view.findViewById(R.id.res_history_rv);
        emptyView = view.findViewById(R.id.empty_history_layout);
        TextView emptyTxt = view.findViewById(R.id.empty_history_text);
        Button emptyButton = view.findViewById(R.id.empty_history_btn);
        TextView header1 = view.findViewById(R.id.text_header1);
        TextView header2 = view.findViewById(R.id.text_header2);

        if (getContext()!=null) {
            Typeface face_Regular;
            Typeface face_Bold ;
            Typeface face_Light;
            if (lang.equals("ar")){
                face_Regular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Regular.ttf");
                face_Bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Bold.ttf");
                face_Light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Cairo-Light.ttf");

            }else {
                face_Regular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Akrobat-Regular.otf");
                face_Bold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Kabrio-Bold.ttf");
                face_Light = Typeface.createFromAsset(getContext().getAssets(), "fonts/Kabrio-Light.ttf");
            }

            header1.setTypeface(face_Bold);
            header2.setTypeface(face_Light);
            emptyTxt.setTypeface(face_Regular);
        }
        emptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), ReservationActivity.class));
            }
        });
    }


    private void reservationQuery(){
        if (resHistoryAdapter.getItemCount() == 0){
            progressBar.setVisibility(View.VISIBLE);
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.reservation_query_id+user.getUserId(),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                JSONArray jsonArrayReservation =  jsonObject.getJSONArray("reservations");

                                if (reservations != null){
                                    reservations.clear();
                                }
                                for(int i = 0 ; i<jsonArrayReservation.length(); i++){
                                    JSONObject jsonObjectSingleRes = jsonArrayReservation.getJSONObject(i);
                                    if (jsonObjectSingleRes.getInt("status") !=2 && jsonObjectSingleRes.getInt("status") !=3) {
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

                               if (reservations.size() == 0){
                                   emptyView.setVisibility(View.VISIBLE);
                                   recyclerView.setVisibility(View.GONE);
                               }else {
                                   emptyView.setVisibility(View.GONE);
                                   recyclerView.setVisibility(View.VISIBLE);
                               }
                                resHistoryAdapter.notifyDataSetChanged();
                                progressBar.setVisibility(View.GONE);


                            }else{
                                progressBar.setVisibility(View.GONE);
                                if (getActivity()!=null)
                                    Toast.makeText(getContext(), getActivity().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();

                            }
                        }catch (JSONException e){
                            progressBar.setVisibility(View.GONE);
                            e.printStackTrace();
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

        resHistoryAdapter = new ResHistoryAdapter(getContext(), reservations);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(resHistoryAdapter);

    }
}
