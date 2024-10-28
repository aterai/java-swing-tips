// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;
import java.util.Optional;
import javax.jnlp.ClipboardService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    ClipboardService cs = getClipboardService();
    JTextArea textArea = new JTextArea() {
      @Override public void copy() {
        if (Objects.nonNull(cs)) {
          cs.setContents(new StringSelection(getSelectedText()));
        } else {
          super.copy();
        }
      }

      @Override public void cut() {
        if (Objects.nonNull(cs)) {
          cs.setContents(new StringSelection(getSelectedText()));
        } else {
          super.cut();
        }
      }

      @Override public void paste() {
        if (Objects.nonNull(cs)) {
          Transferable tr = cs.getContents();
          if (tr.isDataFlavorSupported(DataFlavor.stringFlavor)) {
            getTransferHandler().importData(this, tr);
          }
        } else {
          super.paste();
        }
      }
    };
    textArea.setComponentPopupMenu(new TextComponentPopupMenu(textArea));

    add(makeTitledPanel("ClipboardService", new JScrollPane(textArea)));
    add(makeTitledPanel("Default", new JScrollPane(new JTextArea())));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ClipboardService getClipboardService() {
    Optional<Object> op;
    try {
      op = Optional.ofNullable(ServiceManager.lookup("javax.jnlp.ClipboardService"));
    } catch (UnavailableServiceException ex) {
      op = Optional.empty();
    }
    return op.filter(ClipboardService.class::isInstance)
        .map(ClipboardService.class::cast)
        .orElse(null);
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

final class TextComponentPopupMenu extends JPopupMenu {
  private final Action cutAction = new DefaultEditorKit.CutAction();
  private final Action copyAction = new DefaultEditorKit.CopyAction();
  private final Action deleteAction = new AbstractAction("delete") {
    @Override public void actionPerformed(ActionEvent e) {
      JTextComponent tc = (JTextComponent) getInvoker();
      tc.replaceSelection(null);
    }
  };

  /* default */ TextComponentPopupMenu(JTextComponent textComponent) {
    super();
    add(cutAction);
    add(copyAction);
    add(new DefaultEditorKit.PasteAction());
    add(deleteAction);
    addSeparator();
    UndoManager manager = new UndoManager();
    Action undoAction = new UndoAction(manager);
    add(undoAction);
    Action redoAction = new RedoAction(manager);
    add(redoAction);
    textComponent.getDocument().addUndoableEditListener(manager);
    textComponent.getActionMap().put("undo", undoAction);
    textComponent.getActionMap().put("redo", redoAction);
    InputMap im = textComponent.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    int msk = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int msk = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, msk), "undo");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, msk), "redo");
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      cutAction.setEnabled(hasSelectedText);
      copyAction.setEnabled(hasSelectedText);
      deleteAction.setEnabled(hasSelectedText);
      super.show(c, x, y);
    }
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
    } catch (CannotUndoException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
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
    } catch (CannotRedoException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
    }
  }
}
