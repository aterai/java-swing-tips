package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.event.EventListenerList;

public final class MainPanel extends JPanel {
    private final Box northBox  = Box.createVerticalBox();
    private final Box centerBox = Box.createVerticalBox();
    private final Box southBox  = Box.createVerticalBox();
    private final List<AbstractExpansionPanel> panelList = makeList();
    private final transient ExpansionListener rl = new ExpansionListener() {
        @Override public void expansionStateChanged(ExpansionEvent e) {
            initComps(panelList, (JComponent) e.getSource());
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        JPanel panel = new JPanel(new BorderLayout());
        for (AbstractExpansionPanel exp: panelList) {
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

    private void initComps(List<AbstractExpansionPanel> list, JComponent source) {
        setVisible(false);
        centerBox.removeAll();
        northBox.removeAll();
        southBox.removeAll();
        boolean insertSouth = false;
        for (AbstractExpansionPanel exp: list) {
            if (source.equals(exp) && exp.isSelected()) {
                centerBox.add(exp);
                insertSouth = true;
                continue;
            }
            exp.setSelected(false);
            if (insertSouth) {
                southBox.add(exp);
            } else {
                northBox.add(exp);
            }
        }
        setVisible(true);
    }

    private List<AbstractExpansionPanel> makeList() {
        return Arrays.<AbstractExpansionPanel>asList(
            new AbstractExpansionPanel("Panel1") {
                public Container makePanel() {
                    Box p = Box.createVerticalBox();
                    p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    p.add(new JCheckBox("aaaa"));
                    p.add(new JCheckBox("bbbbbbbbb"));
                    return p;
                }
            },
            new AbstractExpansionPanel("Panel2") {
                public Container makePanel() {
                    Box p = Box.createVerticalBox();
                    p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    for (int i = 0; i < 16; i++) {
                        p.add(new JLabel(String.format("%02d", i)));
                    }
                    return p;
                }
            },
            new AbstractExpansionPanel("Panel3") {
                public Container makePanel() {
                    Box p = Box.createVerticalBox();
                    p.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
                    ButtonGroup bg = new ButtonGroup();
                    for (JRadioButton b: Arrays.<JRadioButton>asList(
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
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException |
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

abstract class AbstractExpansionPanel extends JPanel {
    private final EventListenerList listenerList = new EventListenerList();
    private ExpansionEvent expansionEvent;
    private final JScrollPane scroll;
    private boolean openFlag;

    public AbstractExpansionPanel(String title) {
        super(new BorderLayout());
        JButton button = new JButton(new AbstractAction(title) {
            @Override public void actionPerformed(ActionEvent e) {
                setSelected(!isSelected());
                fireExpansionEvent();
            }
        });
        scroll = new JScrollPane(makePanel());
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        add(button, BorderLayout.NORTH);
    }

    public abstract Container makePanel();

    public boolean isSelected() {
        return openFlag;
    }

    public void setSelected(boolean flg) {
        openFlag = flg;
        if (openFlag) {
            add(scroll);
        } else {
            remove(scroll);
        }
    }

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
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ExpansionListener.class) {
                // Lazily create the event:
                if (expansionEvent == null) {
                    expansionEvent = new ExpansionEvent(this);
                }
                ((ExpansionListener) listeners[i + 1]).expansionStateChanged(expansionEvent);
            }
        }
    }
}

class ExpansionEvent extends EventObject {
    private static final long serialVersionUID = 1L;
    public ExpansionEvent(Object source) {
        super(source);
    }
}

interface ExpansionListener extends EventListener {
    void expansionStateChanged(ExpansionEvent e);
}
