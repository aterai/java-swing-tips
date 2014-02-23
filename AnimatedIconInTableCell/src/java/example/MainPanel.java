package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.image.ImageObserver;
import java.net.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
    private final String[] columnNames = {"String", "ImageIcon"};
    private final JTable table = new JTable();
    public MainPanel() {
        super(new BorderLayout());
        URL url = getClass().getResource("restore_to_background_color.gif");
        Object[][] data =  new Object[][] {
            {"Default ImageIcon", new ImageIcon(url)},
            {"ImageIcon#setImageObserver", makeImageIcon(url, table, 1, 1)}
        };
        table.setModel(new DefaultTableModel(data, columnNames) {
            @Override public Class<?> getColumnClass(int column) {
                return getValueAt(0, column).getClass();
            }
            @Override public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        });
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(20);

        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private static ImageIcon makeImageIcon(URL url, final JTable table, final int row, final int col) {
        ImageIcon icon = new ImageIcon(url);
        //Wastefulness: icon.setImageObserver((ImageObserver) table);
        icon.setImageObserver(new ImageObserver() {
            //@see http://www2.gol.com/users/tame/swing/examples/SwingExamples.html
            @Override public boolean imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
                if (!table.isShowing()) {
                    return false; //@see javax.swing.JLabel#imageUpdate(...)
                }
                if ((infoflags & (FRAMEBITS | ALLBITS)) != 0) { //@see java.awt.Component#imageUpdate(...)
                    int vr = table.convertRowIndexToView(row); //JDK 1.6.0
                    int vc = table.convertColumnIndexToView(col);
                    table.repaint(table.getCellRect(vr, vc, false));
                }
                return (infoflags & (ALLBITS | ABORT)) == 0;
            }
        });
        return icon;
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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
