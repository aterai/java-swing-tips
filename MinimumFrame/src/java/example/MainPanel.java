// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final int MW = 320;
  private static final int MH1 = 100;
  private static final int MH2 = 150;

  private MainPanel() {
    super(new BorderLayout());
    JLabel label = new JLabel();
    label.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        Window w = SwingUtilities.getWindowAncestor(getRootPane());
        ((JLabel) e.getComponent()).setText(w.getSize().toString());
      }
    });

    String title = "the minimum size of this window";
    JCheckBox check1 = new JCheckBox(title + ": " + MW + "x" + MH1, true);
    check1.addActionListener(e -> {
      Object o = e.getSource();
      if (o instanceof JCheckBox && ((JCheckBox) o).isSelected()) {
        initFrameSize(SwingUtilities.getWindowAncestor(getRootPane()));
      }
    });

    JCheckBox check2 = new JCheckBox(title + "(since 1.6): " + MW + "x" + MH2, true);
    check2.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor(getRootPane());
      w.setMinimumSize(check2.isSelected() ? new Dimension(MW, MH2) : new Dimension());
    });

    EventQueue.invokeLater(() -> {
      Window w = SwingUtilities.getWindowAncestor(getRootPane());
      w.setMinimumSize(new Dimension(MW, MH2));
      w.addComponentListener(new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent e) {
          if (check1.isSelected()) {
            initFrameSize((Window) e.getComponent());
          }
        }
      });
    });

    Box box = Box.createVerticalBox();
    box.add(check1);
    box.add(check2);
    add(box, BorderLayout.NORTH);
    add(label);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void initFrameSize(Window frame) {
    int fw = frame.getSize().width;
    int fh = frame.getSize().height;
    frame.setSize(Math.max(MW, fw), Math.max(MH1, fh));
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
    // int MAX = 500;
    // // frame.setMaximumSize(new Dimension(MAX, MAX));
    // Robot r;
    // Robot r2;
    // try {
    //   r = new Robot();
    // } catch (AWTException ex) {
    //   r = null;
    // }
    // r2 = r;
    // frame.getRootPane().addComponentListener(new ComponentAdapter() {
    //   @Override public void componentResized(ComponentEvent e) {
    //     Point pt = frame.getLocationOnScreen();
    //     Point mp = MouseInfo.getPointerInfo().getLocation();
    //     if (r2 != null && (mp.getX() > pt.getX() + MAX || mp.getY() > pt.getY() + MAX)) {
    //       r2.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    //       frame.setSize(Math.min(MAX, frame.getWidth()), Math.min(MAX, frame.getHeight()));
    //     }
    //   }
    // });
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
