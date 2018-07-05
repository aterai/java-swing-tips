package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

public class MainPanel extends JPanel {
    protected final JTextField textField0 = new JTextField("default");
    protected final JTextField textField1 = new JTextField();
    protected final JTextField textField2 = new JTextField();
    protected final UndoManager undoManager0 = new UndoManager();
    protected final UndoManager undoManager1 = new UndoManager();
    protected final DocumentFilterUndoManager undoManager2 = new DocumentFilterUndoManager();
    protected final Action undoAction = new AbstractAction("undo") {
        @Override public void actionPerformed(ActionEvent e) {
            for (UndoManager um: Arrays.asList(undoManager0, undoManager1, undoManager2)) {
                if (um.canUndo()) {
                    um.undo();
                }
            }
        }
    };
    protected final Action redoAction = new AbstractAction("redo") {
        @Override public void actionPerformed(ActionEvent e) {
            for (UndoManager um: Arrays.asList(undoManager0, undoManager1, undoManager2)) {
                if (um.canRedo()) {
                    um.redo();
                }
            }
        }
    };

    public MainPanel() {
        super(new BorderLayout());

        textField0.getDocument().addUndoableEditListener(undoManager0);

        textField1.setDocument(new CustomUndoPlainDocument());
        textField1.setText("aaaaaaaaaaaaaaaaaaaaa");
        textField1.getDocument().addUndoableEditListener(undoManager1);

        textField2.setText("bbbbbbbbbbbbbbb");
        Document d = textField2.getDocument();
        if (d instanceof AbstractDocument) {
            AbstractDocument doc = (AbstractDocument) d;
            doc.addUndoableEditListener(undoManager2);
            doc.setDocumentFilter(undoManager2.getDocumentFilter());
        }

        JButton button = new JButton("setText(LocalDateTime.now())");
        button.addActionListener(e -> {
            String str = LocalDateTime.now().toString();
            for (JTextField tf: Arrays.asList(textField0, textField1, textField2)) {
                tf.setText(str);
            }
        });

        JPanel p = new JPanel();
        p.add(new JButton(undoAction));
        p.add(new JButton(redoAction));
        p.add(button);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makeTitledPanel("Default", textField0));
        box.add(Box.createVerticalStrut(10));
        box.add(makeTitledPanel("Document#replace()+AbstractDocument#fireUndoableEditUpdate()", textField1));
        box.add(Box.createVerticalStrut(10));
        box.add(makeTitledPanel("DocumentFilter#replace()+UndoableEditListener#undoableEditHappened()", textField2));

        add(box, BorderLayout.NORTH);
        add(p, BorderLayout.SOUTH);

        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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

class CustomUndoPlainDocument extends PlainDocument {
    private CompoundEdit compoundEdit;
    @Override protected void fireUndoableEditUpdate(UndoableEditEvent e) {
        // FindBugs: UWF_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR
        // if (Objects.nonNull(compoundEdit)) {
        //     compoundEdit.addEdit(e.getEdit());
        // } else {
        //     super.fireUndoableEditUpdate(e);
        // }
        Optional.ofNullable(compoundEdit).ifPresent(ce -> ce.addEdit(e.getEdit()));
        // JDK9: Optional.ofNullable(compoundEdit).ifPresentOrElse(ce -> ce.addEdit(e.getEdit()), () -> super.fireUndoableEditUpdate(e));
    }
    @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if (length == 0) {
            // System.out.println("insert");
            super.replace(offset, length, text, attrs);
        } else {
            // System.out.println("replace");
            compoundEdit = new CompoundEdit();
            super.replace(offset, length, text, attrs);
            compoundEdit.end();
            super.fireUndoableEditUpdate(new UndoableEditEvent(this, compoundEdit));
            compoundEdit = null;
        }
    }
}

class DocumentFilterUndoManager extends UndoManager {
    protected CompoundEdit compoundEdit;
    private final transient DocumentFilter undoFilter = new DocumentFilter() {
        @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
            if (length == 0) {
                fb.insertString(offset, text, attrs);
            } else {
                compoundEdit = new CompoundEdit();
                fb.replace(offset, length, text, attrs);
                compoundEdit.end();
                addEdit(compoundEdit);
                compoundEdit = null;
            }
        }
    };
    public DocumentFilter getDocumentFilter() {
        return undoFilter;
    }
    @Override public void undoableEditHappened(UndoableEditEvent e) {
        Optional.ofNullable(compoundEdit).orElse(this).addEdit(e.getEdit());
    }
}

// class CustomUndoPlainDocument extends PlainDocument {
// //     private final UndoManager undoManager;
// //     protected CustomUndoPlainDocument(UndoManager undoManager) {
// //         super();
// //         this.undoManager = undoManager;
// //     }
//     @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//         if (length == 0) { // insert
//             super.replace(offset, length, text, attrs);
//         } else { // replace
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
//         protected ReplaceUndoableEdit(int offset, int length, String newValue) {
//             super();
//             String txt;
//             try {
//                 txt = getText(offset, length);
//             } catch (BadLocationException ex) {
//                 txt = null;
//             }
//             this.oldValue = txt;
//             this.newValue = newValue;
//             this.offset = offset;
//         }
//         @Override public void undo() { // throws CannotUndoException {
//             try {
//                 replaceIgnoringUndo(offset, newValue.length(), oldValue, null);
//             } catch (BadLocationException ex) {
//                 throw (CannotUndoException) new CannotUndoException().initCause(ex);
//             }
//         }
//         @Override public void redo() { // throws CannotRedoException {
//             try {
//                 replaceIgnoringUndo(offset, oldValue.length(), newValue, null);
//             } catch (BadLocationException ex) {
//                 throw (CannotUndoException) new CannotUndoException().initCause(ex);
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
