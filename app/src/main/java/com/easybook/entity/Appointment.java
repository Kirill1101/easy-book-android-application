package com.easybook.entity;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public class Appointment {
  private UUID id;

  private String userLogin;

  private LocalDate date;

  private LocalTime startTime;

  private LocalTime endTime;

  private List<Service> services;

  private Long duration;

  private String scheduleTitle;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUserLogin() {
    return userLogin;
  }

  public void setUserLogin(String userLogin) {
    this.userLogin = userLogin;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }


  public LocalTime getStartTime() {
    return startTime;
  }

  public void setStartTime(LocalTime startTime) {
    this.startTime = startTime;
  }

  public LocalTime getEndTime() {
    return endTime;
  }

  public void setEndTime(LocalTime endTime) {
    this.endTime = endTime;
  }

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }

  public String getScheduleTitle() {
    return scheduleTitle;
  }

  public void setScheduleTitle(String scheduleTitle) {
    this.scheduleTitle = scheduleTitle;
  }
}
