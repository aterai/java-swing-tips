package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());
        tabbedPane.addTab("Title", new JLabel("Tab"));
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

class TabbedPanePopupMenu extends JPopupMenu {
    private static final String NEWTAB    = "New tab";
    private static final String CLOSEPAGE = "Close";
    private static final String CLOSEALL  = "Close all";
    private static final String CLOSEALLBUTACTIVE = "Close all bat active";
    protected transient int count;
    private final Action closePageAction = new ClosePageAction(CLOSEPAGE);
    private final Action closeAllAction  = new CloseAllAction(CLOSEALL);
    private final Action closeAllButActiveAction = new CloseAllButActiveAction(CLOSEALLBUTACTIVE);

    protected TabbedPanePopupMenu() {
        super();
        add(new NewTabAction(NEWTAB));
        addSeparator();
        add(closePageAction);
        addSeparator();
        add(closeAllAction);
        add(closeAllButActiveAction);
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) c;
            //JDK 1.3 tabindex = tabbedPane.getUI().tabForCoordinate(tabbedPane, x, y);
            closePageAction.setEnabled(tabbedPane.indexAtLocation(x, y) >= 0);
            closeAllAction.setEnabled(tabbedPane.getTabCount() > 0);
            closeAllButActiveAction.setEnabled(tabbedPane.getTabCount() > 0);
            super.show(c, x, y);
        }
    }
    class NewTabAction extends AbstractAction {
        protected NewTabAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            tabbedPane.addTab("Title: " + count, new JLabel("Tab: " + count));
            tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
            count++;

            System.out.println(getComponent().getClass().getName());
        }
    }
    class ClosePageAction extends AbstractAction {
        protected ClosePageAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            tabbedPane.remove(tabbedPane.getSelectedIndex());
        }
    }
    class CloseAllAction extends AbstractAction {
        protected CloseAllAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            tabbedPane.removeAll();
        }
    }
    class CloseAllButActiveAction extends AbstractAction {
        protected CloseAllButActiveAction(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
            int tabidx = tabbedPane.getSelectedIndex();
            String title = tabbedPane.getTitleAt(tabidx);
            Component cmp = tabbedPane.getComponentAt(tabidx);
            tabbedPane.removeAll();
            tabbedPane.addTab(title, cmp);
        }
    }
}
