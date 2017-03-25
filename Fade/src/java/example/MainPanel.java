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

public final class MainPanel extends JPanel {
    private Fade mode = Fade.In;
    private final Timer animator = new Timer(25, null);
    private int alpha = 10;
    private transient BufferedImage icon;
    private final JButton button1 = new JButton(new AbstractAction("Fade In") {
        @Override public void actionPerformed(ActionEvent e) {
            mode = Fade.In;
            animator.start();
        }
    });
    private final JButton button2 = new JButton(new AbstractAction("Fade Out") {
        @Override public void actionPerformed(ActionEvent e) {
            mode = Fade.Out;
            animator.start();
        }
    });

    public MainPanel() {
        super(new BorderLayout());

        try {
            icon = ImageIO.read(getClass().getResource("test.png"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        JComponent fade = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(Color.BLACK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * .1f));
                g2.drawImage(icon, null, 0, 0);
                g2.dispose();
            }
        };

        animator.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (mode == Fade.In && alpha < 10) {
                    alpha += 1;
                } else if (mode == Fade.Out && alpha > 0) {
                    alpha -= 1;
                } else {
                    animator.stop();
                }
                fade.repaint();
            }
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

enum Fade { In, Out }
