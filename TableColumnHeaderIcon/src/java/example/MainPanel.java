package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;
import javax.swing.table.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        URL[] icons = {getIconURL("wi0062-16.png"), getIconURL("wi0063-16.png"), getIconURL("wi0064-16.png")};
        String[] columnNames = {"Column1", "Column2", "Column3"};
        JTable table = new JTable(new DefaultTableModel(columnNames, 8));
        TableColumnModel m = table.getColumnModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
            // m.getColumn(i).setHeaderRenderer(new IconColumnHeaderRenderer());
            // m.getColumn(i).setHeaderRenderer(new HtmlIconHeaderRenderer());
            // m.getColumn(i).setHeaderValue(String.format("<html><table><td><img src='%s'/></td>%s", icons[i], columnNames[i]));
            m.getColumn(i).setHeaderValue(String.format("<html><table cellpadding='0' cellspacing='0'><td><img src='%s'/></td>&nbsp;%s", icons[i], columnNames[i]));
        }
        table.setAutoCreateRowSorter(true);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }
    private URL getIconURL(String str) {
        return getClass().getResource(str);
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
        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(mb);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

// // TEST: LookAndFeel
// class IconColumnHeaderRenderer implements TableCellRenderer {
//     private final Icon icon = new ImageIcon(getClass().getResource("wi0063-16.png"));
//     @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
//         JLabel l = (JLabel) r.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
//         l.setHorizontalTextPosition(SwingConstants.RIGHT);
//         l.setIcon(icon);
//         return l;
//     }
// }

// TEST: html baseline
// class HtmlIconHeaderRenderer implements TableCellRenderer {
//     private final URL url = getClass().getResource("wi0063-16.png");
//     @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//         TableCellRenderer r = table.getTableHeader().getDefaultRenderer();
//         String str = Objects.toString(value, "");
//         String html = String.format("<html><img src='%s'/>&nbsp;%s", url, str);
//         // String html = String.format("<html><table><td cellpadding='0'><img src='%s'/></td>%s", url, str);
//         return r.getTableCellRendererComponent(table, html, isSelected, hasFocus, row, column);
//     }
// }

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lafRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
        lafItem.setActionCommand(lafClassName);
        lafItem.setHideActionText(true);
        lafItem.addActionListener(e -> {
            ButtonModel m = lafRadioGroup.getSelection();
            try {
                setLookAndFeel(m.getActionCommand());
            } catch (ClassNotFoundException | InstantiationException
                   | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        lafRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
