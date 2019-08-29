package com.emargystudio.bohemea.helperClasses;


import android.annotation.SuppressLint;


import android.content.Context;
import android.util.Log;

import com.emargystudio.bohemea.R;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;


public class CommonReservation {



    public static String changeHourFormat(Context context, double hour){
        String stringHour ;
        String lang = Locale.getDefault().getLanguage();
        if (hour >= 0 && hour <=12 ){
            String am = context.getString(R.string.am_string);
            double dHour = round(hour);
            @SuppressLint("DefaultLocale") String result = String.format("%.2f", dHour);
            String sHour;
            if (lang.equals("ar")){
                sHour = result.replace("٫", ":");

            }else {
                sHour = result.replace(".", ":");
            }
            stringHour =" " + sHour +" "+  am;

        }else if (hour > 12 && hour <13){
            String pm = context.getString(R.string.pm_string);
            double dHour = round(hour);
            @SuppressLint("DefaultLocale") String result = String.format("%.2f", dHour);
            String sHour;
            if (lang.equals("ar")){
                sHour = result.replace("٫", ":");

            }else {
                sHour = result.replace(".", ":");
            }
            stringHour =" " + sHour +" "+  pm;
        }else {
            String am = context.getString(R.string.pm_string);
            double dHour = round(hour-12.00);
            @SuppressLint("DefaultLocale") String result = String.format("%.2f", dHour);
            String sHour;
            if (lang.equals("ar")){
                Log.d("Arabic", "changeHourFormat: "+result);
                sHour = result.replace("٫", ":");

            }else {
                sHour = result.replace(".", ":");
            }
            stringHour = sHour + " " + am;
        }
        return  stringHour;
    }

    private static double round(double value) {

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }



}
