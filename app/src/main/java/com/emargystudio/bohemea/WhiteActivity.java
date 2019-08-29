package com.emargystudio.bohemea;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class WhiteActivity extends AppCompatActivity {

    TextView blockedTxt , updateText;
    Button   callUsBtn , updateBtn;
    RelativeLayout block , update;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_white);

        blockedTxt = findViewById(R.id.block_text);
        callUsBtn   = findViewById(R.id.call_us_btn);
        updateBtn   = findViewById(R.id.update_btn);
        updateText  = findViewById(R.id.update_text);
        block = findViewById(R.id.block_layout);
        update = findViewById(R.id.update_layout);

        if (!getIntent().getStringExtra("type").isEmpty()){
            String type = getIntent().getStringExtra("type");
            if (type.equals("block")){
                block.setVisibility(View.VISIBLE);
                update.setVisibility(View.GONE);
            }else if (type.equals("update")){
                block.setVisibility(View.GONE);
                update.setVisibility(View.VISIBLE);
            }
        }


        Typeface face_Regular = Typeface.createFromAsset(WhiteActivity.this.getAssets(), "fonts/Cairo-Regular.ttf");
        blockedTxt.setTypeface(face_Regular);
        updateText.setTypeface(face_Regular);

        callUsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phone = "0933557668";
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                startActivity(intent);
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.emargystudio.emar"));
                startActivity(browserIntent);
            }
        });

    }
}
