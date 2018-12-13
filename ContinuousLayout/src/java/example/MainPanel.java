package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JSplitPane leftPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    leftPane.setTopComponent(new JScrollPane(new JTextArea("aaaaaaa")));
    leftPane.setBottomComponent(new JScrollPane(new JTextArea("bbbb")));
    leftPane.setContinuousLayout(true);
    leftPane.setResizeWeight(.5);

    JSplitPane rightPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    rightPane.setTopComponent(new JScrollPane(new JTree()));
    rightPane.setBottomComponent(new JScrollPane(new JTree()));
    rightPane.setContinuousLayout(true);
    rightPane.setResizeWeight(.5);

    JSplitPane centerPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    centerPane.setLeftComponent(leftPane);
    centerPane.setRightComponent(rightPane);
    centerPane.setContinuousLayout(true);
    centerPane.setResizeWeight(.5);

    PropertyChangeListener pcl = e -> {
      if (JSplitPane.DIVIDER_LOCATION_PROPERTY.equals(e.getPropertyName())) {
        JSplitPane source = (JSplitPane) e.getSource();
        int location = ((Integer) e.getNewValue()).intValue();
        JSplitPane target = Objects.equals(source, leftPane) ? rightPane : leftPane;
        if (location != target.getDividerLocation()) {
          target.setDividerLocation(location);
        }
      }
    };
    leftPane.addPropertyChangeListener(pcl);
    rightPane.addPropertyChangeListener(pcl);

    JCheckBox check = new JCheckBox("setContinuousLayout", true);
    check.addActionListener(e -> {
      boolean flag = ((JCheckBox) e.getSource()).isSelected();
      leftPane.setContinuousLayout(flag);
      rightPane.setContinuousLayout(flag);
      centerPane.setContinuousLayout(flag);
    });

    add(check, BorderLayout.NORTH);
    add(centerPane);
    setPreferredSize(new Dimension(320, 240));
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
