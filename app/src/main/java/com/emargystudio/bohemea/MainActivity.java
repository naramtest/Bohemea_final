package com.emargystudio.bohemea;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;

import android.view.View;
import android.widget.Button;




import com.emargystudio.bohemea.Cart.CartActivity;
import com.emargystudio.bohemea.Cinema.CinemaActivity;
import com.emargystudio.bohemea.Login.LoginActivity;
import com.emargystudio.bohemea.MakeReservation.ReservationActivity;
import com.emargystudio.bohemea.Menu.MenuActivity;

import com.emargystudio.bohemea.Profile.ProfileActivity;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;




public class MainActivity extends AppCompatActivity {



    SharedPreferenceManger sharedPreferenceManger;
    Button  makeReservationBtn;



    SpaceNavigationView spaceNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        spaceNavigationView =  findViewById(R.id.space);
        makeReservationBtn = findViewById(R.id.make_reservation_btn);


        Typeface face = Typeface.createFromAsset(MainActivity.this.getAssets(),"fonts/Kabrio_Regular.ttf");
        makeReservationBtn.setTypeface(face);

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




    }


    //setup bottom navigation
    private void bottomNavigationInit(Bundle savedInstanceState , final Activity activityA) {
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("CINEMA", R.drawable.ic_clapperboard));
        spaceNavigationView.addSpaceItem(new SpaceItem("CART", R.drawable.ic_shopping_cart));
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
                        startActivity(new Intent(activityA, CinemaActivity.class));
                        break;

                    case 2:
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


    //its part of bottom navigation setup
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(0);
    }

}
