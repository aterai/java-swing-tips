// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.basic.BasicPopupMenuUI;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree0 = new JTree();
    tree0.setComponentPopupMenu(initPopupMenu(new JPopupMenu()));

    JTree tree1 = makeTree();
    tree1.setComponentPopupMenu(initPopupMenu(makePopupMenu()));

    JTree tree2 = makeTree();
    tree2.setComponentPopupMenu(initPopupMenu(new DarkModePopupMenu()));

    JTabbedPane tabs = new JTabbedPane();
    tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("Default", new JScrollPane(tree0));
    tabs.addTab("setBackground(...)", new JScrollPane(tree1));
    tabs.addTab("BasicPopupMenuUI", new JScrollPane(tree2));
    tabs.setSelectedIndex(tabs.getTabCount() - 1);
    add(tabs);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setPreferredSize(new Dimension(320, 240));
  }

  private JTree makeTree() {
    return new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        DefaultTreeCellRenderer r = new DefaultTreeCellRenderer();
        r.setTextNonSelectionColor(Color.WHITE);
        r.setBackgroundNonSelectionColor(Color.DARK_GRAY);
        // r.setTextSelectionColor(Color.RED);
        // r.setBackgroundSelectionColor(Color.GREEN);
        // r.setForeground(Color.BLUE);
        // r.setBackground(Color.PINK);
        setCellRenderer(r);
        setBackground(Color.DARK_GRAY);
        setForeground(Color.WHITE);
      }
    };
  }

  private JPopupMenu makePopupMenu() {
    // UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(Color.LIGHT_GRAY));
    // UIManager.put("PopupMenu.background", Color.DARK_GRAY);
    // UIManager.put("PopupMenu.foreground", Color.WHITE);
    // UIManager.put("MenuItem.foreground", Color.WHITE);
    // UIManager.put("CheckBoxMenuItem.foreground", Color.WHITE);
    // UIManager.put("RadioButtonMenuItem.foreground", Color.WHITE);
    // UIManager.put("MenuItem.background", Color.DARK_GRAY);
    // UIManager.put("CheckBoxMenuItem.background", Color.DARK_GRAY);
    // UIManager.put("RadioButtonMenuItem.background", Color.DARK_GRAY);
    return new JPopupMenu() {
      @Override public void updateUI() {
        super.updateUI();
        // setUI(new BasicPopupMenuUI());
        setBackground(Color.DARK_GRAY);
        setBorder(new BorderUIResource(BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
        EventQueue.invokeLater(() -> {
          for (MenuElement m : getSubElements()) {
            Component c = m.getComponent();
            c.setForeground(Color.WHITE);
            // for Metal, Windows Classic LookAndFeel
            ((JComponent) c).setOpaque(false);
          }
        });
      }
    };
  }

  private static JPopupMenu initPopupMenu(JPopupMenu popup) {
    popup.add("Cut");
    popup.add("Copy");
    popup.add("Paste");
    popup.add("Delete");
    popup.addSeparator();
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
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

final class DarkModePopupMenu extends JPopupMenu {
  @Override public void updateUI() {
    super.updateUI();
    setUI(new BasicPopupMenuUI());
    setBackground(Color.DARK_GRAY);
    setBorder(new BorderUIResource(BorderFactory.createLineBorder(Color.LIGHT_GRAY)));
    EventQueue.invokeLater(() -> {
      for (MenuElement m : getSubElements()) {
        Component c = m.getComponent();
        c.setForeground(Color.WHITE);
        // for Metal, Windows Classic LookAndFeel
        ((JComponent) c).setOpaque(false);
      }
    });
  }

  // @Override protected void paintComponent(Graphics g) {
  //   Graphics2D g2 = (Graphics2D) g.create();
  //   g2.setPaint(Color.DARK_GRAY);
  //   g2.fillRect(0, 0, getWidth(), getHeight());
  //   g2.dispose();
  // }
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
