package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String MENUITEM_NEWTAB    = "New tab";
    private static final String MENUITEM_CLOSEPAGE = "Close";
    private static final String MENUITEM_CLOSEALL  = "Close all";
    private static final String MENUITEM_CLOSEALLBUTACTIVE = "Close all bat active";
    private static final String MENUITEM_SORT = "Sort";
    private static int count;

    private final JTabbedPane tab;
    private final Action closePageAction = new ClosePageAction(MENUITEM_CLOSEPAGE);
    private final Action closeAllAction  = new CloseAllAction(MENUITEM_CLOSEALL);
    private final Action closeAllButActiveAction = new CloseAllButActiveAction(MENUITEM_CLOSEALLBUTACTIVE);
    private final Action sortAction = new SortAction(MENUITEM_SORT);
    private final JPopupMenu pop = new JPopupMenu() {
        @Override public void show(Component c, int x, int y) {
            sortAction.setEnabled(tab.getTabCount() > 1);
            closePageAction.setEnabled(tab.indexAtLocation(x, y) >= 0);
            closeAllAction.setEnabled(tab.getTabCount() > 0);
            closeAllButActiveAction.setEnabled(tab.getTabCount() > 0);
            super.show(c, x, y);
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        tab = new EditableTabbedPane();
        pop.add(new NewTabAction(MENUITEM_NEWTAB));
        pop.add(sortAction);
        pop.addSeparator();
        pop.add(closePageAction);
        pop.addSeparator();
        pop.add(closeAllAction);
        pop.add(closeAllButActiveAction);
        tab.setComponentPopupMenu(pop);
        tab.addTab("Title", new JLabel("Tab"));
        add(tab);
        setPreferredSize(new Dimension(320, 240));
    }
    class NewTabAction extends AbstractAction {
        protected NewTabAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            tab.addTab("Title: " + count, new JLabel("Tab: " + count));
            tab.setSelectedIndex(tab.getTabCount() - 1);
            count++;
        }
    }
    class ClosePageAction extends AbstractAction {
        protected ClosePageAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            tab.remove(tab.getSelectedIndex());
        }
    }
    class CloseAllAction extends AbstractAction {
        protected CloseAllAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            tab.removeAll();
        }
    }
    class CloseAllButActiveAction extends AbstractAction {
        protected CloseAllButActiveAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            int tabidx = tab.getSelectedIndex();
            String title = tab.getTitleAt(tabidx);
            Component cmp = tab.getComponentAt(tabidx);
            tab.removeAll();
            tab.addTab(title, cmp);
        }
    }
    class SortAction extends AbstractAction {
        protected SortAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            setSortedTab(tab, makeSortedList(tab));
        }
        private List<ComparableTab> makeSortedList(JTabbedPane t) {
            List<ComparableTab> l = new ArrayList<>();
            for (int i = 0; i < t.getTabCount(); i++) {
                l.add(new ComparableTab(t.getTitleAt(i), t.getComponentAt(i)));
            }
            Collections.sort(l);
            //Collections.<ComparableTab>sort(l);
            return l;
        }
        private void setSortedTab(JTabbedPane tabbedPane, List<ComparableTab> list) {
            tabbedPane.setVisible(false);
            tabbedPane.removeAll();
            for (ComparableTab c: list) {
                tabbedPane.addTab(c.title, c.comp);
            }
            tabbedPane.setVisible(true);
        }
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

class ComparableTab implements Comparable<ComparableTab> {
    public final String title;
    public final Component comp;
    protected ComparableTab(String title, Component comp) {
        this.title = title;
        this.comp  = comp;
    }
    @Override public int compareTo(ComparableTab o) {
        return title.compareTo(o.title);
    }
    @Override public boolean equals(Object o) {
        if (o instanceof ComparableTab) {
            return compareTo((ComparableTab) o) == 0;
        } else {
            return false;
        }
    }
    @Override public int hashCode() {
        return title.hashCode() + comp.hashCode();
    }
}

class EditableTabbedPane extends JTabbedPane {
    private final JComponent glassPane = new EditorGlassPane();
    private final JTextField editor = new JTextField();
    private Rectangle rect;

    protected EditableTabbedPane() {
        super();
        editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
        editor.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                ((JTextField) e.getComponent()).selectAll();
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    renameTab();
                } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cancelEditing();
                }
            }
        });
        addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    startEditing();
                }
            }
        });
        addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    startEditing();
                }
            }
        });
        glassPane.add(editor);
        glassPane.setVisible(false);
    }
    private void initEditor() {
        rect = getUI().getTabBounds(this, getSelectedIndex());
        Point p = SwingUtilities.convertPoint(this, rect.getLocation(), glassPane);
        rect.setRect(p.x + 2, p.y + 2, rect.width - 4, rect.height - 4);
        editor.setBounds(rect);
        editor.setText(getTitleAt(getSelectedIndex()));
    }
    private void startEditing() {
        getRootPane().setGlassPane(glassPane);
        initEditor();
        glassPane.setVisible(true);
        editor.requestFocusInWindow();
    }
    private void cancelEditing() {
        glassPane.setVisible(false);
    }
    private void renameTab() {
        if (!editor.getText().trim().isEmpty()) {
            setTitleAt(getSelectedIndex(), editor.getText());
            //java 1.6.0 ---->
            Component c = getTabComponentAt(getSelectedIndex());
            if (c instanceof JComponent) {
                ((JComponent) c).revalidate();
            }
            //<----
        }
        glassPane.setVisible(false);
    }
    private class EditorGlassPane extends JComponent {
        protected EditorGlassPane() {
            super();
            setOpaque(false);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override public boolean accept(Component c) {
                    return Objects.equals(c, editor);
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override public void mouseClicked(MouseEvent me) {
                    if (Objects.isNull(rect) || rect.contains(me.getPoint())) {
                        return;
                    }
                    renameTab();
                }
            });
            requestFocusInWindow();
        }
        @Override public void setVisible(boolean flag) {
            super.setVisible(flag);
            setFocusTraversalPolicyProvider(flag);
            setFocusCycleRoot(flag);
        }
    }
}
