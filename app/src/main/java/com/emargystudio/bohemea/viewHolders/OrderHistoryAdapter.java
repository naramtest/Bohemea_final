package com.emargystudio.bohemea.viewHolders;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.emargystudio.bohemea.model.FoodOrder;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.Common;

import java.util.ArrayList;
import java.util.Locale;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.OrderHistoryViewHolder> {

    public interface EventHandler {
        void handle(int position); // if u need know position. If no, just create method without params
    }

    private Context context;
    private EventHandler handler;
    private ArrayList<FoodOrder> foodOrders;
    private String lang;




    public OrderHistoryAdapter(Context context, ArrayList<FoodOrder> foodOrders,EventHandler handler) {
        this.context = context;
        this.foodOrders = foodOrders;
        this.handler = handler;
        lang = Locale.getDefault().getLanguage();

    }

    @NonNull
    @Override
    public OrderHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.order_history_item, viewGroup, false);
        return new OrderHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final OrderHistoryViewHolder holder, int i) {

        holder.foodName.setText(foodOrders.get(i).getFood_name());
        holder.foodPrice.setText(String.valueOf(foodOrders.get(i).getPrice()*foodOrders.get(i).getQuantity()));
        holder.numberButton.setNumber(String.valueOf(foodOrders.get(i).getQuantity()));
        holder.numberButton.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                if (!Common.isCahnged){
                    handler.handle(holder.getAdapterPosition());
                    foodOrders.get(holder.getAdapterPosition()).setQuantity(newValue);
                    holder.foodPrice.setText(String.valueOf(foodOrders.get(holder.getAdapterPosition()).getPrice()*foodOrders.get(holder.getAdapterPosition()).getQuantity()));
                }
            }
        });

    }

    public ArrayList<FoodOrder> getFoodOrders() {
        return foodOrders;
    }

    @Override
    public int getItemCount() {
        return foodOrders.size();
    }

    class OrderHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView foodName, foodPrice;
        ElegantNumberButton numberButton;

        OrderHistoryViewHolder(@NonNull View itemView) {
            super(itemView);

            Typeface face_ExtraBold ;
            if (lang.equals("ar")){
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Regular.ttf");



            }else{
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Akrobat-Regular.otf");


            }

            foodName = itemView.findViewById(R.id.foodName);
            foodPrice = itemView.findViewById(R.id.foodPrice);
            numberButton = itemView.findViewById(R.id.number_button);

            foodName.setTypeface(face_ExtraBold);
        }

    }


}
