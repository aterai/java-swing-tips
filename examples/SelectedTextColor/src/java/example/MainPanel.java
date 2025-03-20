// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
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
  private final JEditorPane editor1 = new JEditorPane();
  private final JEditorPane editor2 = new JEditorPane();

  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(makeStyleSheet());

    Stream.of(editor1, editor2).forEach(e -> {
      e.setEditorKit(htmlEditorKit);
      e.setEditable(false);
      // e.setSelectionColor(new Color(0x64_88_AA_AA, true));
      e.setBackground(new Color(0xEE_EE_EE));
    });

    editor2.setSelectedTextColor(null);
    editor2.setSelectionColor(new Color(0x64_88_AA_AA, true));
    // TEST: editor2.setSelectionColor(null);

    ScriptEngine engine = ScriptEngineUtils.createEngine();
    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().getAbsolutePath(), engine);
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(Box.createHorizontalGlue());
    box.add(button);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(new JScrollPane(editor1));
    p.add(new JScrollPane(editor2));
    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void loadFile(String path, ScriptEngine engine) {
    try (Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
      String txt = lines
          .map(s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
          .collect(Collectors.joining("\n"));
      String html = "<pre>" + ScriptEngineUtils.prettify(engine, txt) + "\n</pre>";
      editor1.setText(html);
      editor2.setText(html);
    } catch (IOException ex) {
      // Logger.getGlobal().severe(ex::getMessage);
      editor1.setText(ex.getMessage());
      editor2.setText(ex.getMessage());
    }
  }

  private static StyleSheet makeStyleSheet() {
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
    return styleSheet;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class ScriptEngineUtils {
  private ScriptEngineUtils() {
    /* Singleton */
  }

  public static ScriptEngine createEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");

    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    URL url = cl.getResource("example/prettify.js");
    try {
      assert url != null;
      try (Reader reader = Files.newBufferedReader(Paths.get(url.toURI()))) {
        engine.eval("var window={}, navigator=null;");
        engine.eval(reader);
      }
    } catch (IOException | ScriptException | URISyntaxException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      Toolkit.getDefaultToolkit().beep();
    }
    return engine;
  }

  public static String prettify(ScriptEngine engine, String src) {
    String printTxt;
    try {
      Object w = engine.get("window");
      printTxt = (String) ((Invocable) engine).invokeMethod(w, "prettyPrintOne", src);
    } catch (ScriptException | NoSuchMethodException ex) {
      printTxt = ex.getMessage();
    }
    return printTxt;
  }
}
