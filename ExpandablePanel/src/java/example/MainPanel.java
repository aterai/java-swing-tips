package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private Box northBox  = Box.createVerticalBox();
    private Box centerBox = Box.createVerticalBox();
    private Box southBox  = Box.createVerticalBox();
    public MainPanel() {
        super(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        final java.util.List<ExpansionPanel> panelList = makeList();
        ExpansionListener rl = new ExpansionListener() {
            public void expansionStateChanged(ExpansionEvent e) {
                initComps(panelList, e);
            }
        };
        for(ExpansionPanel exp: panelList) {
            northBox.add(exp);
            exp.addExpansionListener(rl);
        }
        panel.add(northBox,  BorderLayout.NORTH);
        panel.add(centerBox);
        panel.add(southBox,  BorderLayout.SOUTH);
        panel.setMinimumSize(new Dimension(120, 0));
        JSplitPane sp = new JSplitPane();
        sp.setLeftComponent(panel);
        sp.setRightComponent(new JScrollPane(new JTree()));
        add(sp);
        setPreferredSize(new Dimension(320, 240));
    }

    public void initComps(java.util.List<ExpansionPanel> list, ExpansionEvent e) {
        setVisible(false);
        centerBox.removeAll();
        northBox.removeAll();
        southBox.removeAll();
        ExpansionPanel es = (ExpansionPanel) e.getSource();
        boolean flag = false;
        for(ExpansionPanel exp: list) {
            if(exp==es && exp.isSelected()) {
                centerBox.add(exp);
                flag = true;
            }else if(flag) {
                exp.setSelected(false);
                southBox.add(exp);
            }else{
                exp.setSelected(false);
                northBox.add(exp);
            }
        }
        setVisible(true);
    }

    private java.util.List<ExpansionPanel> makeList() {
        return Arrays.<ExpansionPanel>asList(
            new ExpansionPanel("Panel1") {
                public Container makePanel() {
                    Box p = Box.createVerticalBox();
                    p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    p.add(new JCheckBox("aaaa"));
                    p.add(new JCheckBox("bbbbbbbbb"));
                    return p;
                }
            },
            new ExpansionPanel("Panel2") {
                public Container makePanel() {
                    Box p = Box.createVerticalBox();
                    p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    for(int i=0;i<16;i++) p.add(new JLabel(String.format("%02d", i)));
                    return p;
                }
            },
            new ExpansionPanel("Panel3") {
                public Container makePanel() {
                    Box p = Box.createVerticalBox();
                    p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    ButtonGroup bg = new ButtonGroup();
                    for(JRadioButton b: Arrays.<JRadioButton>asList(
                        new JRadioButton("aa"), new JRadioButton("bb"), new JRadioButton("cc"))) {
                        p.add(b); bg.add(b); b.setSelected(true);
                    }
                    return p;
                }
            });
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

abstract class ExpansionPanel extends JPanel {
    abstract public Container makePanel();

    private final JButton button;
    private final Container panel;
    private final JScrollPane scroll = new JScrollPane();
    private final String title;
    private boolean openFlag = false;

    public ExpansionPanel(String title_) {
        super(new BorderLayout());
        title = title_;
        button = new JButton(new AbstractAction(title) {
            @Override public void actionPerformed(ActionEvent e) {
                openFlag = !openFlag;
                initPanel();
                fireExpansionEvent();
            }
        });
        panel = makePanel();
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().add(panel);
        add(button, BorderLayout.NORTH);
    }

    public boolean isSelected() {
        return openFlag;
    }
    public void setSelected(boolean flg) {
        openFlag = flg;
        initPanel();
    }
    protected void initPanel() {
        if(openFlag) {
            add(scroll);
            setPreferredSize(new Dimension(getSize().width, button.getSize().height+panel.getSize().height));
        }else{
            remove(scroll);
            setPreferredSize(new Dimension(getSize().width, button.getSize().height));
        }
        revalidate();
    }

    protected Vector<ExpansionListener> listenerList = new Vector<ExpansionListener>();
    public void addExpansionListener(ExpansionListener listener) {
        if(!listenerList.contains(listener)) listenerList.add(listener);
    }
    public void removeExpansionListener(ExpansionListener listener) {
        listenerList.remove(listener);
    }
    public void fireExpansionEvent() {
        Vector list = (Vector)listenerList.clone();
        Enumeration enm = list.elements();
        ExpansionEvent e = new ExpansionEvent(this);
        while(enm.hasMoreElements()) {
            ExpansionListener listener = (ExpansionListener)enm.nextElement();
            listener.expansionStateChanged(e);
        }
    }
}

class ExpansionEvent extends java.util.EventObject{
    public ExpansionEvent(Object source) {
        super(source);
    }
}

interface ExpansionListener{
    public void expansionStateChanged(ExpansionEvent e);
}
