package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.imageio.*;
import javax.swing.*;

class MainPanel extends JPanel{
    private TexturePaint texture;
    public JComponent makeUI() {
        final JPanel p = new JPanel() {
            @Override public void paintComponent(Graphics g) {
                if(texture!=null) {
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setPaint(texture);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                super.paintComponent(g);
            }
        };
        p.setBackground(new Color(.5f,.8f,.5f,.5f));
        p.add(new JLabel("aaa: "));
        p.add(new JTextField(10));
        p.add(new JButton("bbb"));

        JComboBox combo = new JComboBox(new String[] {
            "Color(.5f,.8f,.5f,.5f)", "ImageTexturePaint", "CheckerTexturePaint"});
        combo.addItemListener(new ItemListener() {
            private final TexturePaint imageTexture = makeImageTexture();
            private final TexturePaint checkerTexture = makeCheckerTexture();
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    JComboBox cbox = (JComboBox)e.getSource();
                    Object o = cbox.getSelectedItem();
                    if("ImageTexturePaint".equals(o)) {
                        texture = imageTexture;
                        p.setOpaque(false);
                    }else if("CheckerTexturePaint".equals(o)) {
                        texture = checkerTexture;
                        p.setOpaque(false);
                    }else{
                        texture = null;
                        p.setOpaque(true);
                    }
                    Window w = SwingUtilities.getWindowAncestor(p);
                    if(w instanceof JFrame) { //XXX: JDK 1.7.0 ???
                        //((JFrame)w).getRootPane().repaint();
                        ((JFrame)w).getContentPane().repaint();
                    }else{
                        p.revalidate();
                        p.repaint();
                    }
                }
            }
        });
        p.add(combo);
        p.setPreferredSize(new Dimension(320, 240));
        return p;
    }
    private TexturePaint makeImageTexture() {
        BufferedImage bi = null;
        try{
            bi = ImageIO.read(getClass().getResource("unkaku_w.png"));
        }catch(java.io.IOException ioe) {
            ioe.printStackTrace();
            throw new RuntimeException(ioe);
        }
        return new TexturePaint(bi, new Rectangle(bi.getWidth(),bi.getHeight()));
    }
    private TexturePaint makeCheckerTexture() {
        int cs = 6;
        int sz = cs*cs;
        BufferedImage bi = new BufferedImage(sz,sz,BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = bi.createGraphics();
        g2.setPaint(new Color(200,150,100,50));
        g2.fillRect(0,0,sz,sz);
        for(int i=0;i*cs<sz;i++) {
            for(int j=0;j*cs<sz;j++) {
                if((i+j)%2==0) g2.fillRect(i*cs, j*cs, cs, cs);
            }
        }
        g2.dispose();
        return new TexturePaint(bi, new Rectangle(0,0,sz,sz));
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try{
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         }catch(Exception e) {
//             e.printStackTrace();
//         }
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame("@title@");
//         frame.setUndecorated(true);
        com.sun.awt.AWTUtilities.setWindowOpaque(frame, false);
        //frame.setBackground(new Color(0,0,0,0)); //1.7.0
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel().makeUI());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
