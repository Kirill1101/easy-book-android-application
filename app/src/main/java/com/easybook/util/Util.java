package com.easybook.util;

import com.easybook.entity.Appointment;

public class Util {
    public static String getDurationStringBySeconds(Long duration) {
        StringBuilder durationStr = new StringBuilder();
        if (duration / 3600 > 0) {
            durationStr.append(duration / 3600).append("ч ");
        }
        if ((duration % 3600) / 60 > 0) {
            durationStr.append((duration % 3600) / 60).append("м");
        }
        return durationStr.toString();
    }

    public static String getAppointmentStringByAppointment(Appointment appointment) {
        StringBuilder appointmentInfo = new StringBuilder();
        appointmentInfo
                .append("Пользователь: ")
                .append(appointment.getUserLogin())
                .append("\nВремя записи: ")
                .append(appointment.getStartTime())
                .append(" - ")
                .append(appointment.getEndTime())
                .append("\nДлительность записи: ")
                .append(Util.getDurationStringBySeconds(appointment.getDuration()))
                .append("\nУслуги:");
        appointment.getServices().forEach(service ->
                appointmentInfo
                        .append("\n \t \u2022 ")
                        .append(service.getTitle())
                        .append(" (")
                        .append(service.getPrice() + "р , ")
                        .append(Util.getDurationStringBySeconds(service.getDuration()))
                        .append(")"));
        return appointmentInfo.toString();
    }
}
