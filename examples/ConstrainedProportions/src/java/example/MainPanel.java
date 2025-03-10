// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int MW = 300;
  private static final int MH = 200;

  private MainPanel() {
    super(new BorderLayout());
    JCheckBox checkbox = new JCheckBox("Fixed aspect ratio, Minimum size: " + MW + "*" + MH);
    checkbox.addActionListener(e -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window && checkbox.isSelected()) {
        initWindowSize((Window) c);
      }
    });

    EventQueue.invokeLater(() -> {
      Container c = getTopLevelAncestor();
      if (c instanceof Window) {
        Window window = (Window) c;
        window.setMinimumSize(new Dimension(MW, MH)); // JDK 1.6.0
        window.addComponentListener(new ComponentAdapter() {
          @Override public void componentResized(ComponentEvent e) {
            if (checkbox.isSelected()) {
              initWindowSize(window);
            }
          }
        });
      }
    });

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

    Toolkit.getDefaultToolkit().setDynamicLayout(false);
    JCheckBox check = new JCheckBox("Toolkit.getDefaultToolkit().setDynamicLayout: ");
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      Toolkit.getDefaultToolkit().setDynamicLayout(c.isSelected());
    });

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(checkbox);
    p.add(check);
    add(p, BorderLayout.NORTH);
    add(label);
    setPreferredSize(new Dimension(320, 240));
  }

  public void initWindowSize(Window window) {
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
