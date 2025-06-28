// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.im.InputContext;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    String help1 = "Default: IMEの一時ウィンドウからEnterキーで入力キャンセル";
    add(makeTitledPane(new JScrollPane(new JTable(4, 3)), help1));
    JTable table = new CompositionEnabledTable(4, 3);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    // table.setSurrendersFocusOnKeystroke(true);
    String help2 = "IMEの一時ウィンドウからEnterキーでセルに文字列コピー+編集開始";
    add(makeTitledPane(new JScrollPane(table), help2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPane(Component c, String title) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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

class CompositionEnabledTable extends JTable {
  protected CompositionEnabledTable(int numRows, int numColumns) {
    super(numRows, numColumns);
  }

  @Override protected boolean processKeyBinding(KeyStroke ks, KeyEvent e, int condition, boolean pressed) {
    if (!isTabOrEnterKey(ks.getKeyCode())) {
      startEditing(ks, pressed);
    }
    return super.processKeyBinding(ks, e, condition, pressed);
  }

  private void startEditing(KeyStroke ks, boolean pressed) {
    InputContext ic = getInputContext();
    boolean isCompEnabled = ic != null && ic.isCompositionEnabled();
    if (isCompEnabled && !isEditing() && !pressed && !ks.isOnKeyRelease()) {
      int selectedRow = getSelectedRow();
      int selectedColumn = getSelectedColumn();
      if (selectedRow != -1 && selectedColumn != -1) {
        editCellAt(selectedRow, selectedColumn);
        // boolean b = editCellAt(selectedRow, selectedColumn);
        // System.out.println("editCellAt: " + b);
      }
    }
  }

  private boolean isTabOrEnterKey(int keyCode) {
    return keyCode == KeyEvent.VK_TAB || keyCode == KeyEvent.VK_ENTER;
  }
}
