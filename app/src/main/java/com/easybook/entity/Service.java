package com.easybook.entity;

import java.util.UUID;

public class Service {
  private UUID id;

  private String userCreatorLogin;

  private String title;

  private Long duration;

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

  public Long getDuration() {
    return duration;
  }

  public void setDuration(Long duration) {
    this.duration = duration;
  }
}
