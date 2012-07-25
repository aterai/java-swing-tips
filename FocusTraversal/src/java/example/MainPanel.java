package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
    private final JPanel p = new JPanel(new BorderLayout(2,2));
    private final JTextArea textarea = new JTextArea();
    private final JButton nb = new JButton("NORTH");
    private final JButton sb = new JButton("SOUTH");
    private final JButton wb = new JButton("WEST");
    private final JButton eb = new JButton("EAST");
    private final JScrollPane scroll = new JScrollPane(textarea);
    private final Box box = Box.createHorizontalBox();
    private final JFrame frame;
    private final JCheckBox check = new JCheckBox("setEditable",true);

    public MainPanel(JFrame frame) {
        super(new BorderLayout(5,5));
        this.frame = frame;
        p.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
        p.add(scroll);
        p.add(nb, BorderLayout.NORTH);
        p.add(sb, BorderLayout.SOUTH);
        p.add(wb, BorderLayout.WEST);
        p.add(eb, BorderLayout.EAST);

        setFocusTraversalPolicyProvider(true);
        //frame.setFocusTraversalPolicy(policy);
        //setFocusTraversalPolicy(policy);
        //setFocusCycleRoot(true);

        FocusTraversalPolicy policy0 = getFocusTraversalPolicy();
//         final FocusTraversalPolicy policy0 = frame.getFocusTraversalPolicy();
//         final FocusTraversalPolicy policy0 = null;

        FocusTraversalPolicy policy1 = new FocusTraversalPolicy() {
            private final List<? extends Component> order = Arrays.asList(eb, wb, sb, nb);
            @Override public Component getFirstComponent(Container focusCycleRoot) {
                return order.get(0);
            }
            @Override public Component getLastComponent(Container focusCycleRoot) {
                return order.get(order.size()-1);
            }
            @Override public Component getComponentAfter(Container focusCycleRoot, Component aComponent) {
                int i = order.indexOf(aComponent);
                return order.get((i + 1) % order.size());
            }
            @Override public Component getComponentBefore(Container focusCycleRoot, Component aComponent) {
                int i = order.indexOf(aComponent);
                return order.get((i - 1 + order.size()) % order.size());
            }
            @Override public Component getDefaultComponent(Container focusCycleRoot) {
                return order.get(0);
            }
        };

        FocusTraversalPolicy policy2 = new LayoutFocusTraversalPolicy() {
            @Override protected boolean accept(Component c) {
                if(c instanceof JTextComponent) {
                    return ((JTextComponent)c).isEditable();
                }else{
                    return super.accept(c);
                }
            }
        };

        List<JRadioButton> rl = Arrays.asList(
          new JRadioButton(new FocusTraversalPolicyChangeAction("Default", policy0)),
          new JRadioButton(new FocusTraversalPolicyChangeAction("Custom",  policy1)),
          new JRadioButton(new FocusTraversalPolicyChangeAction("Layout",  policy2)));
        ButtonGroup bg = new ButtonGroup();
        box.setBorder(BorderFactory.createTitledBorder("FocusTraversalPolicy"));
        //boolean flag = true;
        for(JRadioButton rb: rl) {
            //if(flag) {rb.setSelected(true); flag = false;}
            bg.add(rb);
            box.add(rb);
            box.add(Box.createHorizontalStrut(3));
        }
        ((JRadioButton)bg.getElements().nextElement()).setSelected(true);
        box.add(Box.createHorizontalGlue());

        check.setHorizontalAlignment(SwingConstants.RIGHT);
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                textarea.setEditable(check.isSelected());
                debugPrint();
            }
        });
        add(p);
        add(box, BorderLayout.NORTH);
        add(check, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
        debugPrint();
    }
    class FocusTraversalPolicyChangeAction extends AbstractAction {
        private final FocusTraversalPolicy policy;
        public FocusTraversalPolicyChangeAction(String name, FocusTraversalPolicy p) {
            super(name);
            this.policy = p;
        }
        @Override public void actionPerformed(ActionEvent e) {
            setFocusTraversalPolicy(policy);
            debugPrint();
        }
    }
    private void debugPrint() {
        textarea.setText(
            debugString("frame",    frame)+
            debugString("this",     this)+
            debugString("p",        p)+
            debugString("box",      box)+
            debugString("scroll",   scroll)+
            debugString("textarea", textarea)+
            debugString("eb",       eb));
    }
    private static String debugString(String label, Container c) {
        return label+"------------------"+
          "\n  isFocusCycleRoot: "+               c.isFocusCycleRoot()+
          "\n  isFocusTraversalPolicySet: "+      c.isFocusTraversalPolicySet()+
          "\n  isFocusTraversalPolicyProvider: "+ c.isFocusTraversalPolicyProvider()+
          "\n";
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
