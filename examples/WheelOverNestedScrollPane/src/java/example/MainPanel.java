// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private static final String TEXT = "aaa\na\na\na\na\naaa\na\na\na\nbbb\n";

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(createModel());
    table.setAutoCreateRowSorter(true);

    JTextArea textArea = new JTextArea(TEXT);
    textArea.setEditable(false);

    // JTextPane#replaceSelection(...) and #insertComponent(...) insert at the caret,
    // which follows the inserted text, and do nothing if the text pane is not editable
    JTextPane textPane = new JTextPane();
    textPane.setMargin(new Insets(5, 10, 5, 5));
    textPane.replaceSelection(TEXT + TEXT + TEXT);
    textPane.insertComponent(createChildScrollPane(textArea));
    textPane.replaceSelection("\n" + TEXT);
    textPane.insertComponent(createChildScrollPane(table));
    textPane.replaceSelection("\n" + TEXT);
    textPane.insertComponent(new JScrollPane(new JTree()));
    textPane.replaceSelection("\n" + TEXT);
    textPane.setEditable(false);

    add(new JLayer<>(new JScrollPane(textPane), new WheelScrollLayerUI()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel createModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"zzz", 6, false}, {"bbb", 22, true}, {"nnn", 9, false},
        {"ccc", 32, true}, {"ooo", 8, false}, {"ddd", 42, true}, {"ppp", 9, false},
        {"eee", 52, true}, {"qqq", 8, false}, {"fff", 62, true}, {"rrr", 7, false},
        {"ggg", 51, true}, {"sss", 6, false}, {"hhh", 41, true}, {"ttt", 5, false},
        {"iii", 51, true}, {"uuu", 4, false}, {"jjj", 61, true}, {"vvv", 3, false},
        {"kkk", 72, true}, {"www", 2, false}, {"lll", 82, true}, {"xxx", 1, false},
        {"mmm", 92, true}, {"yyy", 0, false},
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private static JScrollPane createChildScrollPane(Component view) {
    return new JScrollPane(view) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(240, 120);
      }

      @Override public Dimension getMaximumSize() {
        Dimension d = super.getMaximumSize();
        d.height = getPreferredSize().height;
        return d;
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

class WheelScrollLayerUI extends LayerUI<JScrollPane> {
  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.MOUSE_WHEEL_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
    super.uninstallUI(c);
  }

  @Override protected void processMouseWheelEvent(MouseWheelEvent e, JLayer<? extends JScrollPane> l) {
    Component c = e.getComponent();
    JScrollPane parent = l.getView();
    if (c instanceof JScrollPane && !c.equals(parent)) {
      BoundedRangeModel m = ((JScrollPane) c).getVerticalScrollBar().getModel();
      int rotation = e.getWheelRotation();
      boolean isTop = rotation < 0 && m.getValue() <= m.getMinimum();
      boolean isBottom = rotation > 0 && m.getValue() + m.getExtent() >= m.getMaximum();
      if (isTop || isBottom) {
        parent.dispatchEvent(SwingUtilities.convertMouseEvent(c, e, parent));
      }
    }
  }
}
