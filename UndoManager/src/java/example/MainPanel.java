package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.*;

public final class MainPanel extends JPanel {
    private final JTextField field1 = new JTextField("aaaaaaaaa");
    private final JTextField field2 = new JTextField("bbbbbbbbb");
    public MainPanel() {
        super(new GridLayout(2, 1));

        initUndoRedo(field1);
        initUndoRedo(field2);

        add(makeTitlePanel(field1, "undo:Ctrl-z, redo:Ctrl-y"));
        add(makeTitlePanel(field2, "test"));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 200));
    }
    private static void initUndoRedo(JTextComponent tc) {
        UndoManager manager = new UndoManager();
        tc.getDocument().addUndoableEditListener(manager);
        tc.getActionMap().put("undo", new UndoAction(manager));
        tc.getActionMap().put("redo", new RedoAction(manager));
        InputMap imap = tc.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Event.CTRL_MASK), "undo");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Event.CTRL_MASK), "redo");
    }
    private static class UndoAction extends AbstractAction {
        private final UndoManager undoManager;
        public UndoAction(UndoManager manager) {
            super("undo");
            this.undoManager = manager;
        }
        @Override public void actionPerformed(ActionEvent e) {
            try {
                undoManager.undo();
            } catch (CannotUndoException cue) {
                //cue.printStackTrace();
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }
    private static class RedoAction extends AbstractAction {
        private final UndoManager undoManager;
        public RedoAction(UndoManager manager) {
            super("redo");
            this.undoManager = manager;
        }
        @Override public void actionPerformed(ActionEvent e) {
            try {
                undoManager.redo();
            } catch (CannotRedoException cre) {
                //cre.printStackTrace();
                Toolkit.getDefaultToolkit().beep();
            }
        }
    }

    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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
