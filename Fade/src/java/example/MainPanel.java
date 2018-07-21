package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

public class MainPanel extends JPanel {
    protected Fade mode = Fade.In;
    protected final Timer animator = new Timer(25, null);
    protected int alpha = 10;
    protected transient BufferedImage icon;

    public MainPanel() {
        super(new BorderLayout());

        try {
            icon = ImageIO.read(MainPanel.class.getResource("test.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        Component fade = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * .1f));
                g2.drawImage(icon, null, 0, 0);
                g2.dispose();
            }
        };

        animator.addActionListener(e -> {
            if (mode == Fade.In && alpha < 10) {
                alpha += 1;
            } else if (mode == Fade.Out && alpha > 0) {
                alpha -= 1;
            } else {
                animator.stop();
            }
            fade.repaint();
        });

        JButton button1 = new JButton("Fade In");
        button1.addActionListener(e -> {
            mode = Fade.In;
            animator.start();
        });

        JButton button2 = new JButton("Fade Out");
        button2.addActionListener(e -> {
            mode = Fade.Out;
            animator.start();
        });

        add(fade);
        add(button1, BorderLayout.SOUTH);
        add(button2, BorderLayout.NORTH);
        setOpaque(false);
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

enum Fade { In, Out }
