package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JPanel p = new JPanel(new GridLayout(2,1));
        final JComboBox c0 = makeComboBox(true,  false);
        final JComboBox c1 = makeComboBox(false, false);
        final JComboBox c2 = makeComboBox(true,  true);
        final JComboBox c3 = makeComboBox(false, true);

        p.add(makeTitlePanel("setEditable(false)", Arrays.asList(c0, c1)));
        p.add(makeTitlePanel("setEditable(true)",  Arrays.asList(c2, c3)));
        p.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        add(p, BorderLayout.NORTH);
        add(new JButton(new AbstractAction("add") {
            @SuppressWarnings("unchecked")
            @Override public void actionPerformed(ActionEvent e) {
                String str = new Date().toString();
                for(JComboBox c: Arrays.asList(c0, c1, c2, c3)) {
                    MutableComboBoxModel m = (MutableComboBoxModel)c.getModel();
                    m.insertElementAt(str, m.getSize());
                }
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    @SuppressWarnings("unchecked")
    private static JComboBox makeComboBox(final boolean isDefault, boolean isEditable) {
        String[] m = new String[] {"aaa", "bbb", "ccc"};
        JComboBox comboBox = new JComboBox(m) {
            @Override public void updateUI() {
                super.updateUI();
                if(isDefault) return;
                setRenderer(new ButtonsRenderer(this));
                Accessible a = getAccessibleContext().getAccessibleChild(0);
                if(a instanceof BasicComboPopup) {
                    BasicComboPopup pop = (BasicComboPopup)a;
                    JList list = pop.getList();
                    CellButtonsMouseListener cbml = new CellButtonsMouseListener();
                    list.addMouseListener(cbml);
                    list.addMouseMotionListener(cbml);
                }
            }
        };
        comboBox.setEditable(isEditable);
        return comboBox;
    }
    private JComponent makeTitlePanel(String title, List<? extends JComponent> list) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        c.weightx = 1.0;
        c.gridy   = 0;
        for(JComponent cmp:list) {
            p.add(cmp, c);
            c.gridy++;
        }
        return p;
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
class CellButtonsMouseListener extends MouseAdapter {
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
        //c.doLayout(); //may be needed for mone LayoutManager
        pt.translate(-r.x, -r.y);
        Component b = SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y);
        if(b instanceof JButton) {
            return (JButton)b;
        }else{
            return null;
        }
    }
}

class ButtonsRenderer extends JPanel implements ListCellRenderer {
    private JLabel label = new DefaultListCellRenderer();
    private JButton deleteButton = new JButton(new AbstractAction("x") {
        @Override public void actionPerformed(ActionEvent e) {
            MutableComboBoxModel m = (MutableComboBoxModel)list.getModel();
            if(m.getSize()>1) {
                m.removeElementAt(index);
                comboBox.showPopup();
            }
        }
    }) {
        @Override public Dimension getPreferredSize() {
            return new Dimension(16, 16);
        }
    };
    public ButtonsRenderer(JComboBox comboBox) {
        super(new BorderLayout(0,0));
        this.comboBox = comboBox;
        label.setOpaque(false);
        setOpaque(true);
        add(label);
        deleteButton.setBorder(BorderFactory.createEmptyBorder());
        deleteButton.setFocusable(false);
        deleteButton.setRolloverEnabled(false);
        deleteButton.setContentAreaFilled(false);
        add(deleteButton, BorderLayout.EAST);
    }
    private final JComboBox comboBox;
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
        MutableComboBoxModel m = (MutableComboBoxModel)list.getModel();
        if(index<0 || m.getSize()-1<=0) {
            setOpaque(false);
            deleteButton.setVisible(false);
            label.setForeground(list.getForeground());
        }else{
            setOpaque(true);
            deleteButton.setVisible(true);
            deleteButton.setForeground(list.getForeground());
            if(button!=null && index==rolloverIndex) {
                button.setForeground(Color.WHITE);
            }
        }
        return this;
    }
    public int rolloverIndex = -1;
    public JButton button = null;
}
