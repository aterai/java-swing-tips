package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private static final String INFO = " Start editing: Double-Click, Enter-Key\n"
        + " Commit rename: field-focusLost, Enter-Key\n"
        + "Cancel editing: Esc-Key, title.isEmpty\n";
    private final JTabbedPane tabbedPane = new JTabbedPane();
    public MainPanel() {
        super(new BorderLayout());
        TabTitleEditListener l = new TabTitleEditListener(tabbedPane);
        tabbedPane.addChangeListener(l);
        tabbedPane.addMouseListener(l);
        tabbedPane.addTab("Shortcuts", new JTextArea(INFO));
        tabbedPane.addTab("badfasdfa", new JLabel("bbbbbbbbbbbafasdf"));
        tabbedPane.addTab("cccc", new JScrollPane(new JTree()));
        tabbedPane.addTab("dddddddd", new JLabel("dadfasdfasd"));
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setMinimumSize(new Dimension(256, 100));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class TabTitleEditListener extends MouseAdapter implements ChangeListener, DocumentListener {
    protected final JTextField editor = new JTextField();
    protected final JTabbedPane tabbedPane;
    protected int editingIdx = -1;
    protected int len = -1;
    protected Dimension dim;
    protected Component tabComponent;
    protected final Action startEditing = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            editingIdx = tabbedPane.getSelectedIndex();
            tabComponent = tabbedPane.getTabComponentAt(editingIdx);
            tabbedPane.setTabComponentAt(editingIdx, editor);
            editor.setVisible(true);
            editor.setText(tabbedPane.getTitleAt(editingIdx));
            editor.selectAll();
            editor.requestFocusInWindow();
            len = editor.getText().length();
            dim = editor.getPreferredSize();
            editor.setMinimumSize(dim);
        }
    };
    protected final Action renameTabTitle = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            String title = editor.getText().trim();
            if (editingIdx >= 0 && !title.isEmpty()) {
                tabbedPane.setTitleAt(editingIdx, title);
            }
            cancelEditing.actionPerformed(null);
        }
    };
    protected final Action cancelEditing = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            if (editingIdx >= 0) {
                tabbedPane.setTabComponentAt(editingIdx, tabComponent);
                editor.setVisible(false);
                editingIdx = -1;
                len = -1;
                tabComponent = null;
                editor.setPreferredSize(null);
                tabbedPane.requestFocusInWindow();
            }
        }
    };
    protected TabTitleEditListener(JTabbedPane tabbedPane) {
        super();
        this.tabbedPane = tabbedPane;
        editor.setBorder(BorderFactory.createEmptyBorder());
        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) {
                renameTabTitle.actionPerformed(null);
            }
        });
        InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = editor.getActionMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");
        am.put("cancel-editing", cancelEditing);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-tab-title");
        am.put("rename-tab-title", renameTabTitle);
        editor.getDocument().addDocumentListener(this);
        // editor.addKeyListener(new KeyAdapter() {
        //     @Override public void keyPressed(KeyEvent e) {
        //         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
        //             renameTabTitle();
        //         } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
        //             cancelEditing();
        //         } else {
        //             editor.setPreferredSize(editor.getText().length() > len ? null : dim);
        //             tabbedPane.revalidate();
        //         }
        //     }
        // });
        tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
        tabbedPane.getActionMap().put("start-editing", startEditing);
    }
    @Override public void stateChanged(ChangeEvent e) {
        renameTabTitle.actionPerformed(null);
    }
    @Override public void insertUpdate(DocumentEvent e) {
        updateTabSize();
    }
    @Override public void removeUpdate(DocumentEvent e) {
        updateTabSize();
    }
    @Override public void changedUpdate(DocumentEvent e) { /* not needed */ }
    @Override public void mouseClicked(MouseEvent e) {
        Rectangle r = tabbedPane.getBoundsAt(tabbedPane.getSelectedIndex());
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick && r.contains(e.getPoint())) {
            startEditing.actionPerformed(null);
        } else {
            renameTabTitle.actionPerformed(null);
        }
    }
    protected void updateTabSize() {
        editor.setPreferredSize(editor.getText().length() > len ? null : dim);
        tabbedPane.revalidate();
    }
}
