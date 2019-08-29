package com.emargystudio.bohemea.viewHolders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.emargystudio.bohemea.model.FoodItem;
import com.emargystudio.bohemea.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;


public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>{

    private List<FoodItem> foodItems;
    private View.OnClickListener mOnItemClickListener;
    private Context context;
    private String lang;

    public FoodItemAdapter(List<FoodItem> foodItems, Context context) {
        this.foodItems = foodItems;
        this.context = context;
        lang = Locale.getDefault().getLanguage();
    }

    @NonNull
    @Override
    public FoodItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_menu_item, parent, false);

        return new FoodItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodItemViewHolder holder, int position) {

        FoodItem foodItem = foodItems.get(position);
        Picasso.get().load(foodItem.getImage_url()).into(holder.categoryImage);
        holder.category_name.setText(foodItem.getName());

    }

    @Override
    public int getItemCount() {
        return foodItems.size();
    }


    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    class FoodItemViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImage;
        TextView category_name ;


        FoodItemViewHolder(@NonNull View itemView) {
            super(itemView);
          category_name = itemView.findViewById(R.id.item_name);
          categoryImage = itemView.findViewById(R.id.item_image);
            Typeface face_ExtraBold;

            if (lang.equals("ar")){
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Bold.ttf");



            }else{
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Akrobat-Bold.otf");


            }

            category_name.setTypeface(face_ExtraBold);
            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);

        }

    }
}