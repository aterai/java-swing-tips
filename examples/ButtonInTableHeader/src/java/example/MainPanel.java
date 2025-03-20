// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.plaf.ColorUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new CustomHeaderTable(makeModel());
    JPopupMenu pop = new JPopupMenu();
    pop.add("JMenuItem: 1");
    pop.add("JMenuItem: 22");
    pop.add("JMenuItem: 333");
    HeaderRenderer r = new HeaderRenderer(table.getTableHeader(), pop);
    table.getColumnModel().getColumn(0).setHeaderRenderer(r);
    table.getColumnModel().getColumn(1).setHeaderRenderer(r);
    table.getColumnModel().getColumn(2).setHeaderRenderer(r);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Boolean", "Integer", "String"};
    Object[][] data = {
        {true, 1, "BBB"}, {false, 12, "AAA"}, {true, 2, "DDD"}, {false, 5, "CCC"},
        {true, 3, "EEE"}, {false, 6, "GGG"}, {true, 4, "FFF"}, {false, 7, "HHH"}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
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

class CustomHeaderTable extends JTable {
  protected CustomHeaderTable(TableModel model) {
    super(model);
  }

  @Override public void updateUI() {
    setSelectionForeground(new ColorUIResource(Color.RED));
    setSelectionBackground(new ColorUIResource(Color.RED));
    super.updateUI();
    TableModel m = getModel();
    for (int i = 0; i < m.getColumnCount(); i++) {
      TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
      if (r instanceof Component) {
        SwingUtilities.updateComponentTreeUI((Component) r);
      }
    }
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
}

class HeaderRenderer extends JButton implements TableCellRenderer {
  protected static final int BUTTON_WIDTH = 16;
  protected static final Color BUTTON_BGC = new Color(0x64_C8_C8_C8, true);
  private final JPopupMenu popup;
  private int rolloverIndex = -1;

  protected HeaderRenderer(JTableHeader header, JPopupMenu popup) {
    super();
    this.popup = popup;
    // TableColumnModel columnModel = table.getColumnModel();
    // int vci = columnModel.getColumnIndexAtX(e.getX());
    // int mci = table.convertColumnIndexToModel(vci);
    // TableColumn column = table.getColumnModel().getColumn(mci);
    // int w = column.getWidth(); // Nimbus???
    // int h = header.getHeight();
    // if (!isNimbus) {
    //   Insets i = c.getInsets();
    //   r.translate(r.width - i.right, 0);
    // } else {
    MouseInputListener handler = new RolloverHandler();
    header.addMouseListener(handler);
    header.addMouseMotionListener(handler);
  }

  @Override public void updateUI() {
    super.updateUI();
    // setOpaque(false);
    // setFont(header.getFont());
    setBorder(BorderFactory.createEmptyBorder());
    setContentAreaFilled(false);
    EventQueue.invokeLater(() -> SwingUtilities.updateComponentTreeUI(popup));
  }

  // JButton button = new JButton(new AbstractAction() {
  //   @Override public void actionPerformed(ActionEvent e) {
  //     System.out.println("clicked");
  //   }
  // });

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
    Component c = r.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      setIcon(new MenuArrowIcon());
      l.removeAll();
      int mci = table.convertColumnIndexToModel(column);
      if (rolloverIndex == mci) {
        int w = table.getColumnModel().getColumn(mci).getWidth();
        int h = table.getTableHeader().getHeight();
        // Icon arrowIcon = new MenuArrowIcon();
        Border outside = l.getBorder();
        Border inside = BorderFactory.createEmptyBorder(0, 0, 0, BUTTON_WIDTH);
        Border b = BorderFactory.createCompoundBorder(outside, inside);
        l.setBorder(b);
        l.add(this);
        // Insets i = b.getBorderInsets(l);
        // setBounds(w - i.right, 0, BUTTON_WIDTH, h - 2);
        setBounds(w - BUTTON_WIDTH, 0, BUTTON_WIDTH, h - 2);
        setBackground(BUTTON_BGC);
        setOpaque(true);
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Color.GRAY));
      }
      // if (l.getPreferredSize().height > 1000) { // XXX: Nimbus
      //   System.out.println(l.getPreferredSize().height);
      //   l.setPreferredSize(new Dimension(0, h));
      // }
    }
    return c;
  }

  private final class RolloverHandler extends MouseInputAdapter {
    @Override public void mouseClicked(MouseEvent e) {
      JTableHeader header = (JTableHeader) e.getComponent();
      JTable table = header.getTable();
      // TableColumnModel columnModel = table.getColumnModel();
      // int vci = columnModel.getColumnIndexAtX(e.getX());
      int vci = table.columnAtPoint(e.getPoint());
      // int mci = table.convertColumnIndexToModel(vci);
      // TableColumn column = table.getColumnModel().getColumn(mci);
      // int w = column.getWidth(); // Nimbus???
      // int h = header.getHeight();
      Rectangle r = header.getHeaderRect(vci);
      Container c = (Container) getTableCellRendererComponent(
          table, "", true, true, -1, vci);
      // if (!isNimbus) {
      //   Insets i = c.getInsets();
      //   r.translate(r.width - i.right, 0);
      // } else {
      r.translate(r.width - BUTTON_WIDTH, 0);
      r.setSize(BUTTON_WIDTH, r.height);
      Point pt = e.getPoint();
      if (c.getComponentCount() > 0 && r.contains(pt)) {
        popup.show(header, r.x, r.height);
        JButton b = (JButton) c.getComponent(0);
        b.doClick();
        e.consume();
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      rolloverIndex = -1;
    }

    @Override public void mouseMoved(MouseEvent e) {
      JTableHeader header = (JTableHeader) e.getComponent();
      JTable table = header.getTable();
      int vci = table.columnAtPoint(e.getPoint());
      rolloverIndex = table.convertColumnIndexToModel(vci);
    }
  }
}

class MenuArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(Color.BLACK);
    g2.translate(x, y);
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 10;
  }

  @Override public int getIconHeight() {
    return 10;
  }
}
