// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JScrollPane scroll = new JScrollPane();
  private final JScrollBar verticalBar = scroll.getVerticalScrollBar();
  private final JScrollBar horizontalBar = scroll.getHorizontalScrollBar();
  private final JScrollBar zeroVerticalBar = new JScrollBar(Adjustable.VERTICAL) {
    @Override public boolean isVisible() {
      return !isShiftPressed() && super.isVisible();
    }

    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 0;
      return d;
    }
  };
  private final JScrollBar zeroHorizontalBar = new JScrollBar(Adjustable.HORIZONTAL) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.height = 0;
      return d;
    }
  };
  private boolean shiftPressed;

  private MainPanel() {
    super(new BorderLayout());
    JLabel label = makeImageLabel();
    MouseAdapter ml = new DragScrollListener();
    label.addMouseMotionListener(ml);
    label.addMouseListener(ml);
    Stream.of(zeroVerticalBar, zeroHorizontalBar, verticalBar, horizontalBar)
        .forEach(sb -> sb.setUnitIncrement(25));

    scroll.setViewportView(label);
    scroll.setVerticalScrollBar(zeroVerticalBar);
    scroll.setHorizontalScrollBar(zeroHorizontalBar);

    InputMap im = scroll.getInputMap(WHEN_IN_FOCUSED_WINDOW);
    // KeyStroke shiftKeyPressed = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, false);
    // KeyStroke shiftKeyReleased = KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true);
    im.put(KeyStroke.getKeyStroke("SHIFT"), "pressed");
    im.put(KeyStroke.getKeyStroke("released SHIFT"), "released");

    ActionMap am = scroll.getActionMap();
    am.put("pressed", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        setShiftPressed(true);
      }
    });
    am.put("released", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        setShiftPressed(false);
      }
    });

    // JScrollBar vsb = scroll.getVerticalScrollBar();
    // vsb.setPreferredSize(new Dimension(0, vsb.getPreferredSize().height));
    // vsb.putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
    // JScrollBar hsb = scroll.getHorizontalScrollBar();
    // hsb.setPreferredSize(new Dimension(hsb.getPreferredSize().width, 0));

    add(makeBox(), BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private JPanel makeBox() {
    JRadioButton r0 = new JRadioButton("Size: 0, shift pressed: Horizontal WheelScrolling", true);
    r0.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBar(zeroVerticalBar);
        scroll.setHorizontalScrollBar(zeroHorizontalBar);
      }
    });

    JRadioButton r1 = new JRadioButton("SCROLLBAR_ALWAYS");
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBar(verticalBar);
        scroll.setHorizontalScrollBar(horizontalBar);
      }
    });

    JRadioButton r2 = new JRadioButton("SCROLLBAR_NEVER");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
      }
    });

    ButtonGroup bg = new ButtonGroup();
    Stream.of(r0, r1, r2).forEach(bg::add);

    Box b = Box.createHorizontalBox();
    JPanel p = new JPanel(new GridLayout(2, 1));
    b.add(r1);
    b.add(r2);
    p.add(r0);
    p.add(b);
    return p;
  }

  private JLabel makeImageLabel() {
    String path = "example/CRW_3857_JFR.jpg"; // https://sozai-free.com/
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return new MissingIcon();
      }
    }).orElseGet(MissingIcon::new);
    return new JLabel(icon);
  }

  public boolean isShiftPressed() {
    return shiftPressed;
  }

  public void setShiftPressed(boolean f) {
    shiftPressed = f;
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
      ex.printStackTrace();
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

class DragScrollListener extends MouseAdapter {
  private final Cursor defCursor = Cursor.getDefaultCursor();
  private final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  private final Point pp = new Point();

  @Override public void mouseDragged(MouseEvent e) {
    Component c = e.getComponent();
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport vport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
      Point vp = vport.getViewPosition();
      vp.translate(pp.x - cp.x, pp.y - cp.y);
      ((JComponent) c).scrollRectToVisible(new Rectangle(vp, vport.getSize()));
      pp.setLocation(cp);
    }
  }

  @Override public void mousePressed(MouseEvent e) {
    Component c = e.getComponent();
    c.setCursor(hndCursor);
    Container p = SwingUtilities.getUnwrappedParent(c);
    if (p instanceof JViewport) {
      JViewport vport = (JViewport) p;
      Point cp = SwingUtilities.convertPoint(c, e.getPoint(), vport);
      pp.setLocation(cp);
    }
  }

  @Override public void mouseReleased(MouseEvent e) {
    e.getComponent().setCursor(defCursor);
  }
}

class MissingIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    int w = getIconWidth();
    int h = getIconHeight();
    int gap = w / 5;
    g2.setColor(Color.WHITE);
    g2.translate(x, y);
    g2.fillRect(0, 0, w, h);
    g2.setColor(Color.RED);
    g2.setStroke(new BasicStroke(w / 8f));
    g2.drawLine(gap, gap, w - gap, h - gap);
    g2.drawLine(gap, h - gap, w - gap, gap);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 1000;
  }

  @Override public int getIconHeight() {
    return 1000;
  }
}
