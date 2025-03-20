// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("ignore: F1,F4-F7,F9-", true);
    JTextArea textarea = new JTextArea("F2: startEditing\nF8: focusHeader\nF3: beep");
    textarea.setEditable(false);

    JTable table = new JTable(makeModel()) {
      // JTable starts editing when F3 is pressed - howto disable?
      // https://community.oracle.com/thread/1350192
      @Override public boolean editCellAt(int row, int column, EventObject e) {
        return !isFunctionKey(e) && super.editCellAt(row, column, e);
      }

      private boolean isFunctionKey(EventObject e) {
        boolean b = false;
        if (check.isSelected() && e instanceof KeyEvent) {
          int c = ((KeyEvent) e).getKeyCode();
          b = KeyEvent.VK_F1 <= c && c <= KeyEvent.VK_F21;
        }
        return b;
      }
      // private List<Integer> ignoreKeyList = Arrays.asList(
      //   KeyEvent.VK_F1, KeyEvent.VK_F4, KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7,
      //   KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11,KeyEvent.VK_F12);
      // @Override protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
      //   if (!check.isSelected()) {
      //     return super.processKeyBinding(ks, e, condition, pressed);
      //   }
      //   if (e instanceof KeyEvent) {
      //     int c = ((KeyEvent) e).getKeyCode();
      //     if (KeyEvent.VK_F1 <= c && c <= KeyEvent.VK_F21) {
      //       return false;
      //     }
      //   }
      //   return super.processKeyBinding(ks, e, condition, pressed);
      //   // if (ignoreKeyList.contains(ks.getKeyCode())) {
      //   //   return false;
      //   // } else {
      //   //   return super.processKeyBinding(ks, e, condition, pressed);
      //   // }
      // }
    };
    table.setAutoCreateRowSorter(true);

    table.getActionMap().put("beep", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Toolkit.getDefaultToolkit().beep();
      }
    });
    InputMap im = table.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F3, 0), "beep");

    // for (int ks : Arrays.asList(KeyEvent.VK_F1, KeyEvent.VK_F2, KeyEvent.VK_F3, KeyEvent.VK_F4,
    //              KeyEvent.VK_F5, KeyEvent.VK_F6, KeyEvent.VK_F7, KeyEvent.VK_F8,
    //              KeyEvent.VK_F9, KeyEvent.VK_F10, KeyEvent.VK_F11, KeyEvent.VK_F12)) {
    //   InputMap im = table.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    //   if (im.get(KeyStroke.getKeyStroke(ks, 0)) == null) {
    //     im.put(KeyStroke.getKeyStroke(ks, 0), "none");
    //   }
    //   im.put(KeyStroke.getKeyStroke(ks, InputEvent.CTRL_DOWN_MASK), "none");
    //   im.put(KeyStroke.getKeyStroke(ks, InputEvent.SHIFT_DOWN_MASK), "none");
    // }

    JPanel p = new JPanel(new BorderLayout());
    p.add(check, BorderLayout.NORTH);
    p.add(new JScrollPane(textarea));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
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
