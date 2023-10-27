package com.wt.ocr.pojo;

import android.os.Parcel;
import android.os.Parcelable;

public class Boke implements Parcelable {
    private String nickname;
    private String time;
    private String title;
    private String content;
    private String type;
    public Boke() {
    }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boke(String nickname, String time, String title, String content, String type) {
        this.nickname = nickname;
        this.time = time;
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    protected Boke(Parcel in) {
        nickname = in.readString();
        time = in.readString();
        title = in.readString();
        content = in.readString();
        type = in.readString();
    }

    @Override
    public String toString() {
        return "Boke{" +
                "nickname='" + nickname + '\'' +
                ", time='" + time + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    public static final Creator<Boke> CREATOR = new Creator<Boke>() {
        @Override
        public Boke createFromParcel(Parcel in) {
            return new Boke(in);
        }

        @Override
        public Boke[] newArray(int size) {
            return new Boke[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(nickname);
        dest.writeString(time);
        dest.writeString(title);
        dest.writeString(content);
        dest.writeString(type);
    }
}
