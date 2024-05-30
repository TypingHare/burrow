package me.jameschan.burrow.kernel.common;

import java.beans.JavaBean;

@JavaBean
public final class BurrowResponse {
  private Integer code;
  private String message;
  private String immediateCommand;

  public Integer getCode() {
    return code;
  }

  public void setCode(Integer code) {
    this.code = code;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getImmediateCommand() {
    return immediateCommand;
  }

  public void setImmediateCommand(String immediateCommand) {
    this.immediateCommand = immediateCommand;
  }
}
