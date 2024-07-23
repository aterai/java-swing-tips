// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextArea textArea = new JTextArea();
  private final JCheckBox check1 = new JCheckBox();
  private final JCheckBox check2 = new JCheckBox();

  private MainPanel() {
    super(new BorderLayout());
    append("MainPanel: init");
    // updateCheckBox("MainPanel: init");

    UIManager.addPropertyChangeListener(e -> {
      if (Objects.equals("lookAndFeel", e.getPropertyName())) {
        // String lnf = e.getNewValue().toString();
        updateCheckBox("UIManager: propertyChange");
      }
    });

    EventQueue.invokeLater(() -> {
      ActionListener al = e -> {
        append("JMenuItem: actionPerformed");
        Object o = e.getSource();
        if (o instanceof JRadioButtonMenuItem && ((JRadioButtonMenuItem) o).isSelected()) {
          updateCheckBox("JMenuItem: actionPerformed: invokeLater");
        }
      };
      JMenuBar menuBar = new JMenuBar();
      menuBar.add(LookAndFeelUtils.createLookAndFeelMenu());
      getRootPane().setJMenuBar(menuBar);
      descendants(menuBar)
          .filter(JRadioButtonMenuItem.class::isInstance)
          .map(JRadioButtonMenuItem.class::cast)
          .forEach(mi -> mi.addActionListener(al));
    });

    String key1 = TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString();
    check1.setText(key1);
    check1.setSelected(UIManager.getBoolean(key1));
    check1.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      UIManager.put(key1, c.isSelected());
      SwingUtilities.updateComponentTreeUI(c.getRootPane());
    });

    String key2 = TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString();
    check2.setText(key2);
    check2.setSelected(UIManager.getBoolean(key2));
    check2.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      UIManager.put(key2, c.isSelected());
      SwingUtilities.updateComponentTreeUI(c.getRootPane());
    });

    JPanel p = new JPanel(new GridLayout(2, 1)) {
      @Override public void updateUI() {
        super.updateUI();
        check1.setSelected(UIManager.getBoolean(key1));
        check2.setSelected(UIManager.getBoolean(key2));
      }
    };
    p.add(new JScrollPane(new JTree()));
    p.add(new JScrollPane(textArea));

    JPanel np = new JPanel(new GridLayout(2, 1));
    np.add(check1);
    np.add(check2);

    add(np, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    super.updateUI();
    // System.out.println("JPanel: updateUI");
    updateCheckBox("JPanel: updateUI: invokeLater");
  }

  // public static void searchAllMenuElements(MenuElement me, List<JRadioButtonMenuItem> list) {
  //   if (me instanceof JRadioButtonMenuItem) {
  //     list.add((JRadioButtonMenuItem) me);
  //   }
  //   MenuElement[] sub = me.getSubElements();
  //   if (sub.length != 0) {
  //     for (MenuElement e : sub) {
  //       searchAllMenuElements(e, list);
  //     }
  //   }
  // }

  // private static Stream<MenuElement> descendantOrSelf(MenuElement me) {
  //   return Stream.of(me.getSubElements())
  //       .map(MainPanel::descendantOrSelf).reduce(Stream.of(me), Stream::concat);
  // }

  private static Stream<MenuElement> descendants(MenuElement me) {
    return Stream.of(me.getSubElements())
        .flatMap(m -> Stream.concat(Stream.of(m), descendants(m)));
  }

  private void updateCheckBox(String str) {
    EventQueue.invokeLater(() -> {
      append("--------\n" + str);

      String focusKey = TreeDraws.DRAWS_FOCUS_BORDER_AROUND_ICON.toString();
      append(focusKey + ": " + UIManager.getBoolean(focusKey));
      check1.setSelected(UIManager.getBoolean(focusKey));

      String dashedKey = TreeDraws.DRAW_DASHED_FOCUS_INDICATOR.toString();
      append(dashedKey + ": " + UIManager.getBoolean(dashedKey));
      check2.setSelected(UIManager.getBoolean(dashedKey));
    });
  }

  private void append(String str) {
    textArea.append(str + "\n");
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

  // private static class ActionCheckBox extends JCheckBox {
  //   protected ActionCheckBox(TreeDraws key) {
  //     super(key.toString());
  //     setAction(new AbstractAction(key.toString()) {
  //       @Override public void actionPerformed(ActionEvent e) {
  //         JCheckBox c = (JCheckBox) e.getSource();
  //         UIManager.put(key.toString(), c.isSelected());
  //         SwingUtilities.updateComponentTreeUI(c.getRootPane());
  //       }
  //     });
  //   }
  // }
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
