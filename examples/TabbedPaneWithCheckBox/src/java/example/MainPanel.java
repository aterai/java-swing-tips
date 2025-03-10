// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private final JTabbedPane tabs = new JTabbedPane();
  private final Component panel = new JLabel("Preferences");

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("Details");
    check.setFocusPainted(false);
    check.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        ((AbstractButton) e.getComponent()).doClick();
      }
    });
    check.addActionListener(e -> {
      if (((JCheckBox) e.getSource()).isSelected()) {
        tabs.addTab("Preferences", panel);
        tabs.setSelectedComponent(panel);
      } else {
        tabs.remove(panel);
      }
    });
    TabbedPaneWithCompBorder b = new TabbedPaneWithCompBorder(check, tabs);
    tabs.addMouseListener(b);
    tabs.setBorder(b);
    tabs.addTab("Quick Preferences", new JLabel("JLabel"));
    add(tabs);
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
    frame.setMinimumSize(new Dimension(256, 80));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TabbedPaneWithCompBorder implements Border, MouseListener, SwingConstants {
  private final JCheckBox checkBox;
  private final JTabbedPane tab;
  private final Container rubberStamp = new JPanel();
  private final Rectangle rect = new Rectangle();

  protected TabbedPaneWithCompBorder(JCheckBox checkBox, JTabbedPane tab) {
    this.checkBox = checkBox;
    this.tab = tab;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    Dimension size = checkBox.getPreferredSize();
    int xx = tab.getSize().width - size.width;
    Rectangle lastTab = tab.getBoundsAt(tab.getTabCount() - 1);
    int tabEnd = lastTab.x + lastTab.width;
    if (xx < tabEnd) {
      xx = tabEnd;
    }
    rect.setBounds(xx, -2, size.width, size.height);
    SwingUtilities.paintComponent(g, checkBox, rubberStamp, rect);
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(0, 0, 0, 0);
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  private void dispatchEvent(MouseEvent e) {
    if (!rect.contains(e.getX(), e.getY())) {
      return;
    }
    checkBox.setBounds(rect);
    checkBox.dispatchEvent(SwingUtilities.convertMouseEvent(tab, e, checkBox));
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
}
