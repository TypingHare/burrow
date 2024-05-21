package me.jameschan.burrow.common;

import java.beans.JavaBean;

@JavaBean
public class BurrowResponse {
  private Integer code;

  private String message;

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
}