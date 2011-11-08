package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        final JCheckBox check = new JCheckBox("MAXIMIZED_BOTH: keep the same splitting ratio");
        check.setSelected(true);
        final SplitPaneWrapper sp = new SplitPaneWrapper();
        check.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                sp.setTestFlag(check.isSelected());
            }
        });
        add(check, BorderLayout.NORTH);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
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

class SplitPaneWrapper extends JPanel {
    private final static JTextArea log = new JTextArea();
    private final JSplitPane sp;
    public SplitPaneWrapper() {
        this(new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(log), new JScrollPane(new JTree())));
    }
    public SplitPaneWrapper(JSplitPane splitPane) {
        super(new BorderLayout());
        this.sp = splitPane;
        add(sp);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                sp.setDividerLocation(0.5);
            }
        });
    }
    private boolean flag = true;
    public void setTestFlag(boolean flag) {
        this.flag = flag;
    }
    private static int getOrientedSize(JSplitPane sp) {
        return (sp.getOrientation() == JSplitPane.VERTICAL_SPLIT)
            ? sp.getHeight() - sp.getDividerSize()
            : sp.getWidth()  - sp.getDividerSize();
    }
    private int prev_state = Frame.NORMAL;
    @Override public void doLayout() {
        int size = getOrientedSize(sp);
        final double proportionalLocation = sp.getDividerLocation()/(double)size;
        super.doLayout();
        if(!flag) return;
        int state = ((Frame)SwingUtilities.getWindowAncestor(sp)).getExtendedState();
        if(sp.isShowing() && state!=prev_state) {
            EventQueue.invokeLater(new Runnable() {
                @Override public void run() {
                    int s = getOrientedSize(sp);
                    int iv = (int)Math.round(s * proportionalLocation);
                    log.append(String.format("DividerLocation: %d\n", iv));
                    sp.setDividerLocation(iv);
                }
            });
            prev_state = state;
        }
    }
}
