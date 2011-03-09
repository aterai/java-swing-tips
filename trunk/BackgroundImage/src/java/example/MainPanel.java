package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private final ImageIcon bgimage;
    public MainPanel() {
        super(new BorderLayout());
        bgimage = new ImageIcon(getClass().getResource("16x16.png"));
        add(new JLabel("@title@"));
        setOpaque(false);
        setPreferredSize(new Dimension(320, 180));
    }
    @Override public void paintComponent(Graphics g) {
        Dimension d = getSize();
        int w = bgimage.getIconWidth();
        int h = bgimage.getIconHeight();
        for(int i=0;i*w<d.width;i++) {
            for(int j=0;j*h<d.height;j++) {
                g.drawImage(bgimage.getImage(), i*w, j*h, w, h, null);
            }
        }
//         for(int x=0;x<d.width;x+=w) {
//             for(int y=0;y<d.height;y+=h) {
//                 g.drawImage(bgimage.getImage(), x, y, w, h, this);
//             }
//         }
        super.paintComponent(g);
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
