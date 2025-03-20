// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.lang.invoke.MethodHandles;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  public static final String LOGGER_NAME = MethodHandles.lookup().lookupClass().getName();
  public static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

  private MainPanel() {
    super(new BorderLayout());
    JButton button = new JButton("JButton JButton");

    Timer timer = new Timer(4000, e -> {
      LocalTime now = LocalTime.now(ZoneId.systemDefault());
      printInfo(button, now.toString());
    });

    JCheckBox check1 = new JCheckBox("setVisible", true);
    check1.addActionListener(e -> button.setVisible(((JCheckBox) e.getSource()).isSelected()));

    JCheckBox check2 = new JCheckBox("setEnabled", true);
    check2.addActionListener(e -> button.setEnabled(((JCheckBox) e.getSource()).isSelected()));

    JCheckBox check3 = new JCheckBox("start", true);
    check3.addActionListener(e -> {
      if (((JCheckBox) e.getSource()).isSelected()) {
        timer.start();
      } else {
        timer.stop();
      }
    });

    button.addHierarchyListener(e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
        printInfo(button, "SHOWING_CHANGED");
      }
      if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
        printInfo(button, "DISPLAYABILITY_CHANGED");
      }
    });

    printInfo(button, "after: new JButton, before: add(button); frame.setVisible(true)");

    JPanel panel = new JPanel();
    panel.add(button);
    IntStream.range(0, 15)
        .mapToObj(i -> new JLabel("<html>JLabel<br>&nbsp;idx:" + i))
        .forEach(panel::add);

    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("Main", new JScrollPane(panel));
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JLabel", new JLabel("Test"));

    JPanel p1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p1.add(new JLabel("JButton:"));
    p1.add(check1);
    p1.add(check2);
    JPanel p2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
    p2.add(new JLabel("Timer:"));
    p2.add(check3);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(p1);
    p.add(p2);
    add(p, BorderLayout.NORTH);
    add(tabs);

    timer.start();
    setPreferredSize(new Dimension(320, 240));
  }

  public static void printInfo(Component c, String str) {
    LOGGER.info(() -> String.join(
        "\n",
        c.getClass().getName() + ": " + str,
        "  isDisplayable:" + c.isDisplayable(),
        "  isShowing:" + c.isShowing(),
        "  isVisible:" + c.isVisible()));
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
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
