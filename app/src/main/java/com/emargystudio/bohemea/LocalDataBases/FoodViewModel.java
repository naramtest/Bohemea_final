package com.emargystudio.bohemea.LocalDataBases;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.emargystudio.bohemea.Model.FoodItem;

import java.util.List;

public class FoodViewModel extends AndroidViewModel {



    private LiveData<List<FoodItem>> foodMenus;
    private LiveData<FoodItem> foodMenu;
    AppDatabase database;

    public FoodViewModel(Application application) {
        super(application);
        database = AppDatabase.getInstance(this.getApplication());
        foodMenus = database.foodDao().loadAllFoods();

    }

    public LiveData<List<FoodItem>> getTasks() {
        return foodMenus;
    }

    public LiveData<FoodItem> getFood(int id) {
        foodMenu = database.foodDao().idQuery(id);
        return foodMenu;
    }
}
