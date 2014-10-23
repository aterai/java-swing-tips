package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JRadioButton r0 = new JRadioButton("DISCONTIGUOUS_TREE_SELECTION");
    private final JRadioButton r1 = new JRadioButton("SINGLE_TREE_SELECTION");
    private final JRadioButton r2 = new JRadioButton("CONTIGUOUS_TREE_SELECTION");
    private final ButtonGroup bg = new ButtonGroup();
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());

        ActionListener al = new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                JRadioButton rb = (JRadioButton) e.getSource();
                if (rb.equals(r2)) {
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
                } else if (rb.equals(r1)) {
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
                } else {
                    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
                }
            }
        };
        r0.setSelected(true);
        Box p = Box.createVerticalBox();
        for (AbstractButton b: Arrays.asList(r0, r1, r2)) {
            b.addActionListener(al); bg.add(b); p.add(b);
        }
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
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
