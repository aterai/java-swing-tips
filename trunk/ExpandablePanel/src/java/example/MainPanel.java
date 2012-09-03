package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.EventListenerList;

public class MainPanel extends JPanel{
    private Box northBox  = Box.createVerticalBox();
    private Box centerBox = Box.createVerticalBox();
    private Box southBox  = Box.createVerticalBox();
    public MainPanel() {
        super(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        final List<ExpansionPanel> panelList = makeList();
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
    public void initComps(List<ExpansionPanel> list, ExpansionEvent e) {
        setVisible(false);
        centerBox.removeAll();
        northBox.removeAll();
        southBox.removeAll();
        ExpansionPanel es = (ExpansionPanel) e.getSource();
        boolean insertSouth = false;
        for(ExpansionPanel exp: list) {
            if(exp==es && exp.isSelected()) {
                centerBox.add(exp);
                insertSouth = true;
            }else if(insertSouth) {
                exp.setSelected(false);
                southBox.add(exp);
            }else{
                exp.setSelected(false);
                northBox.add(exp);
            }
        }
        setVisible(true);
    }

    private List<ExpansionPanel> makeList() {
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
    private final JScrollPane scroll;
    private final String title;
    private boolean openFlag = false;

    public ExpansionPanel(String title) {
        super(new BorderLayout());
        this.title = title;
        button = new JButton(new AbstractAction(title) {
            @Override public void actionPerformed(ActionEvent e) {
                setSelected(!isSelected());
                fireExpansionEvent();
            }
        });
        panel  = makePanel();
        scroll = new JScrollPane(panel);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        add(button, BorderLayout.NORTH);
    }

    public boolean isSelected() {
        return openFlag;
    }
    public void setSelected(boolean flg) {
        openFlag = flg;
        if(openFlag) {
            add(scroll);
        }else{
            remove(scroll);
        }
    }
//*
    private final EventListenerList listenerList = new EventListenerList();
    private ExpansionEvent expansionEvent = null;
    public void addExpansionListener(ExpansionListener l) {
        listenerList.add(ExpansionListener.class, l);
    }
    public void removeExpansionListener(ExpansionListener l) {
        listenerList.remove(ExpansionListener.class, l);
    }
    // Notify all listeners that have registered interest for
    // notification on this event type.The event instance
    // is lazily created using the parameters passed into
    // the fire method.
    protected void fireExpansionEvent() {
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();
        // Process the listeners last to first, notifying
        // those that are interested in this event
        for(int i = listeners.length-2; i>=0; i-=2) {
            if(listeners[i]==ExpansionListener.class) {
                // Lazily create the event:
                if(expansionEvent == null) {
                    expansionEvent = new ExpansionEvent(this);
                }
                ((ExpansionListener)listeners[i+1]).expansionStateChanged(expansionEvent);
            }
        }
    }
/*/
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
//*/
}

class ExpansionEvent extends EventObject{
    public ExpansionEvent(Object source) {
        super(source);
    }
}

interface ExpansionListener extends EventListener{
    public void expansionStateChanged(ExpansionEvent e);
}
