// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(createMenuBar()));
    add(new JScrollPane(new JTextArea()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JMenuBar createMenuBar() {
    JMenu menu = new JMenu("File");
    menu.setToolTipText("File JMenu ToolTipText");
    menu.add("JMenuItem").setToolTipText("JMenuItem ToolTipText");
    JMenu sub1 = new JMenu("JMenu(Default)");
    sub1.setToolTipText("JMenu Default ToolTipText");
    sub1.add("JMenuItem1").setToolTipText("JMenuItem1 ToolTipText");
    sub1.add("JMenuItem2").setToolTipText("JMenuItem2 ToolTipText");
    menu.add(sub1);
    JMenu sub2 = new JMenu("JMenu#getToolTipText()") {
      @Override public String getToolTipText() {
        return getPopupMenu().isVisible() ? null : super.getToolTipText();
      }
    };
    sub2.setToolTipText("JMenu ToolTipText");
    sub2.add("JMenuItem1").setToolTipText("JMenuItem1 ToolTipText");
    sub2.add("JMenuItem2").setToolTipText("JMenuItem2 ToolTipText");
    menu.add(sub2);
    JMenuItem item2 = new JCheckBoxMenuItem("JCheckBoxMenuItem", true);
    item2.setToolTipText("JCheckBoxMenuItem ToolTipText");
    menu.add(item2);
    JMenuItem item3 = new JRadioButtonMenuItem("JRadioButtonMenuItem", true);
    item3.setToolTipText("JRadioButtonMenuItem ToolTipText");
    menu.add(item3);
    JMenuBar mb = new JMenuBar();
    mb.setToolTipText("JMenuBar ToolTipText");
    mb.add(menu);
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    return mb;
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
    b.setToolTipText(cmd);
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
