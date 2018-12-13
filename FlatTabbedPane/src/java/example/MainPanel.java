package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;

public final class MainPanel extends JPanel {
  public static final Color SELECTED_BG = new Color(255, 150, 0);
  public static final Color UNSELECTED_BG = new Color(255, 50, 0);

  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("TabbedPane.tabInsets", new Insets(5, 10, 5, 10));
    // UIManager.put("TabbedPane.selectedTabPadInsets", new Insets(2, 3, 2, 2));
    UIManager.put("TabbedPane.contentBorderInsets", new Insets(5, 5, 5, 5));
    UIManager.put("TabbedPane.tabAreaInsets", new Insets(0, 0, 0, 0));

    UIManager.put("TabbedPane.selectedLabelShift", 0);
    UIManager.put("TabbedPane.labelShift", 0);

    // UIManager.put("TabbedPane.foreground", Color.WHITE);
    // UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
    // UIManager.put("TabbedPane.unselectedBackground", UNSELECTED_BG);
    // UIManager.put("TabbedPane.tabAreaBackground", UNSELECTED_BG);

    JTabbedPane tabs = new JTabbedPane() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicTabbedPaneUI() {
          @Override protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect, Rectangle textRect, boolean isSelected) {
            // Do not paint anything
          }

          @Override protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            // Do not paint anything
          }

          @Override  protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
            g.setColor(isSelected ? SELECTED_BG : UNSELECTED_BG);
            g.fillRect(x, y, w, h);
          }

          @Override protected void paintContentBorderTopEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(SELECTED_BG);
            g.fillRect(x, y, w, h);
          }

          @Override protected void paintContentBorderRightEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(SELECTED_BG);
            g.fillRect(x, y, w, h);
          }

          @Override protected void paintContentBorderBottomEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(SELECTED_BG);
            g.fillRect(x, y, w, h);
          }

          @Override protected void paintContentBorderLeftEdge(Graphics g, int tabPlacement, int selectedIndex, int x, int y, int w, int h) {
            g.setColor(SELECTED_BG);
            g.fillRect(x, y, w, h);
          }
        });
        setOpaque(true);
        setForeground(Color.WHITE);
        setBackground(UNSELECTED_BG);
        setTabPlacement(SwingConstants.LEFT);
        setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
      }
    };

    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    tabs.addTab("A", new ImageIcon(getClass().getResource("wi0009-32.png")), new JScrollPane(new JTree()));
    tabs.addTab("B", new ImageIcon(getClass().getResource("wi0054-32.png")), new JScrollPane(new JTextArea()));
    tabs.addTab("C", new ImageIcon(getClass().getResource("wi0062-32.png")), new JScrollPane(new JTree()));
    tabs.addTab("D", new ImageIcon(getClass().getResource("wi0063-32.png")), new JScrollPane(new JTextArea()));

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
