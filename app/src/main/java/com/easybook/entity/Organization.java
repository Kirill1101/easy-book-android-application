package com.easybook.entity;

import java.util.List;
import java.util.UUID;

public class Organization {
  private UUID id;

  private String userCreatorLogin;

  private String title;

  private List<Schedule> schedules;

  private List<String> userAdminLogins;

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

  public List<Schedule> getSchedules() {
    return schedules;
  }

  public void setSchedules(List<Schedule> schedules) {
    this.schedules = schedules;
  }

  public List<String> getUserAdminLogins() {
    return userAdminLogins;
  }

  public void setUserAdminLogins(List<String> userAdminLogins) {
    this.userAdminLogins = userAdminLogins;
  }
}
