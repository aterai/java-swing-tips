package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());

        final Icon emptyIcon = new EmptyIcon();
        UIManager.put("Tree.expandedIcon",  new IconUIResource(emptyIcon));
        UIManager.put("Tree.collapsedIcon", new IconUIResource(emptyIcon));

        final JTree tree = new JTree();
        for(int i=0;i<tree.getRowCount();i++) {
            tree.expandRow(i);
        }

        add(new JCheckBox(new AbstractAction("JTree: paint expanded, collapsed Icon") {
            @Override public void actionPerformed(ActionEvent e) {
                Icon ei;
                Icon ci;
                if(((JCheckBox)e.getSource()).isSelected()) {
                    UIDefaults lnfdef = UIManager.getLookAndFeelDefaults();
                    ei = lnfdef.getIcon("Tree.expandedIcon");
                    ci = lnfdef.getIcon("Tree.collapsedIcon");
                }else{
                    ei = ci = emptyIcon;
                }
                UIManager.put("Tree.expandedIcon",  new IconUIResource(ei));
                UIManager.put("Tree.collapsedIcon", new IconUIResource(ci));
                tree.updateUI();
                //SwingUtilities.updateComponentTreeUI(p);
            }
        }), BorderLayout.NORTH);

        JPanel p = new JPanel(new GridLayout(1,2));
        p.add(new JScrollPane(tree));
        p.add(new JScrollPane(new JTree()));
        add(p);
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
        }catch(ClassNotFoundException | InstantiationException |
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
class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }
  @Override public int getIconWidth()  { return 0; }
  @Override public int getIconHeight() { return 0; }
}
