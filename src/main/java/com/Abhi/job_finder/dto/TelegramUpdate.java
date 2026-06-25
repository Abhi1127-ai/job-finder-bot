package com.Abhi.job_finder.dto;

public class TelegramUpdate {
    private Long update_id;
    private TelegramMessage message;

    public Long getUpdateId() { return update_id; }
    public void setUpdateId(Long updateId) { this.update_id = updateId; }

    public TelegramMessage getMessage() {
        return message;
    }
    public void setMessage(TelegramMessage message) {
        this.message = message;
    }
}
