// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Objects;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  public static final Color BACKGROUND = Color.WHITE;
  public static final Color FOREGROUND = Color.BLACK;
  public static final Color SELECTION_FGC = Color.BLUE;
  public static final Color THUMB = new Color(0xCD_CD_CD);
  public static final String KEY = "ComboBox.border";

  private MainPanel() {
    super(new BorderLayout(15, 15));

    UIManager.put("ScrollBar.width", 10);
    UIManager.put("ScrollBar.thumbHeight", 20); // SynthLookAndFeel(GTK, Nimbus)
    UIManager.put("ScrollBar.minimumThumbSize", new Dimension(30, 30));
    UIManager.put("ScrollBar.incrementButtonGap", 0);
    UIManager.put("ScrollBar.decrementButtonGap", 0);
    UIManager.put("ScrollBar.thumb", THUMB);
    UIManager.put("ScrollBar.track", BACKGROUND);

    UIManager.put("ComboBox.foreground", FOREGROUND);
    UIManager.put("ComboBox.background", BACKGROUND);
    UIManager.put("ComboBox.selectionForeground", SELECTION_FGC);
    UIManager.put("ComboBox.selectionBackground", BACKGROUND);
    UIManager.put("ComboBox.buttonDarkShadow", BACKGROUND);
    UIManager.put("ComboBox.buttonBackground", FOREGROUND);
    UIManager.put("ComboBox.buttonHighlight", FOREGROUND);
    UIManager.put("ComboBox.buttonShadow", FOREGROUND);

    JComboBox<String> combo = new JComboBox<String>(makeModel()) {
      private transient MouseListener handler;
      private transient PopupMenuListener listener;
      @Override public void updateUI() {
        removeMouseListener(handler);
        removePopupMenuListener(listener);
        UIManager.put(KEY, new TopRoundedCornerBorder());
        super.updateUI();
        setUI(new BasicComboBoxUI() {
          @Override protected JButton createArrowButton() {
            JButton b = new JButton(new ArrowIcon(BACKGROUND, FOREGROUND));
            b.setContentAreaFilled(false);
            b.setFocusPainted(false);
            b.setBorder(BorderFactory.createEmptyBorder());
            return b;
          }

          @Override protected ComboPopup createPopup() {
            return new BasicComboPopup(comboBox) {
              @Override protected JScrollPane createScroller() {
                JScrollPane sp = new JScrollPane(list) {
                  @Override public void updateUI() {
                    super.updateUI();
                    getVerticalScrollBar().setUI(new WithoutArrowButtonScrollBarUI());
                    getHorizontalScrollBar().setUI(new WithoutArrowButtonScrollBarUI());
                  }
                };
                sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
                sp.setHorizontalScrollBar(null);
                return sp;
              }
            };
          }
        });
        handler = new ComboRolloverHandler();
        addMouseListener(handler);
        listener = new HeavyWeightContainerListener();
        addPopupMenuListener(listener);
        Object o = getAccessibleContext().getAccessibleChild(0);
        if (o instanceof JComponent) {
          JComponent c = (JComponent) o;
          c.setBorder(new BottomRoundedCornerBorder());
          c.setForeground(FOREGROUND);
          c.setBackground(BACKGROUND);
        }
      }
    };

    JPanel p = new JPanel(new GridLayout(0, 1, 15, 15));
    p.setOpaque(true);
    p.add(combo);

    JTree tree = new JTree();
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row);
      row++;
    }

    JScrollPane scroll = new JScrollPane(tree) {
      @Override public void updateUI() {
        super.updateUI();
        getVerticalScrollBar().setUI(new WithoutArrowButtonScrollBarUI());
        getHorizontalScrollBar().setUI(new WithoutArrowButtonScrollBarUI());
      }
    };
    scroll.setBackground(tree.getBackground());
    scroll.setBorder(new RoundedCornerBorder());

    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    add(scroll);
    add(p, BorderLayout.NORTH);
    setOpaque(true);
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("333333");
    model.addElement("aaa");
    model.addElement("1234555");
    model.addElement("555555555555");
    model.addElement("666666");
    model.addElement("bbb");
    model.addElement("444444444");
    model.addElement("1234");
    model.addElement("000000000000000");
    model.addElement("2222222222");
    model.addElement("ccc");
    model.addElement("111111111111111111");
    return model;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
    //   ex.printStackTrace();
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class HeavyWeightContainerListener implements PopupMenuListener {
  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    EventQueue.invokeLater(() -> {
      JComboBox<?> combo = (JComboBox<?>) e.getSource();
      Accessible a = combo.getUI().getAccessibleChild(combo, 0);
      if (a instanceof JPopupMenu) {
        Window w = SwingUtilities.getWindowAncestor((Component) a);
        // https://ateraimemo.com/Swing/DropShadowPopup.html
        if (w != null && w.getType() == Window.Type.POPUP) {
          // Popup$HeavyWeightWindow
          w.setBackground(new Color(0x0, true));
        }
      }
    });
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }
}

class ComboRolloverHandler extends MouseAdapter {
  private static ButtonModel getButtonModel(MouseEvent e) {
    Container c = (Container) e.getComponent();
    JButton b = (JButton) c.getComponent(0);
    return b.getModel();
  }

