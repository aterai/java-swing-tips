// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String BLACK_CIRCLE = "●"; // U+25CF
  private static final String WHITE_CIRCLE = "○"; // U+25CB

  private MainPanel() {
    super(new BorderLayout());
    JLabel label1 = new JLabel(BLACK_CIRCLE, SwingConstants.CENTER);
    JLabel label2 = new JLabel("", SwingConstants.CENTER);

    Timer timer1 = new Timer(600, e ->
        label1.setText(BLACK_CIRCLE.equals(label1.getText()) ? WHITE_CIRCLE : BLACK_CIRCLE));
    Timer timer2 = new Timer(300, e ->
        label2.setText("".equals(label2.getText()) ? "!!!Warning!!!" : ""));
    addHierarchyListener(e -> {
      if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0) {
        updateTimers(e.getComponent().isDisplayable(), timer1, timer2);
      }
    });

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.add(makeTitledPanel("○<->●", label1));
    p.add(makeTitledPanel("!!!Warning!!!<->Empty", label2));
    add(p);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void updateTimers(boolean b, Timer... timers) {
    for (Timer timer : timers) {
      if (b) {
        timer.start();
      } else {
        timer.stop();
      }
    }
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
