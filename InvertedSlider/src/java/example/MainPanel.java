// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    BoundedRangeModel model = new DefaultBoundedRangeModel(50, 0, 0, 100);
    JSlider slider0 = new JSlider(SwingConstants.VERTICAL);
    JSlider slider1 = new JSlider(SwingConstants.VERTICAL);
    JSlider slider2 = new JSlider(SwingConstants.HORIZONTAL);
    JSlider slider3 = new JSlider(SwingConstants.HORIZONTAL);
    List<JSlider> list = Arrays.asList(slider0, slider1, slider2, slider3);

    JCheckBox check = new JCheckBox("ComponentOrientation.RIGHT_TO_LEFT");
    check.addActionListener(e -> {
      ComponentOrientation orientation = ((JCheckBox) e.getSource()).isSelected()
          ? ComponentOrientation.RIGHT_TO_LEFT
          : ComponentOrientation.LEFT_TO_RIGHT;
      list.forEach(s -> s.setComponentOrientation(orientation));
    });
    list.forEach(s -> {
      s.setModel(model);
      s.setMajorTickSpacing(20);
      s.setMinorTickSpacing(10);
      s.setPaintTicks(true);
      s.setPaintLabels(true);
    });
    slider1.setInverted(true);
    slider3.setInverted(true);

    Box box1 = Box.createHorizontalBox();
    box1.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    box1.add(slider0);
    box1.add(Box.createHorizontalStrut(20));
    box1.add(slider1);
    box1.add(Box.createHorizontalGlue());

    Box box2 = Box.createVerticalBox();
    box2.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 20));
    box2.add(makeTitledPanel("Default", slider2));
    box2.add(Box.createVerticalStrut(20));
    box2.add(makeTitledPanel("setInverted(true)", slider3));
    box2.add(Box.createVerticalGlue());

    add(box1, BorderLayout.WEST);
    add(box2);
    add(check, BorderLayout.SOUTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
