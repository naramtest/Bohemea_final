package com.emargystudio.bohemea.ViewHolders;

import android.content.Context;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import com.emargystudio.bohemea.MainActivity;
import com.emargystudio.bohemea.Model.FoodCategory;
import com.emargystudio.bohemea.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;


public class FoodCategoryAdapter  extends RecyclerView.Adapter<FoodCategoryAdapter.FoodCategoryViewHolder>{

    List<FoodCategory> foodCategories;
    private View.OnClickListener mOnItemClickListener;
    private Context context;

    public FoodCategoryAdapter(List<FoodCategory> foodCategories, Context context) {
        this.foodCategories = foodCategories;
        this.context = context;
    }

    @NonNull
    @Override
    public FoodCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.food_category_item, parent, false);

        return new FoodCategoryViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FoodCategoryViewHolder holder, int position) {

        FoodCategory foodCategory = foodCategories.get(position);
        Picasso.get().load(foodCategory.getImage_url()).into(holder.categoryImage);
        holder.item_count.setText(foodCategory.getItem_count()+" items");
        holder.category_name.setText(foodCategory.getName());

    }

    @Override
    public int getItemCount() {
        return foodCategories.size();
    }


    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    class FoodCategoryViewHolder extends RecyclerView.ViewHolder {

        ImageView categoryImage;
        TextView category_name , item_count;
        //AppCompatImageButton nextButton;


        FoodCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
          category_name = itemView.findViewById(R.id.category_name);
          categoryImage = itemView.findViewById(R.id.category_image);
          item_count = itemView.findViewById(R.id.item_count);
          Typeface face = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-Bold.otf");
          Typeface face_light = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-Light.otf");
          category_name.setTypeface(face);
          item_count.setTypeface(face_light);
          //nextButton = itemView.findViewById(R.id.button_next);

            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);

        }

    }
}