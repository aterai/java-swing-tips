// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
    menuBar.add(makeSpinnerMenu());
    menuBar.add(makeSliderMenu());
    // menuBar.add(makeComboBoxMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeMenuBox(String title, Component c) {
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(new JLabel(title));
    box.add(c);
    return box;
  }

  private static Component makeSpinnerMenuItem(String title, Component c) {
    JMenuItem item = new JMenuItem() {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        setEnabled(false);
        setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = Math.max(d.height, c.getPreferredSize().height);
        return d;
      }
    };
    item.add(new JLabel(title));
    item.add(c);
    return item;
  }

  // private static Component makeSpinnerMenuItem9(String title, Component c) {
  //   UIManager.put("CheckBoxMenuItem.checkIcon", new Icon() {
  //     @Override public void paintIcon(Component c, Graphics g, int x, int y) {
  //       /* Do nothing */
  //     }
  //
  //     @Override public int getIconWidth() {
  //       return 0;
  //     }
  //
  //     @Override public int getIconHeight() {
  //       return c.getPreferredSize().height;
  //     }
  //   });
  //   JMenuItem item = new JCheckBoxMenuItem() {
  //     @Override public void updateUI() {
  //       super.updateUI();
  //       setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
  //       putClientProperty("CheckBoxMenuItem.doNotCloseOnMouseClick", Boolean.TRUE);
  //       setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
  //     }
  //   };
  //   item.add(new JLabel(title));
  //   item.add(c);
  //   return item;
  // }

  private static JMenu makeSpinnerMenu() {
    SpinnerNumberModel model1 = new SpinnerNumberModel(100, 10, 300, 10);
    SpinnerNumberModel model2 = new SpinnerNumberModel(150, 10, 300, 10);
    JMenu menu = new JMenu("JSpinner");
    menu.add(makeMenuBox("L: ", makeSpinner(model1)));
    menu.add(makeMenuBox("R: ", makeSpinner(model2)));
    menu.add(new JSeparator());
    menu.add(makeSpinnerMenuItem("Left: ", makeSpinner(model1)));
    menu.add(makeSpinnerMenuItem("Right: ", makeSpinner(model2)));
    menu.addSeparator();
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    menu.add(Box.createHorizontalStrut(160));
    return menu;
  }

  private static JSpinner makeSpinner(SpinnerModel model) {
    JSpinner spinner = new JSpinner(model);
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setColumns(8);
    spinner.addMouseWheelListener(e -> {
      JSpinner s = (JSpinner) e.getComponent();
      SpinnerModel o = s.getModel();
      if (s.isEnabled() && o instanceof SpinnerNumberModel) {
        SpinnerNumberModel m = (SpinnerNumberModel) o;
        m.setValue(m.getNumber().intValue() - e.getWheelRotation());
      }
      e.consume();
    });
    return spinner;
  }

  private static JMenu makeSliderMenu() {
    BoundedRangeModel model1 = new DefaultBoundedRangeModel(90, 1, 0, 100);
    BoundedRangeModel model2 = new DefaultBoundedRangeModel(50, 1, 0, 100);
    JMenu menu = new JMenu("JSlider");
    menu.add(makeMenuBox("L: ", makeSlider(model1)));
    menu.add(makeMenuBox("R: ", makeSlider(model2)));
    menu.add(new JSeparator());
    menu.add(makeSpinnerMenuItem("Left: ", makeSlider(model1)));
    menu.add(makeSpinnerMenuItem("Right: ", makeSlider(model2)));
    menu.addSeparator();
    menu.add("JMenuItem3");
    menu.add("JMenuItem4");
    menu.add(Box.createHorizontalStrut(160));
    return menu;
  }

  private static JSlider makeSlider(BoundedRangeModel model) {
    // UIManager.put("Slider.paintValue", Boolean.FALSE); // GTKLookAndFeel
    // UIManager.put("Slider.focus", UIManager.get("Slider.background"));
    JSlider slider = new JSlider(model) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.min(d.width, 100);
        return d;
      }
    };
    slider.setBorder(BorderFactory.createEmptyBorder(1, 1, 4, 1));
    slider.setOpaque(false);
    slider.addMouseWheelListener(e -> {
      JSlider s = (JSlider) e.getComponent();
      if (s.isEnabled()) {
        BoundedRangeModel m = s.getModel();
        m.setValue(m.getValue() - e.getWheelRotation());
      }
      e.consume();
    });
    return slider;
  }

  // private static JMenu makeComboBoxMenu() {
  //   String[] model1 = {"A1", "B1", "C1"};
  //   String[] model2 = {"A2", "B2", "C2"};
  //   JMenu menu = new JMenu("JComboBox");
  //   menu.add(makeMenuBox("L: ", makeComboBox(model1)));
  //   menu.add(makeMenuBox("R: ", makeComboBox(model2)));
  //   menu.add(new JSeparator());
  //   menu.add(makeSpinnerMenuItem("Left: ", makeComboBox(model1)));
  //   menu.add(makeSpinnerMenuItem("Right: ", makeComboBox(model2)));
  //   menu.addSeparator();
  //   menu.add("JMenuItem5");
  //   menu.add("JMenuItem6");
  //   menu.add(Box.createHorizontalStrut(160));
  //   return menu;
  // }

  // private static JComboBox<String> makeComboBox(String... model) {
  //   return new JComboBox<String>(model) {
  //     @Override public Dimension getPreferredSize() {
  //       Dimension d = super.getPreferredSize();
  //       d.width = Math.min(d.width, 100);
  //       return d;
  //     }
  //   };
  // }

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
        Logger.getGlobal().severe(ex::getMessage);
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
