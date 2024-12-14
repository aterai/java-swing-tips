// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.math.BigInteger;
import java.util.Collections;
import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

public final class MainPanel extends JPanel {
  public static final int BIT_LENGTH = 50;
  public static final String ONE_PAD = String.join("", Collections.nCopies(BIT_LENGTH, "1"));
  public static final String ZERO_PAD = String.join("", Collections.nCopies(BIT_LENGTH, "0"));
  private BigInteger status = new BigInteger("111000111", 2);
  private final transient UndoableEditSupport undoSupport = new UndoableEditSupport();
  private final JLabel label = new JLabel(print(status));
  private final JPanel panel = new JPanel();

  private MainPanel() {
    super(new BorderLayout());
    UndoManager um = new UndoManager();
    undoSupport.addUndoableEditListener(um);

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    Action undoAction = new UndoAction(um);
    box.add(new JButton(undoAction));
    box.add(Box.createHorizontalStrut(2));
    Action redoAction = new RedoAction(um);
    box.add(new JButton(redoAction));
    box.add(Box.createHorizontalStrut(2));
    // TEST:
    // undoSupport.beginUpdate();
    // try {
    //   ...
    // } finally {
    //   undoSupport.endUpdate();
    // }
    Action selectAllAction = new AbstractAction("select all") {
      @Override public void actionPerformed(ActionEvent e) {
        BigInteger newValue = new BigInteger(ONE_PAD, 2);
        updateUndoSupport(newValue);
        updateCheckBoxes(newValue);
        // TEST:
        // undoSupport.beginUpdate();
        // try {
        //   ...
        // } finally {
        //   undoSupport.endUpdate();
        // }
      }
    };
    box.add(new JButton(selectAllAction));
    box.add(Box.createHorizontalStrut(2));
    Action clearAllAction = new AbstractAction("clear all") {
      @Override public void actionPerformed(ActionEvent e) {
        BigInteger newValue = BigInteger.ZERO;
        updateUndoSupport(newValue);
        updateCheckBoxes(newValue);
      }
    };
    box.add(new JButton(clearAllAction));
    box.add(Box.createHorizontalStrut(2));

    for (int i = 0; i < BIT_LENGTH; i++) {
      panel.add(makeCheckBox(i));
    }
    add(label, BorderLayout.NORTH);
    add(panel);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private JCheckBox makeCheckBox(int idx) {
    BigInteger l = BigInteger.ONE.shiftLeft(idx);
    boolean selected = !status.and(l).equals(BigInteger.ZERO);
    JCheckBox c = new JCheckBox(Integer.toString(idx + 1), selected);
    c.addActionListener(e -> {
      JCheckBox cb = (JCheckBox) e.getSource();
      BigInteger newValue = cb.isSelected() ? status.or(l) : status.xor(l);
      undoSupport.postEdit(new StatusEdit(status, newValue));
      status = newValue;
      label.setText(print(status));
    });
    return c;
  }

  public void updateUndoSupport(BigInteger value) {
    undoSupport.postEdit(new StatusEdit(status, value));
  }

  public void updateCheckBoxes(BigInteger value) {
    status = value;
    for (int i = 0; i < BIT_LENGTH; i++) {
      BigInteger l = BigInteger.ONE.shiftLeft(i);
      JCheckBox c = (JCheckBox) panel.getComponent(i);
      c.setSelected(!status.and(l).equals(BigInteger.ZERO));
    }
    label.setText(print(status));
  }

  /* default */ class StatusEdit extends AbstractUndoableEdit {
    private final BigInteger oldValue;
    private final BigInteger newValue;

    protected StatusEdit(BigInteger oldValue, BigInteger newValue) {
      super();
      this.oldValue = oldValue;
      this.newValue = newValue;
    }

    @Override public void undo() { // throws CannotUndoException {
      super.undo();
      updateCheckBoxes(oldValue);
    }

    @Override public void redo() { // throws CannotRedoException {
      super.redo();
      updateCheckBoxes(newValue);
    }
  }

  // // TEST: bits count
  // BigInteger M1 = new BigInteger("5555555555555555", 16); // binary: 0101...
  // BigInteger M2 = new BigInteger("3333333333333333", 16); // binary: 00110011..
  // BigInteger M4 = new BigInteger("0F0F0F0F0F0F0F0F", 16); // binary: 4 zeros, 4 ones ...
  // BigInteger M8 = new BigInteger("00FF00FF00FF00FF", 16); // binary: 8 zeros, 8 ones ...
  // BigInteger M16 = new BigInteger("0000FFFF0000FFFF", 16); // binary: 16 zeros, 16 ones ...
  // BigInteger M32 = new BigInteger("00000000FFFFFFFF", 16); // binary: 32 zeros, 32 ones
  //
  // private static int numOfBits(BigInteger bits) {
  //   bits = bits.and(M1).add(bits.shiftRight(1).and(M1));
  //   bits = bits.and(M2).add(bits.shiftRight(2).and(M2));
  //   bits = bits.and(M4).add(bits.shiftRight(4).and(M4));
  //   bits = bits.and(M8).add(bits.shiftRight(8).and(M8));
  //   bits = bits.and(M16).add(bits.shiftRight(16).and(M16));
  //   bits = bits.and(M32).add(bits.shiftRight(32).and(M32));
  //   return bits.intValue();
  // }

  private static String print(BigInteger l) {
    String b = l.toString(2);
    int count = l.bitCount();
    return "<html>0b" + ZERO_PAD.substring(b.length()) + b + "<br/> count: " + count;
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
