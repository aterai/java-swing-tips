package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.beans.*;
import javax.swing.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    private final String[] columnNames = {"Name", "Class", "Value"};
    private final DefaultTableModel model = new DefaultTableModel(null, columnNames);
    private final JTable table = new JTable(model);
    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        //for(String s:(String[])Toolkit.getDefaultToolkit().getDesktopProperty("win.propNames")) System.out.println(s);
        Toolkit.getDefaultToolkit().addPropertyChangeListener("win.xpstyle.colorName", new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                System.out.println("----\n"+e.getPropertyName());
                System.out.println(Toolkit.getDefaultToolkit().getDesktopProperty(e.getPropertyName()));
                initModel();
            }
        });
        Toolkit.getDefaultToolkit().addPropertyChangeListener("awt.multiClickInterval", new PropertyChangeListener() {
            @Override public void propertyChange(PropertyChangeEvent e) {
                System.out.println("----\n"+e.getPropertyName());
                System.out.println(Toolkit.getDefaultToolkit().getDesktopProperty(e.getPropertyName()));
                initModel();
            }
        });
        initModel();
        setPreferredSize(new Dimension(320,240));
        add(new JScrollPane(table));
    }
    private void initModel() {
        model.setRowCount(0);
        Toolkit tk = Toolkit.getDefaultToolkit();
        for(String s:(String[])tk.getDesktopProperty("win.propNames")) {
            Object o = tk.getDesktopProperty(s);
            model.addRow(new Object[] {s, o.getClass(), o});
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
