package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    private final JList list = new RollOverList();
    private final DefaultListModel model = new DefaultListModel();
    public MainPanel() {
        super(new BorderLayout());
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
        list.setModel(model);
        add(new JScrollPane(list));
        setPreferredSize(new Dimension(320, 200));
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

class RollOverList extends JList {
    private int rollOverRowIndex = -1;
    public RollOverList() {
        super();
        RollOverListener rol = new RollOverListener();
        addMouseMotionListener(rol);
        addMouseListener(rol);
        setCellRenderer(new RollOverCellRenderer());
    }
    private class RollOverCellRenderer extends DefaultListCellRenderer{
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if(index == rollOverRowIndex) {
                c.setBackground(new Color(220,240,255));
                if(isSelected) c.setForeground(Color.BLACK);
                //c.setForeground(getSelectionForeground());
                //c.setBackground(getSelectionBackground());
            }
            return c;
        }
    }
    private class RollOverListener extends MouseInputAdapter {
        @Override public void mouseExited(MouseEvent e) {
            rollOverRowIndex = -1;
            repaint();
        }
        @Override public void mouseMoved(MouseEvent e) {
            int row = locationToIndex(e.getPoint());
            if(row != rollOverRowIndex) {
                rollOverRowIndex = row;
                repaint();
            }
        }
    }
}
