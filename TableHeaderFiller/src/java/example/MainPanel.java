// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table1 = makeTable();
    JTable table2 = makeTable();

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(new JScrollPane(table1));
    sp.setBottomComponent(new JLayer<>(new JScrollPane(table2), new TableHeaderFillerLayerUI()));
    sp.setResizeWeight(.5);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JTable makeTable() {
    JTable table = new JTable(4, 3);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateRowSorter(true);
    return table;
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

class TableHeaderFillerLayerUI extends LayerUI<JScrollPane> {
  private final JTable tempTable = new JTable(new DefaultTableModel(new Object[] {""}, 0));
  private final JTableHeader filler = tempTable.getTableHeader();
  private final TableColumn fillerColumn = tempTable.getColumnModel().getColumn(0);

  @Override public void updateUI(JLayer<? extends JScrollPane> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(tempTable);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JScrollPane scroll = (JScrollPane) ((JLayer<?>) c).getView();
      JTable table = (JTable) scroll.getViewport().getView();
      JTableHeader header = table.getTableHeader();

      int width = header.getWidth();
      TableColumnModel cm = header.getColumnModel();
      for (int i = 0; i < cm.getColumnCount(); i++) {
        width -= cm.getColumn(i).getWidth();
      }

      Point pt = SwingUtilities.convertPoint(header, 0, 0, c);
      filler.setLocation(pt.x + header.getWidth() - width, pt.y);
      filler.setSize(width, header.getHeight());
      fillerColumn.setWidth(width);

      SwingUtilities.paintComponent(g, filler, tempTable, filler.getBounds());
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.COMPONENT_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processComponentEvent(ComponentEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    if (e.getID() == ComponentEvent.COMPONENT_RESIZED && c instanceof JTableHeader) {
      l.repaint(c.getBounds());
    }
  }
}

// // @see https://web.archive.org/web/20120126055437/http://l2fprod.com/blog/2008/08/30/the-fun-of-swing-jtable-column-resizing/
// class TableHeaderFiller implements ComponentListener, PropertyChangeListener {
//   private JTable table;
//   private JTableHeader header;
//   private List<TableColumn> columns = new ArrayList<>();
//   private JTableHeader filler;
//   private TableColumn fillerColumn;
//
//   private PropertyChangeListener columnWidthChangedListener = e -> update();
//
//   public TableHeaderFiller(JTable table) {
//     this.table = table;
//
//     DefaultTableModel tableModel = new DefaultTableModel(new Object[] {""}, 0);
//     JTable tempTable = new JTable(tableModel);
//     filler = tempTable.getTableHeader();
//     filler.setReorderingAllowed(false);
//
//     for (MouseMotionListener listener : filler.getMouseMotionListeners()) {
//       filler.removeMouseMotionListener(listener);
//     }
//     for (MouseListener listener : filler.getMouseListeners()) {
//       filler.removeMouseListener(listener);
//     }
//
//     fillerColumn = tempTable.getColumnModel().getColumn(0);
//     fillerColumn.setResizable(false);
//
//     installListeners();
//     update();
//   }
//
//   private void installListeners() {
//     header = table.getTableHeader();
//     header.add(filler);
//
//     table.addPropertyChangeListener("model", this);
//     table.addPropertyChangeListener("tableHeader", this);
//     table.addComponentListener(this);
//     header.addComponentListener(this);
//
//     TableColumnModel columnModel = header.getColumnModel();
//     int c = columnModel.getColumnCount();
//     for (int i = 0; i < c; i++) {
//       TableColumn column = columnModel.getColumn(i);
//       columns.add(column);
//       column.addPropertyChangeListener(columnWidthChangedListener);
//     }
//   }
//
//   private void uninstallListeners() {
//     table.removeComponentListener(this);
//     header.removeComponentListener(this);
//     header.remove(filler);
//
//     for (TableColumn column : columns) {
//       column.removePropertyChangeListener(columnWidthChangedListener);
//     }
//     columns.clear();
//   }
//
//   private void update() {
//     int height = header.getHeight();
//     int width = header.getWidth();
//     TableColumnModel columnModel = header.getColumnModel();
//     for (int i = 0, c = columnModel.getColumnCount(); i < c; i++) {
//       width -= columnModel.getColumn(i).getWidth();
//     }
//     filler.setSize(width, height);
//     filler.setLocation(header.getWidth() - width, 0);
//     fillerColumn.setWidth(width);
//   }
//
//   @Override public void propertyChange(PropertyChangeEvent e) {
//     uninstallListeners();
//     installListeners();
//     update();
//   }
//
//   @Override public void componentHidden(ComponentEvent e) {}
//
//   @Override public void componentMoved(ComponentEvent e) {}
//
//   @Override public void componentResized(ComponentEvent e) {
//     update();
//   }
//
//   @Override public void componentShown(ComponentEvent e) {
//     update();
//   }
// }

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
