package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(new JScrollPane(makeList()));
        setPreferredSize(new Dimension(320, 240));
    }
    @SuppressWarnings("unchecked")
    private static JList makeList() {
        DefaultListModel model = new DefaultListModel();
        model.addElement("11\n1");
        model.addElement("222222222222222\n222222222222222");
        model.addElement("3333333333333333333\n33333333333333333333\n33333333333333333");
        model.addElement("444");

        JList list = new JList(model);
        list.setFixedCellHeight(-1);
        CellButtonsMouseListener cbml = new CellButtonsMouseListener();
        list.addMouseListener(cbml);
        list.addMouseMotionListener(cbml);
        list.setCellRenderer(new ButtonsRenderer());
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
class CellButtonsMouseListener extends MouseAdapter{
    private int prevIndex = -1;
    private JButton prevButton = null;
    @Override public void mouseMoved(MouseEvent e) {
        JList list = (JList)e.getComponent();
        Point pt = e.getPoint();
        int index  = list.locationToIndex(pt);
        if(!list.getCellBounds(index, index).contains(pt)) {
            if(prevIndex>=0) {
                Rectangle r = list.getCellBounds(prevIndex, prevIndex);
                if(r!=null) {
                    list.repaint(r);
                }
            }
            index = -1;
            prevButton = null;
            return;
        }
        if(index>=0) {
            JButton button = getButton(list, pt, index);
            ButtonsRenderer renderer = (ButtonsRenderer)list.getCellRenderer();
            renderer.button = button;
            if(button != null) {
                button.getModel().setRollover(true);
                renderer.rolloverIndex = index;
                if(!button.equals(prevButton)) {
                    Rectangle r = list.getCellBounds(prevIndex, index);
                    if(r!=null) {
                        list.repaint(r);
                    }
                }
            }else{
                renderer.rolloverIndex = -1;
                Rectangle r = null;
                if(prevIndex != index) {
                    r = list.getCellBounds(index, index);
                }else if(prevIndex>=0 && prevButton!=null) {
                    r = list.getCellBounds(prevIndex, prevIndex);
                }
                if(r!=null) {
                    list.repaint(r);
                }
                prevIndex = -1;
            }
            prevButton = button;
        }
        prevIndex = index;
    }
    @Override public void mousePressed(MouseEvent e) {
        JList list = (JList)e.getComponent();
        Point pt = e.getPoint();
        int index  = list.locationToIndex(pt);
        if(index>=0) {
            JButton button = getButton(list, pt, index);
            if(button != null) {
                ButtonsRenderer renderer = (ButtonsRenderer)list.getCellRenderer();
                renderer.pressedIndex = index;
                renderer.button = button;
                list.repaint(list.getCellBounds(index, index));
            }
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        JList list = (JList)e.getComponent();
        Point pt = e.getPoint();
        int index  = list.locationToIndex(pt);
        if(index>=0) {
            JButton button = getButton(list, pt, index);
            if(button != null) {
                ButtonsRenderer renderer = (ButtonsRenderer)list.getCellRenderer();
                renderer.pressedIndex = -1;
                renderer.button = null;
                button.doClick();
                Rectangle r = list.getCellBounds(index, index);
                if(r!=null) {
                    list.repaint(r);
                }
            }
        }
    }
    @SuppressWarnings("unchecked")
    private static JButton getButton(JList list, Point pt, int index) {
        Container c = (Container)list.getCellRenderer().getListCellRendererComponent(list, "", index, false, false);
        Rectangle r = list.getCellBounds(index, index);
        c.setBounds(r);
        pt.translate(0,-r.y);
        Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
        if(b instanceof JButton) {
            return (JButton)b;
        }else{
            return null;
        }
    }
}

class ButtonsRenderer extends JPanel implements ListCellRenderer {
    public JTextArea label = new JTextArea();
    private final JButton deleteButton = new JButton(new AbstractAction("delete") {
        @Override public void actionPerformed(ActionEvent e) {
            //System.out.println("aaa");
            DefaultListModel m = (DefaultListModel)list.getModel();
            if(m.getSize()>1)
              m.removeElementAt(index);
        }
    });
    private final JButton copyButton = new JButton(new AbstractAction("copy") {
        @SuppressWarnings("unchecked")
        @Override public void actionPerformed(ActionEvent e) {
            //System.out.println("bbb");
            DefaultListModel m = (DefaultListModel)list.getModel();
            m.insertElementAt(m.getElementAt(index), index);
        }
    });
    public ButtonsRenderer() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(5,5,5,0));
        setOpaque(true);
        label.setLineWrap(true);
        label.setOpaque(false);
        add(label);

        Box box = Box.createHorizontalBox();
        for(JButton b: java.util.Arrays.asList(deleteButton, copyButton)) {
            b.setFocusable(false);
            b.setRolloverEnabled(false);
            box.add(b);
            box.add(Box.createHorizontalStrut(5));
        }
        add(box, BorderLayout.EAST);
    }
    private JList list;
    private int index;
    private final Color evenColor = new Color(230,255,230);
    @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
        label.setText((value==null)?"":value.toString());
        this.list = list;
        this.index = index;
        if(isSelected) {
            setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        }else{
            setBackground(index%2==0 ? evenColor : list.getBackground());
            label.setForeground(list.getForeground());
        }
        for(JButton b: java.util.Arrays.asList(deleteButton, copyButton)) {
            b.getModel().setRollover(false);
            b.getModel().setArmed(false);
            b.getModel().setPressed(false);
            b.getModel().setSelected(false);
        }
        if(button!=null) {
            if(index==pressedIndex) {
                button.getModel().setSelected(true);
                button.getModel().setArmed(true);
                button.getModel().setPressed(true);
            }else if(index==rolloverIndex) {
                button.getModel().setRollover(true);
            }
        }
        return this;
    }
    public int pressedIndex  = -1;
    public int rolloverIndex = -1;
    public JButton button = null;
}
