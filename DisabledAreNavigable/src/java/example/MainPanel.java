// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicToolBarUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    String key = "MenuItem.disabledAreNavigable";
    boolean b = UIManager.getBoolean(key);
    // System.out.println(key + ": " + b);
    JCheckBox check = new JCheckBox(key, b) {
      @Override public void updateUI() {
        super.updateUI();
        setSelected(UIManager.getLookAndFeelDefaults().getBoolean(key));
        UIManager.put(key, isSelected());
      }
    };
    check.addActionListener(e -> UIManager.put(key, ((JCheckBox) e.getSource()).isSelected()));

    // EventQueue.invokeLater(() -> {
    //   ActionListener al = e -> EventQueue.invokeLater(() -> {
    //     Object o = e.getSource();
    //     if (o instanceof JRadioButtonMenuItem) {
    //       JRadioButtonMenuItem mi = (JRadioButtonMenuItem) o;
    //       if (mi.isSelected()) {
    //         boolean b1 = UIManager.getBoolean(key);
    //         System.out.println(mi.getText() + ": " + b1);
    //         check.setSelected(b1);
    //       }
    //     }
    //   });
    //   List<JRadioButtonMenuItem> list = new ArrayList<>();
    //   MenuBarUtils.searchAllMenuElements(getRootPane().getJMenuBar(), list);
    //   for (JRadioButtonMenuItem mi : list) {
    //     mi.addActionListener(al);
    //   }
    // });

    // JMenuBar menuBar = MenuBarUtils.createMenuBar();
    // Stream.of(menuBar)
    //   .flatMap(new Function<MenuElement, Stream<MenuElement>>() {
    //     @Override public Stream<MenuElement> apply(MenuElement me) {
    //       return Stream.concat(
    //           Stream.of(me), Stream.of(me.getSubElements()).flatMap(this::apply));
    //     }
    //   })
    //   .filter(mi -> mi instanceof JRadioButtonMenuItem)
    //   .forEach(mi -> System.out.println("----\n" + mi.getClass()));
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(MenuBarUtils.createMenuBar()));

    JPopupMenu popup = new JPopupMenu();
    MenuBarUtils.initMenu(popup);
    setComponentPopupMenu(popup);
    add(check);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    // try {
    //   UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    // } catch (UnsupportedLookAndFeelException ignored) {
    //   Toolkit.getDefaultToolkit().beep();
    // } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
    //   ex.printStackTrace();
    //   return;
    // }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ExitAction extends AbstractAction {
  protected ExitAction() {
    super("Exit");
  }

  @Override public void actionPerformed(ActionEvent e) {
    Component root;
    Container parent = SwingUtilities.getUnwrappedParent((Component) e.getSource());
    if (parent instanceof JPopupMenu) {
      JPopupMenu popup = (JPopupMenu) parent;
      root = SwingUtilities.getRoot(popup.getInvoker());
    } else if (parent instanceof JToolBar) {
      JToolBar toolBar = (JToolBar) parent;
      if (((BasicToolBarUI) toolBar.getUI()).isFloating()) {
        root = SwingUtilities.getWindowAncestor(toolBar).getOwner();
      } else {
        root = SwingUtilities.getRoot(toolBar);
      }
    } else {
      root = SwingUtilities.getRoot(parent);
    }
    if (root instanceof Window) {
      Window window = (Window) root;
      window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
    }
  }
}

final class MenuBarUtils {
  private MenuBarUtils() {
    /* Singleton */
  }

  public static JMenuBar createMenuBar() {
    JMenuBar mb = new JMenuBar();
    JMenu file = new JMenu("File");
    initMenu(file);
    mb.add(file);

    JMenu edit = new JMenu("Edit");
    Stream.of("Cut", "Copy", "Paste", "Delete")
        .map(edit::add)
        .forEach(mi -> mi.setEnabled(false));
    mb.add(edit);

    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    mb.add(Box.createGlue());

    JMenu help = new JMenu("Help");
    help.add("About");
    mb.add(help);
    return mb;
  }

  public static void initMenu(Container p) {
    JMenuItem item = new JMenuItem("Open(disabled)");
    item.setEnabled(false);
    p.add(item);
    item = new JMenuItem("Save(disabled)");
    item.setEnabled(false);
    p.add(item);
    p.add(new JSeparator());
    p.add(new JMenuItem(new ExitAction()));
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

  // public static Stream<MenuElement> descendants(MenuElement me) {
  //   return Stream.of(me.getSubElements())
  //     .flatMap(m -> Stream.concat(Stream.of(m), descendants(m)));
  // }
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
