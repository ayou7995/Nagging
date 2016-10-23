package com.example.ayou7995.nagging;

/**
 * Created by ayou7995 on 2016/10/22.
 */
public class ChatMessage {
    public boolean left;
    public String message;

    public ChatMessage() {
        super();
        this.left = true;
        this.message = "default";
    }

    public ChatMessage(boolean left, String message) {
        super();
        this.left = left;
        this.message = message;
    }

    public void setLeft(boolean left) { this.left = left; }
    public void setMessage(String message) { this.message = message; }

    public boolean getLeft() {return this.left; }
    public String getMessage() { return this.message; }

}
