package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    @SuppressWarnings("unchecked")
    public MainPanel() {
        super(new GridLayout(1,2));

        DefaultListModel model = new DefaultListModel();
        JList list1 = new JList(model) {
            private CheckBoxCellRenderer renderer;
            @Override public void updateUI() {
                setForeground(null);
                setBackground(null);
                setSelectionForeground(null);
                setSelectionBackground(null);
                if(renderer!=null) {
                    removeMouseListener(renderer);
                    removeMouseMotionListener(renderer);
                }
                super.updateUI();
                renderer = new CheckBoxCellRenderer();
                setCellRenderer(renderer);
                addMouseListener(renderer);
                addMouseMotionListener(renderer);
            }
            //@see SwingUtilities2.pointOutsidePrefSize(...)
            private boolean pointOutsidePrefSize(Point p) {
                int index = locationToIndex(p);
                DefaultListModel m = (DefaultListModel)getModel();
                CheckBoxNode n = (CheckBoxNode)m.get(index);
                Component c = getCellRenderer().getListCellRendererComponent(this, n, index, false, false);
                //c.doLayout();
                Dimension d = c.getPreferredSize();
                Rectangle rect = getCellBounds(index, index);
                rect.width = d.width;
                return index < 0 || !rect.contains(p);
            }
            @Override protected void processMouseEvent(MouseEvent e) {
                if(!pointOutsidePrefSize(e.getPoint())) {
                    super.processMouseEvent(e);
                }
            }
            @Override protected void processMouseMotionEvent(MouseEvent e) {
                if(!pointOutsidePrefSize(e.getPoint())) {
                    super.processMouseMotionEvent(e);
                }else{
                    e = new MouseEvent((Component)e.getSource(), MouseEvent.MOUSE_EXITED, e.getWhen(),
                                       e.getModifiers(), e.getX(), e.getY(), e.getXOnScreen(), e.getYOnScreen(),
                                       e.getClickCount(), e.isPopupTrigger(), MouseEvent.NOBUTTON);
                    super.processMouseEvent(e);
                }
            }
        };
        list1.putClientProperty("List.isFileList", Boolean.TRUE);

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
            this.getModel().setRollover(index==rollOverRowIndex);
        }
        this.setText(value.toString());
        return this;
    }
    private int rollOverRowIndex = -1;
    @Override public void mouseExited(MouseEvent e) {
        JList l = (JList)e.getSource();
        if(rollOverRowIndex>=0) {
            l.repaint(l.getCellBounds(rollOverRowIndex, rollOverRowIndex));
            rollOverRowIndex = -1;
        }
    }
    @SuppressWarnings("unchecked")
    @Override public void mouseClicked(MouseEvent e) {
        if(e.getButton()==MouseEvent.BUTTON1) {
            JList l = (JList)e.getComponent();
            DefaultListModel m = (DefaultListModel)l.getModel();
            Point p = e.getPoint();
            int index  = l.locationToIndex(p);
            if(index>=0) {
                CheckBoxNode n = (CheckBoxNode)m.get(index);
                m.set(index, new CheckBoxNode(n.text, !n.selected));
                l.repaint(l.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseMoved(MouseEvent e) {
        JList l = (JList)e.getSource();
        int index = l.locationToIndex(e.getPoint());
        if(index != rollOverRowIndex) {
            rollOverRowIndex = index;
            l.repaint();
        }
    }
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mousePressed(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseDragged(MouseEvent e) {}
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
