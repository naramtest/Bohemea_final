package com.emargystudio.bohemea.Model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Reservation implements Parcelable {

    private int res_id;
    private int user_id;
    private int table_id;
    private int year;
    private int month;
    private int day;
    private double startHour;
    private double end_hour;
    private int chairNumber;
    private int status; // 0 = waiting ; 1 = approved ; 2 = canceled or removed
    private int total;
    private String movie_name;
    private   ArrayList<Integer> tableArray = new ArrayList<>();

    public int getRes_id() {
        return res_id;
    }

    public void setRes_id(int res_id) {
        this.res_id = res_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getTable_id() {
        return table_id;
    }

    public void setTable_id(int table_id) {
        this.table_id = table_id;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public double getStartHour() {
        return startHour;
    }

    public void setStartHour(double startHour) {
        this.startHour = startHour;
    }

    public double getEnd_hour() {
        return end_hour;
    }

    public void setEnd_hour(double end_hour) {
        this.end_hour = end_hour;
    }

    public int getChairNumber() {
        return chairNumber;
    }

    public void setChairNumber(int chairNumber) {
        this.chairNumber = chairNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public String getMovie_name() {
        return movie_name;
    }

    public void setMovie_name(String movie_name) {
        this.movie_name = movie_name;
    }

    public ArrayList<Integer> getTableArray() {
        return tableArray;
    }

    public void setTableArray(ArrayList<Integer> tableArray) {
        this.tableArray = tableArray;
    }

    public Reservation() {
    }


    public Reservation(int res_id, int user_id, int table_id, int year, int month, int day, double startHour, double end_hour, int chairNumber, int status, int total, String movie_name) {
        this.res_id = res_id;
        this.user_id = user_id;
        this.table_id = table_id;
        this.year = year;
        this.month = month;
        this.day = day;
        this.startHour = startHour;
        this.end_hour = end_hour;
        this.chairNumber = chairNumber;
        this.status = status;
        this.total = total;
        this.movie_name = movie_name;
    }

    protected Reservation(Parcel in) {
        res_id = in.readInt();
        user_id = in.readInt();
        table_id = in.readInt();
        year = in.readInt();
        month = in.readInt();
        day = in.readInt();
        startHour = in.readDouble();
        end_hour = in.readDouble();
        chairNumber = in.readInt();
        status = in.readInt();
        total = in.readInt();
        movie_name = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(res_id);
        dest.writeInt(user_id);
        dest.writeInt(table_id);
        dest.writeInt(year);
        dest.writeInt(month);
        dest.writeInt(day);
        dest.writeDouble(startHour);
        dest.writeDouble(end_hour);
        dest.writeInt(chairNumber);
        dest.writeInt(status);
        dest.writeInt(total);
        dest.writeString(movie_name);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Reservation> CREATOR = new Creator<Reservation>() {
        @Override
        public Reservation createFromParcel(Parcel in) {
            return new Reservation(in);
        }

        @Override
        public Reservation[] newArray(int size) {
            return new Reservation[size];
        }
    };
}
