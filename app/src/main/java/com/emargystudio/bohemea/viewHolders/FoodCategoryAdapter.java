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

import com.emargystudio.bohemea.model.FoodCategory;
import com.emargystudio.bohemea.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;


public class FoodCategoryAdapter  extends RecyclerView.Adapter<FoodCategoryAdapter.FoodCategoryViewHolder>{

    private List<FoodCategory> foodCategories;
    private View.OnClickListener mOnItemClickListener;
    private Context context;
    private String lang;

    public FoodCategoryAdapter(List<FoodCategory> foodCategories, Context context) {
        this.foodCategories = foodCategories;
        this.context = context;
        lang = Locale.getDefault().getLanguage();
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
        holder.item_count.setText(String.format(context.getString(R.string.food_category_item_count),foodCategory.getItem_count()));


        if (lang.equals("ar")){


            holder.category_name.setText(foodCategory.getAr_name());

        }else{

            holder.category_name.setText(foodCategory.getName());

        }


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


        FoodCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
          category_name = itemView.findViewById(R.id.category_name);
          categoryImage = itemView.findViewById(R.id.category_image);
          item_count = itemView.findViewById(R.id.item_count);
            Typeface face_Regular ;
            Typeface face_ExtraBold;

            if (lang.equals("ar")){
                face_Regular = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Light.ttf");
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Bold.ttf");



            }else{
                face_Regular = Typeface.createFromAsset(context.getAssets(), "fonts/Akrobat-Light.otf");
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Akrobat-Bold.otf");


            }

          category_name.setTypeface(face_ExtraBold);
          item_count.setTypeface(face_Regular);

            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);

        }

    }
}