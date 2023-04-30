// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabs1 = makeTabbedPane();
    JMenu menu = new JMenu("JMenu");
    ButtonGroup bg1 = new ButtonGroup();
    ItemListener handler1 = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg1.getSelection().getActionCommand();
        tabs1.setTabPlacement(TabPlacement.valueOf(name).getPlacement());
      }
    };
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JMenuItem item = new JRadioButtonMenuItem(name, selected);
      item.addItemListener(handler1);
      item.setActionCommand(name);
      menu.add(item);
      bg1.add(item);
    });
    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    mb.add(Box.createHorizontalGlue());
    // EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    JTabbedPane tabs2 = makeTabbedPane();
    ButtonGroup bg2 = new ButtonGroup();
    ItemListener handler2 = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg2.getSelection().getActionCommand();
        tabs2.setTabPlacement(TabPlacement.valueOf(name).getPlacement());
      }
    };
    Box box = Box.createHorizontalBox();
    Arrays.asList(TabPlacement.values()).forEach(tp -> {
      String name = tp.name();
      boolean selected = tp == TabPlacement.TOP;
      JRadioButton radio = new JRadioButton(name, selected);
      radio.addItemListener(handler2);
      radio.setActionCommand(name);
      box.add(radio);
      bg2.add(radio);
    });

    JTabbedPane tabs3 = makeTabbedPane();
    JComboBox<TabPlacement> combo = new JComboBox<>(TabPlacement.values());
    combo.addItemListener(e -> {
      Object o = e.getItem();
      if (e.getStateChange() == ItemEvent.SELECTED && o instanceof TabPlacement) {
        tabs3.setTabPlacement(((TabPlacement) o).getPlacement());
        // String name = e.getItem().toString();
        // tabs3.setTabPlacement(TabPlacement.valueOf(name).getPlacement());
        // int idx = combo.getSelectedIndex();
        // tabs3.setTabPlacement(combo.getItemAt(idx).getPlacement());
      }
    });

    JTabbedPane tabs4 = makeTabbedPane();
    SpinnerListModel model4 = new SpinnerListModel(TabPlacement.values());
    JSpinner spinner = new JSpinner(model4) {
      @Override public Object getNextValue() {
        return super.getPreviousValue();
      }

      @Override public Object getPreviousValue() {
        return super.getNextValue();
      }
    };
    ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
    spinner.addChangeListener(e -> {
      Object o = model4.getValue();
      if (o instanceof TabPlacement) {
        tabs4.setTabPlacement(((TabPlacement) o).getPlacement());
      }
    });

    JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabbedPane.addTab("JRadioButtonMenuItem", makePanel(tabs1, mb));
    tabbedPane.addTab("JRadioButton", makePanel(tabs2, box));
    tabbedPane.addTab("JComboBox", makePanel(tabs3, combo));
    tabbedPane.addTab("JSpinner", makePanel(tabs4, spinner));
    add(tabbedPane);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTabbedPane makeTabbedPane() {
    JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JTable", new JScrollPane(new JTable(5, 5)));
    tabs.addTab("JSplitPane", new JSplitPane());
    tabs.addTab("JLabel", new JLabel("JLabel"));
    tabs.addTab("JButton", new JButton("JButton"));
    return tabs;
  }

  private JPanel makePanel(JTabbedPane tabs, JComponent c) {
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
