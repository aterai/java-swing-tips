// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.BitSet;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.UndoManager;
import javax.swing.undo.UndoableEditSupport;

public final class MainPanel extends JPanel {
  // Long.MAX_VALUE
  // 0b111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111_1111
  // public static final int BIT_LENGTH = 63;
  public static final int BIT_LENGTH = 72;
  public static final String ZERO_PAD = String.join("", Collections.nCopies(BIT_LENGTH, "0"));
  public BitSet status = BitSet.valueOf(new long[] {Long.parseLong("111000111", 2)});
  public final transient UndoableEditSupport undoSupport = new UndoableEditSupport();
  private final JLabel label = new JLabel(print(status));
  private final JPanel panel = new JPanel(new GridLayout(0, 8));

  private MainPanel() {
    super(new BorderLayout());
    UndoManager um = new UndoManager();
    undoSupport.addUndoableEditListener(um);

    Action undoAct = new UndoAction(um);
    Action redoAct = new RedoAction(um);
    Action selectAllAct = new AbstractAction("select all") {
      @Override public void actionPerformed(ActionEvent e) {
        BitSet newValue = new BitSet(BIT_LENGTH);
        newValue.set(0, BIT_LENGTH, true);
        undoSupport.postEdit(new StatusEdit(status, newValue));
        updateCheckBoxes(newValue);
      }
    };
    Action clearAllAct = new AbstractAction("clear all") {
      @Override public void actionPerformed(ActionEvent e) {
        BitSet newValue = new BitSet(BIT_LENGTH);
        undoSupport.postEdit(new StatusEdit(status, newValue));
        updateCheckBoxes(newValue);
      }
    };

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    Stream.of(undoAct, redoAct, selectAllAct, clearAllAct)
        .map(JButton::new)
        .forEach(b -> {
          box.add(b);
          box.add(Box.createHorizontalStrut(2));
        });
    IntStream.range(0, BIT_LENGTH)
        .mapToObj(this::makeCheckBox)
        .forEach(panel::add);
    label.setFont(label.getFont().deriveFont(8f));

    add(label, BorderLayout.NORTH);
    add(new JScrollPane(panel));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private JCheckBox makeCheckBox(int i) {
    JCheckBox c = new JCheckBox(Integer.toString(i), status.get(i));
    c.addActionListener(e -> {
      BitSet newValue = status.get(0, BIT_LENGTH);
      newValue.set(i, ((JCheckBox) e.getSource()).isSelected());
      undoSupport.postEdit(new StatusEdit(status, newValue));
      status = newValue;
      label.setText(print(status));
    });
    return c;
  }

  public void updateCheckBoxes(BitSet value) {
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

    @Override public void undo() { // throws CannotUndoException {
      super.undo();
      updateCheckBoxes(oldValue);
    }

    @Override public void redo() { // throws CannotRedoException {
      super.redo();
      updateCheckBoxes(newValue);
    }
  }

  private static String print(BitSet bitSet) {
    StringBuilder buf = new StringBuilder();
    for (long lv : bitSet.toLongArray()) {
      buf.insert(0, Long.toUnsignedString(lv, 2));
    }
    String b = buf.toString();
    int count = bitSet.cardinality();
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
      Logger.getGlobal().severe(ex::getMessage);
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
