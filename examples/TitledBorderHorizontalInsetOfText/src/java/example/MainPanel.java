// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private static final String TITLE = "TitledBorder Test";

  private MainPanel() {
    super(new GridLayout(0, 1, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    Border b1 = new TitledBorder(TITLE + "1") {
      @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        super.paintBorder(c, g, x + 10, y, width, height);
      }
    };
    add(makeComp("override TitledBorder#paintBorder(...)", b1));

    Border b2 = new TitledBorder(TITLE + "2") {
      @Override public Insets getBorderInsets(Component c, Insets insets) {
        Insets i = super.getBorderInsets(c, insets);
        i.left += 10;
        return i;
      }
    };
    add(makeComp("override TitledBorder#getBorderInsets(...)", b2));

    JLabel label = new JLabel(TITLE + "3", null, SwingConstants.LEFT);
    label.setBorder(new EmptyBorder(0, 5, 0, 5));
    Border b3 = new ComponentTitledBorder(label, UIManager.getBorder("TitledBorder.border"));
    add(makeComp("ComponentTitledBorder + EmptyBorder", b3));

    Border b4 = new TitledBorder2(TITLE + "4");
    add(makeComp("TitledBorder2: copied from TitledBorder", b4));

    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeComp(String str, Border border) {
    JLabel l = new JLabel();
    l.setBorder(border);
    l.putClientProperty("html.disable", Boolean.TRUE);
    l.setText(str);
    return l;
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

// https://github.com/santhosh-tekuri/MyBlog/tree/master/ComponentTitledBorder
// https://ateraimemo.com/Swing/ComponentTitledBorder.html
class ComponentTitledBorder implements Border, SwingConstants {
  private static final int OFFSET = 10;
  private final Component comp;
  private final Border border;

  protected ComponentTitledBorder(Component comp, Border border) {
    this.comp = comp;
    this.border = border;
    if (comp instanceof JComponent) {
      ((JComponent) comp).setOpaque(true);
    }
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (c instanceof Container) {
      Insets borderInsets = border.getBorderInsets(c);
      Insets insets = getBorderInsets(c);
      int v = Math.max(0, (insets.top - borderInsets.top) / 2);
      border.paintBorder(c, g, x, y + v, width, height - v);
      Dimension size = comp.getPreferredSize();
      Rectangle rect = new Rectangle(OFFSET, 0, size.width, size.height);
      comp.setBounds(rect);
      SwingUtilities.paintComponent(g, comp, (Container) c, rect);
    }
  }

  @Override public Insets getBorderInsets(Component c) {
    Dimension size = comp.getPreferredSize();
    Insets insets = border.getBorderInsets(c);
    insets.top = Math.max(insets.top, size.height);
    return insets;
  }
}
