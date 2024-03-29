package com.emargystudio.bohemea.menu;


import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.emargystudio.bohemea.localDataBases.AppDatabase;
import com.emargystudio.bohemea.localDataBases.AppExecutors;
import com.emargystudio.bohemea.model.FoodCategory;
import com.emargystudio.bohemea.model.FoodItem;
import com.emargystudio.bohemea.model.FoodOrder;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.viewHolders.FoodItemAdapter;
import com.emargystudio.bohemea.helperClasses.Common;
import com.emargystudio.bohemea.helperClasses.URLS;
import com.emargystudio.bohemea.helperClasses.VolleyHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;


import static com.facebook.FacebookSdk.getApplicationContext;


public class FoodItemFragment extends Fragment {



    private TextView priceTxt , nameTxt , descriptionTxt , quantityTxt,category_name;
    private ImageView food_image;
    private Button add_btn , cancel_btn;
    private ElegantNumberButton numberButton;
    private int itemCount = 0;

    private String lang = Locale.getDefault().getLanguage();



    private ArrayList<FoodItem> foodItems;
    private FoodItemAdapter foodItemAdapter;


    private AppDatabase mDb;

    private View.OnClickListener onItemClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) view.getTag();
            int position = viewHolder.getAdapterPosition();
            // viewHolder.getItemId();
            // viewHolder.getItemViewType();

            showDialog(foodItems.get(position));

        }
    };


    public FoodItemFragment() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_food_item, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mDb = AppDatabase.getInstance(getApplicationContext());

        RecyclerView recyclerView = view.findViewById(R.id.food_category_rv);
        category_name = view.findViewById(R.id.category_name);
        ImageView category_image = view.findViewById(R.id.category_image);

        if (getActivity()!=null) {
            Typeface face_Bold;

            if (lang.equals("ar")) {
                face_Bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Cairo-Bold.ttf");
            } else {
                face_Bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Akrobat-Bold.otf");
            }
            category_name.setTypeface(face_Bold);
        }




        foodItems = new ArrayList<>();
        foodItemAdapter = new FoodItemAdapter(foodItems,getContext());
        String lang = Locale.getDefault().getLanguage();


        try {
            FoodCategory foodCategory = getCategoryFromBundle();
            if (foodCategory !=null) {

                Picasso.get().load(foodCategory.getImage_url()).into(category_image);
                if(lang.equals("ar")){
                    category_name.setText(foodCategory.getAr_name());

                    foodQuery_ar(foodCategory.getId());
                }else if (lang.equals("en")){
                    String lower = foodCategory.getName().toLowerCase();
                    String upperString = lower.substring(0,1).toUpperCase() + lower.substring(1);
                    category_name.setText(upperString);
                    foodQuery(foodCategory.getId());
                }



            }
        }catch (NullPointerException e){
            if (getContext()!=null){
                Toast.makeText(getContext(), getContext().getString(R.string.internet_off), Toast.LENGTH_SHORT).show();
            }
        }



        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(foodItemAdapter);
        foodItemAdapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setHasFixedSize(true);


    }

    private void foodQuery(int category_id){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.food_menu_query+category_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                JSONArray jsonObjectCategory =  jsonObject.getJSONArray("food_menu");


                                for(int i = 0 ; i<jsonObjectCategory.length(); i++){
                                    JSONObject jfood = jsonObjectCategory.getJSONObject(i);


                                    foodItems.add(new FoodItem(jfood.getInt("id"),
                                            jfood.getInt("category_id"),
                                            jfood.getString("name"),
                                            jfood.getString("image_url"),
                                            jfood.getString("description"),
                                            jfood.getInt("price")));
                                }

                                foodItemAdapter.notifyDataSetChanged();
                            }else{
                                Toast.makeText(getContext(),getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                    }
                }
        );
        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);

    }

    private void foodQuery_ar(int category_id){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URLS.food_menu_query_ar+category_id,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            if(!jsonObject.getBoolean("error")){

                                JSONArray jsonObjectCategory =  jsonObject.getJSONArray("food_menu");


                                for(int i = 0 ; i<jsonObjectCategory.length(); i++){
                                    JSONObject jfood = jsonObjectCategory.getJSONObject(i);


                                    foodItems.add(new FoodItem(jfood.getInt("id"),
                                            jfood.getInt("category_id"),
                                            jfood.getString("ar_name"),
                                            jfood.getString("image_url"),
                                            jfood.getString("ar_description"),
                                            jfood.getInt("price")));

                                }

                                foodItemAdapter.notifyDataSetChanged();
                            }else{
                                Toast.makeText(getContext(),getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                            }

                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(),getString(R.string.internet_off),Toast.LENGTH_LONG).show();
                    }
                }
        );
        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 20000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 20000;
            }

            @Override
            public void retry(VolleyError error) {

            }
        });
        VolleyHandler.getInstance(getContext()).addRequetToQueue(stringRequest);

    }

    private FoodCategory getCategoryFromBundle(){

        Bundle bundle = this.getArguments();
        if(bundle != null){
            return bundle.getParcelable(getString(R.string.food_catrgory_model));
        }else{
            return null;
        }
    }


    private void showDialog(final FoodItem foodItem){

        if (getActivity()!=null) {
            final Dialog dialog = new Dialog(getActivity());
            if (dialog.getWindow()!=null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            dialog.setContentView(R.layout.food_item_dialog);


            initDialogViews(dialog);
            changeTxtViewFonts();


            Picasso.get().load(foodItem.getImage_url()).into(food_image);
            priceTxt.setText(String.format(getString(R.string.foodDetailActivity_food_price), foodItem.getPrice()));
            nameTxt.setText(foodItem.getName());
            descriptionTxt.setText(foodItem.getDescription());
            descriptionTxt.setMovementMethod(new ScrollingMovementMethod());


            numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
                @Override
                public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {

                    int newPrice = foodItem.getPrice() * newValue;
                    priceTxt.setText(String.format(getString(R.string.foodDetailActivity_food_price), newPrice));

                }
            });

            cancel_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            add_btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addToCart(foodItem);
                    itemCount += 1;
                    if (itemCount == 1) {
                        ((MenuActivity) getActivity()).spaceNavigationView.showBadgeAtIndex(1, itemCount, Color.RED);
                    } else {
                        ((MenuActivity) getActivity()).spaceNavigationView.changeBadgeTextAtIndex(1, itemCount);
                    }

                    dialog.dismiss();
                }
            });

            dialog.show();

        }

    }

    private void changeTxtViewFonts() {
        if (getActivity()!=null) {
            Typeface face_Bold;
            Typeface akrobat_Regular;

            if (lang.equals("ar")) {

                akrobat_Regular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Cairo-Regular.ttf");
                face_Bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Cairo-Bold.ttf");
                quantityTxt.setTextSize(16);

            } else {
                face_Bold = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Akrobat-Bold.otf");
                akrobat_Regular = Typeface.createFromAsset(getActivity().getAssets(), "fonts/Akrobat-Regular.otf");

            }

            quantityTxt.setTypeface(face_Bold);
            priceTxt.setTypeface(akrobat_Regular);
            nameTxt.setTypeface(face_Bold);
            add_btn.setTypeface(face_Bold);
            cancel_btn.setTypeface(akrobat_Regular);
            descriptionTxt.setTypeface(akrobat_Regular);
            category_name.setTypeface(face_Bold);
        }
    }

    private void initDialogViews(Dialog dialog) {
        priceTxt = dialog.findViewById(R.id.price_txt);
        nameTxt   = dialog.findViewById(R.id.food_name);
        descriptionTxt = dialog.findViewById(R.id.food_description);
        quantityTxt    = dialog.findViewById(R.id.quantity);
        food_image = dialog.findViewById(R.id.food_image);
        add_btn    = dialog.findViewById(R.id.add_toCart_btn);
        cancel_btn = dialog.findViewById(R.id.cancel_btn);
        numberButton = dialog.findViewById(R.id.number_button);



    }

    private void addToCart(FoodItem foodItem) {
        int food_id = foodItem.getId();

        int quantity = Integer.parseInt(numberButton.getNumber());
        int price = foodItem.getPrice();
        Common.total = (Common.total)+(price*quantity);
        int res_id = Common.res_id;
        String food_image = foodItem.getImage_url();
        String food_name = foodItem.getName();


        final FoodOrder foodOrder = new FoodOrder(res_id,food_id,food_name,quantity,price,food_image,"");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {

                mDb.orderDao().insertFood(foodOrder);
                if (!Common.isOrdered ){
                    Common.isOrdered = true;

                }
            }

        });



        Toast.makeText(getContext(), getString(R.string.f_foodItem_add), Toast.LENGTH_SHORT).show();

    }
}
