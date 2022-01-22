// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        super.updateUI();
        TableCellRenderer r = getDefaultRenderer(Boolean.class);
        String key = "JComponent.sizeVariant";
        Object value = getClientProperty(key);
        if (r instanceof JComponent && value != null) {
          ((JComponent) r).putClientProperty(key, value);
        }
      }
    };
    table.setAutoCreateRowSorter(true);

    JPanel p1 = new JPanel(new GridLayout(2, 1));
    p1.add(new JScrollPane(table));
    p1.add(new JScrollPane(new JTree()));

    JPanel p2 = new JPanel(new GridLayout(1, 2));
    p2.add(new JLabel("abc"));
    p2.add(new JCheckBox("def"));
    p2.add(new JButton("ghi"));

    JMenuBar mb = new JMenuBar();
    mb.add(SizeVariantUtil.createSizeVariantMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(new JSlider(), BorderLayout.NORTH);
    add(p1);
    add(p2, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

final class SizeVariantUtil {
  private SizeVariantUtil() {
    /* Singleton */
  }

  public static JMenu createSizeVariantMenu() {
    JMenu menu = new JMenu("Resizing a Component");
    ButtonGroup bg = new ButtonGroup();
    Stream.of("regular", "mini", "small", "large")
        .forEach(cmd -> menu.add(createSizeVariantItem(cmd, bg)));
    return menu;
  }

  private static JRadioButtonMenuItem createSizeVariantItem(String cmd, ButtonGroup bg) {
    JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(cmd, "regular".equals(cmd));
    menuItem.addActionListener(e -> setSizeVariant(bg.getSelection().getActionCommand()));
    menuItem.setActionCommand(cmd);
    bg.add(menuItem);
    return menuItem;
  }

  private static void setSizeVariant(String cmd) {
    Stream.of(Window.getWindows()).forEach(window -> {
      setSizeVariantAllComponents(window, cmd);
      SwingUtilities.updateComponentTreeUI(window);
      window.pack();
    });
  }

  private static void setSizeVariantAllComponents(Container me, String cmd) {
    if (me instanceof JComponent) {
      JComponent jc = (JComponent) me;
      jc.setFont(new FontUIResource(jc.getFont()));
      jc.putClientProperty("JComponent.sizeVariant", cmd);
    }
    for (Component c : me.getComponents()) {
      if (c instanceof Container) {
        setSizeVariantAllComponents((Container) c, cmd);
      }
    }
  }
}
