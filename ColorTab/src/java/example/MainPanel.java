package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private static final String MENUITEM_NEWTAB    = "New tab";
    private static final String MENUITEM_CLOSEPAGE = "Close";
    private static final String MENUITEM_CLOSEALL  = "Close all";
    private static final String MENUITEM_CLOSEALLBUTACTIVE = "Close all bat active";
    private static int count;

    private final JTabbedPane tab = new JTabbedPane();
    private final AbstractAction closePageAction = new ClosePageAction(MENUITEM_CLOSEPAGE, null);
    private final AbstractAction closeAllAction  = new CloseAllAction(MENUITEM_CLOSEALL, null);
    private final AbstractAction closeAllButActiveAction = new CloseAllButActiveAction(MENUITEM_CLOSEALLBUTACTIVE, null);
    private final JPopupMenu pop = new JPopupMenu() {
        @Override public void show(Component c, int x, int y) {
            closePageAction.setEnabled(tab.indexAtLocation(x, y)>=0);
            closeAllAction.setEnabled(tab.getTabCount()>0);
            closeAllButActiveAction.setEnabled(tab.getTabCount()>0);
            super.show(c, x, y);
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        pop.add(new NewTabAction(MENUITEM_NEWTAB, null));
        pop.addSeparator();
        pop.add(closePageAction);
        pop.addSeparator();
        pop.add(closeAllAction);
        pop.add(closeAllButActiveAction);
        tab.setComponentPopupMenu(pop);
        tab.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JTabbedPane jtab = (JTabbedPane)e.getSource();
                if(jtab.getTabCount()<=0) { return; }
                int sindex = jtab.getSelectedIndex();
                for(int i=0;i<jtab.getTabCount();i++) {
                    if(i==sindex && jtab.getTitleAt(sindex).endsWith("1")) {
                        jtab.setForegroundAt(i, Color.GREEN);
                    }else if(i==sindex) {
                        Color sc = (sindex%2==0)?Color.RED:Color.BLUE;
                        jtab.setForegroundAt(i, sc);
                    }else{
                        jtab.setForegroundAt(i, Color.BLACK);
                    }
                }
            }
        });
        tab.addTab("Title", new JLabel("Tab"));
        add(tab);
        setPreferredSize(new Dimension(320, 200));
    }

    class NewTabAction extends AbstractAction {
        public NewTabAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent e) {
            tab.addTab("Title: "+count,
                       (count%2==0)? new JLabel("Tab: "+count)
                                   : new JScrollPane(new JTree()));
            tab.setSelectedIndex(tab.getTabCount()-1);
            count++;
        }
    }
    class ClosePageAction extends AbstractAction {
        public ClosePageAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.remove(tab.getSelectedIndex());
        }
    }
    class CloseAllAction extends AbstractAction {
        public CloseAllAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            tab.removeAll();
        }
    }
    class CloseAllButActiveAction extends AbstractAction {
        public CloseAllButActiveAction(String label, Icon icon) {
            super(label,icon);
        }
        @Override public void actionPerformed(ActionEvent evt) {
            int tabidx = tab.getSelectedIndex();
            String title = tab.getTitleAt(tabidx);
            Component cmp = tab.getComponentAt(tabidx);
            tab.removeAll();
            tab.addTab(title, cmp);
        }
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
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
