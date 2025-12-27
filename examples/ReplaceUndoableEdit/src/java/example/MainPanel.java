// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    UndoManager undoManager0 = new UndoManager();
    JTextField field0 = new JTextField("Default");
    field0.getDocument().addUndoableEditListener(undoManager0);

    UndoManager undoManager1 = new UndoManager();
    JTextField field1 = new JTextField();
    field1.setDocument(new CustomUndoPlainDocument());
    field1.setText("Document#replace() + AbstractDocument#fireUndoableEditUpdate()");
    field1.getDocument().addUndoableEditListener(undoManager1);

    DocumentFilterUndoManager undoManager2 = new DocumentFilterUndoManager();
    JTextField field2 = new JTextField();
    field2.setText("DocumentFilter#replace() + UndoableEditListener#undoableEditHappened()");
    Document d = field2.getDocument();
    if (d instanceof AbstractDocument) {
      AbstractDocument doc = (AbstractDocument) d;
      doc.addUndoableEditListener(undoManager2);
      doc.setDocumentFilter(undoManager2.getDocumentFilter());
    }

    JButton button = new JButton("setText(LocalDateTime.now(...))");
    button.addActionListener(e -> {
      String str = LocalDateTime.now(ZoneId.systemDefault()).toString();
      Stream.of(field0, field1, field2).forEach(tf -> tf.setText(str));
    });

    Action undoAction = new AbstractAction("undo") {
      @Override public void actionPerformed(ActionEvent e) {
        Stream.of(undoManager0, undoManager1, undoManager2)
            .filter(UndoManager::canUndo)
            .forEach(UndoManager::undo);
      }
    };
    Action redoAction = new AbstractAction("redo") {
      @Override public void actionPerformed(ActionEvent e) {
        Stream.of(undoManager0, undoManager1, undoManager2)
            .filter(UndoManager::canRedo)
            .forEach(UndoManager::redo);
      }
    };

    JPanel p = new JPanel();
    p.add(new JButton(undoAction));
    p.add(new JButton(redoAction));
    p.add(button);

    add(makeTextFieldBox(field0, field1, field2), BorderLayout.NORTH);
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeTextFieldBox(JTextField... list) {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    for (JTextField field : list) {
      box.add(makeTitledPanel(field.getText(), field));
      box.add(Box.createVerticalStrut(10));
    }
    return box;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class CustomUndoPlainDocument extends PlainDocument {
  private CompoundEdit compoundEdit;

  @Override protected void fireUndoableEditUpdate(UndoableEditEvent e) {
    if (Objects.nonNull(compoundEdit)) {
      compoundEdit.addEdit(e.getEdit());
    } else {
      super.fireUndoableEditUpdate(e);
    }
    // // JDK9:
    // Optional.ofNullable(compoundEdit)
    //     .ifPresentOrElse(
    //       ce -> ce.addEdit(e.getEdit()),
    //       () -> super.fireUndoableEditUpdate(e));
  }

  @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    if (length == 0) {
      super.replace(offset, length, text, attrs);
    } else {
      CompoundEdit edit = new CompoundEdit();
      setCompoundEdit(edit);
      super.replace(offset, length, text, attrs);
      edit.end();
      super.fireUndoableEditUpdate(new UndoableEditEvent(this, edit));
      setCompoundEdit(null);
    }
  }

  private void setCompoundEdit(CompoundEdit edit) {
    this.compoundEdit = edit;
  }
}

class DocumentFilterUndoManager extends UndoManager {
  private CompoundEdit compoundEdit;

  private final transient DocumentFilter undoFilter = new DocumentFilter() {
    @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
      if (length == 0) {
        fb.insertString(offset, text, attrs);
      } else {
        CompoundEdit edit = new CompoundEdit();
        setCompoundEdit(edit);
        fb.replace(offset, length, text, attrs);
        edit.end();
        addEdit(edit);
        setCompoundEdit(null);
      }
    }
  };

  public DocumentFilter getDocumentFilter() {
    return undoFilter;
  }

  @Override public void undoableEditHappened(UndoableEditEvent e) {
    Optional.ofNullable(compoundEdit).orElse(this).addEdit(e.getEdit());
  }

  private void setCompoundEdit(CompoundEdit edit) {
    this.compoundEdit = edit;
  }
}

// class CustomUndoPlainDocument extends PlainDocument {
//   // private final UndoManager undoManager;
//   // protected CustomUndoPlainDocument(UndoManager undoManager) {
//   //   super();
//   //   this.undoManager = undoManager;
//   // }
//
//   @Override public void replace(int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//     if (length == 0) { // insert
//       super.replace(offset, length, text, attrs);
//     } else { // replace
//       // undoManager.undoableEditHappened(
//       //     new UndoableEditEvent(this, new ReplaceUndoableEdit(offset, length, text)));
//       fireUndoableEditUpdate(
//           new UndoableEditEvent(this, new ReplaceUndoableEdit(offset, length, text)));
//       replaceIgnoringUndo(offset, length, text, attrs);
//     }
//   }
//
//   private void replaceIgnoringUndo(int offset, int length, String text, AttributeSet attrs)
//       throws BadLocationException {
//     UndoableEditListener[] ls = getUndoableEditListeners();
//     for (UndoableEditListener l : ls) {
//       removeUndoableEditListener(l);
//     }
//     super.replace(offset, length, text, attrs);
//     for (UndoableEditListener l : ls) {
//       addUndoableEditListener(l);
//     }
//     // removeUndoableEditListener(undoManager);
//     // super.replace(offset, length, text, attrs);
//     // addUndoableEditListener(undoManager);
//   }
//
//   class ReplaceUndoableEdit extends AbstractUndoableEdit {
//     private final String oldValue;
//     private final String newValue;
//     private final int offset;
//     protected ReplaceUndoableEdit(int offset, int length, String newValue) {
//       super();
//       String txt;
//       try {
//         txt = getText(offset, length);
//       } catch (BadLocationException ex) {
//         txt = null;
//       }
//       this.oldValue = txt;
//       this.newValue = newValue;
//       this.offset = offset;
//     }
//
//     @Override public void undo() { // throws CannotUndoException {
//       try {
//         replaceIgnoringUndo(offset, newValue.length(), oldValue, null);
//       } catch (BadLocationException ex) {
//         throw (CannotUndoException) new CannotUndoException().initCause(ex);
//       }
//     }
//
//     @Override public void redo() { // throws CannotRedoException {
//       try {
//         replaceIgnoringUndo(offset, oldValue.length(), newValue, null);
//       } catch (BadLocationException ex) {
//         throw (CannotUndoException) new CannotUndoException().initCause(ex);
//       }
//     }
//
//     @Override public boolean canUndo() {
//       return true;
//     }
//
//     @Override public boolean canRedo() {
//       return true;
//     }
//   }
// }
