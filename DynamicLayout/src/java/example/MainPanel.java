package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JLabel label = new JLabel("", SwingConstants.CENTER);
    label.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        label.setText(label.getSize().toString());
      }
    });

    Toolkit.getDefaultToolkit().setDynamicLayout(true);
    JCheckBox check = new JCheckBox("DynamicLayout", true);
    check.addActionListener(e -> Toolkit.getDefaultToolkit().setDynamicLayout(((JCheckBox) e.getSource()).isSelected()));

    add(label);
    add(check, BorderLayout.NORTH);
    // setPreferredSize(new Dimension(320, 240));
    // setMinimumSize(new Dimension(300, 150));
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(320, 200);
  }

  @Override public Dimension getMinimumSize() {
    return new Dimension(300, 150);
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    // frame.addComponentListener(new ComponentAdapter() {
    //   private final int mw = 256;
    //   private final int mh = 100;
    //   @Override public void componentResized(ComponentEvent e) {
    //     int fw = frame.getSize().width;
    //     int fh = frame.getSize().height;
    //     frame.setSize(mw > fw ? mw : fw, mh > fh ? mh : fh);
    //   }
    // });
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
