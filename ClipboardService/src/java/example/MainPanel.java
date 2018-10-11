package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.util.Objects;
import javax.jnlp.ClipboardService;
import javax.jnlp.ServiceManager;
import javax.jnlp.UnavailableServiceException;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

public class MainPanel extends JPanel {
    protected ClipboardService cs;

    public MainPanel() {
        super(new GridLayout(2, 1));
        try {
            cs = (ClipboardService) ServiceManager.lookup("javax.jnlp.ClipboardService");
        } catch (UnavailableServiceException ex) {
            cs = null;
        }
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
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
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

class TextComponentPopupMenu extends JPopupMenu {
    private final UndoManager manager = new UndoManager();
    private final Action cutAction = new DefaultEditorKit.CutAction();
    private final Action copyAction = new DefaultEditorKit.CopyAction();
    private final Action pasteAction = new DefaultEditorKit.PasteAction();
    private final Action undoAction = new UndoAction(manager);
    private final Action redoAction = new RedoAction(manager);
    private final Action deleteAction = new AbstractAction("delete") {
        @Override public void actionPerformed(ActionEvent e) {
            JTextComponent tc = (JTextComponent) getInvoker();
            tc.replaceSelection(null);
        }
    };
    protected TextComponentPopupMenu(JTextComponent textComponent) {
        super();
        add(cutAction);
        add(copyAction);
        add(pasteAction);
        add(deleteAction);
        addSeparator();
        add(undoAction);
        add(redoAction);
        textComponent.getDocument().addUndoableEditListener(manager);
        textComponent.getActionMap().put("undo", undoAction);
        textComponent.getActionMap().put("redo", redoAction);
        InputMap imap = textComponent.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "undo");
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), "redo");
        // Java 10:
        // imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Z, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "undo");
        // imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_Y, Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx()), "redo");
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
            // ex.printStackTrace();
            Toolkit.getDefaultToolkit().beep();
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
            // ex.printStackTrace();
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
