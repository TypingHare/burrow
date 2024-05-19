package me.jameschan.burrow.chamber;

public class ChamberNotFoundException extends Exception {
  public ChamberNotFoundException(final String chamberName) {
    super("Chamber not found: " + chamberName);
  }
}
