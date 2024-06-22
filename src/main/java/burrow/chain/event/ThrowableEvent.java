package burrow.chain.event;

import org.springframework.lang.NonNull;

public final class ThrowableEvent extends Event {
  private final Throwable throwable;

  public ThrowableEvent(@NonNull final Throwable throwable) {
    this.throwable = throwable;
  }

  @NonNull
  public Throwable getThrowable() {
    return throwable;
  }
}
