package com.EBworkplace.scannerapp.UserModel;

public class User_detail_model {
    private String morning_check_in_time , morning_check_out_time ,evening_check_in_time,evening_check_out_time , morning_opening_stock , evening_opening_stock , morning_closing_stock , evening_closing_stock , morning_money_collected , evening_money_collected , name;

    public String getMorning_closing_stock() {
        return morning_closing_stock;
    }

    public String getEvening_closing_stock() {
        return evening_closing_stock;
    }

    public String getMorning_money_collected() {
        return morning_money_collected;
    }

    public String getEvening_money_collected() {
        return evening_money_collected;
    }

    public String getMorning_check_in_time() {
        return morning_check_in_time;
    }

    public String getName(){return name;}

    public void setMorning_check_in_time(String morning_check_in_time) {
        this.morning_check_in_time = morning_check_in_time;
    }
    public void setName(String name) {this.name=name;}
    public String getMorning_check_out_time() {
        return morning_check_out_time;
    }

    public void setMorning_check_out_time(String morning_check_out_time) {
        this.morning_check_out_time = morning_check_out_time;
    }

    public String getEvening_check_in_time() {
        return evening_check_in_time;
    }

    public void setEvening_check_in_time(String evening_check_in_time) {
        this.evening_check_in_time = evening_check_in_time;
    }

    public String getEvening_check_out_time() {
        return evening_check_out_time;
    }

    public void setEvening_check_out_time(String evening_check_out_time) {
        this.evening_check_out_time = evening_check_out_time;
    }


    public String getMorning_opening_stock() {
        return morning_opening_stock;
    }

    public void setMorning_opening_stock(String morning_opening_stock) {
        this.morning_opening_stock = morning_opening_stock;
    }

    public String getEvening_opening_stock() {
        return evening_opening_stock;
    }

    public void setEvening_opening_stock(String evening_opening_stock) {
        this.evening_opening_stock = evening_opening_stock;
    }
    public User_detail_model(){}

    public User_detail_model(String morning_check_in_time, String morning_check_out_time, String evening_check_in_time, String evening_check_out_time, String morning_opening_stock, String evening_opening_stock, String morning_closing_stock, String evening_closing_stock, String name) {
        this.morning_check_in_time = morning_check_in_time;
        this.morning_check_out_time = morning_check_out_time;
        this.evening_check_in_time = evening_check_in_time;
        this.evening_check_out_time = evening_check_out_time;
        this.morning_opening_stock = morning_opening_stock;
        this.evening_opening_stock = evening_opening_stock;
        this.morning_closing_stock = morning_closing_stock;
        this.evening_closing_stock = evening_closing_stock;

        this.name=name;
    }
}
