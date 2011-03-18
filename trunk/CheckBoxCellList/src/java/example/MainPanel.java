package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
//import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(1,2));

        DefaultListModel model = new DefaultListModel();
        JList list1 = new JList(model) {
            @Override public void updateUI() {
                setForeground(null);
                setBackground(null);
                setSelectionForeground(null);
                setSelectionBackground(null);
                super.updateUI();
            }
        };
        list1.putClientProperty("List.isFileList", Boolean.TRUE);
        CheckBoxCellRenderer r = new CheckBoxCellRenderer();
        list1.setCellRenderer(r);
        list1.addMouseListener(r);
        list1.addMouseMotionListener(r);

        Box list2 = Box.createVerticalBox();

        for(String title: Arrays.asList(
                "aaaa", "bbbbbbb", "ccc", "dddddd", "eeeeeee",
                "fffffffff", "gggggg", "hhhhh", "iiii", "jjjjjjjjjj")) {
            boolean flag = title.length()%2==0;
            model.addElement(new CheckBoxNode(title, flag));
            addComp(list2, new JCheckBox(title, flag));
        }
        add(makeTitledPanel("JCheckBox Cell in JList", list1));
        add(makeTitledPanel("JCheckBox in Box",        list2));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeTitledPanel(String title, JComponent tree) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(tree));
        return p;
    }
    private static void addComp(Box box, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        box.add(c);
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

class CheckBoxCellRenderer extends JCheckBox implements ListCellRenderer, MouseListener, MouseMotionListener {
    @Override public Component getListCellRendererComponent(
        JList list,
        Object value,
        int index,
        boolean isSelected,
        boolean cellHasFocus) {
        this.setOpaque(true);
        if(isSelected) {
            this.setBackground(list.getSelectionBackground());
            this.setForeground(list.getSelectionForeground());
        }else{
            this.setBackground(list.getBackground());
            this.setForeground(list.getForeground());
        }
        if(value instanceof CheckBoxNode) {
            this.setSelected(((CheckBoxNode)value).selected);
            if(index==pressedRowIndex) {
                this.getModel().setArmed(true);
                this.getModel().setPressed(true);
            }else{
                this.getModel().setRollover(index==rollOverRowIndex);
                this.getModel().setArmed(false);
                this.getModel().setPressed(false);
            }
        }
        this.setText(value.toString());
        return this;
    }
    private int rollOverRowIndex = -1;
    private int pressedRowIndex = -3;
    @Override public void mouseExited(MouseEvent e) {
        rollOverRowIndex = -1;
        JList c = (JList)e.getSource();
        c.repaint();
    }
    @Override public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON1) {
            JList t = (JList)e.getComponent();
            DefaultListModel m = (DefaultListModel)t.getModel();
            Point p = e.getPoint();
            int index  = t.locationToIndex(p);
            CheckBoxNode n = (CheckBoxNode)m.get(index);
            Component c = t.getCellRenderer().getListCellRendererComponent(t, n, index, false, false);
            Dimension d = c.getPreferredSize();
            if(d.width>=p.x) {
                m.set(index, new CheckBoxNode(n.text, !n.selected));
                t.repaint();
                t.repaint(t.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseEntered(MouseEvent e) {}
    private int pressStartIndex = -1;
    @Override public void mousePressed(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON1) {
            JList c = (JList)e.getSource();
            int row = c.locationToIndex(e.getPoint());
            pressStartIndex = row;
            if(row != pressedRowIndex) {
                pressedRowIndex = row;
                c.repaint();
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        pressedRowIndex = -1;
        pressStartIndex = -1;
    }
    @Override public void mouseDragged(MouseEvent e) {
        JList c = (JList)e.getSource();
        int row = c.locationToIndex(e.getPoint());
        if(row != rollOverRowIndex) {
            rollOverRowIndex = -1;
        }
        if(row != pressedRowIndex) {
            pressedRowIndex = -1;
        }
        if(row == pressStartIndex) {
            pressedRowIndex = row;
        }
        c.repaint();
    }
    @Override public void mouseMoved(MouseEvent e) {
        JList c = (JList)e.getSource();
        int row = c.locationToIndex(e.getPoint());
        if(row != rollOverRowIndex) {
            rollOverRowIndex = row;
            c.repaint();
        }
    }
}

class CheckBoxNode {
    public final String text;
    public final boolean selected;
    public CheckBoxNode(String text, boolean selected) {
        this.text = text;
        this.selected = selected;
    }
    @Override public String toString() {
        return text;
    }
}
