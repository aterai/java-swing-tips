package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.BitSet;
import java.util.Collections;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

public class MainPanel extends JPanel {
    // Long.MAX_VALUE
    // 0b111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111
    // protected static final int BIT_LENGTH = 63;
    protected static final int BIT_LENGTH = 72;
    protected BitSet status = BitSet.valueOf(new long[] {Long.valueOf("111000111", 2)});
    protected static final String ZEROPAD = String.join("", Collections.nCopies(BIT_LENGTH, "0"));
    protected final transient UndoableEditSupport undoSupport = new UndoableEditSupport();
    private final JLabel label = new JLabel(print(status));
    private final JPanel panel = new JPanel(new GridLayout(0, 8));
    private final UndoManager um = new UndoManager();
    private final Action undoAction = new UndoAction(um);
    private final Action redoAction = new RedoAction(um);
    private final Action selectAllAction = new AbstractAction("select all") {
        @Override public void actionPerformed(ActionEvent e) {
            BitSet newValue = new BitSet(BIT_LENGTH);
            newValue.set(0, BIT_LENGTH, true);
            undoSupport.postEdit(new StatusEdit(status, newValue));
            updateCheckBoxes(newValue);
        }
    };
    private final Action clearAllAction = new AbstractAction("clear all") {
        @Override public void actionPerformed(ActionEvent e) {
            BitSet newValue = new BitSet(BIT_LENGTH);
            undoSupport.postEdit(new StatusEdit(status, newValue));
            updateCheckBoxes(newValue);
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        undoSupport.addUndoableEditListener(um);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(undoAction));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(redoAction));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(selectAllAction));
        box.add(Box.createHorizontalStrut(2));
        box.add(new JButton(clearAllAction));
        box.add(Box.createHorizontalStrut(2));

        IntStream.range(0, BIT_LENGTH).forEach(i -> {
            JCheckBox c = new JCheckBox(Integer.toString(i), status.get(i));
            c.addActionListener(e -> {
                JCheckBox cb = (JCheckBox) e.getSource();
                BitSet newValue = status.get(0, BIT_LENGTH);
                newValue.set(i, cb.isSelected());
                undoSupport.postEdit(new StatusEdit(status, newValue));
                status = newValue;
                label.setText(print(status));
            });
            panel.add(c);
        });

        label.setFont(label.getFont().deriveFont(8f));

        add(label, BorderLayout.NORTH);
        add(new JScrollPane(panel));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    protected final void updateCheckBoxes(BitSet value) {
        status = value;
        for (int i = 0; i < BIT_LENGTH; i++) {
            ((JCheckBox) panel.getComponent(i)).setSelected(status.get(i));
        }
        label.setText(print(status));
    }
    private class StatusEdit extends AbstractUndoableEdit {
        private final BitSet oldValue;
        private final BitSet newValue;
        protected StatusEdit(BitSet oldValue, BitSet newValue) {
            super();
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
        @Override public void undo() throws CannotUndoException {
            super.undo();
            updateCheckBoxes(oldValue);
        }
        @Override public void redo() throws CannotRedoException {
            super.redo();
            updateCheckBoxes(newValue);
        }
    }
    private static String print(BitSet bitSet) {
        StringBuilder buf = new StringBuilder();
        for (long lv: bitSet.toLongArray()) {
            buf.insert(0, Long.toUnsignedString(lv, 2));
        }
        String b = buf.toString();
        int count = bitSet.cardinality();
        return "<html>0b" + ZEROPAD.substring(b.length()) + b + "<br/> count: " + count;
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

class UndoAction extends AbstractAction {
    private final UndoManager um;
    protected UndoAction(UndoManager um) {
        super("undo");
        this.um = um;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (um.canUndo()) {
            um.undo();
        }
    }
}

class RedoAction extends AbstractAction {
    private final UndoManager um;
    protected RedoAction(UndoManager um) {
        super("redo");
        this.um = um;
    }
    @Override public void actionPerformed(ActionEvent e) {
        if (um.canRedo()) {
            um.redo();
        }
    }
}
