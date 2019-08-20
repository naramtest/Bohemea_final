package com.emargystudio.bohemea.model;


import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "food")
public class FoodOrder {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private int res_id;
    private int food_id;
    private String food_name;
    private int quantity;
    private int price;
    private String food_image;
    private String note;


    public FoodOrder(int id, int res_id, int food_id, String food_name, int quantity, int price, String food_image, String note) {
        this.id = id;
        this.res_id = res_id;
        this.food_id = food_id;
        this.food_name = food_name;
        this.quantity = quantity;
        this.price = price;
        this.food_image = food_image;
        this.note = note;
    }

    @Ignore
    public FoodOrder(int res_id, int food_id, String food_name, int quantity, int price, String food_image, String note) {
        this.res_id = res_id;
        this.food_id = food_id;
        this.food_name = food_name;
        this.quantity = quantity;
        this.price = price;
        this.food_image = food_image;
        this.note = note;
    }


    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getFood_image() {
        return food_image;
    }

    public void setFood_image(String food_image) {
        this.food_image = food_image;
    }



    public String getFood_name() {
        return food_name;
    }

    public void setFood_name(String food_name) {
        this.food_name = food_name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRes_id() {
        return res_id;
    }

    public void setRes_id(int res_id) {
        this.res_id = res_id;
    }

    public int getFood_id() {
        return food_id;
    }

    public void setFood_id(int food_id) {
        this.food_id = food_id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }


}

