package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "String"};
    private final Object[][] data = {
        {"Undo", "Ctrl Z"}, {"Redo", "Ctrl Y"},
        {"AAA",  "bbbbbb"}, {"CCC", "ddddddd"}
    };
    private final DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    private final JTable table = new JTable(model);

    public MainPanel() {
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
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
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
    protected TextComponentPopupMenu(final JTextComponent tc) {
        super();

        final UndoManager manager = new UndoManager();
        final Action undoAction   = new UndoAction(manager);
        final Action redoAction   = new RedoAction(manager);
        final Action cutAction    = new DefaultEditorKit.CutAction();
        final Action copyAction   = new DefaultEditorKit.CopyAction();
        final Action pasteAction  = new DefaultEditorKit.PasteAction();
        final Action deleteAction = new DeleteAction();
//         final Action deleteAction = new AbstractAction("delete") {
//             @Override public void actionPerformed(ActionEvent e) {
//                 ((JTextComponent) getInvoker()).replaceSelection(null);
//             }
//         };
        tc.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                manager.discardAllEdits();
                tc.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e)   { /* not needed */ }
            @Override public void ancestorRemoved(AncestorEvent e) { /* not needed */ }
        });
        tc.getDocument().addUndoableEditListener(manager);
        tc.getActionMap().put("undo", undoAction);
        tc.getActionMap().put("redo", redoAction);
        InputMap imap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "redo");

        add(cutAction);
        add(copyAction);
        add(pasteAction);
        add(deleteAction);
        addSeparator();
        add(undoAction);
        add(redoAction);

        addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                undoAction.setEnabled(true);
                redoAction.setEnabled(true);
            }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                JTextComponent tc = (JTextComponent) getInvoker();
                boolean flg = tc.getSelectionStart() != tc.getSelectionEnd();
                cutAction.setEnabled(flg);
                copyAction.setEnabled(flg);
                deleteAction.setEnabled(flg);
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
        } catch (CannotUndoException cue) {
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
        } catch (CannotRedoException cre) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}

class DeleteAction extends AbstractAction {
    protected DeleteAction() {
        super("delete");
    }
    @Override public void actionPerformed(ActionEvent e) {
        //Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, (Component) e.getSource());
        Container c = SwingUtilities.getUnwrappedParent((Component) e.getSource());
        if (c instanceof JPopupMenu) {
            JPopupMenu pop = (JPopupMenu) c;
            ((JTextComponent) pop.getInvoker()).replaceSelection(null);
        }
    }
}
