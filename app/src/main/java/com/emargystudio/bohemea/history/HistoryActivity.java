package com.emargystudio.bohemea.history;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;


import com.emargystudio.bohemea.cart.CartActivity;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.makeReservation.ReservationActivity;
import com.emargystudio.bohemea.menu.MenuActivity;
import com.emargystudio.bohemea.profile.ProfileActivity;
import com.emargystudio.bohemea.R;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;


public class HistoryActivity extends AppCompatActivity {

    SpaceNavigationView spaceNavigationView;
    Fragment ReservationFragment;
    Fragment OrderFragment;

    //intent var
    String res_id,user_id,result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cinema_activity);
        spaceNavigationView =  findViewById(R.id.space);
        bottomNavigationInit(savedInstanceState , HistoryActivity.this);



        if (getIntent()!=null){
            res_id = getIntent().getStringExtra("res_id");
            user_id = getIntent().getStringExtra("user_id");
            result  = getIntent().getStringExtra("result");

        }

        if (result!=null && result.equals("3")){
            reservationDeclinedDialog();
        }else {

            if (savedInstanceState != null) {

                ReservationFragment = getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName");
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, ReservationFragment);
                ft.commit();
            } else {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, new ReservationFragment(), "Reservation");
                ft.commit();
            }
        }

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
        spaceNavigationView.changeCurrentItem(2);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                spaceNavigationView.setActiveSpaceItemColor(ContextCompat.getColor(HistoryActivity.this,R.color.inactive_color));
                spaceNavigationView.changeCurrentItem(-1);
                startActivity(new Intent(activityA, MenuActivity.class));

            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {

                switch (itemIndex){
                    case 0:
                        startActivity(new Intent(activityA,MainActivity.class));
                        break;


                    case 1:
                        startActivity(new Intent(activityA, CartActivity.class));
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
        ReservationFragment = getSupportFragmentManager().findFragmentByTag("Reservation");
        OrderFragment = getSupportFragmentManager().findFragmentByTag("Order");
        if (ReservationFragment != null && ReservationFragment.isVisible()) {
            getSupportFragmentManager().putFragment(outState, "myFragmentName", ReservationFragment);
        }else if (OrderFragment != null && OrderFragment.isVisible()){
            getSupportFragmentManager().putFragment(outState, "myFragmentName", OrderFragment);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(2);
    }

    public void reservationDeclinedDialog(){
        final AlertDialog.Builder alert = new AlertDialog.Builder(HistoryActivity.this);
        LayoutInflater li = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("InflateParams") View alertLayout = li.inflate(R.layout.reservation_dialog_decline, null);
        TextView number = alertLayout.findViewById(R.id.number1);
        TextView goToReservationBtn = alertLayout.findViewById(R.id.reservation_btn);


        Linkify.addLinks(number  , Linkify.PHONE_NUMBERS);
        number.setLinkTextColor(Color.parseColor("#3498db"));

        goToReservationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HistoryActivity.this, ReservationActivity.class));
            }
        });


        // this is set the view from XML inside AlertDialog
        alert.setView(alertLayout);
        final AlertDialog dialog = alert.create();
        dialog.show();





    }


}
