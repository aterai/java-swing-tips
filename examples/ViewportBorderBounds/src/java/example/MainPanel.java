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
import javax.swing.plaf.LayerUI;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(makeStyleSheet());
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    editor.setSelectedTextColor(null);
    editor.setSelectionColor(new Color(0x64_88_AA_AA, true));
    editor.setBackground(new Color(0x64_64_64)); // 0x33_33_33

    ScriptEngine engine = ScriptEngineUtils.createEngine();
    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().getAbsolutePath(), engine, editor);
      }
    });

    JScrollPane scroll = new JScrollPane(editor) {
      @Override public void updateUI() {
        super.updateUI();
        setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
      }
    };
    JCheckBox check = new JCheckBox("HORIZONTAL_SCROLLBAR_NEVER", true);
    check.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      int p = f ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
          : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
      scroll.setHorizontalScrollBarPolicy(p);
    });

    JPanel box = new JPanel();
    box.add(check);
    box.add(button);

    add(new JLayer<>(scroll, new ScrollPaneLayerUI()));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static StyleSheet makeStyleSheet() {
    // https://github.com/google/code-prettify/blob/master/styles/desert.css
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str {color:#ffa0a0}");
    styleSheet.addRule(".kwd {color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".com {color:#87ceeb}");
    styleSheet.addRule(".typ {color:#98fb98}");
    styleSheet.addRule(".lit {color:#cd5c5c}");
    styleSheet.addRule(".pun {color:#ffffff}");
    styleSheet.addRule(".pln {color:#ffffff}");
    styleSheet.addRule(".tag {color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".atn {color:#bdb76b;font-weight:bold}");
    styleSheet.addRule(".atv {color:#ffa0a0}");
    styleSheet.addRule(".dec {color:#98fb98}");
    return styleSheet;
  }

  private static void loadFile(String path, ScriptEngine engine, JEditorPane editor) {
    try (Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
      String txt = lines
          .map(s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
          .collect(Collectors.joining("\n"));
      editor.setText("<pre>" + ScriptEngineUtils.prettify(engine, txt) + "\n</pre>");
    } catch (IOException ex) {
      // ex.printStackTrace();
      editor.setText(ex.getMessage());
    }
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

class ScrollPaneLayerUI extends LayerUI<JScrollPane> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      Rectangle rect = scroll.getViewportBorderBounds();
      BoundedRangeModel m = scroll.getHorizontalScrollBar().getModel();
      int extent = m.getExtent();
      int maximum = m.getMaximum();
      int value = m.getValue();
      if (value + extent < maximum) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new Color(0x08_00_00_00, true));
        int shd = 6;
        int w = rect.width;
        int h = rect.height;
        g2.translate(rect.x + w - shd, rect.y);
        for (int i = 0; i < shd; i++) {
          g2.fillRect(i, 0, shd - i, h);
        }
        // g2.setPaint(Color.RED);
        g2.fillRect(shd - 2, 0, 2, h); // Make the edge a bit darker
        g2.dispose();
      }
    }
  }
}

final class ScriptEngineUtils {
  private ScriptEngineUtils() {
    /* Singleton */
  }

  public static ScriptEngine createEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");

    // String p = "https://raw.githubusercontent.com/google/code-prettify/f5ad44e3253f1bc8e288477a36b2ce5972e8e161/src/prettify.js";
    // URL url = new URL(p);
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
      // ex.printStackTrace();
      printTxt = ex.getMessage();
    }
    return printTxt;
  }
}
