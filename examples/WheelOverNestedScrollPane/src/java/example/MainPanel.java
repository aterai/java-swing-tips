// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseWheelEvent;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private static final String TEXT = "aaa\na\na\na\na\naaa\na\na\na\nbbb\n";

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);

    JTextPane textPane = new JTextPane();
    textPane.setEditable(false);
    textPane.setMargin(new Insets(5, 10, 5, 5));

    JTextComponent c = new JTextArea(TEXT);
    c.setEditable(false);

    Document doc = textPane.getDocument();
    try {
      doc.insertString(doc.getLength(), TEXT, null);
      doc.insertString(doc.getLength(), TEXT, null);
      doc.insertString(doc.getLength(), TEXT, null);
      textPane.insertComponent(createChildScrollPane(c));
      doc.insertString(doc.getLength(), "\n", null);
      doc.insertString(doc.getLength(), TEXT, null);
      textPane.insertComponent(createChildScrollPane(table));
      doc.insertString(doc.getLength(), "\n", null);
      doc.insertString(doc.getLength(), TEXT, null);
      textPane.insertComponent(new JScrollPane(new JTree()));
      doc.insertString(doc.getLength(), "\n", null);
      doc.insertString(doc.getLength(), TEXT, null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    add(new JLayer<>(new JScrollPane(textPane), new WheelScrollLayerUI()));
    // add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"zzz", 6, false}, {"bbb", 22, true}, {"nnn", 9, false},
        {"ccc", 32, true}, {"ooo", 8, false}, {"ddd", 42, true}, {"ppp", 9, false},
        {"eee", 52, true}, {"qqq", 8, false}, {"fff", 62, true}, {"rrr", 7, false},
        {"ggg", 51, true}, {"sss", 6, false}, {"hhh", 41, true}, {"ttt", 5, false},
        {"iii", 51, true}, {"uuu", 4, false}, {"jjj", 61, true}, {"vvv", 3, false},
        {"kkk", 72, true}, {"www", 2, false}, {"lll", 82, true}, {"xxx", 1, false},
        {"mmm", 92, true}, {"yyy", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  public static JScrollPane createChildScrollPane(Component view) {
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
    int dir = e.getWheelRotation();
    JScrollPane main = l.getView();
    if (c instanceof JScrollPane && !c.equals(main)) {
      JScrollPane child = (JScrollPane) c;
      BoundedRangeModel m = child.getVerticalScrollBar().getModel();
      int extent = m.getExtent();
      int minimum = m.getMinimum();
      int maximum = m.getMaximum();
      int value = m.getValue();
      boolean b1 = dir > 0 && value + extent >= maximum;
      boolean b2 = dir < 0 && value <= minimum;
      if (b1 || b2) {
        main.dispatchEvent(SwingUtilities.convertMouseEvent(c, e, main));
      }
    }
  }
}
