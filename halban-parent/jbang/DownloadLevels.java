///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 23+
//REPOS mavenlocal,mavencentral
//DEPS no.hal.halban:sokoban-levels:1.0-SNAPSHOT

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import no.hal.sokoban.impl.SokobanFactoryImpl;
import no.hal.sokoban.level.SokobanLevel;
import no.hal.sokoban.levels.DownloadableLevelCollectionsProvider;
import no.hal.sokoban.levels.borgarnet.BorgarNetLevelCollectionsProvider;
import no.hal.sokoban.levels.sourcecodese.SourcecodeSeLevelCollectionsProvider;
import no.hal.sokoban.parser.SokobanParser;
import no.hal.sokoban.parser.SokobanSerializer;

/**
 * Downloads the levels of the collection providers that download their levels, into folders that are like the tree of the app:
 * a folder for each provider (named after the host, as in the tree), and in it a text file for each collection (named after its title),
 * which has all the levels of the collection, one after the other with a blank line between them.
 * <p>
 * With {@code --canonical} the files are in the standard format of the app instead, which is what its parser reads without any special rules:
 * the properties of the collection first ({@code Title:}, {@code Author:} and {@code Description:} without any comment marks), and then the levels, each with
 * its own properties in front of the grid. Each file is read again by that parser, and compared to the levels that were downloaded, before it is accepted.
 * That is the same however the site writes its levels, which is the point of it.
 * <p>
 * The classes of the app are used, so the providers, the collections and the levels are the same as in the app, and so is the parsing.
 * They are found in the local maven repository, so build the app first: {@code mvn -f halban-parent/pom.xml install}.
 * <p>
 * Usage: {@code jbang DownloadLevels.java [options] [provider...]}
 * <ul>
 * <li>provider: part of the host of the providers that are downloaded, e.g. {@code borgar} or {@code sourcecode}. All of them are by default.</li>
 * <li>{@code --out <folder>}: where the folders are made, {@code downloaded-levels} (in the folder that the script is run from, and ignored by git) by default.</li>
 * <li>{@code --limit <n>}: only the first n collections of each provider, to try it out.</li>
 * <li>{@code --delay <millis>}: the wait before each collection is downloaded, 250 by default, to be kind to the servers.</li>
 * <li>{@code --force}: download collections that are in the folder already, which are otherwise skipped, so an interrupted run can be continued.</li>
 * <li>{@code --canonical}: write the standard format of the app, which is checked by reading it again.</li>
 * <li>{@code --list}: only lists the providers and their collections.</li>
 * </ul>
 */
public class DownloadLevels {

  private static final SokobanSerializer SERIALIZER = new SokobanSerializer();
  private static final SokobanParser PARSER = new SokobanParser(new SokobanFactoryImpl());

  public static void main(String[] args) throws Exception {
    var out = Path.of("downloaded-levels");
    int limit = Integer.MAX_VALUE;
    long delay = 250;
    boolean force = false, listOnly = false, canonical = false;
    var providerNames = new ArrayList<String>();
    for (int i = 0; i < args.length; i++) {
      switch (args[i]) {
        case "--out" -> out = Path.of(args[++i]);
        case "--limit" -> limit = Integer.parseInt(args[++i]);
        case "--delay" -> delay = Long.parseLong(args[++i]);
        case "--force" -> force = true;
        case "--canonical" -> canonical = true;
        case "--list" -> listOnly = true;
        case "--help", "-h" -> {
          System.out.println("Usage: jbang DownloadLevels.java [--out folder] [--limit n] [--delay millis] [--force] [--canonical] [--list] [provider...]");
          return;
        }
        default -> providerNames.add(args[i].toLowerCase());
      }
    }
    // the providers that the app has, which download their levels (the others have them in the app)
    List<DownloadableLevelCollectionsProvider> providers = List.of(
        new SourcecodeSeLevelCollectionsProvider(),
        new BorgarNetLevelCollectionsProvider());
    int downloaded = 0, skipped = 0, failed = 0;
    for (var provider : providers) {
      var host = new DownloadableLevelCollectionsProvider.LabelAdapter().getText(provider);
      if (! providerNames.isEmpty() && providerNames.stream().noneMatch(host.toLowerCase()::contains)) {
        continue;
      }
      System.out.println(host);
      var collections = provider.getSokobanLevelCollections();
      System.out.println("  " + collections.size() + " collections");
      var providerFolder = out.resolve(fileName(host));
      int count = 0;
      for (var collection : collections) {
        if (count++ >= limit) {
          break;
        }
        var title = collection.getMetaData("Title");
        if (title == null || title.isBlank()) {
          title = "collection-" + count;
        }
        if (listOnly) {
          System.out.println("  " + title);
          continue;
        }
        var file = uniqueFile(providerFolder, fileName(title), ".txt");
        if (! force && Files.exists(file)) {
          skipped++;
          continue;
        }
        Thread.sleep(delay);
        try {
          var levels = collection.getSokobanLevels();
          if (levels == null || levels.isEmpty()) {
            System.out.println("  " + title + ": no levels");
            failed++;
            continue;
          }
          var text = canonical ? toCanonicalText(collection, levels) : toText(collection, levels);
          if (canonical) {
            var problem = checkCanonical(text, levels);
            if (problem != null) {
              System.out.println("  " + title + ": not written, " + problem);
              failed++;
              continue;
            }
          }
          Files.createDirectories(providerFolder);
          Files.writeString(file, text, StandardCharsets.UTF_8);
          System.out.println("  " + title + ": " + levels.size() + " levels, " + out.relativize(file));
          downloaded++;
        } catch (IOException | RuntimeException e) {
          System.out.println("  " + title + ": failed, " + e);
          failed++;
        }
      }
    }
    if (! listOnly) {
      System.out.println("Downloaded %d collections, skipped %d that were there, %d failed or had no levels".formatted(downloaded, skipped, failed));
    }
  }

