package com.jimmy.skripsi.models;

public class ChatModel {
    private String name;
    private String message;
    private String userId;
    private String time;
    private String status;
    private String roomId;

    public ChatModel() {
    }

    public ChatModel(String userId, String name, String message, String time, String status) {
        this.userId = userId;
        this.name = name;
        this.message = message;
        this.time = time;
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }
}
