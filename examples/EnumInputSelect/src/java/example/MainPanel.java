// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("JRadioButtonMenuItem", makeTest1());
    tabbedPane.addTab("JRadioButton", makeTest2());
    tabbedPane.addTab("JComboBox", makeTest3());
    tabbedPane.addTab("JSpinner", makeTest4());
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTest1() {
    JTabbedPane tabs = makeTabbedPane();
    JMenu menu = new JMenu("JMenu");
    ButtonGroup bg = new ButtonGroup();
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg.getSelection().getActionCommand();
        tabs.setTabPlacement(TabPlacement.valueOf(name).getPlacement());
      }
    };
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JRadioButtonMenuItem item = new JRadioButtonMenuItem(name, selected);
      item.addItemListener(handler);
      item.setActionCommand(name);
      menu.add(item);
      bg.add(item);
    });
    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    // EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    return makePanel(tabs, mb);
  }

  private static Component makeTest2() {
    JTabbedPane tabs = makeTabbedPane();
    ButtonGroup bg = new ButtonGroup();
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg.getSelection().getActionCommand();
        tabs.setTabPlacement(TabPlacement.valueOf(name).getPlacement());
      }
    };
    Box box = Box.createHorizontalBox();
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JRadioButton radio = new JRadioButton(name, selected);
      radio.addItemListener(handler);
      radio.setActionCommand(name);
      box.add(radio);
      bg.add(radio);
    });
    return makePanel(tabs, box);
  }

  private static Component makeTest3() {
    JTabbedPane tabs = makeTabbedPane();
    JComboBox<TabPlacement> combo = new JComboBox<>(TabPlacement.values());
    combo.addItemListener(e -> {
      Object o = e.getItem();
      if (e.getStateChange() == ItemEvent.SELECTED && o instanceof TabPlacement) {
        tabs.setTabPlacement(((TabPlacement) o).getPlacement());
        // String name = e.getItem().toString();
        // tabs.setTabPlacement(TabPlacement.valueOf(name).getPlacement());
        // int idx = combo.getSelectedIndex();
        // tabs.setTabPlacement(combo.getItemAt(idx).getPlacement());
      }
    });
    return makePanel(tabs, combo);
  }

  private static Component makeTest4() {
    JTabbedPane tabs = makeTabbedPane();
    SpinnerListModel model = new SpinnerListModel(TabPlacement.values());
    JSpinner spinner = new JSpinner(model) {
      @Override public Object getNextValue() {
        return super.getPreviousValue();
      }

      @Override public Object getPreviousValue() {
        return super.getNextValue();
      }
    };
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
    spinner.addChangeListener(e -> {
      Object o = model.getValue();
      if (o instanceof TabPlacement) {
        tabs.setTabPlacement(((TabPlacement) o).getPlacement());
      }
    });
    return makePanel(tabs, spinner);
  }

  private static JTabbedPane makeTabbedPane() {
    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JTable", new JScrollPane(new JTable(5, 5)));
    // tabs.addTab("JSplitPane", new JSplitPane());
    // tabs.addTab("JLabel", new JLabel("JLabel"));
    tabs.addTab("JButton", new JButton("JButton"));
    return tabs;
  }

  private static Component makePanel(Component tabs, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    if (c != null) {
      p.add(c, BorderLayout.NORTH);
    }
    p.add(tabs);
    return p;
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

enum TabPlacement {
  TOP(SwingConstants.TOP),
  LEFT(SwingConstants.LEFT),
  BOTTOM(SwingConstants.BOTTOM),
  RIGHT(SwingConstants.RIGHT);

  private final int placement;

  TabPlacement(int placement) {
    this.placement = placement;
  }

  public int getPlacement() {
    return placement;
  }
}
