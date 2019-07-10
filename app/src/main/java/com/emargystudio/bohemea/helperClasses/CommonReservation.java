package com.emargystudio.bohemea.helperClasses;


import android.app.Activity;

import android.view.inputmethod.InputMethodManager;

import java.math.BigDecimal;
import java.math.RoundingMode;



public class CommonReservation {



    public static String changeHourFormat(double hour){
        String stringHour ;
        if (hour >= 0 && hour <=12 ){
            String am = "AM";
            String sHour = String.valueOf(hour).replace(".",":");
            stringHour = sHour + " " + am;

        }else {
            String am = "PM";
            double dHour = round(hour-12.00,2);
            String result = String.format("%.2f", dHour);
            String sHour = result.replace(".",":");
            stringHour = sHour + " " + am;
        }
        return  stringHour;
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    //hide soft keyBoard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(
                        Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }


}
