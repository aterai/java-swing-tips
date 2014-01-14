package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    private transient final MouseListener ml = new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
            JTree tree = (JTree)e.getSource();
            if(tree.getRowForLocation(e.getX(), e.getY())<0) {
                tree.clearSelection();
            }
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        add(new JCheckBox(new AbstractAction("JTree#clearSelection: when user clicks empty surface") {
            @Override public void actionPerformed(ActionEvent e) {
                if(((JCheckBox)e.getSource()).isSelected()) {
                    tree.addMouseListener(ml);
                }else{
                    tree.removeMouseListener(ml);
                }
            }
        }), BorderLayout.NORTH);
        add(new JScrollPane(tree));
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
