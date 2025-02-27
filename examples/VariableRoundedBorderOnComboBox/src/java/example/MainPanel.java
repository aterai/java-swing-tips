// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
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
  private MainPanel() {
    super(new BorderLayout());
    String[] items = {"111", "2222", "33333"};
    ComboBoxModel<String> model = new DefaultComboBoxModel<>(items);
    JComboBox<String> combo0 = new JComboBox<>(model);
    JComboBox<String> combo1 = new RoundedComboBox<>(model);

    JCheckBox check = new JCheckBox("setEditable");
    check.setOpaque(false);
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      for (JComboBox<?> c : Arrays.asList(combo0, combo1)) {
        c.setEditable(b);
        c.repaint();
      }
    });

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("Default JComboBox", combo0));
    box.add(Box.createVerticalStrut(15));
    box.add(makeTitledPanel("RoundedCornerComboBox", combo1));
    add(box, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(check);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEmptyBorder(), title));
    p.add(c);
    return p;
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

class RoundedComboBox<E> extends JComboBox<E> {
  private transient PopupMenuListener listener;

  protected RoundedComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setRenderer(null);
    removePopupMenuListener(listener);
    super.updateUI();
    setBorder(new RoundedCornerBorder());
    setRenderer(new RoundedCornerListCellRenderer());
    setUI(new BasicComboBoxUI() {
      @Override protected JButton createArrowButton() {
        JButton b = new JButton(new ArrowIcon(Color.WHITE, Color.BLACK));
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder());
        return b;
      }

      @Override protected ComboPopup createPopup() {
        return new BasicComboPopup(comboBox) {
          @Override protected JScrollPane createScroller() {
            return new JScrollPane(list) {
              @Override public void updateUI() {
                super.updateUI();
                getVerticalScrollBar().setUI(new WithoutArrowButtonScrollBarUI());
                getHorizontalScrollBar().setUI(new WithoutArrowButtonScrollBarUI());
                setVerticalScrollBarPolicy(VERTICAL_SCROLLBAR_AS_NEEDED);
                setHorizontalScrollBarPolicy(HORIZONTAL_SCROLLBAR_NEVER);
                // setHorizontalScrollBar(null);
              }
            };
          }
        };
      }
    });
    listener = new HeavyWeightContainerListener();
    addPopupMenuListener(listener);
    Object o = getAccessibleContext().getAccessibleChild(0);
    if (o instanceof JComponent) {
      JComponent c = (JComponent) o;
      c.setBorder(new BottomRoundedCornerBorder());
      // c.setBackground(UIManager.getColor("List.background")); // ???
      c.setBackground(Color.WHITE);
    }
  }
}

class RoundedCornerListCellRenderer extends DefaultListCellRenderer {
  private final transient Icon indentIcon = new GapIcon();

  @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = super.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (c instanceof JLabel) {
      JLabel label = (JLabel) c;
      label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      label.setIconTextGap(0);
      boolean isListItem = index >= 0;
      label.setIcon(isListItem ? indentIcon : null);
      label.setOpaque(!isListItem);
    }
    return c;
  }

  @Override protected void paintComponent(Graphics g) {
    if (getIcon() != null) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setPaint(getBackground());
      Rectangle r = SwingUtilities.calculateInnerArea(this, null);
      g2.fill(new RoundRectangle2D.Double(r.x, r.y, r.width, r.height, 12d, 12d));
      super.paintComponent(g2);
      g2.dispose();
    } else {
      super.paintComponent(g);
    }
  }
}

class GapIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 2;
  }

  @Override public int getIconHeight() {
    return 18;
  }
}

class HeavyWeightContainerListener implements PopupMenuListener {
  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    EventQueue.invokeLater(() -> {
      JComboBox<?> combo = (JComboBox<?>) e.getSource();
      combo.setBorder(new TopRoundedCornerBorder());
      Accessible a = combo.getUI().getAccessibleChild(combo, 0);
      if (a instanceof JPopupMenu) {
        Optional.ofNullable(SwingUtilities.getWindowAncestor((Component) a))
            .filter(HeavyWeightContainerListener::isHeavyWeight)
            .ifPresent(w -> w.setBackground(new Color(0x0, true)));
      }
    });
  }

  private static boolean isHeavyWeight(Window w) {
    boolean isHeavyWeight = w.getType() == Window.Type.POPUP;
    GraphicsConfiguration gc = w.getGraphicsConfiguration();
    return gc != null && gc.isTranslucencyCapable() && isHeavyWeight;
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    ((JComponent) e.getSource()).setBorder(new RoundedCornerBorder());
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
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
  protected static final int ARC = 6;

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    int r = ARC * 2;
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
    g2.setPaint(Color.GRAY);
    g2.draw(round);
    g2.dispose();
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(4, 4, 4, 4);
  }

  @Override public Insets getBorderInsets(Component c, Insets insets) {
    insets.set(4, 4, 4, 4);
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
    double r = ARC * 2d;
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
    g2.setPaint(Color.GRAY);
    g2.draw(round);
    g2.dispose();
  }
}

class BottomRoundedCornerBorder extends RoundedCornerBorder {
  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    if (c instanceof JPopupMenu) {
      Window w = SwingUtilities.getWindowAncestor(c);
      if (w != null && w.getType() == Window.Type.POPUP) {
        Composite cmp = g2.getComposite();
        g2.setComposite(AlphaComposite.Clear);
        g2.setPaint(new Color(0x0, true));
        g2.clearRect(x, y, width, height);
        g2.setComposite(cmp);
      }
    }

    double r = ARC;
    double w = width - 1d;
    double h = height - 1d;
    double rr = r * 4d * (Math.sqrt(2d) - 1d) / 3d; // = r * .5522;
    Path2D p = new Path2D.Double();
    p.moveTo(x, y);
    p.lineTo(x, y + h - r);
    p.curveTo(x, y + h - r + rr, x + r - rr, y + h, x + r, y + h);
    p.lineTo(x + w - r, y + h);
    p.curveTo(x + w - r + rr, y + h, x + w, y + h - r + rr, x + w, y + h - r);
    p.lineTo(x + w, y);
    p.closePath();

    g2.setPaint(c.getBackground());
    g2.fill(p);
    g2.setPaint(Color.GRAY);
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

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
