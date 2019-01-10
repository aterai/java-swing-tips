// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Stream;
import javax.swing.*;

public class MainPanel extends JPanel {
  protected boolean isShiftPressed;
  protected final JLabel label = new JLabel();
  protected final JScrollPane scroll = new JScrollPane(label);
  protected final JScrollBar verticalBar = scroll.getVerticalScrollBar();
  protected final JScrollBar horizontalBar = scroll.getHorizontalScrollBar();
  protected final JScrollBar zeroVerticalBar = new JScrollBar(Adjustable.VERTICAL) {
    @Override public boolean isVisible() {
      return !isShiftPressed && super.isVisible();
      // if (isShiftPressed) {
      //   return false;
      // } else {
      //   return super.isVisible();
      // }
    }

    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 0;
      return d;
    }
  };
  protected final JScrollBar zeroHorizontalBar = new JScrollBar(Adjustable.HORIZONTAL) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.height = 0;
      return d;
    }
  };
  protected final JRadioButton r0 = new JRadioButton("PreferredSize: 0, shift pressed: Horizontal WheelScrolling", true);
  protected final JRadioButton r1 = new JRadioButton("SCROLLBAR_ALWAYS");
  protected final JRadioButton r2 = new JRadioButton("SCROLLBAR_NEVER");

  public MainPanel() {
    super(new BorderLayout());

    label.setIcon(new ImageIcon(MainPanel.class.getResource("CRW_3857_JFR.jpg"))); // http://sozai-free.com/
    MouseAdapter ml = new DragScrollListener();
    label.addMouseMotionListener(ml);
    label.addMouseListener(ml);
    Stream.of(zeroVerticalBar, zeroHorizontalBar, verticalBar, horizontalBar).forEach(sb -> sb.setUnitIncrement(25));

    InputMap im = scroll.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    ActionMap am = scroll.getActionMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, InputEvent.SHIFT_DOWN_MASK, false), "pressed");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SHIFT, 0, true), "released");
    am.put("pressed", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        isShiftPressed = true;
      }
    });
    am.put("released", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        isShiftPressed = false;
      }
    });

    r0.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBar(zeroVerticalBar);
        scroll.setHorizontalScrollBar(zeroHorizontalBar);
      }
    });

    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scroll.setVerticalScrollBar(verticalBar);
        scroll.setHorizontalScrollBar(horizontalBar);
      }
    });

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

    scroll.setVerticalScrollBar(zeroVerticalBar);
    scroll.setHorizontalScrollBar(zeroHorizontalBar);

    // JScrollBar vsb = scroll.getVerticalScrollBar();
    // vsb.setPreferredSize(new Dimension(0, vsb.getPreferredSize().height));
    // vsb.putClientProperty("JScrollBar.fastWheelScrolling", Boolean.TRUE);
    // JScrollBar hsb = scroll.getHorizontalScrollBar();
    // hsb.setPreferredSize(new Dimension(hsb.getPreferredSize().width, 0));

    add(p, BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
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

class DragScrollListener extends MouseAdapter {
  protected final Cursor defCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
  protected final Cursor hndCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
  protected final Point pp = new Point();

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
