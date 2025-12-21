// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    String title0 = "Box + Multiple JList + JSeparator";
    add(makeTitledPanel(title0, makeListBox()));
    String title1 = "JSeparator + ListModel";
    add(makeTitledPanel(title1, new GroupList<>(makeModel1())));
    String title2 = "CellRenderer + MatteBorder";
    add(makeTitledPanel(title2, new GroupBorderList<>(makeModel2())));
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
    JTree tree = new JTree();
    TreeModel model = tree.getModel();
    TreeNode root = (TreeNode) model.getRoot();
    Collections.list((Enumeration<?>) root.children())
        .stream()
        .filter(TreeNode.class::isInstance)
        .map(TreeNode.class::cast)
        .forEach(node -> {
          if (!node.isLeaf()) {
            Enumeration<?> children = node.children();
            box.add(makeList(Collections.list(children).toArray()));
            box.add(new JSeparator());
          }
        });
    JPanel p = new JPanel(new BorderLayout());
    p.setBackground(UIManager.getColor("List.background"));
    p.add(box, BorderLayout.NORTH);
    return p;
  }

  private static JList<Object> makeList(Object... ary) {
    JList<Object> c = new JList<Object>(ary) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super Object> r = getCellRenderer();
        setCellRenderer((l, v, index, isSelected, cellHasFocus) -> {
          boolean selected = isSelected && l.isFocusOwner();
          return r.getListCellRendererComponent(l, v, index, selected, cellHasFocus);
        });
      }
    };
    int height = c.getPreferredSize().height;
    c.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
    return c;
  }

  private static ListModel<Object> makeModel1() {
    JTree tree = new JTree();
    TreeNode root = (TreeNode) tree.getModel().getRoot();
    DefaultListModel<Object> model = new DefaultListModel<>();
    Collections.list((Enumeration<?>) root.children())
        .stream().filter(TreeNode.class::isInstance)
        .map(TreeNode.class::cast)
        .forEach(node -> {
          if (!node.isLeaf()) {
            Collections.list((Enumeration<?>) node.children())
                .stream()
                .filter(TreeNode.class::isInstance)
                .map(TreeNode.class::cast)
                .forEach(model::addElement);
            // Java 9: model.addAll(Collections.list(node.children()));
            model.addElement(new JSeparator());
          }
        });
    int last = model.getSize() - 1;
    if (model.getElementAt(last) instanceof JSeparator) {
      model.remove(last);
    }
    return model;
  }

  private static ListModel<TreeNode> makeModel2() {
    JTree tree = new JTree();
    TreeNode root = (TreeNode) tree.getModel().getRoot();
    DefaultListModel<TreeNode> model = new DefaultListModel<>();
    Collections.list((Enumeration<?>) root.children())
        .stream()
        .filter(TreeNode.class::isInstance)
        .map(TreeNode.class::cast)
        .forEach(node -> {
          if (!node.isLeaf()) {
            Collections.list((Enumeration<?>) node.children())
                .stream()
                .filter(TreeNode.class::isInstance)
                .map(TreeNode.class::cast)
                .forEach(model::addElement);
            // Java 9: model.addAll(Collections.list(node.children()));
          }
        });
    return model;
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
    initActionMpa(this);
  }

  @Override public void updateUI() {
    setCellRenderer(null);
    super.updateUI();
    ListCellRenderer<? super E> r = getCellRenderer();
    setCellRenderer((l, v, index, isSelected, cellHasFocus) ->
        v instanceof JSeparator
            ? (JSeparator) v
            : r.getListCellRendererComponent(l, v, index, isSelected, cellHasFocus)
    );
  }

  private static <E> void initActionMpa(JList<E> list) {
    ActionMap am = list.getActionMap();
    String selectPrevKey = "selectPreviousRow";
    Action prev = am.get(selectPrevKey);
    am.put(selectPrevKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        prev.actionPerformed(e);
        JList<?> l = (JList<?>) e.getSource();
        int index = l.getSelectedIndex();
        Object o = l.getModel().getElementAt(index);
        if (o instanceof JSeparator) {
          prev.actionPerformed(e);
        }
      }
    });
    String selectNextKey = "selectNextRow";
    Action next = am.get(selectNextKey);
    am.put(selectNextKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        next.actionPerformed(e);
        JList<?> l = (JList<?>) e.getSource();
        int index = l.getSelectedIndex();
        Object o = l.getModel().getElementAt(index);
        if (o instanceof JSeparator) {
          next.actionPerformed(e);
        }
      }
    });

    InputMap im = list.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), selectPrevKey);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_UP, 0), selectPrevKey);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), selectNextKey);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_DOWN, 0), selectNextKey);
  }
}

class GroupBorderList<E extends TreeNode> extends JList<E> {
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
        Border outside = getOutsideBorder(l, v, index);
        String key = isSelected ? "List.focusCellHighlightBorder" : "List.noFocusBorder";
        Border inside = UIManager.getBorder(key);
        ((JComponent) c).setBorder(BorderFactory.createCompoundBorder(outside, inside));
      }
      return c;
    });
  }

  private static Border getOutsideBorder(JList<?> l, TreeNode v, int index) {
    int max = l.getModel().getSize();
    int next = index + 1;
    Object n = next < max ? l.getModel().getElementAt(next) : null;
    return Optional.ofNullable(n)
        .filter(TreeNode.class::isInstance)
        .map(TreeNode.class::cast)
        .map(TreeNode::getParent)
        .filter(p -> !Objects.equals(p, v.getParent()))
        .<Border>map(p -> BorderFactory.createMatteBorder(0, 0, 1, 0, Color.GRAY))
        .orElseGet(() -> BorderFactory.createMatteBorder(0, 0, 1, 0, l.getBackground()));
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
