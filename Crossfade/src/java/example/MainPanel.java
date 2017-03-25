package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JCheckBox check = new JCheckBox("Crossfade Type?", true);
    private final Timer animator  = new Timer(50, null);
    private int alpha = 10;
    private Crossfade mode = Crossfade.In;
    private boolean crossfadeType = true;

    public MainPanel() {
        super(new BorderLayout());

        ImageIcon icon1 = new ImageIcon(getClass().getResource("test.png"));
        ImageIcon icon2 = new ImageIcon(getClass().getResource("test.jpg"));
        JComponent crossfade = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(getBackground());
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (crossfadeType) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f - alpha * .1f));
                }
                g2.drawImage(icon1.getImage(), 0, 0, icon1.getIconWidth(), icon1.getIconHeight(), this);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * .1f));
                g2.drawImage(icon2.getImage(), 0, 0, icon2.getIconWidth(), icon2.getIconHeight(), this);
                g2.dispose();
            }
        };

        animator.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                if (mode == Crossfade.In && alpha < 10) {
                    alpha += 1;
                } else if (mode == Crossfade.Out && alpha > 0) {
                    alpha -= 1;
                } else {
                    animator.stop();
                }
                crossfadeType = check.isSelected();
                crossfade.repaint();
            }
        });

        add(crossfade);
        add(new JButton(new AbstractAction("change") {
            @Override public void actionPerformed(ActionEvent e) {
                mode = mode.toggle();
                animator.start();
            }
        }), BorderLayout.NORTH);
        add(check,  BorderLayout.SOUTH);
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

enum Crossfade {
    In, Out;
    public Crossfade toggle() {
        return this.equals(In) ? Out : In;
    }
}
