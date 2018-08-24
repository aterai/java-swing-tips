package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTree tree = new JTree();

        JRadioButton r0 = new JRadioButton("DISCONTIGUOUS_TREE_SELECTION", true);
        r0.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
            }
        });

        JRadioButton r1 = new JRadioButton("SINGLE_TREE_SELECTION");
        r1.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            }
        });

        JRadioButton r2 = new JRadioButton("CONTIGUOUS_TREE_SELECTION");
        r2.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
            }
        });

        Box p = Box.createVerticalBox();
        ButtonGroup bg = new ButtonGroup();
        for (AbstractButton b: Arrays.asList(r0, r1, r2)) {
            bg.add(b);
            p.add(b);
        }
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }
    public static void createAndShowGui() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
