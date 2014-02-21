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

public class MainPanel extends JPanel {
    private final JTextField tf = new JTextField(24);
    private final JTextField field = new JTextField(24);
    private final UndoManager um = new UndoManager();
    private final UndoManager undoManager = new UndoManager();
    public MainPanel() {
        super(new BorderLayout());

        tf.getDocument().addUndoableEditListener(um);

        Document doc = new CustomUndoPlainDocument();
        doc.addUndoableEditListener(undoManager);
        field.setDocument(doc);

        field.setText("aaaaaaaaaaa");
        tf.setText("default");

        JPanel p = new JPanel();
        p.add(new JButton(new AbstractAction("undo") {
            @Override public void actionPerformed(ActionEvent e) {
                if(undoManager.canUndo()) {
                    undoManager.undo();
                }
                if(um.canUndo()) {
                    um.undo();
                }
            }
        }));
        p.add(new JButton(new AbstractAction("redo") {
            @Override public void actionPerformed(ActionEvent e) {
                if(undoManager.canRedo()) {
                    undoManager.redo();
                }
                if(um.canRedo()) {
                    um.redo();
                }
            }
        }));
        p.add(new JButton(new AbstractAction("setText(new Date())") {
            @Override public void actionPerformed(ActionEvent e) {
                String str = new Date().toString();
                tf.setText(str);
                field.setText(str);
            }
        }));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        box.add(makePanel("Default", tf));
        box.add(Box.createVerticalStrut(5));
        box.add(makePanel("replace ignoring undo", field));

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

class CustomUndoPlainDocument extends PlainDocument {
//     private final UndoManager undoManager;
//     public CustomUndoPlainDocument(UndoManager undoManager) {
//         this.undoManager = undoManager;
//     }
    @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        if(length==0) { //insert
            super.replace(offset, length, text, attrs);
        }else{ //replace
//             undoManager.undoableEditHappened(new UndoableEditEvent(this, new ReplaceUndoableEdit(offset, length, text)));
            fireUndoableEditUpdate(new UndoableEditEvent(this, new ReplaceUndoableEdit(offset, length, text)));
            replaceIgnoringUndo(offset, length, text, attrs);
        }
    }
    private void replaceIgnoringUndo(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        UndoableEditListener[] uels = getUndoableEditListeners();
        for(UndoableEditListener l: uels) {
            removeUndoableEditListener(l);
        }
        super.replace(offset, length, text, attrs);
        for(UndoableEditListener l: uels) {
            addUndoableEditListener(l);
        }
//         removeUndoableEditListener(undoManager);
//         super.replace(offset, length, text, attrs);
//         addUndoableEditListener(undoManager);
    }
    class ReplaceUndoableEdit extends AbstractUndoableEdit {
        private final String oldValue;
        private final String newValue;
        private final int offset;
        public ReplaceUndoableEdit(int offset, int length, String newValue) {
            super();
            String txt;
            try{
                txt = getText(offset, length);
            }catch(BadLocationException e) {
                txt = null;
            }
            this.oldValue = txt;
            this.newValue = newValue;
            this.offset = offset;
        }
        @Override public void undo() { //throws CannotUndoException {
            try{
                replaceIgnoringUndo(offset, newValue.length(), oldValue, null);
            }catch(BadLocationException e) {
                CannotUndoException ex = new CannotUndoException();
                ex.initCause(e);
                throw ex;
            }
        }
        @Override public void redo() { //throws CannotRedoException {
            try{
                replaceIgnoringUndo(offset, oldValue.length(), newValue, null);
            }catch(final BadLocationException e) {
                CannotUndoException ex = new CannotUndoException();
                ex.initCause(e);
                throw ex;
            }
        }
        @Override public boolean canUndo() {
            return true;
        }
        @Override public boolean canRedo() {
            return true;
        }
    }
}
