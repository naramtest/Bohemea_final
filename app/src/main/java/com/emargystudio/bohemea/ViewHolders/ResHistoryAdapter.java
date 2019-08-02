package com.emargystudio.bohemea.ViewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.emargystudio.bohemea.History.HistoryActivity;
import com.emargystudio.bohemea.History.OrderFragment;
import com.emargystudio.bohemea.Model.Reservation;
import com.emargystudio.bohemea.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ResHistoryAdapter extends RecyclerView.Adapter<ResHistoryAdapter.RisHistoryViewHolder> {

    private Context context;
    private ArrayList<Reservation> reservations;


    public ResHistoryAdapter(Context context, ArrayList<Reservation> reservations) {
        this.context = context;
        this.reservations = reservations;

    }

    @NonNull
    @Override
    public RisHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.res_history_item, viewGroup, false);
        return new RisHistoryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull final RisHistoryViewHolder holder, int i) {

        int year = reservations.get(i).getYear();
        int month = reservations.get(i).getMonth();
        int day = reservations.get(i).getDay();

        String lang = Locale.getDefault().getLanguage();
        String dateString;
        if(lang.equals("ar")){
            NumberFormat nf = NumberFormat.getInstance(new Locale("ar","LB")); //or "nb","No" - for Norway
            String sYear = nf.format(year);
            String yearString = sYear.replace("٬", "");
            String monthString = nf.format(month);
            String dayString = nf.format(day);
            dateString = yearString+"/"+monthString+"/"+dayString;
        }else {
            dateString = "Date: "+year+"/"+month+"/"+day;
        }

        holder.res_date.setText(dateString);
        int status = reservations.get(i).getStatus();
        switch (status){
            case 0:
                holder.status.setText(context.getString(R.string.f_res_history_waiting));
                holder.status.setTextColor(Color.parseColor("#dd3538"));
                break;

            case 1:
                holder.status.setText(context.getString(R.string.f_res_history_approved));
                holder.status.setTextColor(Color.parseColor("#006400"));
                break;
        }



        String price = String.format(context.getString(R.string.total_res_history_j),reservations.get(i).getTotal());
        holder.price.setText(price);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable("reservation", reservations.get(holder.getAdapterPosition()));
                Fragment fragment = new OrderFragment();
                fragment.setArguments(args);
                FragmentTransaction ft = ((HistoryActivity)context).getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.your_placeholder, fragment,"Order");
                ft.addToBackStack("Order");
                ft.commit();
            }
        });
    }


    @Override
    public int getItemCount() {
        return reservations.size();
    }

    class RisHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView res_date, price ,status ,show ;
        FrameLayout cardView;

        RisHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            res_date  = itemView.findViewById(R.id.res_date);
            price = itemView.findViewById(R.id.price);
            //res_id = itemView.findViewById(R.id.res_id);
            status = itemView.findViewById(R.id.status);
            cardView = itemView.findViewById(R.id.res_container);
            show     = itemView.findViewById(R.id.show_txt);

            Typeface face_bold = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-ExtraBold.otf");
            Typeface face = Typeface.createFromAsset(context.getAssets(),"fonts/Akrobat-Light.otf");

            res_date.setTypeface(face_bold);
            price.setTypeface(face);
            status.setTypeface(face_bold);
            show.setTypeface(face_bold);


        }

    }
}
