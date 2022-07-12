// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private  MainPanel() {
    super(new BorderLayout());
    JCheckBox check = new JCheckBox("一時ウィンドウ(入力モード)->enterでセル編集開始");

    JTable table = new JTable(4, 3) {
      @Override protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
        // System.out.println("key: " + ks.toString());
        if (!check.isSelected() || isTabOrEnterKey(ks)) {
          // System.out.println("tab or enter typed");
          return super.processKeyBinding(ks, e, condition, pressed);
        }
        InputContext ic = getInputContext();
        if (ic.isCompositionEnabled() && !isEditing() && !pressed && !ks.isOnKeyRelease()) {
          int selectedRow = getSelectedRow();
          int selectedColumn = getSelectedColumn();
          if (selectedRow != -1 && selectedColumn != -1) {
            editCellAt(selectedRow, selectedColumn);
            // boolean b = editCellAt(selectedRow, selectedColumn);
            // System.out.println("editCellAt: " + b);
          }
        }
        return super.processKeyBinding(ks, e, condition, pressed);
      }

      private boolean isTabOrEnterKey(KeyStroke ks) {
        return KeyStroke.getKeyStroke('\t').equals(ks) || KeyStroke.getKeyStroke('\n').equals(ks);
      }
    };
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    // table.setSurrendersFocusOnKeystroke(true);
    // table.setShowGrid(false);
    // table.setShowHorizontalLines(false);
    // table.setShowVerticalLines(false);

    add(check, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
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
