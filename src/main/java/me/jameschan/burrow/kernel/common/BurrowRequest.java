package me.jameschan.burrow.kernel.common;

import java.beans.JavaBean;

@JavaBean
public final class BurrowRequest {
  private String command;
  private String workingDirectory;
  private Integer consoleWidth;

  public String getCommand() {
    return command;
  }

  public void setCommand(String command) {
    this.command = command;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public Integer getConsoleWidth() {
    return consoleWidth;
  }

  public void setConsoleWidth(Integer consoleWidth) {
    this.consoleWidth = consoleWidth;
  }
}
