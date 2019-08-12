// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private static final Color THUMB_COLOR = new Color(0x32_00_00_FF, true);
  private static final double SCALE = .15;
  private final ScriptEngine engine = createEngine();
  private final JEditorPane editor = new JEditorPane();
  private final JScrollPane scroll = new JScrollPane(editor);
  private final JLabel label = new JLabel() {
    private transient MouseInputListener handler;
    @Override public void updateUI() {
      removeMouseListener(handler);
      removeMouseMotionListener(handler);
      super.updateUI();
      handler = new MiniMapHandler();
      addMouseListener(handler);
      addMouseMotionListener(handler);
    }

    @Override public void paintComponent(Graphics g) {
      super.paintComponent(g);
      Container c = SwingUtilities.getAncestorOfClass(JViewport.class, editor);
      if (!(c instanceof JViewport) || editor == null) {
        return;
      }
      JViewport vport = (JViewport) c;
      Rectangle vrect = vport.getBounds(); // scroll.getViewportBorderBounds();
      Rectangle erect = editor.getBounds();
      Rectangle crect = SwingUtilities.calculateInnerArea(this, new Rectangle());

      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      double sy = crect.getHeight() / erect.getHeight();
      AffineTransform at = AffineTransform.getScaleInstance(1d, sy);

      // paint Thumb
      Rectangle thumbRect = new Rectangle(vrect);
      thumbRect.y = vport.getViewPosition().y;
      Rectangle r = at.createTransformedShape(thumbRect).getBounds();
      int y = crect.y + r.y;
      g2.setColor(THUMB_COLOR);
      g2.fillRect(0, y, crect.width, r.height);
      g2.setColor(THUMB_COLOR.darker());
      g2.drawRect(0, y, crect.width - 1, r.height - 1);
      g2.dispose();
    }
  };

  private class MiniMapHandler extends MouseInputAdapter {
    @Override public void mousePressed(MouseEvent e) {
      processMiniMapMouseEvent(e);
    }

    @Override public void mouseDragged(MouseEvent e) {
      processMiniMapMouseEvent(e);
    }

    protected final void processMiniMapMouseEvent(MouseEvent e) {
      Point pt = e.getPoint();
      Component c = e.getComponent();
      BoundedRangeModel m = scroll.getVerticalScrollBar().getModel();
      int iv = (int) (.5 - m.getExtent() * .5 + pt.y * (m.getMaximum() - m.getMinimum()) / (double) c.getHeight());
      m.setValue(iv);
    }
  }

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
    editor.setBackground(new Color(0xEE_EE_EE));
    editor.setSelectedTextColor(null);
    editor.setSelectionColor(new Color(0x64_88_AA_AA, true));

    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().getAbsolutePath());
        EventQueue.invokeLater(() -> {
          label.setIcon(makeMiniMap(editor));
          revalidate();
          repaint();
        });
      }
    });

    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
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

    JPanel p = new JPanel() {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    p.setLayout(new BorderLayout(0, 0) {
      @Override public void layoutContainer(Container parent) {
        synchronized (parent.getTreeLock()) {
          Insets insets = parent.getInsets();
          int width = parent.getWidth();
          int height = parent.getHeight();
          int top = insets.top;
          int bottom = height - insets.bottom;
          int left = insets.left;
          int right = width - insets.right;
          Component ec = getLayoutComponent(parent, BorderLayout.EAST);
          if (Objects.nonNull(ec)) {
            Dimension d = ec.getPreferredSize();
            JScrollBar vsb = scroll.getVerticalScrollBar();
            int vsw = vsb.isVisible() ? vsb.getSize().width : 0;
            ec.setBounds(right - d.width - vsw, top, d.width, bottom - top);
          }
          Component cc = getLayoutComponent(parent, BorderLayout.CENTER);
          if (Objects.nonNull(cc)) {
            cc.setBounds(left, top, right - left, bottom - top);
          }
        }
      }
    });
    p.add(minimap, BorderLayout.EAST);
    p.add(scroll);
    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Icon makeMiniMap(Component c) {
    Dimension d = c.getSize();
    int newW = (int) (d.width * SCALE);
    int newH = (int) (d.height * SCALE);
    BufferedImage image = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.scale(SCALE, SCALE);
    c.paint(g2);
    g2.dispose();
    return new ImageIcon(image);
  }

  private void loadFile(String path) {
    try (Stream<String> lines = Files.lines(Paths.get(path), StandardCharsets.UTF_8)) {
      String txt = lines.map(s -> s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;"))
          .collect(Collectors.joining("\n"));
      String html = "<pre>" + prettify(engine, txt) + "\n</pre>";
      editor.setText(html);
      editor.moveCaretPosition(0);
    } catch (IOException ex) {
      ex.printStackTrace();
      editor.setText(ex.getMessage());
    }
  }

  private static ScriptEngine createEngine() {
    ScriptEngineManager manager = new ScriptEngineManager();
    ScriptEngine engine = manager.getEngineByName("JavaScript");
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
