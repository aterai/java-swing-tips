// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    // TEST: UIManager.put("TabbedPane.tabRunOverlay", -4);
    // UIManager.put("TabbedPane.extendTabsToBase", Boolean.TRUE); // Default
    // UIManager.put("TabbedPane.extendTabsToBase", Boolean.FALSE);
    JTabbedPane tabbedPane = makeTabbedPane();
    UIDefaults d = UIManager.getLookAndFeelDefaults();
    d.put("TabbedPane.extendTabsToBase", Boolean.FALSE);
    tabbedPane.putClientProperty("Nimbus.Overrides", d);
    tabbedPane.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);

    add(makeTitledPanel("TabbedPane.extendTabsToBase: true", makeTabbedPane()));
    add(makeTitledPanel("TabbedPane.extendTabsToBase: false", tabbedPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane() {
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("JTable", new JScrollPane(new JTable(8, 3)));
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JLabel", new JLabel("label"));
    tabbedPane.addTab("JButton", new JButton("button"));
    tabbedPane.addTab("JSplitPane", new JSplitPane());
    return tabbedPane;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
