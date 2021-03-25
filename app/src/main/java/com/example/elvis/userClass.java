package com.example.elvis;

public class userClass {
    String name, dob, sex, email, mobile, uid;

    public userClass(String name, String dob, String sex, String email, String mobile,String uid) {
        this.name = name;
        this.dob = dob;
        this.sex = sex;
        this.email = email;
        this.mobile = mobile;
        this.uid = uid;
    }

    public userClass() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
