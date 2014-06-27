package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public final class MainPanel extends JPanel {
    private final JTextField textField0 = new JTextField("default");
    private final JTextField textField1 = new JTextField();
    private final UndoManager undoManager0 = new UndoManager();
    private final UndoManager undoManager1 = new UndoManager();

    public MainPanel() {
        super(new BorderLayout());

        textField1.setDocument(new CustomUndoPlainDocument());
        textField1.setText("aaaaaaaaaaaaaaaaaaaaa");

        textField0.getDocument().addUndoableEditListener(undoManager0);
        textField1.getDocument().addUndoableEditListener(undoManager1);

        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("undo") {
            @Override public void actionPerformed(ActionEvent e) {
                if (undoManager0.canUndo()) {
                    undoManager0.undo();
                }
                if (undoManager1.canUndo()) {
                    undoManager1.undo();
                }
            }
        }));
        p.add(new JButton(new AbstractAction("redo") {
            @Override public void actionPerformed(ActionEvent e) {
                if (undoManager0.canRedo()) {
                    undoManager0.redo();
                }
                if (undoManager1.canRedo()) {
                    undoManager1.redo();
                }
            }
        }));
        p.add(new JButton(new AbstractAction("setText(new Date())") {
            @Override public void actionPerformed(ActionEvent e) {
                String str = new Date().toString();
                textField0.setText(str);
                textField1.setText(str);
            }
        }));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makePanel("Default", textField0));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("replace ignoring undo", textField1));

        add(box, BorderLayout.NORTH);
        add(p, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(320, 240));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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

class CustomUndoPlainDocument extends PlainDocument {
    private CompoundEdit compoundEdit;
    @Override protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        if (compoundEdit == null) {
            super.fireUndoableEditUpdate(e);
        } else {
            compoundEdit.addEdit(e.getEdit());
        }
    }
    @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (length == 0) {
            //System.out.println("insert");
            super.replace(offset, length, text, attrs);
        } else {
            //System.out.println("replace");
            compoundEdit = new CompoundEdit();
            super.fireUndoableEditUpdate(new UndoableEditEvent(this, compoundEdit));
            super.replace(offset, length, text, attrs);
            compoundEdit.end();
            compoundEdit = null;
        }
    }
}

// class CustomUndoPlainDocument extends PlainDocument {
// //     private final UndoManager undoManager;
// //     public CustomUndoPlainDocument(UndoManager undoManager) {
// //         this.undoManager = undoManager;
// //     }
//     @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//         if (length == 0) { //insert
//             super.replace(offset, length, text, attrs);
//         } else { //replace
// //             undoManager.undoableEditHappened(new UndoableEditEvent(this, new ReplaceUndoableEdit(offset, length, text)));
//             fireUndoableEditUpdate(new UndoableEditEvent(this, new ReplaceUndoableEdit(offset, length, text)));
//             replaceIgnoringUndo(offset, length, text, attrs);
//         }
//     }
//     private void replaceIgnoringUndo(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//         UndoableEditListener[] uels = getUndoableEditListeners();
//         for (UndoableEditListener l: uels) {
//             removeUndoableEditListener(l);
//         }
//         super.replace(offset, length, text, attrs);
//         for (UndoableEditListener l: uels) {
//             addUndoableEditListener(l);
//         }
// //         removeUndoableEditListener(undoManager);
// //         super.replace(offset, length, text, attrs);
// //         addUndoableEditListener(undoManager);
//     }
//     class ReplaceUndoableEdit extends AbstractUndoableEdit {
//         private final String oldValue;
//         private final String newValue;
//         private final int offset;
//         public ReplaceUndoableEdit(int offset, int length, String newValue) {
//             super();
//             String txt;
//             try {
//                 txt = getText(offset, length);
//             } catch (BadLocationException e) {
//                 txt = null;
//             }
//             this.oldValue = txt;
//             this.newValue = newValue;
//             this.offset = offset;
//         }
//         @Override public void undo() { //throws CannotUndoException {
//             try {
//                 replaceIgnoringUndo(offset, newValue.length(), oldValue, null);
//             } catch (BadLocationException e) {
//                 CannotUndoException ex = new CannotUndoException();
//                 ex.initCause(e);
//                 throw ex;
//             }
//         }
//         @Override public void redo() { //throws CannotRedoException {
//             try {
//                 replaceIgnoringUndo(offset, oldValue.length(), newValue, null);
//             } catch (BadLocationException e) {
//                 CannotUndoException ex = new CannotUndoException();
//                 ex.initCause(e);
//                 throw ex;
//             }
//         }
//         @Override public boolean canUndo() {
//             return true;
//         }
//         @Override public boolean canRedo() {
//             return true;
//         }
//     }
// }
