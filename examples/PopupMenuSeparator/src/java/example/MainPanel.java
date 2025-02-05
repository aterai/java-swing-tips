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
    tree.setComponentPopupMenu(makePopupMenu());
    add(new JScrollPane(tree));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    // Object off = popup.getClientProperty(new StringUIClientPropertyKey("GUTTER_OFFSET_KEY"));
    // System.out.println(off);
    popup.add("↓ add(new JSeparator()");
    popup.add(new JSeparator());
    popup.add("↓ JSeparator(): height = 8");
    popup.add(new JSeparator() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 8;
        return d;
      }
    });
    popup.add("↓ addSeparator()");
    popup.addSeparator();
    popup.add("↓ JPopupMenu.Separator(): height = 4");
    popup.add(new JPopupMenu.Separator() {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = 4;
        return d;
      }
    });
    popup.add("↓ JPopupMenu.Separator(): font size 16f");
    popup.add(new JPopupMenu.Separator() {
      @Override public Font getFont() {
        return super.getFont().deriveFont(16f);
      }
    });
    popup.add("↓ PopupMenuSeparator.contentMargins");
    popup.add(new JPopupMenu.Separator() {
      @Override public void updateUI() {
        super.updateUI();
        UIDefaults d = new UIDefaults();
        d.put("PopupMenuSeparator.contentMargins", new Insets(3, 0, 3, 0));
        putClientProperty("Nimbus.Overrides", d);
        putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
      }
    });
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    return popup;
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
