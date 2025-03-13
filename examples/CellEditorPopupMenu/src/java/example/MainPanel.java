// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);
    DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
    JTextComponent textField = (JTextComponent) ce.getComponent();
    JPopupMenu popup = new TextComponentPopupMenu(textField);
    textField.setComponentPopupMenu(popup);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "String"};
    Object[][] data = {
        {"Undo", "Ctrl Z"}, {"Redo", "Ctrl Y"},
        {"AAA", "bbb bbb"}, {"CCC", "ddd ddd"}
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

final class TextComponentPopupMenu extends JPopupMenu {
  /* default */ TextComponentPopupMenu(JTextComponent tc) {
    super();
    Action cutAction = new DefaultEditorKit.CutAction();
    add(cutAction);
    Action copyAction = new DefaultEditorKit.CopyAction();
    add(copyAction);
    Action pasteAction = new DefaultEditorKit.PasteAction();
    add(pasteAction);
    Action deleteAction = new DeleteAction();
    add(deleteAction);
    addSeparator();

    UndoManager manager = new UndoManager();
    Action undoAction = new UndoAction(manager);
    add(undoAction);

    Action redoAction = new RedoAction(manager);
    add(redoAction);

    tc.addAncestorListener(new AncestorListener() {
      @Override public void ancestorAdded(AncestorEvent e) {
        manager.discardAllEdits();
        e.getComponent().requestFocusInWindow();
      }

      @Override public void ancestorMoved(AncestorEvent e) {
        /* not needed */
      }

      @Override public void ancestorRemoved(AncestorEvent e) {
        /* not needed */
      }
    });
    tc.getDocument().addUndoableEditListener(manager);
    tc.getActionMap().put("undo", undoAction);
    tc.getActionMap().put("redo", redoAction);
    InputMap im = tc.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, mask), "undo");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, mask), "redo");

    addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        undoAction.setEnabled(true);
        redoAction.setEnabled(true);
      }

      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JTextComponent tc = (JTextComponent) getInvoker();
        boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
        cutAction.setEnabled(hasSelectedText);
        copyAction.setEnabled(hasSelectedText);
        deleteAction.setEnabled(hasSelectedText);
        undoAction.setEnabled(manager.canUndo());
        redoAction.setEnabled(manager.canRedo());
      }
    });
  }
}

class UndoAction extends AbstractAction {
  private final UndoManager undoManager;

  protected UndoAction(UndoManager manager) {
    super("undo");
    this.undoManager = manager;
  }

  @Override public void actionPerformed(ActionEvent e) {
    try {
      undoManager.undo();
    } catch (CannotUndoException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
    }
  }
}

class RedoAction extends AbstractAction {
  private final UndoManager undoManager;

  protected RedoAction(UndoManager manager) {
    super("redo");
    this.undoManager = manager;
  }

  @Override public void actionPerformed(ActionEvent e) {
    try {
      undoManager.redo();
    } catch (CannotRedoException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
    }
  }
}

class DeleteAction extends AbstractAction {
  protected DeleteAction() {
    super("delete");
  }

  @Override public void actionPerformed(ActionEvent e) {
    // Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, src);
    Container c = SwingUtilities.getUnwrappedParent((Component) e.getSource());
    if (c instanceof JPopupMenu) {
      JPopupMenu pop = (JPopupMenu) c;
      ((JTextComponent) pop.getInvoker()).replaceSelection(null);
    }
  }
}
