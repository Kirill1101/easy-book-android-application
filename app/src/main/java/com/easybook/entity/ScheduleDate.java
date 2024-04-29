package com.easybook.entity;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ScheduleDate {
  private UUID id;

  private LocalDate date;

  private List<Slot> slots;

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public List<Slot> getSlots() {
    return slots;
  }

  public void setSlots(List<Slot> slots) {
    this.slots = slots;
  }
}
