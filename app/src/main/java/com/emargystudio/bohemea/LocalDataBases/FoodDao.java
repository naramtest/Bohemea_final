package com.emargystudio.bohemea.LocalDataBases;



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.emargystudio.bohemea.Model.FoodItem;

import java.util.List;


@Dao
public interface FoodDao {

    @Query("SELECT * FROM foodItem")
    LiveData<List<FoodItem>> loadAllFoods();

    @Query("SELECT * FROM foodItem WHERE food_id = :food_id")
    LiveData<FoodItem> idQuery(int food_id);

    @Query("SELECT * FROM foodItem")
    List<FoodItem> loadAllFoodsAdapter();

    @Insert
    void insertFood(FoodItem foodMenu);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFood(FoodItem foodMenu);

    @Delete
    void deleteFood(FoodItem foodMenu);

    @Query("DELETE FROM foodItem")
    void deleteAllFood();


    @Query("SELECT * FROM foodItem WHERE id = :id")
    LiveData<FoodItem> loadFoodById(int id);
}
