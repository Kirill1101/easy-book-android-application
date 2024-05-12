package com.easybook.entity;

import java.util.List;
import java.util.UUID;

public class Schedule {
  private UUID id;

  private String userCreatorLogin;

  private String title;

  private Long durationOfOneSlot;

  private List<ScheduleDate> availableDates;

  private List<Service> services;

  private List<Appointment> appointments;

  private UUID organizationId;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getUserCreatorLogin() {
    return userCreatorLogin;
  }

  public void setUserCreatorLogin(String userCreatorLogin) {
    this.userCreatorLogin = userCreatorLogin;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Long getDurationOfOneSlot() {
    return durationOfOneSlot;
  }

  public void setDurationOfOneSlot(Long durationOfOneSlot) {
    this.durationOfOneSlot = durationOfOneSlot;
  }

  public List<ScheduleDate> getAvailableDates() {
    return availableDates;
  }

  public void setAvailableDates(List<ScheduleDate> availableDates) {
    this.availableDates = availableDates;
  }

  public List<Service> getServices() {
    return services;
  }

  public void setServices(List<Service> services) {
    this.services = services;
  }

  public List<Appointment> getAppointments() {
    return appointments;
  }

  public void setAppointments(List<Appointment> appointments) {
    this.appointments = appointments;
  }

  public UUID getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(UUID organizationId) {
    this.organizationId = organizationId;
  }
}
