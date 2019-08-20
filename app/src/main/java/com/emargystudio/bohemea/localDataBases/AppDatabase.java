package com.emargystudio.bohemea.localDataBases;


import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.emargystudio.bohemea.model.FoodItem;
import com.emargystudio.bohemea.model.FoodOrder;


@Database(entities = { FoodItem.class, FoodOrder.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static final Object LOCK = new Object();
    private static final String DATABASE_NAME = "bohemea_database";
    private static AppDatabase sInstance;


    public static AppDatabase getInstance(Context context) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, AppDatabase.DATABASE_NAME)
                        .build();
            }
        }
        return sInstance;
    }


    public abstract FoodDao foodDao();
    public abstract OrderDao orderDao();

}
