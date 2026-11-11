package no.hal.fx;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import javafx.scene.text.Font;

/**
 * Registers the font of the Ikonli icons (Material Design Icons 2) by name, which is what an icon says in its style
 * ({@code -fx-font-family: 'Material Design Icons'}), before the first icon is made, so call {@link #load()} when the application starts.
 * <p>
 * Ikonli loads the font itself from a url, which gives no font in a native app on Android (the icons are boxes with a cross):
 * JavaFX copies the font to a temporary file first, which fails there, and a stream gives no font either (JavaFX 24, Gluon's GraalVM 23).
 * A file does, so it is tried when a stream (as on the desktop) gives no font: the font is written to a file in the temporary directory,
 * which is the private directory of the app on Android. This needs no Ikonli classes, only the font file, which is found
 * through the class loader, since it is in another module than this one.
 */
public final class IconFonts {

  private IconFonts() {
  }

  private static final String MATERIAL_DESIGN_ICONS_2 = "META-INF/resources/materialdesignicons2/5.8.55/fonts/materialdesignicons-webfont.ttf";

  private static boolean loaded;

  /** Registers the font, the first time it is called. */
  public static synchronized void load() {
    if (loaded) {
      return;
    }
    loaded = true;
    load(MATERIAL_DESIGN_ICONS_2, "halban-mdi2.ttf");
  }

  private static void load(String resource, String fileName) {
    try {
      byte[] bytes;
      try (var stream = IconFonts.class.getClassLoader().getResourceAsStream(resource)) {
        bytes = stream.readAllBytes();
      }
      if (Font.loadFont(new ByteArrayInputStream(bytes), 16) == null) {
        // the file stays, since the font may be read from it later, and is replaced the next time
        var file = Path.of(System.getProperty("java.io.tmpdir"), fileName);
        Files.write(file, bytes);
        if (Font.loadFont(file.toUri().toString(), 16) == null) {
          System.err.println("The icon font " + resource + " could not be loaded, so its icons are boxes");
        }
      }
    } catch (Exception e) {
      System.err.println("The icon font " + resource + " could not be loaded, so its icons are boxes: " + e);
    }
  }
}
