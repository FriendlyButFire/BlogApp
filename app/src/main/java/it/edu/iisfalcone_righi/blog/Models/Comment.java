package it.edu.iisfalcone_righi.blog.Models;

import com.google.firebase.database.ServerValue;

public class Comment {
    private String content, userId, userImg, userName,key,postkey;
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

    public Comment() {
    }

    public String getPostkey() {
        return postkey;
    }

    public void setPostkey(String postkey) {
        this.postkey = postkey;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
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

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
