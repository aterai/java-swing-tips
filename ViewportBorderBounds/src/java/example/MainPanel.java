// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
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
  private final JEditorPane editor = new JEditorPane();
  private final ScriptEngine engine = createEngine();

  private MainPanel() {
    super(new BorderLayout());

    // https://github.com/google/code-prettify/blob/master/styles/desert.css
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str {color:#ffa0a0}");
    styleSheet.addRule(".kwd {color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".com {color:#87ceeb0}");
    styleSheet.addRule(".typ {color:#98fb98}");
    styleSheet.addRule(".lit {color:#cd5c5c}");
    styleSheet.addRule(".pun {color:#ffffff}");
    styleSheet.addRule(".pln {color:#ffffff}");
    styleSheet.addRule(".tag {color:#f0e68c;font-weight:bold}");
    styleSheet.addRule(".atn {color:#bdb76b;font-weight:bold}");
    styleSheet.addRule(".atv {color:#ffa0a0}");
    styleSheet.addRule(".dec {color:#98fb98}");

    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    editor.setSelectedTextColor(null);
    editor.setSelectionColor(new Color(0x64_88_AA_AA, true));
    editor.setBackground(new Color(0x64_64_64)); // 0x33_33_33

    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().getAbsolutePath());
      }
    });

    JScrollPane scroll = new JScrollPane(editor);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    JCheckBox check = new JCheckBox("HORIZONTAL_SCROLLBAR_NEVER", true);
    check.addActionListener(e -> {
      boolean f = ((JCheckBox) e.getSource()).isSelected();
      int p = f ? ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER
                : ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
      scroll.setHorizontalScrollBarPolicy(p);
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(button);

    add(new JLayer<>(scroll, new ScrollPaneLayerUI()));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void loadFile(String path) {
    try (Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
      String txt = lines.map(s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
          .collect(Collectors.joining("\n"));
      editor.setText("<pre>" + prettify(engine, txt) + "\n</pre>");
    } catch (IOException ex) {
      ex.printStackTrace();
      editor.setText(ex.getMessage());
    }
  }

  private static ScriptEngine createEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");

    // String p = "https://raw.githubusercontent.com/google/code-prettify/f5ad44e3253f1bc8e288477a36b2ce5972e8e161/src/prettify.js";
    // URL url = new URL(p);
    URL url = MainPanel.class.getResource("prettify.js");
    try (Reader r = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
      engine.eval("var window={}, navigator=null;");
      engine.eval(r);
    } catch (IOException | ScriptException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    return engine;
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
      Toolkit.getDefaultToolkit().beep();
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
        int w = rect.width;
        int h = rect.height;
        int pad = 6;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(rect.x + w - pad, rect.y);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(new Color(0x08_00_00_00, true));
        for (int i = 0; i < pad; i++) {
          g2.fillRect(i, 0, pad - i, h);
        }
        // g2.setPaint(Color.RED);
        g2.fillRect(pad - 2, 0, 2, h); // Make the edge a bit darker
        g2.dispose();
      }
    }
  }
}
