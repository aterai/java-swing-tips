package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.table.*;
import javax.swing.undo.*;

public class MainPanel extends JPanel {
    String[] columnNames = {"String", "String"};
    Object[][] data = {
        {"Undo", "Ctrl Z"}, {"Redo", "Ctrl Y"},
        {"AAA",  "bbbbbb"}, {"CCC", "ddddddd"}
    };
    DefaultTableModel model = new DefaultTableModel(data, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            return getValueAt(0, column).getClass();
        }
    };
    JTable table = new JTable(model);

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        DefaultCellEditor ce = (DefaultCellEditor)table.getDefaultEditor(Object.class);
        JTextComponent textField = (JTextComponent)ce.getComponent();
        installTextComponentPopupMenu(textField);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 200));
    }

    public static JPopupMenu installTextComponentPopupMenu(final JTextComponent tc) {
        final UndoManager manager = new UndoManager();
        final Action undoAction   = new UndoAction(manager);
        final Action redoAction   = new RedoAction(manager);
        final Action cutAction    = new DefaultEditorKit.CutAction();
        final Action copyAction   = new DefaultEditorKit.CopyAction();
        final Action pasteAction  = new DefaultEditorKit.PasteAction();
        final Action deleteAction = new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                JPopupMenu pop = (JPopupMenu)e.getSource();
                ((JTextComponent)pop.getInvoker()).replaceSelection(null);
            }
        };
        tc.addAncestorListener(new AncestorListener() {
            @Override public void ancestorAdded(AncestorEvent e) {
                manager.discardAllEdits();
                tc.requestFocusInWindow();
            }
            @Override public void ancestorMoved(AncestorEvent e) {}
            @Override public void ancestorRemoved(AncestorEvent e) {}
        });
        tc.getDocument().addUndoableEditListener(manager);
        tc.getActionMap().put("undo", undoAction);
        tc.getActionMap().put("redo", redoAction);
        InputMap imap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK), "undo");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK), "redo");

        JPopupMenu popup = new JPopupMenu();
        popup.add(cutAction);
        popup.add(copyAction);
        popup.add(pasteAction);
        popup.add(deleteAction);
        popup.addSeparator();
        popup.add(undoAction);
        popup.add(redoAction);

        popup.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuCanceled(PopupMenuEvent e) {}
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                undoAction.setEnabled(true);
                redoAction.setEnabled(true);
            }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                JPopupMenu pop = (JPopupMenu)e.getSource();
                JTextComponent field = (JTextComponent)pop.getInvoker();
                boolean flg = field.getSelectedText()!=null;
                cutAction.setEnabled(flg);
                copyAction.setEnabled(flg);
                deleteAction.setEnabled(flg);
                undoAction.setEnabled(manager.canUndo());
                redoAction.setEnabled(manager.canRedo());
            }
        });
        tc.setComponentPopupMenu(popup);
        return popup;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
class UndoAction extends AbstractAction {
    private final UndoManager undoManager;
    public UndoAction(UndoManager manager) {
        super("undo");
        this.undoManager = manager;
    }
    @Override public void actionPerformed(ActionEvent e) {
        try{
            undoManager.undo();
        }catch(CannotUndoException cue) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
class RedoAction extends AbstractAction {
    private final UndoManager undoManager;
    public RedoAction(UndoManager manager) {
        super("redo");
        this.undoManager = manager;
    }
    @Override public void actionPerformed(ActionEvent e) {
        try{
            undoManager.redo();
        }catch(CannotRedoException cre) {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
