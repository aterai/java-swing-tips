// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs = new CloseableTabbedPane();
    tabs.setComponentPopupMenu(new TabbedPanePopupMenu());
    tabs.addTab("JLabel", new JLabel("JDK 6"));
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    add(tabs);
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
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class CloseableTabbedPane extends JTabbedPane {
  private static final Icon CLOSE_ICON = new CloseTabIcon();

  @Override public void addTab(String title, Component content) {
    JPanel tab = new JPanel(new BorderLayout());
    tab.setOpaque(false);
    JLabel label = new JLabel(title);
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
    JButton button = new JButton(CLOSE_ICON);
    // button.setBorderPainted(false);
    // button.setFocusPainted(false);
    // button.setContentAreaFilled(false);
    button.setBorder(BorderFactory.createEmptyBorder());
    button.addActionListener(e -> removeTabAt(indexOfComponent(content)));
    tab.add(label, BorderLayout.WEST);
    tab.add(button, BorderLayout.EAST);
    tab.setBorder(BorderFactory.createEmptyBorder(2, 1, 1, 1));
    super.addTab(title, content);
    setTabComponentAt(getTabCount() - 1, tab);
  }
}

class CloseTabIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.BLACK);
    g2.drawLine(4, 4, 11, 11);
    g2.drawLine(4, 5, 10, 11);
    g2.drawLine(5, 4, 11, 10);
    g2.drawLine(11, 4, 4, 11);
    g2.drawLine(11, 5, 5, 11);
    g2.drawLine(10, 4, 4, 10);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 16;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

class TabbedPanePopupMenu extends JPopupMenu {
  private transient int count;
  private final JMenuItem closeAll;

  protected TabbedPanePopupMenu() {
    super();
    add("Add").addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.addTab("Title" + count, new JLabel("Tab" + count));
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
      count++;
    });
    addSeparator();
    closeAll = add("Close All");
    closeAll.addActionListener(e -> ((JTabbedPane) getInvoker()).removeAll());
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      closeAll.setEnabled(((JTabbedPane) c).getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
