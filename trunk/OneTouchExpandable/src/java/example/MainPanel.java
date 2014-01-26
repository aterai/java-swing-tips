package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.lang.reflect.*;
import javax.jnlp.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
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
        JScrollPane s1 = new JScrollPane(new JTable(model));
        JScrollPane s2 = new JScrollPane(new JTree());
        s1.setMinimumSize(new Dimension(0, 100));
        s2.setMinimumSize(new Dimension(0, 100));

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, s1, s2);
        splitPane.setOneTouchExpandable(true);
        //splitPane.setDividerLocation(0);

        BasicService bs;
        try{
            bs = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
        }catch(UnavailableServiceException ex) {
            bs = null;
        }
        s1.setMinimumSize(new Dimension(0, 100));
        s2.setMinimumSize(new Dimension(0, 100));
        if(bs != null) {
//             s1.setMinimumSize(new Dimension(0, 0));
//             s2.setMinimumSize(new Dimension(0, 0));
//             EventQueue.invokeLater(new Runnable() {
//                 @Override public void run() {
//                     splitPane.setDividerLocation(1.0);
//                     splitPane.setResizeWeight(1.0);
//                 }
//             });
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    Container divider = ((BasicSplitPaneUI)splitPane.getUI()).getDivider();
                    for(Component c: divider.getComponents()) {
                        if(c instanceof JButton) {
                            ((JButton)c).doClick();
                            break;
                        }
                    }
                }
            });
        }else{
            if(splitPane.getUI() instanceof BasicSplitPaneUI) {
                try{
                    //splitPane.setDividerLocation(1);
                    splitPane.setDividerLocation(0);
                    Method setKeepHidden = BasicSplitPaneUI.class.getDeclaredMethod(
                        "setKeepHidden", new Class<?>[] { Boolean.TYPE }); //boolean.class });
                    setKeepHidden.setAccessible(true);
                    setKeepHidden.invoke(splitPane.getUI(), new Object[] { Boolean.TRUE });
                }catch(NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        add(splitPane);
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
