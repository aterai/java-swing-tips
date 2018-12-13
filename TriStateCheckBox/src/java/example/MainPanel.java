// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final JCheckBox checkBox = new TriStateCheckBox("TriState JCheckBox");
  private final Object[] columnNames = {Status.INDETERMINATE, "Integer", "String"};
  private final Object[][] data = {
    {true, 1, "BBB"}, {false, 12, "AAA"},
    {true, 2, "DDD"}, {false, 5, "CCC"},
    {true, 3, "EEE"}, {false, 6, "GGG"},
    {true, 4, "FFF"}, {false, 7, "HHH"}
  };
  private final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model) {
    protected static final int CHECKBOX_COLUMN = 0;
    protected transient HeaderCheckBoxHandler handler;
    @Override public void updateUI() {
      // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
      // https://bugs.openjdk.java.net/browse/JDK-6788475
      // XXX: set dummy ColorUIResource
      setSelectionForeground(new ColorUIResource(Color.RED));
      setSelectionBackground(new ColorUIResource(Color.RED));
      getTableHeader().removeMouseListener(handler);
      TableModel m = getModel();
      if (Objects.nonNull(m)) {
        m.removeTableModelListener(handler);
      }
      super.updateUI();

      m = getModel();
      for (int i = 0; i < m.getColumnCount(); i++) {
        TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
        if (r instanceof Component) {
          SwingUtilities.updateComponentTreeUI((Component) r);
        }
      }
      TableColumn column = getColumnModel().getColumn(CHECKBOX_COLUMN);
      column.setHeaderRenderer(new HeaderRenderer());
      column.setHeaderValue(Status.INDETERMINATE);

      handler = new HeaderCheckBoxHandler(this, CHECKBOX_COLUMN);
      m.addTableModelListener(handler);
      getTableHeader().addMouseListener(handler);
    }

    @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
      Component c = super.prepareEditor(editor, row, column);
      if (c instanceof JCheckBox) {
        JCheckBox b = (JCheckBox) c;
        b.setBackground(getSelectionBackground());
        b.setBorderPainted(true);
      }
      return c;
    }
  };

  public MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    p.add(checkBox);
    JTabbedPane tp = new JTabbedPane();
    tp.addTab("JCheckBox", p);
    tp.addTab("JTableHeader", new JScrollPane(table));
    add(tp);
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
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtil.createLookAndFeelMenu());
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.setJMenuBar(mb);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class HeaderRenderer implements TableCellRenderer {
  private final TriStateCheckBox check = new TriStateCheckBox("Check All");

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    JLabel l = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    check.setOpaque(false);
    if (value instanceof Status) {
      check.updateStatus((Status) value);
    } else {
      check.setSelected(true);
    }
    l.setIcon(new ComponentIcon(check));
    l.setText(null); // XXX: Nimbus???
    // System.out.println("getHeaderRect: " + table.getTableHeader().getHeaderRect(column));
    // System.out.println("getPreferredSize: " + l.getPreferredSize());
    // System.out.println("getMaximunSize: " + l.getMaximumSize());
    // System.out.println("----");
    // if (l.getPreferredSize().height > 1000) { // XXX: Nimbus???
    //   System.out.println(l.getPreferredSize().height);
    //   Rectangle rect = table.getTableHeader().getHeaderRect(column);
    //   l.setPreferredSize(new Dimension(0, rect.height));
    // }
    return l;
  }
}

class TriStateActionListener implements ActionListener {
  protected Icon icon;

  public void setIcon(Icon icon) {
    this.icon = icon;
  }

  @Override public void actionPerformed(ActionEvent e) {
    JCheckBox cb = (JCheckBox) e.getSource();
    if (cb.isSelected()) {
      if (Objects.nonNull(cb.getIcon())) {
        cb.setIcon(null);
        cb.setSelected(false);
      }
    } else {
      cb.setIcon(icon);
    }
  }
}

class TriStateCheckBox extends JCheckBox {
  protected transient TriStateActionListener listener;
  private transient Icon icon;

  protected TriStateCheckBox(String title) {
    super(title);
  }

  public void updateStatus(Status s) {
    switch (s) {
      case SELECTED:
        setSelected(true);
        setIcon(null);
        break;
      case DESELECTED:
        setSelected(false);
        setIcon(null);
        break;
      case INDETERMINATE:
        setSelected(false);
        setIcon(icon);
        break;
      default:
        throw new AssertionError("Unknown Status");
    }
  }

  @Override public void updateUI() {
    setIcon(null);
    removeActionListener(listener);
    super.updateUI();
    listener = new TriStateActionListener();
    icon = new IndeterminateIcon();
    listener.setIcon(icon);
    addActionListener(listener);
    if (Objects.nonNull(getIcon())) {
      setIcon(icon);
    }
  }
}

class IndeterminateIcon implements Icon {
  private static final Color FOREGROUND = Color.BLACK; // TEST: UIManager.getColor("CheckBox.foreground");
  private static final int SIDE_MARGIN = 4;
  private static final int HEIGHT = 2;
  private final Icon icon = UIManager.getIcon("CheckBox.icon");

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    icon.paintIcon(c, g2, 0, 0);
    g2.setPaint(FOREGROUND);
    g2.fillRect(SIDE_MARGIN, (getIconHeight() - HEIGHT) / 2, getIconWidth() - SIDE_MARGIN - SIDE_MARGIN, HEIGHT);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}

class ComponentIcon implements Icon {
  private final Component cmp;

  protected ComponentIcon(Component cmp) {
    this.cmp = cmp;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    SwingUtilities.paintComponent(g, cmp, c.getParent(), x, y, getIconWidth(), getIconHeight());
  }

  @Override public int getIconWidth() {
    return cmp.getPreferredSize().width;
  }

  @Override public int getIconHeight() {
    return cmp.getPreferredSize().height;
  }
}

enum Status { SELECTED, DESELECTED, INDETERMINATE }

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() { /* Singleton */ }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafRadioGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
    }
    return menu;
  }

  private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
    JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
    lafItem.setActionCommand(lafClassName);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = lafRadioGroup.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException
           | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        ex.printStackTrace();
      }
    });
    lafRadioGroup.add(lafItem);
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
    for (Window window: Frame.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
