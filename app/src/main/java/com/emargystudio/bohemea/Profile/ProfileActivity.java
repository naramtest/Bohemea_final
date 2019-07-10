package com.emargystudio.bohemea.Profile;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.emargystudio.bohemea.Cart.CartActivity;
import com.emargystudio.bohemea.Cinema.CinemaActivity;
import com.emargystudio.bohemea.Login.LoginActivity;
import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.Menu.MenuActivity;
import com.emargystudio.bohemea.Model.User;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.SharedPreferenceManger;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {


    SpaceNavigationView spaceNavigationView;
    TextView header1, header2,user_name,preference,logout,helpCenter;
    CircleImageView user_image;
    LinearLayout preference_container, logout_container, help_container ;

    User user;
    SharedPreferenceManger sharedPreferenceManger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sharedPreferenceManger = SharedPreferenceManger.getInstance(ProfileActivity.this);
        user = sharedPreferenceManger.getUserData();
        initView();



        spaceNavigationView =  findViewById(R.id.space);
        bottomNavigationInit(savedInstanceState ,ProfileActivity.this);
        logout_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sharedPreferenceManger.logUserOut();
                sharedPreferenceManger.logUserDeleteTokens();
                Common.isNewToken =false;
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();

            }
        });

        preference_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivity(new Intent(ProfileActivity.this,PreferencesActivity.class));
            }
        });
    }

    private void initView() {

        user_image = findViewById(R.id.user_image);
        user_name  = findViewById(R.id.user_name);
        header1    = findViewById(R.id.text_header1);
        header2    = findViewById(R.id.text_header2);
        preference = findViewById(R.id.preferences_txt);
        logout     = findViewById(R.id.logout_txt);
        helpCenter = findViewById(R.id.help_txt);
        preference_container = findViewById(R.id.preferences_container);
        logout_container = findViewById(R.id.log_out_container);
        help_container       = findViewById(R.id.help_container);

        Typeface face_Bold = Typeface.createFromAsset(ProfileActivity.this.getAssets(),"fonts/Kabrio-Bold.ttf");
        Typeface face_Light = Typeface.createFromAsset(ProfileActivity.this.getAssets(),"fonts/Kabrio-Light.ttf");
        header1.setTypeface(face_Bold);
        user_name.setTypeface(face_Bold);
        header2.setTypeface(face_Light);
        preference.setTypeface(face_Light);
        helpCenter.setTypeface(face_Light);
        logout.setTypeface(face_Light);

        Picasso.get().load(user.getUserPhoto()).into(user_image);
        user_name.setText(user.getUserName());



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
        spaceNavigationView.changeCurrentItem(3);
        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                spaceNavigationView.setActiveSpaceItemColor(ContextCompat.getColor(ProfileActivity.this,R.color.inactive_color));
                spaceNavigationView.changeCurrentItem(-1);
                startActivity(new Intent(activityA, MenuActivity.class));
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {

                switch (itemIndex){
                    case 0:
                        startActivity(new Intent(activityA, MainActivity.class));
                        break;

                    case 1:
                        startActivity(new Intent(activityA,CinemaActivity.class));
                        break;

                    case 2:
                        startActivity(new Intent(activityA, CartActivity.class));
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

    @Override
    protected void onResume() {
        super.onResume();
        spaceNavigationView.changeCurrentItem(3);
    }
}
