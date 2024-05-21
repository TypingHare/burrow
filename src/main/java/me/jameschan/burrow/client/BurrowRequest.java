package me.jameschan.burrow.client;

import java.beans.JavaBean;

@JavaBean
public class BurrowRequest {
  private String command;

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }
}
