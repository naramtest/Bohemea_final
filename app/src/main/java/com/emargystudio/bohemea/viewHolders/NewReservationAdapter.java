package com.emargystudio.bohemea.viewHolders;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.emargystudio.bohemea.model.Reservation;
import com.emargystudio.bohemea.R;
import com.emargystudio.bohemea.helperClasses.CommonReservation;

import java.util.ArrayList;
import java.util.Locale;

public class NewReservationAdapter extends RecyclerView.Adapter<NewReservationAdapter.NewReservationViewHolder>{

    private ArrayList<Reservation> reservations;
    private View.OnClickListener mOnItemClickListener;
    private Context context;
    private String lang;

    public NewReservationAdapter(ArrayList<Reservation> reservations, Context context) {
        this.reservations = reservations;
        this.context = context;
        lang = Locale.getDefault().getLanguage();

    }

    @NonNull
    @Override
    public NewReservationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.new_reservation_item, parent, false);

        return new NewReservationViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull NewReservationViewHolder holder, int position) {

        Reservation reservation = reservations.get(position);
        holder.timeTxt.setText(CommonReservation.changeHourFormat(context,reservation.getStartHour()));

        String date = reservation.getYear()+"-"+reservation.getMonth()+"-"+reservation.getDay();
        holder.dateTxt.setText(date);

        int list_number = position+1;
        holder.countTxt.setText(list_number+"-");
    }

    @Override
    public int getItemCount() {
        return reservations.size();
    }


    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        mOnItemClickListener = itemClickListener;
    }

    class NewReservationViewHolder extends RecyclerView.ViewHolder {

        TextView dateTxt , timeTxt ,countTxt;


        NewReservationViewHolder(@NonNull View itemView) {
            super(itemView);
            dateTxt = itemView.findViewById(R.id.date_txt);
            timeTxt = itemView.findViewById(R.id.time_txt);
            countTxt = itemView.findViewById(R.id.count_txt);
            Typeface face_ExtraBold ;
            if (lang.equals("ar")){
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Cairo-Bold.ttf");



            }else{
                face_ExtraBold = Typeface.createFromAsset(context.getAssets(), "fonts/Akrobat-ExtraBold.otf");


            }
            dateTxt.setTypeface(face_ExtraBold);
            timeTxt.setTypeface(face_ExtraBold);
            countTxt.setTypeface(face_ExtraBold);
            itemView.setTag(this);
            itemView.setOnClickListener(mOnItemClickListener);

        }

    }
}