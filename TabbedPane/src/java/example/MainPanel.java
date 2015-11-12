package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String MENUITEM_NEWTAB    = "New tab";
    private static final String MENUITEM_CLOSEPAGE = "Close";
    private static final String MENUITEM_CLOSEALL  = "Close all";
    private static final String MENUITEM_CLOSEALLBUTACTIVE = "Close all bat active";
    private static int count;

    private final JTabbedPane tab = new JTabbedPane();
    private final Action closePageAction = new ClosePageAction(MENUITEM_CLOSEPAGE);
    private final Action closeAllAction  = new CloseAllAction(MENUITEM_CLOSEALL);
    private final Action closeAllButActiveAction = new CloseAllButActiveAction(MENUITEM_CLOSEALLBUTACTIVE);
    private final JPopupMenu pop = new JPopupMenu() {
        @Override public void show(Component c, int x, int y) {
            //JDK 1.3 tabindex = tab.getUI().tabForCoordinate(tab, x, y);
            closePageAction.setEnabled(tab.indexAtLocation(x, y) >= 0);
            closeAllAction.setEnabled(tab.getTabCount() > 0);
            closeAllButActiveAction.setEnabled(tab.getTabCount() > 0);
            super.show(c, x, y);
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        pop.add(new NewTabAction(MENUITEM_NEWTAB));
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
