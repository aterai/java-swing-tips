// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

public final class MainPanel extends JPanel {
  @SuppressWarnings("unchecked")
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"Red", "Green", "Blue"};
    JList<String> list = new JList<>(model);
    UIDefaults d = new UIDefaults();
    d.put("List.rendererUseListColors", true);
    String key = "Nimbus.Overrides";
    list.putClientProperty(key, d);

    JPanel p0 = new JPanel(new GridLayout(1, 2));
    p0.add(new JScrollPane(new JList<>(model)));
    p0.add(new JScrollPane(list));

    JComboBox<String> combo0 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        UIDefaults d = new UIDefaults();
        d.put("ComboBox.rendererUseListColors", true);
        putClientProperty(key, d);
        // Accessible o = getAccessibleContext().getAccessibleChild(0);
        // if (o instanceof ComboPopup) {
        //   JList<?> list = ((ComboPopup) o).getList();
        //   list.setSelectionForeground(Color.WHITE);
        //   list.setSelectionBackground(Color.RED);
        // }
      }
    };

    // UIManager.put("ComboBox.rendererUseListColors", Boolean.TRUE);
    JComboBox<String> combo1 = new JComboBox<>(model);
    combo1.setRenderer(new DefaultListCellRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(
            list, "TEST0: " + value, index, isSelected, cellHasFocus);
      }
    });
    // UIDefaults d1 = new UIDefaults();
    // d1.put("ComboBox.rendererUseListColors", true);
    // combo1.putClientProperty("Nimbus.Overrides", d1);

    JComboBox<String> combo2 = new JComboBox<>(model);
    combo2.setRenderer(new BasicComboBoxRenderer() {
      @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(
            list, "TEST1: " + value, index, isSelected, cellHasFocus);
      }
    });
    // combo2.putClientProperty("Nimbus.Overrides", d1);

    JComboBox<String> combo3 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        UIDefaults d = new UIDefaults();
        d.put("ComboBox.rendererUseListColors", false);
        putClientProperty(key, d);
        ListCellRenderer<? super String> r = getRenderer();
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

    JPanel grid = new JPanel(new GridLayout(2, 2));
    grid.add(makeTitledPanel("ComboBox.rendererUseListColors: true", combo0));
    grid.add(makeTitledPanel("DefaultListCellRenderer", combo1));
    grid.add(makeTitledPanel("BasicComboBoxRenderer", combo2));
    grid.add(makeTitledPanel("SynthComboBoxRenderer + ListCellRenderer", combo3));
    JPanel p1 = new JPanel(new BorderLayout());
    p1.add(grid, BorderLayout.NORTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JTabbedPane tabs = new JTabbedPane();
    tabs.add("JList", p0);
    tabs.add("JComboBox", p1);
    add(tabs);
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
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
