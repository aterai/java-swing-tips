// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private final JEditorPane editor = new JEditorPane();
  private final JScrollPane scroll = new JScrollPane(editor) {
    @Override public void updateUI() {
      super.updateUI();
      setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
    }
  };
  private final JLabel label = new MiniMapLabel(scroll);
  private final JCheckBox check = new JCheckBox("minimap", true);

  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(createStyleSheet());

    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    editor.setSelectedTextColor(null);
    editor.setSelectionColor(new Color(0x64_88_AA_AA, true));
    editor.setBackground(new Color(0xEE_EE_EE));
    editor.addPropertyChangeListener("page", e -> updateMiniMap());
    editor.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        // The HTML is reflowed when the editor width changes,
        // so the minimap image must be regenerated
        updateMiniMap();
      }
    });

    String path = "example/test.html";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Optional.ofNullable(url).ifPresent(this::loadPage);

    JPanel labelPanel = new JPanel(new BorderLayout(0, 0));
    labelPanel.add(label, BorderLayout.NORTH);
    JScrollPane minimap = new JScrollPane(labelPanel);
    minimap.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
    minimap.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

    check.addActionListener(e -> updateMiniMap());

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(check);

    JScrollBar verticalScrollBar = scroll.getVerticalScrollBar();
    verticalScrollBar.getModel().addChangeListener(e -> label.repaint());
    JPanel overlayPanel = new JPanel(new MiniMapLayout(verticalScrollBar)) {
      @Override public boolean isOptimizedDrawingEnabled() {
        return false;
      }
    };
    overlayPanel.add(minimap, BorderLayout.EAST);
    overlayPanel.add(scroll);

    add(overlayPanel);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void loadPage(URL url) {
    try {
      editor.setPage(url);
    } catch (IOException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(editor);
      editor.setText(ex.getMessage());
    }
  }

  private void updateMiniMap() {
    label.setIcon(check.isSelected() ? MiniMapLabel.createMiniMapIcon(editor) : null);
    revalidate();
    repaint();
  }

  private static StyleSheet createStyleSheet() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str{color:#008800}");
    styleSheet.addRule(".kwd{color:#000088}");
    styleSheet.addRule(".com{color:#880000}");
    styleSheet.addRule(".typ{color:#660066}");
    styleSheet.addRule(".lit{color:#006666}");
    styleSheet.addRule(".pun{color:#666600}");
    styleSheet.addRule(".pln{color:#000000}");
    styleSheet.addRule(".tag{color:#000088}");
    styleSheet.addRule(".atn{color:#660066}");
    styleSheet.addRule(".atv{color:#008800}");
    styleSheet.addRule(".dec{color:#660066}");
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

class MiniMapLabel extends JLabel {
  private static final float SCALE = .15f;
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

  public static Icon createMiniMapIcon(Component c) {
    Dimension size = c.getSize();
    int width = Math.max(1, Math.round(size.width * SCALE));
    int height = Math.max(1, Math.round(size.height * SCALE));
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = image.createGraphics();
    g2.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g2.scale(SCALE, SCALE);
    c.print(g2);
    g2.dispose();
    return new ImageIcon(image);
  }

  // Calculating the thumb rectangle shared
  // by paintComponent and processMiniMapMouseEvent
  private Rectangle computeThumbRect() {
    JViewport viewport = scroll.getViewport();
    Rectangle innerRect = SwingUtilities.calculateInnerArea(this, null);
    Rectangle thumbRect = new Rectangle(innerRect);
    thumbRect.height = 0;
    int viewHeight = viewport.getView().getHeight();
    if (innerRect.height > 0 && viewHeight > 0) {
      // Scale factor from the editor (view) height to the minimap label height
      double sy = innerRect.getHeight() / viewHeight;
      int viewY = viewport.getViewPosition().y;
      int extent = viewport.getExtentSize().height;
      int y = (int) Math.round(viewY * sy);
      thumbRect.y += y;
      thumbRect.height = (int) Math.round((viewY + extent) * sy) - y;
    }
    return thumbRect;
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Rectangle r = computeThumbRect();

    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setColor(THUMB_COLOR);
    g2.fillRect(r.x, r.y, r.width, r.height);
    g2.setColor(THUMB_COLOR.darker());
    g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
    g2.dispose();
  }

  private final class MiniMapHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      processMiniMapMouseEvent(e);
    }

    @Override public void mouseDragged(MouseEvent e) {
      processMiniMapMouseEvent(e);
    }

    private void processMiniMapMouseEvent(MouseEvent e) {
      Rectangle innerRect = SwingUtilities.calculateInnerArea(MiniMapLabel.this, null);
      if (innerRect.height > 0) {
        // Center the visible area (thumb) on the clicked position
        BoundedRangeModel m = scroll.getVerticalScrollBar().getModel();
        int range = m.getMaximum() - m.getMinimum();
        float y = (e.getY() - innerRect.y) * range / (float) innerRect.height;
        int value = m.getMinimum() + Math.round(y - m.getExtent() / 2f);
        m.setValue(value); // Scroll main editor side

        // The display position of the minimap itself will also follow
        // the position where the thumb (window) can be seen.
        scrollRectToVisible(computeThumbRect());
      }
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
      Component east = getLayoutComponent(parent, EAST);
      if (Objects.nonNull(east)) {
        Dimension d = east.getPreferredSize();
        // Place the minimap just to the left of the vertical scroll bar
        int vsbWidth = vsb.isVisible() ? vsb.getWidth() : 0;
        east.setBounds(right - d.width - vsbWidth, top, d.width, bottom - top);
      }
      Component center = getLayoutComponent(parent, CENTER);
      if (Objects.nonNull(center)) {
        center.setBounds(left, top, right - left, bottom - top);
      }
    }
  }
}
