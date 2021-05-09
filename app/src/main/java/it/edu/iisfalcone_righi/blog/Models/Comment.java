package it.edu.iisfalcone_righi.blog.Models;

import com.google.firebase.database.ServerValue;

public class Comment {
    private String content,userId,userImg,userName;
    private Object timestamp;

    public Comment(String content, String userId, String userImg, String userName) {
        this.content = content;
        this.userId = userId;
        this.userImg = userImg;
        this.userName = userName;

        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Comment(String content, String userId, String userImg, String userName, Object timestamp) {
        this.content = content;
        this.userId = userId;
        this.userImg = userImg;
        this.userName = userName;
        this.timestamp = timestamp;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserImg() {
        return userImg;
    }

    public void setUserImg(String userImg) {
        this.userImg = userImg;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
