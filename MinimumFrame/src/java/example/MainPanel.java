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

    JCheckBox checkbox1 = new JCheckBox("the minimum size of this window: " + MW + "x" + MH1, true);
    checkbox1.addActionListener(e -> {
      Object o = e.getSource();
      if (o instanceof JCheckBox && ((JCheckBox) o).isSelected()) {
        initFrameSize(SwingUtilities.getWindowAncestor(getRootPane()));
      }
    });

    JCheckBox checkbox2 = new JCheckBox("the minimum size of this window(since 1.6): " + MW + "x" + MH2, true);
    checkbox2.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor(getRootPane());
      w.setMinimumSize(checkbox2.isSelected() ? new Dimension(MW, MH2) : null);
    });

    EventQueue.invokeLater(() -> {
      Window w = SwingUtilities.getWindowAncestor(getRootPane());
      w.setMinimumSize(new Dimension(MW, MH2));
      w.addComponentListener(new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent e) {
          if (checkbox1.isSelected()) {
            initFrameSize((Window) e.getComponent());
          }
        }
      });
    });

    Box box = Box.createVerticalBox();
    box.add(checkbox1);
    box.add(checkbox2);
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
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
    //     Point loc = frame.getLocationOnScreen();
    //     Point mouse = MouseInfo.getPointerInfo().getLocation();
    //     if (Objects.nonNull(r2) && (mouse.getX() > loc.getX() + MAX || mouse.getY() > loc.getY() + MAX)) {
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
