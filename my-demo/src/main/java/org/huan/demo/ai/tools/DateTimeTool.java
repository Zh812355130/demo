package org.huan.demo.ai.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateTimeTool {

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        ZoneId zoneId = LocaleContextHolder.getTimeZone().toZoneId();
        System.out.println("zoneId:" + zoneId);
        String dateTime = LocalDateTime.now().atZone(zoneId).toString();
        System.out.println("dateTime:" + dateTime);
        return dateTime;
    }

    @Tool(description = "Set a user alarm for the given time,provide in ISO-8601 format")
    void setAlarm(String time){
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set fro :" + alarmTime);
    }

}
