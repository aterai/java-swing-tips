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

    JCheckBox visibleCheck = new JCheckBox("setVisible", true);
    visibleCheck.addActionListener(e -> button.setVisible(visibleCheck.isSelected()));

    JCheckBox enabledCheck = new JCheckBox("setEnabled", true);
    enabledCheck.addActionListener(e -> button.setEnabled(enabledCheck.isSelected()));

    JCheckBox timerCheck = new JCheckBox("start", true);
    timerCheck.addActionListener(e -> {
      if (timerCheck.isSelected()) {
        timer.start();
      } else {
        timer.stop();
      }
    });

    button.addHierarchyListener(e -> hierarchyChanged(e, timer));
    printInfo(button, "after: new JButton, before: add(button); frame.setVisible(true)");

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    buttonPanel.add(new JLabel("JButton:"));
    buttonPanel.add(visibleCheck);
    buttonPanel.add(enabledCheck);
    JPanel timerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    timerPanel.add(new JLabel("Timer:"));
    timerPanel.add(timerCheck);

    JPanel controls = new JPanel(new GridLayout(2, 1));
    controls.add(buttonPanel);
    controls.add(timerPanel);
    add(controls, BorderLayout.NORTH);
    add(makeTabbedPane(button));

    timer.start();
    setPreferredSize(new Dimension(320, 240));
  }

  private static void hierarchyChanged(HierarchyEvent e, Timer timer) {
    Component c = e.getComponent();
    long flags = e.getChangeFlags();
    if ((flags & HierarchyEvent.SHOWING_CHANGED) != 0) {
      printInfo(c, "SHOWING_CHANGED");
    }
    if ((flags & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
      printInfo(c, "DISPLAYABILITY_CHANGED");
      // Stop the Timer when the frame is disposed so that it does not
      // keep logging and prevent the application from shutting down.
      if (!c.isDisplayable()) {
        timer.stop();
      }
    }
  }

  private static JTabbedPane makeTabbedPane(JButton button) {
    JPanel panel = new JPanel();
    panel.add(button);
    IntStream.range(0, 15)
        .mapToObj(i -> new JLabel("<html>JLabel<br>&nbsp;idx:" + i))
        .forEach(panel::add);

    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("Main", new JScrollPane(panel));
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JLabel", new JLabel("Test"));
    return tabs;
  }

  public static void printInfo(Component c, String message) {
    LOGGER.info(() -> String.join(
        "\n",
        c.getClass().getName() + ": " + message,
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
      Logger.getGlobal().severe(ex::getMessage);
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
