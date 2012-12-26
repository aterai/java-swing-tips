package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
// import java.awt.font.*;
// import java.awt.geom.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    String[] columnNames = {"Integer", "String", "Boolean"};
    DefaultTableModel model = new DefaultTableModel(null, columnNames) {
        @Override public Class<?> getColumnClass(int column) {
            switch(column) {
              case 0:  return Integer.class;
              case 2:  return Boolean.class;
              default: return String.class;
            }
            //return getValueAt(0, column).getClass();
        }
    };
    JTable table = new JTable(model);
// {
//         String text = "No Data: drawGlyphVector";
//         Font font = getFont(); //new Font("serif", Font.PLAIN, 50);
//         FontRenderContext frc = new FontRenderContext(null,true,true);
//         GlyphVector gv = font.createGlyphVector(frc, text);
//         float gw = (float)gv.getVisualBounds().getWidth();
//         float gh = (float)gv.getVisualBounds().getHeight();
//         @Override protected void paintComponent(Graphics g) {
//             super.paintComponent(g);
//             if(table.getRowCount()==0) {
//                 Graphics2D g2 = (Graphics2D)g;
//                 g2.setPaint(Color.BLACK);
//                 //g2.draw(new Line2D.Float(0,getHeight()/2f,1000,getHeight()/2f));
//                 g2.drawGlyphVector(gv, (getWidth()-gw)/2f, (getHeight()+gh)/2f);
//             }
//         }
//     };
    //JButton button = new JButton("No Data: JButton");
    JEditorPane editor = new JEditorPane("text/html", "<html>No data! <a href='dummy'>Input hint(beep)</a></html>");

    public MainPanel() {
        super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setComponentPopupMenu(new TablePopupMenu());
        table.setLayout(new GridBagLayout());

        model.addTableModelListener(new TableModelListener() {
            @Override public void tableChanged(TableModelEvent e) {
                DefaultTableModel model = (DefaultTableModel)e.getSource();
                editor.setVisible(model.getRowCount()==0);
            }
        });

        editor.setOpaque(false);
        editor.setEditable(false);
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if(e.getEventType()==HyperlinkEvent.EventType.ACTIVATED) {
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }
            }
        });
        table.add(editor);
        add(new JScrollPane(table));
        setPreferredSize(new Dimension(320, 240));
    }

    private class TablePopupMenu extends JPopupMenu {
        private final Action addAction = new AbstractAction("add") {
            @Override public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)getInvoker();
                DefaultTableModel model = (DefaultTableModel)table.getModel();
                model.addRow(new Object[] {0, "aaa", Boolean.FALSE});
                Rectangle rect = table.getCellRect(model.getRowCount()-1, 0, true);
                table.scrollRectToVisible(rect);
                //if(table.getRowCount()>0) editor.setVisible(false);
            }
        };
        private final Action deleteAction = new AbstractAction("delete") {
            @Override public void actionPerformed(ActionEvent e) {
                JTable table = (JTable)getInvoker();
                DefaultTableModel model = (DefaultTableModel)table.getModel();
                int[] selection = table.getSelectedRows();
                if(selection==null || selection.length<=0) return;
                for(int i=selection.length-1;i>=0;i--) {
                    model.removeRow(table.convertRowIndexToModel(selection[i]));
                }
                //if(table.getRowCount()==0) editor.setVisible(true);
            }
        };
        public TablePopupMenu() {
            super();
            add(addAction);
            addSeparator();
            add(deleteAction);
        }
        @Override public void show(Component c, int x, int y) {
            JTable table = (JTable)c;
            int[] l = table.getSelectedRows();
            deleteAction.setEnabled(l!=null && l.length>0);
            super.show(c, x, y);
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
