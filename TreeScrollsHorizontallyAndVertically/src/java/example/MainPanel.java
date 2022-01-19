// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2));
    JTree tree1 = new JTree(makeTreeRoot());
    expandAll(tree1);

    JTree tree2 = new JTree(makeTreeRoot());
    expandAll(tree2);
    tree2.addTreeSelectionListener(e -> tree2.scrollPathToVisible(e.getNewLeadSelectionPath()));

    String key = "Tree.scrollsHorizontallyAndVertically";
    JCheckBoxMenuItem check = new JCheckBoxMenuItem(key);
    check.addActionListener(e -> UIManager.put(key, check.isSelected()));

    JMenu menu = new JMenu("View");
    menu.add(check);

    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(makeTitledPanel("Default", new JScrollPane(tree1)));
    add(makeTitledPanel("TreeSelectionListener", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static DefaultMutableTreeNode makeTreeRoot() {
    DefaultMutableTreeNode set4 = new DefaultMutableTreeNode("Set 00000004");
    set4.add(new DefaultMutableTreeNode("222222111111111111111122222"));
    set4.add(new DefaultMutableTreeNode("00000000000"));
    set4.add(new DefaultMutableTreeNode("1111111111"));
    set4.add(new DefaultMutableTreeNode("22222222"));

    DefaultMutableTreeNode set3 = new DefaultMutableTreeNode("Set 00000003");
    set3.add(new DefaultMutableTreeNode("5555555555"));
    set3.add(set4);
    set3.add(new DefaultMutableTreeNode("66666666666"));
    set3.add(new DefaultMutableTreeNode("7777777777"));

    DefaultMutableTreeNode set2 = new DefaultMutableTreeNode("Set 00000002");
    set2.add(set3);
    set2.add(new DefaultMutableTreeNode("333333333"));
    set2.add(new DefaultMutableTreeNode("4444444444444"));


    DefaultMutableTreeNode set1 = new DefaultMutableTreeNode("Set 00000001");
    set1.add(new DefaultMutableTreeNode("3333333333333333333333333333"));
    set1.add(set2);
    set1.add(new DefaultMutableTreeNode("111111111"));
    set1.add(new DefaultMutableTreeNode("22222222222"));
    set1.add(new DefaultMutableTreeNode("222222222"));

    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
    root.add(new DefaultMutableTreeNode("888"));
    root.add(new DefaultMutableTreeNode("99"));
    root.add(set1);
    root.add(new DefaultMutableTreeNode("2222"));
    root.add(new DefaultMutableTreeNode("11111"));
    return root;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void expandAll(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row++);
    }
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
