package com.emargystudio.bohemea.ViewHolders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.emargystudio.bohemea.Model.FoodOrder;
import com.emargystudio.bohemea.R;
import com.squareup.picasso.Picasso;


import java.util.List;


public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder>{

    private List<FoodOrder> foodOrders;
    private Context context;
    public DetailsAdapterListener onClickListener;


    public CartAdapter( Context context ,DetailsAdapterListener listener) {
        this.context = context;
        this.onClickListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);

        return new CartViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final CartViewHolder holder, final int position) {

        Picasso.get().load(foodOrders.get(position).getFood_image()).resize(200,200).into(holder.cartImage);
        holder.cartName.setText(foodOrders.get(position).getFood_name());
        holder.cartPrice.setText(String.format(context.getString(R.string.foodDetailActivity_food_price),foodOrders.get(position).getPrice()));

        holder.deleteCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.deleteCartItem(holder,position);
            }
        });

        holder.editCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickListener.editCartItem(holder,position);
            }
        });

    }

    @Override
    public int getItemCount() {
        if (foodOrders == null) {
            return 0;
        }
        return foodOrders.size();
    }

    public void setTasks(List<FoodOrder> taskEntries) {
        foodOrders = taskEntries;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        foodOrders.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(FoodOrder item, int position) {
        foodOrders.add(position, item);
        // notify item added by position
        notifyItemInserted(position);
    }




    class CartViewHolder extends RecyclerView.ViewHolder {

        ImageView cartImage;
        TextView cartName , cartPrice  ;
        ImageButton deleteCart , editCart ;


        CartViewHolder(@NonNull View itemView) {
            super(itemView);
          cartImage = itemView.findViewById(R.id.image_cart);
          cartName  = itemView.findViewById(R.id.name_cart);
          cartPrice = itemView.findViewById(R.id.price_cart);
          deleteCart= itemView.findViewById(R.id.delete_item);
          editCart  = itemView.findViewById(R.id.edit_cart);
          Typeface face_bold = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-Bold.otf");
          Typeface face_light = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-Light.otf");
          cartName.setTypeface(face_bold);
          cartPrice.setTypeface(face_light);





        }

    }

    public interface DetailsAdapterListener {

        void deleteCartItem(RecyclerView.ViewHolder v, int position);

        void editCartItem(RecyclerView.ViewHolder v, int position);
    }
}