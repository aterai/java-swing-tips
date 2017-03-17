package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTabbedPane tabbedPane = new EditableTabbedPane();
        tabbedPane.addTab("Title", new JLabel("Tab1"));
        tabbedPane.addTab("aaa", new JLabel("Tab2"));
        tabbedPane.addTab("000", new JLabel("Tab3"));
        tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());

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
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ComparableTab { //implements Comparable<ComparableTab> {
    private final String title;
    private final Component comp;
    protected ComparableTab(String title, Component comp) {
        this.title = title;
        this.comp  = comp;
    }
    public String getTitle() {
        return title;
    }
    public Component getComponent() {
        return comp;
    }
//     @Override public int compareTo(ComparableTab o) {
//         return title.compareToIgnoreCase(o.title);
//     }
//     // http://jqno.nl/equalsverifier/errormessages/subclass-equals-is-not-final/
//     @Override public final boolean equals(Object o) {
//         if (o == this) {
//             return true;
//         }
//         if (o instanceof ComparableTab) {
//             ComparableTab other = (ComparableTab) o;
//             return Objects.equals(title, other.title) && Objects.equals(comp, other.comp);
//         }
//         return false;
//     }
//     @Override public final int hashCode() {
//         return Objects.hash(title, comp);
//     }
}

class EditableTabbedPane extends JTabbedPane {
    private final JComponent glassPane = new EditorGlassPane();
    private final JTextField editor = new JTextField();
    private final Action startEditing = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            getRootPane().setGlassPane(glassPane);
            Rectangle rect = getBoundsAt(getSelectedIndex());
            Point p = SwingUtilities.convertPoint(EditableTabbedPane.this, rect.getLocation(), glassPane);
            //rect.setBounds(p.x + 2, p.y + 2, rect.width - 4, rect.height - 4);
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
    private final Action cancelEditing = new AbstractAction() {
        @Override public void actionPerformed(ActionEvent e) {
            glassPane.setVisible(false);
        }
    };
    protected EditableTabbedPane() {
        super();
        editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-tab");
        editor.getActionMap().put("rename-tab", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                if (!editor.getText().trim().isEmpty()) {
                    setTitleAt(getSelectedIndex(), editor.getText());
                    Component c = getTabComponentAt(getSelectedIndex());
                    if (c instanceof JComponent) {
                        ((JComponent) c).revalidate();
                    }
                }
                glassPane.setVisible(false);
            }
        });
        editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");
        editor.getActionMap().put("cancel-editing", cancelEditing);

        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    startEditing.actionPerformed(null);
                }
            }
        });
        getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
        getActionMap().put("start-editing", startEditing);
    }
    protected JTextField getEditor() {
        return editor;
    }
    private class EditorGlassPane extends JComponent {
        protected EditorGlassPane() {
            super();
            setOpaque(false);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override public boolean accept(Component c) {
                    return Objects.equals(c, getEditor());
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent e) {
                    JTextField editor = getEditor();
                    Optional.ofNullable(editor.getActionMap().get("rename-tab"))
                      .filter(a -> !editor.getBounds().contains(e.getPoint()))
                      .ifPresent(a -> a.actionPerformed(null));
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

class TabbedPanePopupMenu extends JPopupMenu {
    private static final String MENUITEM_NEWTAB = "New tab";
    private static final String MENUITEM_CLOSEPAGE = "Close";
    private static final String MENUITEM_CLOSEALL = "Close all";
    private static final String MENUITEM_CLOSEALLBUTACTIVE = "Close all bat active";
    private static final String MENUITEM_SORT = "Sort";
    private static int count;
    private final Action closePageAction = new AbstractAction(MENUITEM_CLOSEPAGE) {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            tabbedPane.remove(tabbedPane.getSelectedIndex());
        }
    };
    private final Action closeAllAction = new AbstractAction(MENUITEM_CLOSEALL) {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            tabbedPane.removeAll();
        }
    };
    private final Action closeAllButActiveAction = new AbstractAction(MENUITEM_CLOSEALLBUTACTIVE) {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            int tabidx = tabbedPane.getSelectedIndex();
            String title = tabbedPane.getTitleAt(tabidx);
            Component cmp = tabbedPane.getComponentAt(tabidx);
            tabbedPane.removeAll();
            tabbedPane.addTab(title, cmp);
        }
    };
    private final Action sortAction = new AbstractAction(MENUITEM_SORT) {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            List<ComparableTab> list = IntStream.range(0, tabbedPane.getTabCount())
                .mapToObj(i -> new ComparableTab(tabbedPane.getTitleAt(i), tabbedPane.getComponentAt(i)))
                .sorted(Comparator.comparing(ComparableTab::getTitle)).collect(Collectors.toList());
            tabbedPane.removeAll();
            list.forEach(c -> tabbedPane.addTab(c.getTitle(), c.getComponent()));
        }
    };
    private final Action newTabAction = new AbstractAction(MENUITEM_NEWTAB) {
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            tabbedPane.addTab("Title: " + count, new JLabel("Tab: " + count));
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            count++;
        }
    };
    protected TabbedPanePopupMenu() {
        super();
        add(newTabAction);
        add(sortAction);
        addSeparator();
        add(closePageAction);
        addSeparator();
        add(closeAllAction);
        add(closeAllButActiveAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) c;
            sortAction.setEnabled(tabbedPane.getTabCount() > 1);
            closePageAction.setEnabled(tabbedPane.indexAtLocation(x, y) >= 0);
            closeAllAction.setEnabled(tabbedPane.getTabCount() > 0);
            closeAllButActiveAction.setEnabled(tabbedPane.getTabCount() > 0);
            super.show(c, x, y);
        }
    }
}
