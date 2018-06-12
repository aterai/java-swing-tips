package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String INFO = " Start editing: Double-Click, Enter-Key\n"
        + " Commit rename: field-focusLost, Enter-Key\n"
        + "Cancel editing: Esc-Key, title.isEmpty\n";
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new EditableTabbedPane();
        // for (int i = 0; i < 5; i++) {
        //     String title = "Tab " + i;
        //     tabbedPane.add(title, new JLabel(title));
        //     tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
        // }
        JTextArea a = new JTextArea(INFO);
        a.setEditable(false);
        tabbedPane.addTab("Shortcuts", new JScrollPane(a));
        tabbedPane.addTab("badfasdf", new JLabel("bbbbbbbbbbbafasdf"));
        tabbedPane.addTab("cccc", new JScrollPane(new JTree()));
        tabbedPane.addTab("ddddddd", new JButton("dadfasdfasd"));
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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

class EditableTabbedPane extends JTabbedPane {
    protected final Container glassPane = new EditorGlassPane();
    protected final JTextField editor = new JTextField();
    protected final Action startEditing = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            getRootPane().setGlassPane(glassPane);
            Rectangle rect = getBoundsAt(getSelectedIndex());
            Point p = SwingUtilities.convertPoint(EditableTabbedPane.this, rect.getLocation(), glassPane);
            // rect.setBounds(p.x + 2, p.y + 2, rect.width - 4, rect.height - 4);
            rect.setLocation(p);
            rect.grow(-2, -2);
            editor.setBounds(rect);
            editor.setText(getTitleAt(getSelectedIndex()));
            editor.selectAll();
            glassPane.add(editor);
            glassPane.setVisible(true);
            editor.requestFocusInWindow();
        }
    };
    protected final Action cancelEditing = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            glassPane.setVisible(false);
        }
    };
    protected final Action renameTab = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            if (!editor.getText().trim().isEmpty()) {
                setTitleAt(getSelectedIndex(), editor.getText());
                // Java 1.6.0 ---->
                Component c = getTabComponentAt(getSelectedIndex());
                if (c instanceof JComponent) {
                    ((JComponent) c).revalidate();
                }
                // <----
            }
            glassPane.setVisible(false);
        }
    };
    protected EditableTabbedPane() {
        super();
        editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-tab");
        editor.getActionMap().put("rename-tab", renameTab);
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");
        editor.getActionMap().put("cancel-editing", cancelEditing);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                boolean isDoubleClick = e.getClickCount() >= 2;
                if (isDoubleClick) {
                    startEditing.actionPerformed(null);
                }
            }
        });
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
        getActionMap().put("start-editing", startEditing);
    }
    protected JTextField getEditorTextField() {
        return editor;
    }
    private class EditorGlassPane extends JComponent {
        protected EditorGlassPane() {
            super();
            setOpaque(false);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override public boolean accept(Component c) {
                    return Objects.equals(c, getEditorTextField());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    // if (Objects.nonNull(rect) && !rect.contains(e.getPoint())) {
                    if (!getEditorTextField().getBounds().contains(e.getPoint())) {
                        renameTab.actionPerformed(null);
                    }
                }
            });
        }
        @Override public void setVisible(boolean flag) {
            super.setVisible(flag);
            setFocusTraversalPolicyProvider(flag);
            setFocusCycleRoot(flag);
        }
    }
}
