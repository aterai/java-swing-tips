package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public JComponent makeUI() {
        String[] columnNames = {"String", "Integer", "Boolean"};
        Object[][] data = {
            {"aaa", 12, true}, {"bbb", 5, false},
            {"CCC", 92, true}, {"DDD", 0, false}
        };
        DefaultTableModel model = new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
        };
        JTable table = new JTable(model);
        table.setAutoCreateRowSorter(true);
        JScrollPane s1, s2;
        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                                                    s1 = new JScrollPane(new JTable(model)),
                                                    s2 = new JScrollPane(new JTree()));
        splitPane.setOneTouchExpandable(true);
        s1.setMinimumSize(new Dimension(0, 100));
        s2.setMinimumSize(new Dimension(0, 100));

        JPanel north = new JPanel(new GridLayout(0,2,5,5));
        north.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        north.add(new JButton(new AbstractAction("Min:DividerLocation") {
            public void actionPerformed(ActionEvent e) {
                splitPane.setDividerLocation(0);
            }
        }));
        north.add(new JButton(new AbstractAction("Max:DividerLocation") {
            public void actionPerformed(ActionEvent e) {
                Insets i = splitPane.getInsets();
                if(splitPane.getOrientation()==JSplitPane.VERTICAL_SPLIT) {
                    splitPane.setDividerLocation(splitPane.getHeight() - (i!=null?i.bottom:0));
                }else{
                    splitPane.setDividerLocation(splitPane.getWidth()  - (i!=null?i.right :0));
                }
//                 int lastLoc    = splitPane.getLastDividerLocation();
//                 int currentLoc = splitPane.getDividerLocation();
//                 int newLoc;
//                 BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI)splitPane.getUI();
//                 Container divider = ((BasicSplitPaneUI)splitPane.getUI()).getDivider();
//
//                 if (currentLoc == insets.top) {
//                     int maxLoc = splitPane.getMaximumDividerLocation();
//                     newLoc = Math.min(lastLoc, maxLoc);
//                     //splitPaneUI.setKeepHidden(false);
//                 } else {
//                     newLoc = splitPane.getHeight() - divider.getHeight() - insets.top;
//                     //splitPaneUI.setKeepHidden(true);
//                 }
//                 if (currentLoc != newLoc) {
//                     splitPane.setDividerLocation(newLoc);
//                     splitPane.setLastDividerLocation(currentLoc);
//                 }
            }
        }));

        north.add(new JButton(new AbstractAction("Min:Action") {
            public void actionPerformed(final ActionEvent e) {
                splitPane.requestFocusInWindow();
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        Action selectMinAction = splitPane.getActionMap().get("selectMin");
                        selectMinAction.actionPerformed(new ActionEvent(splitPane, 1001, null));
                    }
                });
            }
        }));
        north.add(new JButton(new AbstractAction("Max:Action") {
            public void actionPerformed(final ActionEvent e) {
                splitPane.requestFocusInWindow();
                EventQueue.invokeLater(new Runnable() {
                    @Override public void run() {
                        Action selectMaxAction = splitPane.getActionMap().get("selectMax");
                        e.setSource(splitPane);
                        selectMaxAction.actionPerformed(e);
                    }
                });
            }
        }));

        final Container divider = ((BasicSplitPaneUI)splitPane.getUI()).getDivider();
        ButtonModel selectMinModel = null;
        ButtonModel selectMaxModel = null;
        for(Component c: divider.getComponents()) {
            if(c instanceof JButton) {
                ButtonModel m = ((JButton)c).getModel();
                if(selectMinModel==null && selectMaxModel==null) {
                    selectMinModel = m;
                }else if(selectMaxModel==null) {
                    selectMaxModel = m;
                }
            }
        }
        JButton smin = new JButton("Min:keepHidden");
        smin.setModel(selectMinModel);
        JButton smax = new JButton("Max:keepHidden");
        smax.setModel(selectMaxModel);
        north.add(smin);
        north.add(smax);

        JPanel p = new JPanel(new BorderLayout());
        p.add(north, BorderLayout.NORTH);
        p.add(splitPane);
        return p;
    }
    public MainPanel() {
        super(new BorderLayout());
        add(makeUI());
        setPreferredSize(new Dimension(320, 240));
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
