package com.iapp.lolchat.models;

import java.util.Objects;

public class Message {

    private final long time;
    private final String sender;
    private final String content;

    public Message(long time, String sender, String content) {
        this.time = time;
        this.sender = sender;
        this.content = content;
    }

    public long getTime() {
        return time;
    }

    public String getSender() {
        return sender;
    }

    public String getContent() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return time == message.time && Objects.equals(sender, message.sender) && Objects.equals(content, message.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, sender, content);
    }
}
