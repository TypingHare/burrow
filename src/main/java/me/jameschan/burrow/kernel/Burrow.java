package me.jameschan.burrow.kernel;

import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class Burrow {
  public static final Path ROOT_DIR = Path.of("/opt/burrow/").normalize().toAbsolutePath();

  public Burrow() {
    checkRootDir();
  }

  private void checkRootDir() {
    if (ROOT_DIR.toFile().isDirectory()) return;
    if (ROOT_DIR.toFile().mkdirs()) {
      initializeFiles();
      return;
    }

    // Fail to creat a root directory
    throw new RuntimeException("Fail to create root directory: " + ROOT_DIR);
  }

  /** Copies initial files to the root directory. */
  private void initializeFiles() {
    if (!ChamberShepherd.CHAMBER_ROOT_DIR.toFile().mkdirs()) {
      throw new RuntimeException("Fail to create chamber root directory: " + ROOT_DIR);
    }
  }
}
