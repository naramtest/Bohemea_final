package com.emargystudio.bohemea;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emargystudio.bohemea.Cart.CartActivity;

public class WhiteActivity extends AppCompatActivity {

    TextView blockedTxt;
    Button   callUsBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white);

        blockedTxt = findViewById(R.id.block_text);
        callUsBtn   = findViewById(R.id.call_us_btn);


        Typeface face_Regular = Typeface.createFromAsset(WhiteActivity.this.getAssets(), "fonts/Akrobat-Regular.otf");
        blockedTxt.setTypeface(face_Regular);
        callUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "0933557668";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

    }
}
