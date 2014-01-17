package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;

public class MainPanel extends JPanel {
    //private final Box box = Box.createVerticalBox();
    //private final Component glue = Box.createVerticalGlue();
    public MainPanel() {
        super(new BorderLayout());

        Box accordion = Box.createVerticalBox();
        accordion.setOpaque(true);
        accordion.setBackground(new Color(180, 180, 255));
        accordion.setBorder(BorderFactory.createEmptyBorder(10, 5, 5, 5));
        for(ExpansionPanel p: makeList()) {
            accordion.add(p);
            accordion.add(Box.createVerticalStrut(5));
        }
        accordion.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                             JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);
        scroll.getViewport().add(accordion);

        JSplitPane split = new JSplitPane();
        split.setResizeWeight(0.5);
        split.setDividerSize(2);
        split.setLeftComponent(scroll);
        split.setRightComponent(new JLabel("Dummy"));
        add(split);
        setPreferredSize(new Dimension(320, 240));
    }

//     public void addComp(Component comp) {
//         box.remove(glue);
//         box.add(Box.createVerticalStrut(5));
//         box.add(comp);
//         box.add(glue);
//         box.revalidate();
//     }

    private List<ExpansionPanel> makeList() {
        return Arrays.asList(
            new ExpansionPanel("System Tasks") {
                @Override public JPanel makePanel() {
                    JPanel pnl = new JPanel(new GridLayout(0,1));
                    JCheckBox c1 = new JCheckBox("aaaa");
                    JCheckBox c2 = new JCheckBox("aaaaaaa");
                    c1.setOpaque(false);
                    c2.setOpaque(false);
                    pnl.add(c1);
                    pnl.add(c2);
                    return pnl;
                }
            },
            new ExpansionPanel("Other Places") {
                @Override public JPanel makePanel() {
                    JPanel pnl = new JPanel(new GridLayout(0,1));
                    pnl.add(new JLabel("Desktop"));
                    pnl.add(new JLabel("My Network Places"));
                    pnl.add(new JLabel("My Documents"));
                    pnl.add(new JLabel("Shared Documents"));
                    return pnl;
                }
            },
            new ExpansionPanel("Details") {
                @Override public JPanel makePanel() {
                    JPanel pnl = new JPanel(new GridLayout(0,1));
                    ButtonGroup bg = new ButtonGroup();
                    JRadioButton b1 = new JRadioButton("aaa");
                    JRadioButton b2 = new JRadioButton("bbb");
                    JRadioButton b3 = new JRadioButton("ccc");
                    JRadioButton b4 = new JRadioButton("ddd");
                    for(JRadioButton b:Arrays.asList(b1,b2,b3,b4)) {
                        b.setOpaque(false); pnl.add(b); bg.add(b);
                    }
                    b1.setSelected(true);
                    return pnl;
                }
            }
        );
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
    abstract public JPanel makePanel();
    private final String title;
    private final JLabel label;
    private final JPanel panel;

    public ExpansionPanel(String title_) {
        super(new BorderLayout());
        title = title_;
        label = new JLabel("\u25BC "+title) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g;
                //Insets ins = getInsets();
                g2.setPaint(new GradientPaint(50, 0, Color.WHITE, getWidth(), getHeight(), new Color(200, 200, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        label.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent evt) {
                initPanel();
            }
        });
        label.setForeground(Color.BLUE);
        label.setBorder(BorderFactory.createEmptyBorder(2,5,2,2));
        add(label, BorderLayout.NORTH);

        panel = makePanel();
        panel.setVisible(false);
        panel.setOpaque(true);
        panel.setBackground(new Color(240, 240, 255));
        Border outBorder = BorderFactory.createMatteBorder(0,2,2,2,Color.WHITE);
        Border inBorder  = BorderFactory.createEmptyBorder(10,10,10,10);
        Border border    = BorderFactory.createCompoundBorder(outBorder, inBorder);
        panel.setBorder(border);
        add(panel);
    }
    @Override public Dimension getPreferredSize() {
        Dimension d = label.getPreferredSize();
        if(panel.isVisible()) {
            d.height += panel.getPreferredSize().height;
        }
        return d;
    }
    @Override public Dimension getMaximumSize() {
        Dimension d = getPreferredSize();
        d.width = Short.MAX_VALUE;
        return d;
    }
    protected void initPanel() {
        panel.setVisible(!panel.isVisible());
        label.setText(String.format("%s %s", panel.isVisible()?"\u25B3":"\u25BC", title));
        revalidate();
        //fireExpansionEvent();
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                panel.scrollRectToVisible(panel.getBounds());
            }
        });
    }

//     protected Vector<ExpansionListener> expansionListenerList = new Vector<ExpansionListener>();
//     public void addExpansionListener(ExpansionListener listener) {
//         if(!expansionListenerList.contains(listener)) { expansionListenerList.add(listener); }
//     }
//     public void removeExpansionListener(ExpansionListener listener) {
//         expansionListenerList.remove(listener);
//     }
//     public void fireExpansionEvent() {
//         Vector list = (Vector)expansionListenerList.clone();
//         Enumeration enm = list.elements();
//         ExpansionEvent e = new ExpansionEvent(this);
//         while(enm.hasMoreElements()) {
//             ExpansionListener listener = (ExpansionListener)enm.nextElement();
//             listener.expansionStateChanged(e);
//         }
//     }
}

// class ExpansionEvent extends EventObject {
//     public ExpansionEvent(Object source) {
//         super(source);
//     }
// }
//
// interface ExpansionListener {
//     public void expansionStateChanged(ExpansionEvent e);
// }
