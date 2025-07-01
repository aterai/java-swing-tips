// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int MW = 300;
  private static final int MH = 200;

  private MainPanel() {
    super(new BorderLayout());
    String txt1 = "Fixed aspect ratio, Minimum size: " + MW + "*" + MH;
    JCheckBox check1 = new JCheckBox(txt1);
    check1.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window && check1.isSelected()) {
        resetWindowSize((Window) c);
      }
    });
    EventQueue.invokeLater(() -> initWindowSize(check1));

    Toolkit.getDefaultToolkit().setDynamicLayout(false);
    String txt2 = "Toolkit.getDefaultToolkit().setDynamicLayout: ";
    JCheckBox check2 = new JCheckBox(txt2);
    check2.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      Toolkit.getDefaultToolkit().setDynamicLayout(c.isSelected());
    });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(check1);
    p.add(check2);
    add(p, BorderLayout.NORTH);

    JLabel label = new JLabel();
    label.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        JLabel l = (JLabel) e.getComponent();
        Container c = getTopLevelAncestor();
        if (c instanceof Window) {
          l.setText(c.getSize().toString());
        }
      }
    });
    add(label);
    setPreferredSize(new Dimension(320, 240));
  }

  private void initWindowSize(JCheckBox check) {
    Container c = getTopLevelAncestor();
    if (c instanceof Window) {
      Window window = (Window) c;
      window.setMinimumSize(new Dimension(MW, MH)); // JDK 1.6.0
      window.addComponentListener(new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent e) {
          if (check.isSelected()) {
            resetWindowSize(window);
          }
        }
      });
    }
  }

  public static void resetWindowSize(Window window) {
    int fw = window.getSize().width;
    int fh = MH * fw / MW;
    window.setSize(Math.max(MW, fw), Math.max(MH, fh));
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
