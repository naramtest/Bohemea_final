package com.emargystudio.bohemea.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class FoodCategory implements Parcelable {

    private int id , item_count;
    private String name ,ar_name, image_url;


    public FoodCategory(int id, int item_count, String name, String ar_name, String image_url) {
        this.id = id;
        this.item_count = item_count;
        this.name = name;
        this.ar_name = ar_name;
        this.image_url = image_url;
    }

    protected FoodCategory(Parcel in) {
        id = in.readInt();
        item_count = in.readInt();
        name = in.readString();
        ar_name = in.readString();
        image_url = in.readString();
    }

    public static final Creator<FoodCategory> CREATOR = new Creator<FoodCategory>() {
        @Override
        public FoodCategory createFromParcel(Parcel in) {
            return new FoodCategory(in);
        }

        @Override
        public FoodCategory[] newArray(int size) {
            return new FoodCategory[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getItem_count() {
        return item_count;
    }

    public void setItem_count(int item_count) {
        this.item_count = item_count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAr_name() {
        return ar_name;
    }

    public void setAr_name(String ar_name) {
        this.ar_name = ar_name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeInt(item_count);
        dest.writeString(name);
        dest.writeString(ar_name);
        dest.writeString(image_url);
    }
}
