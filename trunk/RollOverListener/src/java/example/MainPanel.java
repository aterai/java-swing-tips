package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeList()));
        setPreferredSize(new Dimension(320, 200));
    }
    @SuppressWarnings("unchecked")
    private static JList makeList() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("Name1-comment");
        model.addElement("Name2-test");
        model.addElement("asdfasd");
        model.addElement("35663456345634563456");
        model.addElement("jklghlghjlghjlghlgh");
        model.addElement("Name0-testaa");
        model.addElement("nmvnvnvbnvbmnvbnmvbnmbvmnvbn");
        model.addElement("asdfffasddddddddddddddddd");
        model.addElement("asdfasdfasdfasdfas");
        model.addElement("4352345123452345234523452345234534");
        JList list = new RollOverList();
        list.setModel(model);
        return list;
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

class RollOverList extends JList {
    private static final Color ROLLOVERBACKGROUND = new Color(220,240,255);
    private int rollOverRowIndex = -1;
    private RollOverListener rollOverListener = null;
    @Override public void updateUI() {
        if(rollOverListener!=null) {
            removeMouseListener(rollOverListener);
            removeMouseMotionListener(rollOverListener);
        }
        setSelectionBackground(null); //Nimbus
        super.updateUI();
        EventQueue.invokeLater(new Runnable() {
            @SuppressWarnings("unchecked")
            @Override public void run() {
                rollOverListener = new RollOverListener();
                addMouseMotionListener(rollOverListener);
                addMouseListener(rollOverListener);
                setCellRenderer(new RollOverCellRenderer());
            }
        });
    }
    private class RollOverCellRenderer extends DefaultListCellRenderer {
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(index == rollOverRowIndex) {
                c.setBackground(ROLLOVERBACKGROUND);
                if(isSelected) { c.setForeground(Color.BLACK); }
                //c.setForeground(getSelectionForeground());
                //c.setBackground(getSelectionBackground());
            }
            return c;
        }
    }
    private class RollOverListener extends MouseAdapter {
        @Override public void mouseExited(MouseEvent e) {
            rollOverRowIndex = -1;
            repaint();
        }
        @Override public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if(row != rollOverRowIndex) {
                Rectangle rect = getCellBounds(row,row);
                if(rollOverRowIndex>=0) {
                    rect.add(getCellBounds(rollOverRowIndex,rollOverRowIndex));
                }
                rollOverRowIndex = row;
                repaint(rect);
            }
        }
    }
}
