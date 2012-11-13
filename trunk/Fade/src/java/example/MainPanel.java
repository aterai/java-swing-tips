package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.io.*;
import java.net.*;
import javax.imageio.*;
import javax.swing.*;

class MainPanel extends JPanel {
    private boolean mode = true;
    private final Timer animator;
    private BufferedImage icon;

    public MainPanel() {
        super(new BorderLayout());
        URL url = getClass().getResource("test.png");
        try{
            icon = ImageIO.read(url);
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }
        FadeImage fade = new FadeImage();
        animator = new Timer(25, fade);

        JButton button1 = new JButton(new AbstractAction("Open") {
            @Override public void actionPerformed(ActionEvent ae) {
                mode = true;
                animator.start();
            }
        });
        JButton button2 = new JButton(new AbstractAction("Close") {
            @Override public void actionPerformed(ActionEvent ae) {
                mode = false;
                animator.start();
            }
        });
        add(fade);
        add(button1, BorderLayout.SOUTH);
        add(button2, BorderLayout.NORTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
    }

    class FadeImage extends JComponent implements ActionListener {
        private int alpha = 10;
        public FadeImage() {
            super();
            setBackground(Color.BLACK);
        }
        @Override public void paintComponent(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            g2d.setPaint(getBackground());
            g2d.fillRect(0, 0, getWidth(), getHeight());
            if(mode && alpha<10) {
                alpha = alpha + 1;
            }else if(!mode && alpha>0) {
                alpha = alpha - 1;
            }else{
                animator.stop();
            }
            g2d.setComposite(makeAlphaComposite(alpha*0.1f));
            g2d.drawImage(icon, null, 0, 0);
        }
        @Override public void actionPerformed(ActionEvent e) {
            repaint();
        }
        private AlphaComposite makeAlphaComposite(float alpha) {
            return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
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
