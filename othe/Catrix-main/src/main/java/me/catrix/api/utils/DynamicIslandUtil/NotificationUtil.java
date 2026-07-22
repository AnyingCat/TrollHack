package me.catrix.api.utils.DynamicIslandUtil;

public class NotificationUtil {
    public String message;
    public long startTime;
    public boolean isEnable;

    public NotificationUtil(String message, boolean isEnable) {
        this.message = message;
        this.isEnable = isEnable;
        this.startTime = System.currentTimeMillis();
    }
}