package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.tree.*;

public final class MainPanel extends JPanel {
    private final JTree tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setComponentPopupMenu(new TreePopupMenu());
        add(new JScrollPane(tree));
        add(new JButton(new AbstractAction("Clear node selection") {
            @Override public void actionPerformed(ActionEvent e) {
                tree.clearSelection();
            }
        }), BorderLayout.SOUTH);
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

class TreePopupMenu extends JPopupMenu {
    private TreePath[] tsp;
    public TreePopupMenu() {
        super();
        add(new AbstractAction("path") {
            @Override public void actionPerformed(ActionEvent e) {
                //for (TreePath path: tsp) System.out.println(path);
                JOptionPane.showMessageDialog(null, tsp, "path", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        add(new JMenuItem("dummy"));
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTree) {
            JTree tree = (JTree) c;
            tsp = tree.getSelectionPaths();
            if (tsp != null && tsp.length > 0) {
                super.show(c, x, y);
            }
        }
    }
}
