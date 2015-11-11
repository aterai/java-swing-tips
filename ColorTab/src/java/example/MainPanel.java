package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private static final String MENUITEM_NEWTAB    = "New tab";
    private static final String MENUITEM_CLOSEPAGE = "Close";
    private static final String MENUITEM_CLOSEALL  = "Close all";
    private static final String MENUITEM_CLOSEALLBUTACTIVE = "Close all bat active";
    private int count;

    private final JTabbedPane tab = new JTabbedPane();
    private final Action closePageAction = new AbstractAction(MENUITEM_CLOSEPAGE) {
        @Override public void actionPerformed(ActionEvent e) {
            tab.remove(tab.getSelectedIndex());
        }
    };
    private final Action closeAllAction  = new AbstractAction(MENUITEM_CLOSEALL) {
        @Override public void actionPerformed(ActionEvent e) {
            tab.removeAll();
        }
    };
    private final Action closeAllButActiveAction = new AbstractAction(MENUITEM_CLOSEALLBUTACTIVE) {
        @Override public void actionPerformed(ActionEvent e) {
            int tabidx = tab.getSelectedIndex();
            String title = tab.getTitleAt(tabidx);
            Component cmp = tab.getComponentAt(tabidx);
            tab.removeAll();
            tab.addTab(title, cmp);
        }
    };
    private final JPopupMenu pop = new JPopupMenu() {
        @Override public void show(Component c, int x, int y) {
            closePageAction.setEnabled(tab.indexAtLocation(x, y) >= 0);
            closeAllAction.setEnabled(tab.getTabCount() > 0);
            closeAllButActiveAction.setEnabled(tab.getTabCount() > 0);
            super.show(c, x, y);
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        pop.add(new AbstractAction(MENUITEM_NEWTAB) {
            @Override public void actionPerformed(ActionEvent e) {
                tab.addTab("Title: " + count, count % 2 == 0 ? new JLabel("Tab: " + count) : new JScrollPane(new JTree()));
                tab.setSelectedIndex(tab.getTabCount() - 1);
                count++;
            }
        });
        pop.addSeparator();
        pop.add(closePageAction);
        pop.addSeparator();
        pop.add(closeAllAction);
        pop.add(closeAllButActiveAction);
        tab.setComponentPopupMenu(pop);
        tab.addChangeListener(new TabChangeListener());
        tab.addTab("Title", new JLabel("Tab"));
        add(tab);
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

class TabChangeListener implements ChangeListener {
    @Override public void stateChanged(ChangeEvent e) {
        JTabbedPane tabbedPane = (JTabbedPane) e.getSource();
        if (tabbedPane.getTabCount() <= 0) {
            return;
        }
        int sindex = tabbedPane.getSelectedIndex();
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (i == sindex && tabbedPane.getTitleAt(sindex).endsWith("1")) {
                tabbedPane.setForegroundAt(i, Color.GREEN);
            } else if (i == sindex) {
                Color sc = sindex % 2 == 0 ? Color.RED : Color.BLUE;
                tabbedPane.setForegroundAt(i, sc);
            } else {
                tabbedPane.setForegroundAt(i, Color.BLACK);
            }
        }
    }
}
