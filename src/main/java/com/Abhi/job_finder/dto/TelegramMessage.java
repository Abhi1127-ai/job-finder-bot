package com.Abhi.job_finder.dto;

public class TelegramMessage {
    private String text;
    private TelegramChat chat;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public TelegramChat getChat() { return chat; }
    public void setChat(TelegramChat chat) { this.chat = chat; }
}