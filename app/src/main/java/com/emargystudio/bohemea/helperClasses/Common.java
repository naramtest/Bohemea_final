package com.emargystudio.bohemea.helperClasses;

import android.content.Context;

public class Common {

    public static String code  ="";
    public static int res_id = 0;
    public static int total = 0;
    public static boolean isOrdered = false;
    public static boolean isSended = false;
    public Context context;

    public static boolean isCahnged = false;

    //Token commons
    public static boolean isNewToken = false;



    public static void clearCommon() {
        res_id = 0;
        total = 0;
        isOrdered = false;

    }
}
