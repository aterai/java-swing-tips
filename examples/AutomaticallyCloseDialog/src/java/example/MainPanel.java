// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    JLabel l = new JLabel();
    l.addHierarchyListener(new AutomaticallyCloseListener());
    JButton button = new JButton("show");
    button.addActionListener(e -> {
      String title = "Automatically close dialog";
      int option = JOptionPane.OK_CANCEL_OPTION;
      int type = JOptionPane.INFORMATION_MESSAGE;
      int r = JOptionPane.showConfirmDialog(getRootPane(), l, title, option, type);
      textArea.append(info(r));
    });
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder("HierarchyListener"));
    p.add(button);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String info(int ret) {
    String msg;
    switch (ret) {
      case JOptionPane.OK_OPTION:
        msg = "OK";
        break;
      case JOptionPane.CANCEL_OPTION:
        msg = "Cancel";
        break;
      case JOptionPane.CLOSED_OPTION:
        msg = "Closed(automatically)";
        break;
      default:
        msg = "----";
        break;
    }
    return msg + "\n";
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AutomaticallyCloseListener implements HierarchyListener {
  // private static final Logger LOGGER = Logger.getLogger(MainPanel.LOGGER_NAME);
  private static final int SECONDS = 5;
  private final AtomicInteger atomicDown = new AtomicInteger(SECONDS);
  private final Timer timer = new Timer(1000, null);
  private ActionListener listener;

  @Override public void hierarchyChanged(HierarchyEvent e) {
    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
      Component c = e.getComponent();
      if (c.isShowing() && c instanceof JLabel) {
        JLabel l = (JLabel) c;
        atomicDown.set(SECONDS);
        l.setText(String.format("Closing in %d seconds", SECONDS));
        timer.removeActionListener(listener);
        listener = event -> {
          int i = atomicDown.decrementAndGet();
          l.setText(String.format("Closing in %d seconds", i));
          if (i <= 0 && timer.isRunning()) {
            timer.stop();
            Optional.ofNullable(l.getTopLevelAncestor())
                .filter(Window.class::isInstance).map(Window.class::cast)
                .ifPresent(Window::dispose);
          }
        };
        timer.addActionListener(listener);
        timer.start();
      } else {
        if (timer.isRunning()) {
          timer.stop();
        }
      }
    }
  }
}
