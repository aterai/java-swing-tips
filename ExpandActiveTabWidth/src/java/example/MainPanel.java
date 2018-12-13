// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
  private final List<String> icons = Arrays.asList(
      "wi0009-16.png",
      "wi0054-16.png",
      "wi0062-16.png",
      "wi0063-16.png",
      "wi0124-16.png",
      "wi0126-16.png");

  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    icons.forEach(s -> {
      Icon icon = new ImageIcon(getClass().getResource(s));
      ShrinkLabel label = new ShrinkLabel(s, icon);
      tabbedPane.addTab(s, icon, new JLabel(s), s);
      tabbedPane.setTabComponentAt(tabbedPane.getTabCount() - 1, label);
    });
    updateTabWidth(tabbedPane);
    tabbedPane.addChangeListener(e -> updateTabWidth((JTabbedPane) e.getSource()));
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void updateTabWidth(JTabbedPane tabs) {
    int tp = tabs.getTabPlacement();
    if (tp == JTabbedPane.LEFT || tp == JTabbedPane.RIGHT) {
      return;
    }
    int sidx = tabs.getSelectedIndex();
    for (int i = 0; i < tabs.getTabCount(); i++) {
      Component c = tabs.getTabComponentAt(i);
      if (c instanceof ShrinkLabel) {
        ((ShrinkLabel) c).setSelected(i == sidx);
      }
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class ShrinkLabel extends JLabel {
  private boolean selected;

  protected ShrinkLabel(String title, Icon icon) {
    super(title, icon, SwingConstants.LEFT);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = super.getPreferredSize();
    if (!selected) {
      d.width = 20;
    }
    return d;
  }

  public void setSelected(boolean active) {
    this.selected = active;
  }

  public boolean isSelected() {
    return selected;
  }
}
