// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
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
  private static final float SCALE = .15f;
  private final JEditorPane editor = new JEditorPane();
  private final JScrollPane scroll = new JScrollPane(editor) {
    @Override public void updateUI() {
      super.updateUI();
      setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    }
  };
  private final JLabel label = new MiniMapLabel(scroll);

  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(makeStyleSheet());

    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    editor.setSelectedTextColor(null);
    editor.setSelectionColor(new Color(0x64_88_AA_AA, true));
    editor.setBackground(new Color(0xEE_EE_EE));

    ScriptEngine engine = ScriptEngineUtils.createEngine();
    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().getAbsolutePath(), engine);
      }
    });

    scroll.getVerticalScrollBar().getModel().addChangeListener(e -> label.repaint());

    JPanel pp = new JPanel(new BorderLayout(0, 0));
    pp.add(label, BorderLayout.NORTH);
    JScrollPane minimap = new JScrollPane(pp);
    minimap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    minimap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(Box.createHorizontalGlue());
    box.add(button);

    JPanel p = new JPanel(new MiniMapLayout(scroll.getVerticalScrollBar())) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.add(minimap, BorderLayout.EAST);
    p.add(scroll);

    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void loadFile(String path, ScriptEngine engine) {
    try (Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
      String txt = lines
          .map(s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
          .collect(Collectors.joining("\n"));
      editor.setText("<pre>" + ScriptEngineUtils.prettify(engine, txt) + "\n</pre>");
    } catch (IOException ex) {
      editor.setText(ex.getMessage());
    }
    EventQueue.invokeLater(() -> {
      label.setIcon(makeMiniMapImageIcon(editor));
      revalidate();
      repaint();
    });
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

  private static Icon makeMiniMapImageIcon(Component c) {
    Dimension d = c.getSize();
    int newW = Math.round(d.width * SCALE);
    int newH = Math.round(d.height * SCALE);
    BufferedImage image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.scale(SCALE, SCALE);
    c.print(g2);
    g2.dispose();
    return new ImageIcon(image);
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

class MiniMapLabel extends JLabel {
  private static final Color THUMB_COLOR = new Color(0x32_00_00_FF, true);
  private final JScrollPane scroll;
  private transient MouseAdapter handler;

  protected MiniMapLabel(JScrollPane scroll) {
    super();
    this.scroll = scroll;
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    super.updateUI();
    handler = new MiniMapHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    JViewport viewport = scroll.getViewport();
    // Rectangle vr = scroll.getViewport().getVisibleRect(); // = viewport.getBounds();
    Rectangle vr = viewport.getBounds();
    Rectangle er = scroll.getViewport().getView().getBounds();
    Rectangle cr = SwingUtilities.calculateInnerArea(this, new Rectangle());

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    double sy = cr.getHeight() / er.getHeight();
    AffineTransform at = AffineTransform.getScaleInstance(1d, sy);

    // paint Thumb
    Rectangle thumbRect = new Rectangle(vr);
    thumbRect.y = viewport.getViewPosition().y;
    Rectangle r = at.createTransformedShape(thumbRect).getBounds();
    int y = cr.y + r.y;
    g2.setColor(THUMB_COLOR);
    g2.fillRect(0, y, cr.width, r.height);
    g2.setColor(THUMB_COLOR.darker());
    g2.drawRect(0, y, cr.width - 1, r.height - 1);
    g2.dispose();
  }

  private final class MiniMapHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      processMiniMapMouseEvent(e);
    }

    @Override public void mouseDragged(MouseEvent e) {
      processMiniMapMouseEvent(e);
    }

    public void processMiniMapMouseEvent(MouseEvent e) {
      Point pt = e.getPoint();
      Component c = e.getComponent();
      BoundedRangeModel m = scroll.getVerticalScrollBar().getModel();
      int range = m.getMaximum() - m.getMinimum();
      int iv = Math.round(pt.y * range / (float) c.getHeight() - m.getExtent() / 2f);
      m.setValue(iv);
    }
  }
}

class MiniMapLayout extends BorderLayout {
  private final JScrollBar vsb;

  protected MiniMapLayout(JScrollBar vsb) {
    super(0, 0);
    this.vsb = vsb;
  }

  @SuppressWarnings("PMD.AvoidSynchronizedStatement")
  @Override public void layoutContainer(Container parent) {
    synchronized (parent.getTreeLock()) {
      Insets insets = parent.getInsets();
      int width = parent.getWidth();
      int height = parent.getHeight();
      int top = insets.top;
      int bottom = height - insets.bottom;
      int left = insets.left;
      int right = width - insets.right;
      Component ec = getLayoutComponent(parent, EAST);
      if (Objects.nonNull(ec)) {
        Dimension d = ec.getPreferredSize();
        // JScrollBar vsb = scroll.getVerticalScrollBar();
        int vsw = vsb.isVisible() ? vsb.getSize().width : 0;
        ec.setBounds(right - d.width - vsw, top, d.width, bottom - top);
      }
      Component cc = getLayoutComponent(parent, CENTER);
      if (Objects.nonNull(cc)) {
        cc.setBounds(left, top, right - left, bottom - top);
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
