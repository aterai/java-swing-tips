// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree();

    String key1 = TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString();
    JCheckBox check1 = new JCheckBox(key1, UIManager.getBoolean(key1));
    check1.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      UIManager.put(key1, c.isSelected());
      SwingUtilities.updateComponentTreeUI(c.getRootPane());
    });

    String key2 = TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString();
    JCheckBox check2 = new JCheckBox(key2, UIManager.getBoolean(key2));
    check2.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      UIManager.put(key2, c.isSelected());
      SwingUtilities.updateComponentTreeUI(c.getRootPane());
    });

    JPanel p = new JPanel(new BorderLayout()) {
      @Override public void updateUI() {
        super.updateUI();
        check1.setSelected(UIManager.getBoolean(key1));
        check2.setSelected(UIManager.getBoolean(key2));
      }
    };
    p.add(new JScrollPane(tree));

    JPanel np = new JPanel(new GridLayout(2, 1));
    np.add(check1);
    np.add(check2);

    add(np, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
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

@SuppressWarnings("PMD.LongVariable")
enum TreeDraws {
  DRAWS_FOCUS_BORDER_AROUND_ICON("Tree.drawsFocusBorderAroundIcon"),
  DRAW_DASHED_FOCUS_INDICATOR("Tree.drawDashedFocusIndicator");
  private final String key;

  /* default */ TreeDraws(String key) {
    this.key = key;
  }

  @Override public String toString() {
    return key;
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