  /** The levels as text that is read by Sokoban programs: the title and the author of the collection as comments, and a title in front of each level that has one. */
  private static String toText(SokobanLevel.Collection collection, List<SokobanLevel> levels) {
    var text = new StringBuilder();
    for (var property : List.of("Title", "Author", "Description")) {
      var value = collection.getMetaData(property);
      if (value != null && ! value.isBlank()) {
        text.append("; ").append(property).append(": ").append(value.replace('\n', ' ')).append("\n");
      }
    }
    text.append("\n");
    int number = 0;
    for (var level : levels) {
      number++;
      var title = level.getMetaData("Title");
      text.append("Title: ").append(title != null && ! title.isBlank() ? title.replace('\n', ' ') : String.valueOf(number)).append("\n");
      var author = level.getMetaData("Author");
      if (author != null && ! author.isBlank()) {
        text.append("Author: ").append(author.replace('\n', ' ')).append("\n");
      }
      text.append(SERIALIZER.toString(level.getSokobanGrid(), null)).append("\n");
    }
    return text.toString();
  }

  /**
   * The levels in the standard format of the app: the properties of the collection, then the levels, each with its properties and its grid, with blank lines between them.
   * The properties are those that mean something in the levels themselves, as the others (the hash, the address) are made when the levels are read.
   */
  private static String toCanonicalText(SokobanLevel.Collection collection, List<SokobanLevel> levels) {
    var text = new StringBuilder();
    boolean header = appendProperties(text, collection::getMetaData);
    if (header) {
      text.append("\n");
    }
    for (var level : levels) {
      appendProperties(text, level::getMetaData);
      text.append(gridText(level)).append("\n\n");
    }
    return text.toString();
  }

  private static boolean appendProperties(StringBuilder text, java.util.function.Function<String, String> properties) {
    boolean any = false;
    for (var property : List.of("Title", "Author", "Description")) {
      var value = properties.apply(property);
      if (value != null && ! value.isBlank()) {
        // one line, and without a colon in front of the value that the parser would take as part of the name
        text.append(property).append(": ").append(value.replaceAll("\\s+", " ").strip()).append("\n");
        any = true;
      }
    }
    return any;
  }

  /** The rows of the grid, without the spaces at the end of them, as a row that ends in spaces is not recognized as a row of a grid. */
  private static String gridText(SokobanLevel level) {
    return SERIALIZER.toString(level.getSokobanGrid(), null).lines().map(row -> row.stripTrailing()).collect(java.util.stream.Collectors.joining("\n"));
  }

  /** Reads the text with the parser of the app and compares with the levels, which are the same if the number, the grids and the titles are. Null if they are, otherwise what is not. */
  private static String checkCanonical(String text, List<SokobanLevel> levels) {
    try {
      var read = PARSER.parse(text).getSokobanLevels();
      if (read.size() != levels.size()) {
        return "it has %d levels when read again, and %d were downloaded".formatted(read.size(), levels.size());
      }
      for (int i = 0; i < levels.size(); i++) {
        if (! gridText(read.get(i)).equals(gridText(levels.get(i)))) {
          return "the grid of level %d is not the same when read again".formatted(i + 1);
        }
        if (! normalized(read.get(i).getMetaData("Title")).equals(normalized(levels.get(i).getMetaData("Title")))) {
          return "the title of level %d is not the same when read again".formatted(i + 1);
        }
      }
      return null;
    } catch (IOException | RuntimeException e) {
      return "it can't be read again: " + e;
    }
  }

  private static String normalized(String text) {
    return text == null ? "" : text.replaceAll("\\s+", " ").strip();
  }

  /** A name that is fine as the name of a file everywhere. */
  private static String fileName(String name) {
    var cleaned = name.strip().replaceAll("[\\\\/:*?\"<>|\\p{Cntrl}]", "_").replaceAll("\\s+", " ");
    cleaned = cleaned.replaceAll("^\\.+", "_");
    return cleaned.isEmpty() ? "_" : cleaned.length() > 120 ? cleaned.substring(0, 120).strip() : cleaned;
  }

  /** The file of the name, or one with a number added if the title of another collection gave the same name in this run, so no collection overwrites another. */
  private static final java.util.Set<Path> USED = new java.util.HashSet<>();

  private static Path uniqueFile(Path folder, String name, String extension) {
    var file = folder.resolve(name + extension);
    for (int n = 2; ! USED.add(file); n++) {
      file = folder.resolve(name + " (" + n + ")" + extension);
    }
    return file;
  }
}
