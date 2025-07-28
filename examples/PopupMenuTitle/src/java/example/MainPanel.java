// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JPopupMenu popup0 = new JPopupMenu("Default JPopupMenu");
    JTree tree0 = new JTree();
    tree0.setComponentPopupMenu(initPopup(popup0));
    add(new JScrollPane(tree0));

    JPopupMenu popup1 = new JPopupMenu("JPopupMenu#setLabel(...)") {
      @Override public void updateUI() {
        setBorder(null);
        super.updateUI();
        Border border = getBorder();
        String title = getLabel();
        if (!isCompoundMotifBorderBorder(border) && title != null) {
          Color color = UIManager.getColor("Separator.foreground");
          Border underline = BorderFactory.createMatteBorder(1, 0, 0, 0, color);
          Border labelBorder = BorderFactory.createTitledBorder(
              underline,
              title,
              TitledBorder.CENTER,
              TitledBorder.ABOVE_TOP,
              getFont(),
              getForeground());
          setBorder(BorderFactory.createCompoundBorder(border, labelBorder));
        }
      }
    };
    JTree tree1 = new JTree();
    tree1.setComponentPopupMenu(initPopup(popup1));
    add(new JScrollPane(tree1));
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean isCompoundMotifBorderBorder(Border border) {
    boolean b0 = border instanceof CompoundBorder;
    boolean b1 = b0 && isMotifBorder(((CompoundBorder) border).getInsideBorder());
    boolean b2 = b0 && isMotifBorder(((CompoundBorder) border).getOutsideBorder());
    return b1 || b2;
  }

  private static boolean isMotifBorder(Border border) {
    return border.getClass().getName().contains("MotifBorders");
  }

  private static JPopupMenu initPopup(JPopupMenu popup) {
    popup.add("JMenuItem1");
    popup.add("JMenuItem2");
    popup.addSeparator();
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    JMenu menu = new JMenu("JMenu");
    menu.add("Sub JMenuItem 1");
    menu.add("Sub JMenuItem 2");
    popup.add(menu);
    return popup;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      //UIManager.setLookAndFeel("com.sun.java.swing.plaf.motif.MotifLookAndFeel");
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
