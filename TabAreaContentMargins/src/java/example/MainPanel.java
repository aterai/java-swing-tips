// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));

    JTabbedPane tabbedPane = makeTabbedPane();
    UIDefaults d = new UIDefaults();
    d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 30, 4, 30));
    tabbedPane.putClientProperty("Nimbus.Overrides", d);
    tabbedPane.putClientProperty("Nimbus.Overrides.InheritDefaults", true);

    add(makeTabbedPane());
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private JTabbedPane makeTabbedPane() {
    JTabbedPane tabbedPane = new JTabbedPane();
    // tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    tabbedPane.addTab("JTextArea", new JScrollPane(new JTextArea()));
    return tabbedPane;
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
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      // UIManager.put("TabbedPane.tabAreaInsets", new Insets(10, 10, 2, 10));

      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
      UIDefaults d = UIManager.getLookAndFeelDefaults();
      // d.put("TabbedPane:TabbedPaneContent.contentMargins", new Insets(0, 5, 5, 5));
      // d.put("TabbedPane:TabbedPaneTab.contentMargins", new Insets(2, 8, 3, 8));
      // d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(3, 10, 4, 10));
      Insets i = d.getInsets("TabbedPane:TabbedPaneTabArea.contentMargins");
      d.put("TabbedPane:TabbedPaneTabArea.contentMargins", new Insets(i.top, 0, i.bottom, 0));
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
