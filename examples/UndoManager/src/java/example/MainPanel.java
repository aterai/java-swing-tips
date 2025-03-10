// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JTextField field1 = new JTextField("111111111");
    initUndoRedo(field1);

    JTextField field2 = new JTextField("222222222");
    initUndoRedo(field2);

    add(makeTitledPanel("undo:Ctrl-z, redo:Ctrl-y", field1));
    add(makeTitledPanel("test", field2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initUndoRedo(JTextComponent tc) {
    String undo = "undo";
    String redo = "redo";

    UndoManager manager = new UndoManager();
    tc.getDocument().addUndoableEditListener(manager);
    tc.getActionMap().put(undo, new UndoAction(manager));
    tc.getActionMap().put(redo, new RedoAction(manager));

    InputMap im = tc.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    im.put(KeyStroke.getKeyStroke("ctrl Z"), undo);
    im.put(KeyStroke.getKeyStroke("ctrl shift Z"), redo);
    im.put(KeyStroke.getKeyStroke("ctrl Y"), redo);
    // int m = tc.getToolkit().getMenuShortcutKeyMask();
    // Java 10: int m = tc.getToolkit().getMenuShortcutKeyMaskEx();
    // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, m), undo);
    // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, m | InputEvent.SHIFT_DOWN_MASK), redo);
    // im.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, m), redo);
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
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
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
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
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
