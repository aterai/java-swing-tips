package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JTree tree = new JTree();
        tree.setComponentPopupMenu(new TreePopupMenu());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
    static class TreePopupMenu extends JPopupMenu {
        private TreePath[] tsp;
        public TreePopupMenu() {
            super();
            add(new AbstractAction("path") {
                @Override public void actionPerformed(ActionEvent e) {
                    //for(TreePath path:tsp) System.out.println(path);
                    JOptionPane.showMessageDialog(null, tsp, "path", JOptionPane.INFORMATION_MESSAGE);
                }
            });
            add(new JMenuItem("dummy"));
        }
        @Override public void show(Component c, int x, int y) {
            JTree tree = (JTree)c;
            tsp = tree.getSelectionPaths();
            if(tsp!=null && tsp.length>0) {
                super.show(c, x, y);
            }
        }
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
