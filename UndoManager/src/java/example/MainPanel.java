// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTextField field1 = new JTextField("aaaaaaaaa");
    initUndoRedo(field1);

    JTextField field2 = new JTextField("bbbbbbbbb");
    initUndoRedo(field2);

    add(makeTitledPanel("undo:Ctrl-z, redo:Ctrl-y", field1));
    add(makeTitledPanel("test", field2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initUndoRedo(JTextComponent tc) {
    UndoManager manager = new UndoManager();
    tc.getDocument().addUndoableEditListener(manager);
    tc.getActionMap().put("undo", new UndoAction(manager));
    tc.getActionMap().put("redo", new RedoAction(manager));

    int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
    // Java 10: int modifiers = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
    InputMap imap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, modifiers), "undo");
    imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, modifiers), "redo");
  }

  private static class UndoAction extends AbstractAction {
    private final UndoManager undoManager;

    protected UndoAction(UndoManager manager) {
      super("undo");
      this.undoManager = manager;
    }

    @Override public void actionPerformed(ActionEvent e) {
      try {
        undoManager.undo();
      } catch (CannotUndoException ex) {
        // ex.printStackTrace();
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  private static class RedoAction extends AbstractAction {
    private final UndoManager undoManager;

    protected RedoAction(UndoManager manager) {
      super("redo");
      this.undoManager = manager;
    }

    @Override public void actionPerformed(ActionEvent e) {
      try {
        undoManager.redo();
      } catch (CannotRedoException ex) {
        Toolkit.getDefaultToolkit().beep();
      }
    }
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
