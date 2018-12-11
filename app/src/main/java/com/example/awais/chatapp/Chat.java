package com.example.awais.chatapp;

public class Chat {
    private String id,sender, receiver, message, sendTime, modify;
    private boolean isseen;

    public Chat(String id, String sender, String receiver, String message, boolean isseen, String sendTime, String modify) {
        this.id = id;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isseen = isseen;
        this.sendTime = sendTime;
        this.modify = modify;
    }

    public Chat(){
        //empty constructor
}

    public String getModify() {
        return modify;
    }

    public void setModify(String modify) {
        this.modify = modify;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public String getSender()
    {
        return sender;
    }

    public void setSender(String sender)
    {
        this.sender = sender;
    }

    public String getReceiver()
    {
        return receiver;
    }

    public void setReceiver(String receiver)
    {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsseen() {
        return isseen;
    }

    public void setIsseen(boolean isseen) {
        this.isseen = isseen;
    }
}
