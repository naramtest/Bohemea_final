package com.emargystudio.bohemea.LocalDataBases;



import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.emargystudio.bohemea.Model.FoodOrder;

import java.util.List;

@Dao
public interface OrderDao {

    @Query("SELECT * FROM food")
    LiveData<List<FoodOrder>> loadAllFoods();

    @Query("SELECT * FROM food")
    List<FoodOrder> loadAllFoodsAdapter();

    @Insert
    void insertFood(FoodOrder foodOrder);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFood(FoodOrder foodOrder);

    @Delete
    void deleteFood(FoodOrder foodOrder);

    @Query("DELETE FROM food")
    void deleteAllFood();


    @Query("SELECT * FROM food WHERE id = :id")
    LiveData<FoodOrder> loadFoodById(int id);
}
