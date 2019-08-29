package com.emargystudio.bohemea.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;



import com.emargystudio.bohemea.cart.CartActivity;
import com.emargystudio.bohemea.history.HistoryActivity;
import com.emargystudio.bohemea.MainActivity;


import com.emargystudio.bohemea.profile.ProfileActivity;
import com.emargystudio.bohemea.R;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;



public class MenuActivity extends AppCompatActivity {

    public SpaceNavigationView spaceNavigationView;
    Fragment fragment;
    Fragment foodItemFragment;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        spaceNavigationView =  findViewById(R.id.space);
        bottomNavigationInit(savedInstanceState ,MenuActivity.this);

        if(savedInstanceState != null && getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName")!= null ) {
            //Restore the fragment's instance
            fragment = getSupportFragmentManager().getFragment(savedInstanceState, "myFragmentName");
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.your_placeholder, fragment);
            ft.commit();
        }else{
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            Fragment categoryFragment = new FoodCategoriesFragment();
            ft.replace(R.id.your_placeholder, categoryFragment, getString(R.string.foodCategoriesFragment_name));
            ft.addToBackStack(getString(R.string.foodCategoriesFragment_name));
            ft.commit();

        }


    }


    //setup bottom navigation (this the center button FAB)
    private void bottomNavigationInit(Bundle savedInstanceState , final Activity activityA) {
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.addSpaceItem(new SpaceItem("HOME", R.drawable.ic_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("CART", R.drawable.ic_shopping_cart));
        spaceNavigationView.addSpaceItem(new SpaceItem("HISTORY", R.drawable.ic_history));
        spaceNavigationView.addSpaceItem(new SpaceItem("PROFILE", R.drawable.ic_man_user));
        spaceNavigationView.showIconOnly();
        spaceNavigationView.changeCurrentItem(-1);
        spaceNavigationView.setCentreButtonIconColorFilterEnabled(false);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                spaceNavigationView.setActiveSpaceItemColor(ContextCompat.getColor(MenuActivity.this,R.color.inactive_color));
                spaceNavigationView.changeCurrentItem(-1);
                foodItemFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.foodItemFragment_name));
                if (foodItemFragment != null && foodItemFragment.isVisible()){
                    getSupportFragmentManager().popBackStack();
                }
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                switch (itemIndex){
                    case 0:
                        startActivity(new Intent(activityA,MainActivity.class));
                        break;

                    case 2:
                        startActivity(new Intent(activityA, HistoryActivity.class));
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

        foodItemFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.foodItemFragment_name));

       if (foodItemFragment != null && foodItemFragment.isVisible()){
           getSupportFragmentManager().putFragment(outState, "myFragmentName", foodItemFragment);
       }
    }

    @Override
    public void onBackPressed() {

        int count = getSupportFragmentManager().getBackStackEntryCount();
        Fragment foodCategoriesFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.foodCategoriesFragment_name));
        foodItemFragment = getSupportFragmentManager().findFragmentByTag(getString(R.string.foodItemFragment_name));

        if (foodCategoriesFragment!= null && foodCategoriesFragment.isVisible()){
            startActivity(new Intent(MenuActivity.this,MainActivity.class));
            finish();
        }else if(foodItemFragment != null && foodItemFragment.isVisible()){

            getSupportFragmentManager().popBackStack();
        }else if (count == 0){
            startActivity(new Intent(MenuActivity.this,MainActivity.class));
            finish();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(-1);
    }
}
