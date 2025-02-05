// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(3, 1, 5, 5));
    JTree tree = new JTree();
    JCheckBox c = new JCheckBox("CheckBox", true);
    c.addActionListener(e -> tree.setEnabled(c.isSelected()));
    c.setFocusPainted(false);
    JScrollPane l1 = new JScrollPane(tree);
    l1.setBorder(new ComponentTitledBorder(c, l1, BorderFactory.createEtchedBorder()));

    JLabel icon = new JLabel(UIManager.getIcon("FileChooser.detailsViewIcon"));
    JLabel l2 = new JLabel("<html>ComponentTitledBorder<br>+ JLabel + Icon");
    l2.setBorder(new ComponentTitledBorder(icon, l2, BorderFactory.createEtchedBorder()));

    JButton b = new JButton("Button");
    b.setFocusPainted(false);
    JLabel l3 = new JLabel("ComponentTitledBorder + JButton");
    l3.setBorder(new ComponentTitledBorder(b, l3, BorderFactory.createEtchedBorder()));

    add(l1);
    add(l2);
    add(l3);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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

class ComponentTitledBorder extends MouseAdapter implements Border, SwingConstants {
  private static final int OFFSET = 5;
  private final Component comp;
  private final Border border;

  protected ComponentTitledBorder(Component comp, Container container, Border border) {
    super();
    this.comp = comp;
    this.border = border;
    if (comp instanceof JComponent) {
      ((JComponent) comp).setOpaque(true);
    }
    container.addMouseListener(this);
    container.addMouseMotionListener(this);
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (c instanceof Container) {
      Insets borderInsets = border.getBorderInsets(c);
      Insets insets = getBorderInsets(c);
      int temp = (insets.top - borderInsets.top) / 2;
      border.paintBorder(c, g, x, y + temp, width, height - temp);
      Dimension size = comp.getPreferredSize();
      Rectangle rect = new Rectangle(OFFSET, 0, size.width, size.height);
      SwingUtilities.paintComponent(g, comp, (Container) c, rect);
      comp.setBounds(rect);
    }
  }

  @Override public Insets getBorderInsets(Component c) {
    Dimension size = comp.getPreferredSize();
    Insets insets = border.getBorderInsets(c);
    insets.top = Math.max(insets.top, size.height);
    return insets;
  }

  private void dispatchEvent(MouseEvent e) {
    Component src = e.getComponent();
    comp.dispatchEvent(SwingUtilities.convertMouseEvent(src, e, comp));
    src.repaint();
  }

  @Override public void mouseClicked(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseEntered(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseExited(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mousePressed(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseReleased(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseMoved(MouseEvent e) {
    dispatchEvent(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    dispatchEvent(e);
  }
}
