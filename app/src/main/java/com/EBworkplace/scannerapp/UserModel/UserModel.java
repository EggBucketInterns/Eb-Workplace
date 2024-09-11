package com.EBworkplace.scannerapp.UserModel;

import com.google.firebase.Timestamp;

public class UserModel {
    private String phoneNo;
    private boolean is_Verified;
    private Timestamp time_created;
    private String name;

    public UserModel() {
        is_Verified = false;
    }
    public UserModel(String phoneNo){
        this.phoneNo = phoneNo;
        is_Verified = false;
    }
    public UserModel(String phoneNo, boolean is_Verified, Timestamp time_created,String name) {
        this.phoneNo = phoneNo;
        this.is_Verified = is_Verified;
        this.time_created = time_created;
        this.name=name;
    }

    public boolean isIs_Verified() {
        return is_Verified;
    }

    public void setIs_Verified(boolean is_Verified) {
        this.is_Verified = is_Verified;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public Timestamp getTime_created() {
        return time_created;
    }

    public void setTime_created(Timestamp time_created) {
        this.time_created = time_created;
    }
}
