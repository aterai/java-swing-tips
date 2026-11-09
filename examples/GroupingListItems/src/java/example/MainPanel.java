// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.Position;
import javax.swing.tree.TreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    String title0 = "Box + Multiple JList + JSeparator";
    add(makeTitledPanel(title0, makeListBox()));
    String title1 = "JSeparator + ListModel";
    add(makeTitledPanel(title1, new GroupList<>(makeSeparatorModel())));
    String title2 = "CellRenderer + MatteBorder";
    add(makeTitledPanel(title2, new GroupBorderList<>(makeTreeNodeModel())));
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JScrollPane scroll = new JScrollPane(c);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(scroll);
    return p;
  }

  private static Container makeListBox() {
    Box box = Box.createVerticalBox();
    getGroups().forEach(node -> {
      box.add(makeList(getChildren(node).toArray()));
      box.add(new JSeparator());
    });
    JPanel p = new JPanel(new BorderLayout()) {
      @Override public void updateUI() {
        super.updateUI();
        // A color set only once would be reset to Panel.background by a LookAndFeel change
        setBackground(UIManager.getColor("List.background"));
      }
    };
    p.add(box, BorderLayout.NORTH);
    return p;
  }

  private static JList<Object> makeList(Object... items) {
    JList<Object> list = new JList<Object>(items) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super Object> r = getCellRenderer();
        setCellRenderer((l, v, index, isSelected, cellHasFocus) -> {
          boolean selected = isSelected && l.isFocusOwner();
          return r.getListCellRendererComponent(l, v, index, selected, cellHasFocus);
        });
      }

      @Override protected void processFocusEvent(FocusEvent e) {
        super.processFocusEvent(e);
        // ListUI repaints only the lead cell, but every selected cell depends on the focus
        repaint();
      }
    };
    int height = list.getPreferredSize().height;
    list.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
    return list;
  }

  private static ListModel<Object> makeSeparatorModel() {
    DefaultListModel<Object> model = new DefaultListModel<>();
    getGroups().forEach(node -> {
      if (!model.isEmpty()) {
        model.addElement(new JSeparator());
      }
      getChildren(node).forEach(model::addElement);
    });
    return model;
  }

  private static ListModel<TreeNode> makeTreeNodeModel() {
    DefaultListModel<TreeNode> model = new DefaultListModel<>();
    getGroups().flatMap(MainPanel::getChildren).forEach(model::addElement);
    return model;
  }

  private static Stream<TreeNode> getGroups() {
    TreeNode root = (TreeNode) new JTree().getModel().getRoot();
    return getChildren(root).filter(node -> !node.isLeaf());
  }

  private static Stream<TreeNode> getChildren(TreeNode parent) {
    return Collections.list((Enumeration<?>) parent.children())
        .stream()
        .filter(TreeNode.class::isInstance)
        .map(TreeNode.class::cast);
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

class GroupList<E> extends JList<E> {
  protected GroupList(ListModel<E> model) {
    super(model);
    ActionMap am = getActionMap();
    skipSeparator(am, "selectPreviousRow");
    skipSeparator(am, "selectNextRow");
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    // The JSeparator in the ListModel is out of the component tree and would keep the old UI
    JSeparator separator = new JSeparator();
    ListCellRenderer<? super E> r = getCellRenderer();
    setCellRenderer((l, v, index, isSelected, cellHasFocus) ->
        v instanceof JSeparator
            ? separator
            : r.getListCellRendererComponent(l, v, index, isSelected, cellHasFocus)
    );
  }

  @Override public int getNextMatch(String prefix, int startIndex, Position.Bias bias) {
    // Keep the type-ahead search from matching JSeparator#toString()
    int index = super.getNextMatch(prefix, startIndex, bias);
    return isSeparator(this, index) ? -1 : index;
  }

  private static void skipSeparator(ActionMap am, String key) {
    Action action = am.get(key);
    am.put(key, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        action.actionPerformed(e);
        JList<?> l = (JList<?>) e.getSource();
        if (isSeparator(l, l.getSelectedIndex())) {
          action.actionPerformed(e);
        }
      }
    });
  }

  private static boolean isSeparator(JList<?> list, int index) {
    return index >= 0 && list.getModel().getElementAt(index) instanceof JSeparator;
  }
}

class GroupBorderList<E extends TreeNode> extends JList<E> {
  private static final Border GROUP_END = BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY);
  private static final Border GROUP_ITEM = BorderFactory.createEmptyBorder(0, 0, 1, 0);

  protected GroupBorderList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    ListCellRenderer<? super E> r = getCellRenderer();
    setCellRenderer((l, v, index, isSelected, cellHasFocus) -> {
      Component c = r.getListCellRendererComponent(l, v, index, isSelected, cellHasFocus);
      if (c instanceof JComponent) {
        JComponent renderer = (JComponent) c;
        // The default renderer resets its focus Border on every call, so it never nests
        Border outside = isGroupEnd(l, v, index) ? GROUP_END : GROUP_ITEM;
        renderer.setBorder(BorderFactory.createCompoundBorder(outside, renderer.getBorder()));
      }
      return c;
    });
  }

  private static boolean isGroupEnd(JList<? extends TreeNode> l, TreeNode v, int index) {
    ListModel<? extends TreeNode> m = l.getModel();
    int next = index + 1;
    return next < m.getSize() && !Objects.equals(m.getElementAt(next).getParent(), v.getParent());
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
