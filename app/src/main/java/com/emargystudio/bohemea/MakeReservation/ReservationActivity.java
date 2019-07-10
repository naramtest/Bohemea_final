package com.emargystudio.bohemea.MakeReservation;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.emargystudio.bohemea.R;

public class ReservationActivity extends AppCompatActivity {

    Fragment dataFragment;
    Fragment fragment;
    Fragment tableFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        if (savedInstanceState != null) {
            //Restore the dataFragment's instance
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName");
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment);
            ft.commit();
        }else {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment dataFragment = new DataFragment();
            ft.replace(R.id.your_placeholder,dataFragment,"Data");
            ft.commit();
        }

    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Save the dataFragment's instance

        dataFragment = getSupportFragmentManager().findFragmentByTag("Data");
        tableFragment = getSupportFragmentManager().findFragmentByTag("table");

        if (dataFragment != null && dataFragment.isVisible()) {
            getSupportFragmentManager().putFragment(outState, "myFragmentName", dataFragment);
        }else if (tableFragment != null && tableFragment.isVisible()){
            getSupportFragmentManager().putFragment(outState, "myFragmentName", tableFragment);
        }
    }

}
