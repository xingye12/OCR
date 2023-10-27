package com.wt.ocr.pojo;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class User implements Parcelable  {
    private String username;
    private String password;
    private String idCard;
    private String sex;
    private String nickname;
    private ArrayList<String> friend;
    private String address;

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    private String phone;
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public User() {
    }

    public User(String username, String password, String idCard, String sex, String nickname, String address, String phone) {
        this.username = username;
        this.password = password;
        this.idCard = idCard;
        this.sex = sex;
        this.nickname = nickname;
        this.address = address;
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public ArrayList<String> getFriend() {
        return friend;
    }

    public void setFriend(ArrayList<String> friend) {
        this.friend = friend;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", idCard='" + idCard + '\'' +
                ", sex='" + sex + '\'' +
                ", nickname='" + nickname + '\'' +
                ", friend=" + friend +
                '}';
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(password);
        dest.writeString(idCard);
        dest.writeString(sex);
        dest.writeString(nickname);
        dest.writeString(phone);
        dest.writeString(address);
//        dest.writeStringList(friend);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    // 添加一个Parcelable.Creator接口
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // 添加一个从Parcel构造User对象的私有构造方法
    private User(Parcel in) {
        username = in.readString();
        password = in.readString();
        idCard = in.readString();
        sex = in.readString();
        nickname = in.readString();
        phone=in.readString();
        address=in.readString();
//        in.readStringList(friend);
    }
}