package com.emargystudio.bohemea.localDataBases;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.emargystudio.bohemea.model.FoodOrder;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<FoodOrder>> order;

    public MainViewModel(Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(this.getApplication());
        order = database.orderDao().loadAllFoods();
    }

    public LiveData<List<FoodOrder>> getTasks() {
        return order;
    }
}
