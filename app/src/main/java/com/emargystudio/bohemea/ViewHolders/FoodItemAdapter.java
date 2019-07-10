package com.emargystudio.bohemea.ViewHolders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.emargystudio.bohemea.Model.FoodItem;
import com.emargystudio.bohemea.R;
import com.squareup.picasso.Picasso;

import java.util.List;


public class FoodItemAdapter extends RecyclerView.Adapter<FoodItemAdapter.FoodItemViewHolder>{

    private List<FoodItem> foodItems;
    private View.OnClickListener mOnItemClickListener;
    private Context context;

    public FoodItemAdapter(List<FoodItem> foodItems, Context context) {
        this.foodItems = foodItems;
        this.context = context;
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
          Typeface face = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-Bold.otf");
          category_name.setTypeface(face);
            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);

        }

    }
}