  @Override public void mouseEntered(MouseEvent e) {
    getButtonModel(e).setRollover(true);
  }

  @Override public void mouseExited(MouseEvent e) {
    getButtonModel(e).setRollover(false);
  }

  @Override public void mousePressed(MouseEvent e) {
    getButtonModel(e).setPressed(true);
  }

  @Override public void mouseReleased(MouseEvent e) {
    getButtonModel(e).setPressed(false);
  }
}

class ArrowIcon implements Icon {
  private final Color color;
  private final Color rollover;

  protected ArrowIcon(Color color, Color rollover) {
    this.color = color;
    this.rollover = rollover;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(color);
    int shift = 0;
    if (c instanceof AbstractButton) {
      ButtonModel m = ((AbstractButton) c).getModel();
      if (m.isPressed()) {
        shift = 1;
      } else {
        if (m.isRollover()) {
          g2.setPaint(rollover);
        }
      }
    }
    g2.translate(x, y + shift);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}

class RoundedCornerBorder extends AbstractBorder {
  protected static final int ARC = 12;

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int r = ARC;
    int w = width - 1;
    int h = height - 1;

    Area round = new Area(new RoundRectangle2D.Double(x, y, w, h, r, r));
    if (c instanceof JPopupMenu) {
      g2.setPaint(c.getBackground());
      g2.fill(round);
    } else {
      Container parent = c.getParent();
      if (Objects.nonNull(parent)) {
        g2.setPaint(parent.getBackground());
        Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
        corner.subtract(round);
        g2.fill(corner);
      }
    }
    g2.setPaint(c.getForeground());
    g2.draw(round);
    g2.dispose();
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(4, 8, 4, 8);
  }

  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(4, 8, 4, 8);
    return insets;
  }
}

class TopRoundedCornerBorder extends RoundedCornerBorder {
  // https://ateraimemo.com/Swing/RoundedComboBox.html
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    if (c instanceof JPopupMenu) {
      g2.clearRect(x, y, width, height);
    }
    double r = ARC;
    double w = width - 1d;
    double h = height - 1d;

    Area round = new Area(new RoundRectangle2D.Double(x, y, w, h, r, r));
    Rectangle b = round.getBounds();
    b.setBounds(b.x, b.y + ARC, b.width, b.height - ARC);
    round.add(new Area(b));

    Container parent = c.getParent();
    if (Objects.nonNull(parent)) {
      g2.setPaint(parent.getBackground());
      Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
      corner.subtract(round);
      g2.fill(corner);
    }

    g2.setPaint(c.getForeground());
    g2.draw(round);
    g2.dispose();
  }
}

class BottomRoundedCornerBorder extends RoundedCornerBorder {
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    // // TEST: WindowsLookAndFeel
    // if (c instanceof JPopupMenu) {
    //   Window w = SwingUtilities.getWindowAncestor((Component) c);
    //   if (w != null && w.getType() == Window.Type.POPUP) {
    //     Composite cmp = g2.getComposite();
    //     g2.setComposite(AlphaComposite.Clear);
    //     g2.setPaint(new Color(0x0, true));
    //     g2.clearRect(x, y, width, height);
    //     g2.setComposite(cmp);
    //   }
    // }

    double r = ARC;
    double w = width - 1d;
    double h = height - 1d;

    Path2D p = new Path2D.Double();
    p.moveTo(x, y);
    p.lineTo(x, y + h - r);
    p.quadTo(x, y + h, x + r, y + h);
    p.lineTo(x + w - r, y + h);
    p.quadTo(x + w, y + h, x + w, y + h - r);
    p.lineTo(x + w, y);
    p.closePath();
    // Area round = new Area(p);

    g2.setPaint(c.getBackground());
    g2.fill(p);

    g2.setPaint(c.getForeground());
    g2.draw(p);
    g2.setPaint(c.getBackground());
    g2.drawLine(x + 1, y, x + width - 2, y);
    g2.dispose();
  }
}

class ZeroSizeButton extends JButton {
  @Override public Dimension getPreferredSize() {
    return new Dimension();
  }
}

class WithoutArrowButtonScrollBarUI extends BasicScrollBarUI {
  @Override protected JButton createDecreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  @Override protected JButton createIncreaseButton(int orientation) {
    return new ZeroSizeButton();
  }

  // @Override protected Dimension getMinimumThumbSize() {
  //   // return new Dimension(20, 20);
  //   return UIManager.getDimension("ScrollBar.minimumThumbSize");
  // }

  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(trackColor);
    g2.fill(r);
    g2.dispose();
  }

  @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
    JScrollBar sb = (JScrollBar) c;
    if (!sb.isEnabled()) {
      return;
    }
    BoundedRangeModel m = sb.getModel();
    if (m.getMaximum() - m.getMinimum() - m.getExtent() > 0) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      Color color;
      if (isDragging) {
        color = thumbDarkShadowColor;
      } else if (isThumbRollover()) {
        color = thumbLightShadowColor;
      } else {
        color = thumbColor;
      }
      g2.setPaint(color);
      g2.fillRoundRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2, 10, 10);
      g2.dispose();
    }
  }
}
