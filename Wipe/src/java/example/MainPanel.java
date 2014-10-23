package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private boolean mode = true;
    private final Timer animator;
    private final ImageIcon icon;

    public MainPanel() {
        super(new BorderLayout());
        icon = new ImageIcon(getClass().getResource("test.png"));
        WipeImage wipe = new WipeImage();
        animator = new Timer(5, wipe);

        JButton button1 = new JButton(new AbstractAction("Wipe In") {
            @Override public void actionPerformed(ActionEvent ae) {
                mode = true;
                animator.start();
            }
        });
        JButton button2 = new JButton(new AbstractAction("Wipe Out") {
            @Override public void actionPerformed(ActionEvent ae) {
                mode = false;
                animator.start();
            }
        });
        add(wipe);
        add(button1, BorderLayout.SOUTH);
        add(button2, BorderLayout.NORTH);
        setOpaque(false);
        setPreferredSize(new Dimension(320, 240));
        animator.start();
    }

    class WipeImage extends JComponent implements ActionListener {
        private int ww;
        public WipeImage() {
            super();
            setBackground(Color.BLACK);
        }
        @Override public void paintComponent(Graphics g) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
            if (mode) {
                if (ww < icon.getIconWidth()) {
                    ww += 10;
                } else {
                    animator.stop();
                }
            } else {
                if (ww > 0) {
                    ww -= 10;
                } else {
                    animator.stop();
                }
            }
            g.drawImage(icon.getImage(), 0, 0, (int) (icon.getIconWidth()), (int) (icon.getIconHeight()), this);
            g.fillRect(ww, 0, (int) (icon.getIconWidth()), (int) (icon.getIconHeight()));
        }
        @Override public void actionPerformed(ActionEvent e) {
            repaint();
        }
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
        } catch (ClassNotFoundException | InstantiationException |
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
