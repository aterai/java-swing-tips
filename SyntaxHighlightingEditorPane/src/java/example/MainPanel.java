// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private final JEditorPane editor = new JEditorPane();
  private final ScriptEngine engine = createEngine();

  private MainPanel() {
    super(new BorderLayout());

    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str {color:#008800}");
    styleSheet.addRule(".kwd {color:#000088}");
    styleSheet.addRule(".com {color:#880000}");
    styleSheet.addRule(".typ {color:#660066}");
    styleSheet.addRule(".lit {color:#006666}");
    styleSheet.addRule(".pun {color:#666600}");
    styleSheet.addRule(".pln {color:#000000}");
    styleSheet.addRule(".tag {color:#000088}");
    styleSheet.addRule(".atn {color:#660066}");
    styleSheet.addRule(".atv {color:#008800}");
    styleSheet.addRule(".dec {color:#660066}");

    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);

    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(Box.createHorizontalGlue());
    box.add(button);

    add(new JScrollPane(editor));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void loadFile(String path) {
    try (Stream<String> lines = Files.lines(Paths.get(path), Charset.forName("UTF-8"))) {
      String txt = lines.map(s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
          .collect(Collectors.joining("\n"));
      editor.setText("<pre>" + prettify(engine, txt) + "\n</pre>");
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private static ScriptEngine createEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");
    try {
      URI uri = MainPanel.class.getResource("prettify.js").toURI();
      // https://stackoverflow.com/questions/22605666/java-access-files-in-jar-causes-java-nio-file-filesystemnotfoundexception
      for (FileSystemProvider provider: FileSystemProvider.installedProviders()) {
        if (provider.getScheme().equalsIgnoreCase("jar")) {
          try {
            provider.getFileSystem(uri);
          } catch (FileSystemNotFoundException e) {
            // in this case we need to initialize it first:
            provider.newFileSystem(uri, Collections.emptyMap());
          }
        }
      }
      Path path = Paths.get(uri);
      // Path path = Paths.get(MainPanel.class.getResource("prettify.js").toURI());

      // String p = "https://raw.githubusercontent.com/google/code-prettify/f5ad44e3253f1bc8e288477a36b2ce5972e8e161/src/prettify.js";
      // try (Reader r = new BufferedReader(new InputStreamReader(new URL(p).openStream(), StandardCharsets.UTF_8))) {
      try (Reader r = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
        engine.eval("var window={}, navigator=null;");
        engine.eval(r);
        return engine;
      }
    } catch (IOException | ScriptException | URISyntaxException ex) {
      ex.printStackTrace();
    }
    return null;
  }

  private static String prettify(ScriptEngine engine, String src) {
    try {
      Object w = engine.get("window");
      return (String) ((Invocable) engine).invokeMethod(w, "prettyPrintOne", src);
    } catch (ScriptException | NoSuchMethodException ex) {
      ex.printStackTrace();
      return "";
    }
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
