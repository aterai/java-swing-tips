package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

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
  private final String[] columnNames = {"String", "String"};
  private final Object[][] data = {
    {"Undo", "Ctrl Z"}, {"Redo", "Ctrl Y"},
    {"AAA", "bbbbbb"}, {"CCC", "ddddddd"}
  };
  private final TableModel model = new DefaultTableModel(data, columnNames) {
    @Override public Class<?> getColumnClass(int column) {
      return getValueAt(0, column).getClass();
    }
  };
  private final JTable table = new JTable(model);

  private MainPanel() {
    super(new BorderLayout());
    table.setAutoCreateRowSorter(true);
    DefaultCellEditor ce = (DefaultCellEditor) table.getDefaultEditor(Object.class);
    JTextComponent textField = (JTextComponent) ce.getComponent();
    JPopupMenu popup = new TextComponentPopupMenu(textField);
    textField.setComponentPopupMenu(popup);
    add(new JScrollPane(table));
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
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TextComponentPopupMenu extends JPopupMenu {
  protected TextComponentPopupMenu(JTextComponent tc) {
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

      @Override public void ancestorMoved(AncestorEvent e) { /* not needed */ }

      @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
    });
    tc.getDocument().addUndoableEditListener(manager);
    tc.getActionMap().put("undo", undoAction);
    tc.getActionMap().put("redo", redoAction);
    InputMap imap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "redo");

    addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }

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
      Toolkit.getDefaultToolkit().beep();
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
      Toolkit.getDefaultToolkit().beep();
    }
  }
}

class DeleteAction extends AbstractAction {
  protected DeleteAction() {
    super("delete");
  }

  @Override public void actionPerformed(ActionEvent e) {
    // Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, (Component) e.getSource());
    Container c = SwingUtilities.getUnwrappedParent((Component) e.getSource());
    if (c instanceof JPopupMenu) {
      JPopupMenu pop = (JPopupMenu) c;
      ((JTextComponent) pop.getInvoker()).replaceSelection(null);
    }
  }
}
