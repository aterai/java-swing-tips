package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;

public class MainPanel extends JPanel {
    private final JComponent tree = new JTree();
    public MainPanel() {
        super(new BorderLayout());
        tree.setComponentPopupMenu(makePopupMenu());
        add(new JScrollPane(tree));
        setPreferredSize(new Dimension(320, 240));
    }
//     @Override public void updateUI() {
//         super.updateUI();
//         if(tree!=null && System.getProperty("java.version").startsWith("1.6.0")) {
//             System.out.println("???: 1.6.0_xx bug??? remake JPopupMenu");
//             tree.setComponentPopupMenu(makePopupMenu());
//         }
//     }
    private static JPopupMenu makePopupMenu() {
        JPopupMenu popup = new TranslucentPopupMenu();
        popup.add(new JMenuItem("Undo"));
        popup.add(new JMenuItem("Redo"));
        popup.addSeparator();
        popup.add(new JMenuItem("Cut"));
        popup.add(new JMenuItem("Copy"));
        popup.add(new JMenuItem("Paste"));
        popup.add(new JMenuItem("Delete"));
        return popup;
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

class TranslucentPopupMenu extends JPopupMenu {
    private static final Color ALPHA_ZERO = new Color(0, true);
    private static final Color POPUP_BACK = new Color(250,250,250,200);
    private static final Color POPUP_LEFT = new Color(230,230,230,200);
    private static final int LEFT_WIDTH = 24;
    @Override public boolean isOpaque() {
        return false;
    }
    @Override public void updateUI() {
        super.updateUI();
        if(UIManager.getBorder("PopupMenu.border")==null) {
            setBorder(new BorderUIResource(BorderFactory.createLineBorder(Color.GRAY)));
        }
    }
    @Override public JMenuItem add(JMenuItem menuItem) {
        menuItem.setOpaque(false);
        //menuItem.setBackground(ALPHA_ZERO);
        return super.add(menuItem);
    }
    @Override public void show(Component c, int x, int y) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                Window p = SwingUtilities.getWindowAncestor(TranslucentPopupMenu.this);
                if(p instanceof JWindow) {
                    System.out.println("Heavy weight");
                    JWindow w = (JWindow)p;
//                     if(System.getProperty("java.version").startsWith("1.6.0")) {
//                         w.dispose();
//                         if(com.sun.awt.AWTUtilities.isWindowOpaque(w)) {
//                             com.sun.awt.AWTUtilities.setWindowOpaque(w, false);
//                         }
//                         w.setVisible(true);
//                     }else{
                        w.setBackground(ALPHA_ZERO);
//                     }
                }else{
                    System.out.println("Light weight");
                }
            }
        });
        super.show(c, x, y);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g.create();
        g2.setPaint(POPUP_LEFT);
        g2.fillRect(0,0,LEFT_WIDTH,getHeight());
        g2.setPaint(POPUP_BACK);
        g2.fillRect(LEFT_WIDTH,0,getWidth(),getHeight());
        g2.dispose();
        //super.paintComponent(g);
    }
}
