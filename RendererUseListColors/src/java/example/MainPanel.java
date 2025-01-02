// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public final class MainPanel extends JPanel {
  private static final String NIMBUS_OVERRIDES = "Nimbus.Overrides";

  private MainPanel() {
    super(new BorderLayout());
    String[] colors = {"Red", "Green", "Blue"};
    ComboBoxModel<String> model = new DefaultComboBoxModel<>(colors);
    JList<String> list = new JList<>(colors);
    UIDefaults d = new UIDefaults();
    d.put("List.rendererUseListColors", true);
    list.putClientProperty(NIMBUS_OVERRIDES, d);
    JPanel p0 = new JPanel(new GridLayout(1, 2));
    p0.add(new JScrollPane(new JList<>(model)));
    p0.add(new JScrollPane(list));

    JPanel grid = new JPanel(new GridLayout(2, 2));
    JComboBox<String> combo0 = makeComboBox0(model);
    grid.add(makeTitledPanel("ComboBox.rendererUseListColors: true", combo0));
    JComboBox<String> combo1 = makeComboBox1(model);
    grid.add(makeTitledPanel("DefaultListCellRenderer", combo1));
    JComboBox<String> combo2 = makeComboBox2(model);
    grid.add(makeTitledPanel("BasicComboBoxRenderer", combo2));
    JComboBox<String> combo3 = makeComboBox3(model);
    grid.add(makeTitledPanel("SynthComboBoxRenderer + ListCellRenderer", combo3));
    JPanel p1 = new JPanel(new BorderLayout());
    p1.add(grid, BorderLayout.NORTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JTabbedPane tabs = new JTabbedPane();
    tabs.add("JList", p0);
    tabs.add("JComboBox", p1);
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
  }

  private static <E> JComboBox<E> makeComboBox0(ComboBoxModel<E> model) {
    return new JComboBox<E>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        UIDefaults d = new UIDefaults();
        d.put("ComboBox.rendererUseListColors", true);
        putClientProperty(NIMBUS_OVERRIDES, d);
        // Accessible o = getAccessibleContext().getAccessibleChild(0);
        // if (o instanceof ComboPopup) {
        //   JList<?> list = ((ComboPopup) o).getList();
        //   list.setSelectionForeground(Color.WHITE);
        //   list.setSelectionBackground(Color.RED);
        // }
      }
    };
  }

  private static <E> JComboBox<E> makeComboBox1(ComboBoxModel<E> model) {
    // UIManager.put("ComboBox.rendererUseListColors", Boolean.TRUE);
    JComboBox<E> combo = new JComboBox<>(model);
    combo.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(
            list, "TEST0: " + value, index, isSelected, cellHasFocus);
      }
    });
    // UIDefaults d1 = new UIDefaults();
    // d1.put("ComboBox.rendererUseListColors", true);
    // combo.putClientProperty("Nimbus.Overrides", d1);
    return combo;
  }

  @SuppressWarnings("unchecked")
  private static <E> JComboBox<E> makeComboBox2(ComboBoxModel<E> model) {
    JComboBox<E> combo = new JComboBox<>(model);
    combo.setRenderer(new BasicComboBoxRenderer() {
      @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(
            list, "TEST1: " + value, index, isSelected, cellHasFocus);
      }
    });
    // combo.putClientProperty("Nimbus.Overrides", d1);
    return combo;
  }

  private static <E> JComboBox<E> makeComboBox3(ComboBoxModel<E> model) {
    return new JComboBox<E>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        UIDefaults d = new UIDefaults();
        d.put("ComboBox.rendererUseListColors", false);
        putClientProperty(NIMBUS_OVERRIDES, d);
        ListCellRenderer<? super E> r = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (c instanceof JLabel) {
            ((JLabel) c).setText("TEST2: " + value);
          }
          return c;
        });
      }
    };
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
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
