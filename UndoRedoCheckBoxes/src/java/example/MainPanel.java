package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.Collections;
import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

public class MainPanel extends JPanel {
    protected BigInteger status = new BigInteger("111000111", 2);
    protected static final int BIT_LENGTH = 50;
    protected static final String ONEPAD = String.join("", Collections.nCopies(BIT_LENGTH, "1"));
    protected static final String ZEROPAD = String.join("", Collections.nCopies(BIT_LENGTH, "0"));
    protected final transient UndoableEditSupport undoSupport = new UndoableEditSupport();
    private final JLabel label = new JLabel(print(status));
    private final JPanel panel = new JPanel();
    private final UndoManager um = new UndoManager();
    private final Action undoAction = new UndoAction(um);
    private final Action redoAction = new RedoAction(um);
    private final Action selectAllAction = new AbstractAction("select all") {
        @Override public void actionPerformed(ActionEvent e) {
            BigInteger newValue = new BigInteger(ONEPAD, 2);
            undoSupport.postEdit(new StatusEdit(status, newValue));
            updateCheckBoxes(newValue);
            // TEST:
            // undoSupport.beginUpdate();
            // try {
            //     ...
            // } finally {
            //     undoSupport.endUpdate();
            // }
        }
    };
    private final Action clearAllAction = new AbstractAction("clear all") {
        @Override public void actionPerformed(ActionEvent e) {
            BigInteger newValue = BigInteger.ZERO;
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

        for (int i = 0; i < BIT_LENGTH; i++) {
            BigInteger l = BigInteger.ONE.shiftLeft(i);
            JCheckBox c = new JCheckBox(Integer.toString(i + 1), !status.and(l).equals(BigInteger.ZERO));
            // c.setSelected(!status.and(l).equals(BigInteger.ZERO));
            c.addActionListener(e -> {
                JCheckBox cb = (JCheckBox) e.getSource();
                BigInteger newValue = cb.isSelected() ? status.or(l) : status.xor(l);
                undoSupport.postEdit(new StatusEdit(status, newValue));
                status = newValue;
                label.setText(print(status));
            });
            panel.add(c);
        }
        add(label, BorderLayout.NORTH);
        add(panel);
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    protected final void updateCheckBoxes(BigInteger value) {
        status = value;
        for (int i = 0; i < BIT_LENGTH; i++) {
            BigInteger l = BigInteger.ONE.shiftLeft(i);
            JCheckBox c = (JCheckBox) panel.getComponent(i);
            c.setSelected(!status.and(l).equals(BigInteger.ZERO));
        }
        label.setText(print(status));
    }
    private class StatusEdit extends AbstractUndoableEdit {
        private final BigInteger oldValue;
        private final BigInteger newValue;
        protected StatusEdit(BigInteger oldValue, BigInteger newValue) {
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
    // // TEST: bit count
    // private static final BigInteger M1 = new BigInteger("5555555555555555", 16); // binary: 0101...
    // private static final BigInteger M2 = new BigInteger("3333333333333333", 16); // binary: 00110011..
    // private static final BigInteger M4 = new BigInteger("0F0F0F0F0F0F0F0F", 16); // binary: 4 zeros, 4 ones ...
    // private static final BigInteger M8 = new BigInteger("00FF00FF00FF00FF", 16); // binary: 8 zeros, 8 ones ...
    // private static final BigInteger M16 = new BigInteger("0000FFFF0000FFFF", 16); // binary: 16 zeros, 16 ones ...
    // private static final BigInteger M32 = new BigInteger("00000000FFFFFFFF", 16); // binary: 32 zeros, 32 ones
    // private static int numofbits(BigInteger bits) {
    //     bits = bits.and(M1).add(bits.shiftRight(1).and(M1));
    //     bits = bits.and(M2).add(bits.shiftRight(2).and(M2));
    //     bits = bits.and(M4).add(bits.shiftRight(4).and(M4));
    //     bits = bits.and(M8).add(bits.shiftRight(8).and(M8));
    //     bits = bits.and(M16).add(bits.shiftRight(16).and(M16));
    //     bits = bits.and(M32).add(bits.shiftRight(32).and(M32));
    //     return bits.intValue();
    // }
    private static String print(BigInteger l) {
        String b = l.toString(2);
        int count = l.bitCount();
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
