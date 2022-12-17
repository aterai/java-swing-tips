// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final SpinnerNumberModel spmodel;

  private MainPanel() {
    super(new BorderLayout());
    Object o = UIManager.get("Tree.timeFactor");
    Number lv = o instanceof Number ? (Number) o : 500L;
    spmodel = new SpinnerNumberModel(lv, 0L, 5000L, 500L);
    UIManager.put("List.timeFactor", 5000L);

    String[] model = {"a", "aa", "b", "bbb", "bbc"};
    JComboBox<String> combo = new JComboBox<>(model);
    combo.setPrototypeDisplayValue("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");

    JPanel p = new JPanel();
    p.add(new JSpinner(spmodel));
    p.add(combo);

    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.addTab("ComboBox.timeFactor", p);
    tabbedPane.addTab("List.timeFactor", new JScrollPane(new JList<>(model)));
    tabbedPane.addTab("Table.timeFactor(JFileChooser)", new JFileChooser());
    tabbedPane.addTab("Tree.timeFactor", new JScrollPane(new JTree()));
    add(tabbedPane);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    Long lv = Optional.ofNullable(spmodel).map(m -> m.getNumber().longValue()).orElse(1000L);
    UIManager.put("ComboBox.timeFactor", lv);
    UIManager.put("List.timeFactor", lv);
    UIManager.put("Table.timeFactor", lv);
    UIManager.put("Tree.timeFactor", lv);
    super.updateUI();
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
