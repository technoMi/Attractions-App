package com.example.attractions.chats;

public class Chat {
    private String chat_id, chat_name, userId1, userId2;

    public Chat(String chat_id, String chat_name, String userId1, String userId2) {
        this.chat_id = chat_id;
        this.chat_name = chat_name;
        this.userId1 = userId1;
        this.userId2 = userId2;
    }

    public String getChat_id() {
        return chat_id;
    }

    public String getChat_name() {
        return chat_name;
    }

    public String getUserId1() {
        return userId1;
    }

    public String getUserId2() {
        return userId2;
    }
}
