// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
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
    JPanel p = new JPanel(new GridLayout(0, 1, 16, 16));
    p.setOpaque(true);
    JComboBox<String> combo1 = new JComboBox<>(makeModel());
    p.add(combo1);
    JComboBox<String> combo2 = makeFlatComboBox();
    combo2.setModel(makeModel());
    p.add(combo2);
    setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
    add(p, BorderLayout.NORTH);
    setOpaque(true);
    setPreferredSize(new Dimension(320, 240));
  }

  private static <E> JComboBox<E> makeFlatComboBox() {
    return new JComboBox<E>() {
      @Override public void updateUI() {
        UIManager.put(KEY, BorderFactory.createLineBorder(Color.GRAY));
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
        Object o = getAccessibleContext().getAccessibleChild(0);
        if (o instanceof JComponent) {
          JComponent c = (JComponent) o;
          c.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.GRAY));
          c.setForeground(FOREGROUND);
          c.setBackground(BACKGROUND);
        }
      }
    };
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
    // } catch (UnsupportedLookAndFeelException ignored) {
    //   Toolkit.getDefaultToolkit().beep();
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
    //   ex.printStackTrace();
    //   return;
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
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
        color = thumbDarkShadowColor.brighter();
      } else if (isThumbRollover()) {
        color = thumbLightShadowColor.brighter();
      } else {
        color = thumbColor;
      }
      g2.setPaint(color);
      g2.fillRect(r.x + 1, r.y + 1, r.width - 2, r.height - 2);
      g2.dispose();
    }
  }
}
