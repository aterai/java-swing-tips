// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model);
    table.setAutoCreateRowSorter(true);
    ((JCheckBox) table.getDefaultRenderer(Boolean.class)).putClientProperty("JComponent.sizeVariant", "mini");

    JPanel p1 = new JPanel(new GridLayout(2, 1));
    p1.add(new JScrollPane(table));
    p1.add(new JScrollPane(new JTree()));

    JPanel p2 = new JPanel(new GridLayout(1, 2));
    p2.add(new JLabel("abc"));
    p2.add(new JCheckBox("def"));
    p2.add(new JButton("ghi"));

    add(new JSlider(), BorderLayout.NORTH);
    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JMenuBar mb = new JMenuBar();
    mb.add(SizeVariantUtil.createSizeVariantMenu());
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(mb);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class SizeVariantUtil {
  private SizeVariantUtil() { /* Singleton */ }

  public static JMenu createSizeVariantMenu() {
    JMenu menu = new JMenu("Resizing a Component");
    ButtonGroup bg = new ButtonGroup();
    Stream.of("regular", "mini", "small", "large").forEach(key -> menu.add(createSizeVariantItem(key, bg)));
    return menu;
  }

  private static JRadioButtonMenuItem createSizeVariantItem(String key, ButtonGroup bg) {
    JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(key, "regular".equals(key));
    menuItem.addActionListener(e -> setSizeVariant(bg.getSelection().getActionCommand()));
    menuItem.setActionCommand(key);
    bg.add(menuItem);
    return menuItem;
  }

  private static void setSizeVariant(String key) {
    Stream.of(Frame.getWindows()).forEach(window -> {
      setSizeVariantAllComponents(window, key);
      SwingUtilities.updateComponentTreeUI(window);
      window.pack();
    });
  }

  private static void setSizeVariantAllComponents(Container me, String key) {
    if (me instanceof JComponent) {
      JComponent jc = (JComponent) me;
      // if (jc instanceof JTable) {
      //   JTable table = (JTable) jc;
      //   JCheckBox cb = (JCheckBox) table.getDefaultRenderer(Boolean.class);
      //   cb.setFont(new FontUIResource(new Font(null)));
      //   cb.putClientProperty("JComponent.sizeVariant", key);
      // }
      jc.setFont(new FontUIResource(jc.getFont()));
      jc.putClientProperty("JComponent.sizeVariant", key);
    }
    for (Component c: me.getComponents()) {
      if (c instanceof Container) {
        setSizeVariantAllComponents((Container) c, key);
      }
    }
  }
}
