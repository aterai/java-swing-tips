package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JTabbedPane tab = new JTabbedPane();
        tab.addTab("Label", new JLabel("label"));
        tab.setMnemonicAt(0, KeyEvent.VK_L);
        tab.setDisplayedMnemonicIndexAt(0, 0);

        tab.addTab("Tree", new JScrollPane(new JTree()));
        tab.setMnemonicAt(1, KeyEvent.VK_T);
        tab.setDisplayedMnemonicIndexAt(1, 0);

        tab.addTab("TextField", new JTextField("field"));
        tab.setMnemonicAt(2, KeyEvent.VK_F);
        tab.setDisplayedMnemonicIndexAt(2, 4);

        tab.addTab("Button", new JButton("button"));
        tab.setMnemonicAt(3, KeyEvent.VK_B);
        tab.setDisplayedMnemonicIndexAt(3, 0);

        add(tab);
        setPreferredSize(new Dimension(320, 200));
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

// class MyJTabbedPane extends JTabbedPane {
//     private static final Icon icon = new CloseTabIcon();
//     public MyJTabbedPane() {
//         super();
//     }
//     @Override public void addTab(String title, final Component content) {
//         JPanel tab = new JPanel(new BorderLayout());
//         tab.setOpaque(false);
//         JLabel label = new JLabel(title);
//         label.setBorder(BorderFactory.createEmptyBorder(0,0,0,4));
//         JButton button = new JButton(icon);
//         button.setPreferredSize(new Dimension(icon.getIconWidth(),
//                                               icon.getIconHeight()));
//         button.addActionListener(new ActionListener() {
//             @Override public void actionPerformed(ActionEvent e) {
//                 removeTabAt(indexOfComponent(content));
//             }
//         });
//         label.setDisplayedMnemonic(KeyEvent.VK_A);
//         tab.add(label,  BorderLayout.WEST);
//         tab.add(button, BorderLayout.EAST);
//         tab.setBorder(BorderFactory.createEmptyBorder(2,1,1,1));
//         super.addTab(title, content);
//         setTabComponentAt(getTabCount()-1, tab);
//     }
//     private static class CloseTabIcon implements Icon {
//         private int width;
//         private int height;
//         public CloseTabIcon() {
//             width  = 16;
//             height = 16;
//         }
//         @Override public void paintIcon(Component c, Graphics g, int x, int y) {
//             g.translate(x, y);
//             g.setColor(Color.BLACK);
//             g.drawLine(4,  4, 11, 11);
//             g.drawLine(4,  5, 10, 11);
//             g.drawLine(5,  4, 11, 10);
//             g.drawLine(11, 4,  4, 11);
//             g.drawLine(11, 5,  5, 11);
//             g.drawLine(10, 4,  4, 10);
//             g.translate(-x, -y);
//         }
//         @Override public int getIconWidth() {
//             return width;
//         }
//         @Override public int getIconHeight() {
//             return height;
//         }
// //         @Override public Rectangle getBounds() {
// //             return new Rectangle(0, 0, width, height);
// //         }
//     }
// }
