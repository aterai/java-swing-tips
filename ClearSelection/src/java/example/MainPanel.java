package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(1,2));
        add(makeTitledPanel("Default", makeList(true)));
        add(makeTitledPanel("clearSelection", makeList(false)));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JList<String> makeList(boolean def) {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("aaaaaaa");
        model.addElement("bbbbbbbbbbbbb");
        model.addElement("cccccccccc");
        model.addElement("ddddddddd");
        model.addElement("eeeeeeeeee");
        if(def) {
            return new JList<String>(model);
        }
        JList<String> list = new JList<String>(model) {
            private transient MouseAdapter listener;
            @Override public void updateUI() {
                removeMouseListener(listener);
                removeMouseMotionListener(listener);
                setForeground(null);
                setBackground(null);
                setSelectionForeground(null);
                setSelectionBackground(null);
                super.updateUI();
                if(listener==null) {
                    listener = new ClearSelectionListener();
                }
                addMouseListener(listener);
                addMouseMotionListener(listener);
            }
        };
        //list.putClientProperty("List.isFileList", Boolean.TRUE);
//     list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
//     list.setFixedCellWidth(64);
//     list.setFixedCellHeight(64);
//     list.setVisibleRowCount(0);
        return list;
    }

    private static JComponent makeTitledPanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(new JScrollPane(c));
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

class ClearSelectionListener extends MouseAdapter {
    private boolean startOutside;
    private static void clearSelectionAndFocus(JList list) {
        list.clearSelection();
        list.getSelectionModel().setAnchorSelectionIndex(-1);
        list.getSelectionModel().setLeadSelectionIndex(-1);
    }
    private static boolean contains(JList list, Point pt) {
        for(int i=0;i<list.getModel().getSize();i++) {
            Rectangle r = list.getCellBounds(i, i);
            if(r.contains(pt)) {
                return true;
            }
        }
        return false;
    }
    @Override public void mousePressed(MouseEvent e) {
        JList list = (JList)e.getSource();
        startOutside = contains(list, e.getPoint());
        startOutside ^= true;
        if(startOutside) {
            clearSelectionAndFocus(list);
        }
    }
    @Override public void mouseReleased(MouseEvent e) {
        startOutside = false;
    }
    @Override public void mouseDragged(MouseEvent e) {
        JList list = (JList)e.getSource();
        if(contains(list, e.getPoint())) {
            startOutside = false;
        }else if(startOutside) {
            clearSelectionAndFocus(list);
        }
    }
}
