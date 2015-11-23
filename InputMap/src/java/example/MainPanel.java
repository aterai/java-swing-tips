package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1));
        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
        p1.add(new JButton(new AbstractAction("JOptionPane.showMessageDialog") {
            @Override public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(getRootPane(), "showMessageDialog");
            }
        }));
        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("JDialog"));
        p2.add(new JButton(new AbstractAction("Default") {
            @Override public void actionPerformed(ActionEvent e) {
                final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(getRootPane()), "title", true);
                Action act = new AbstractAction("OK") {
                    @Override public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                };
                dialog.getContentPane().add(makePanel(act));
                dialog.pack();
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(getRootPane());
                dialog.setVisible(true);
            }
        }));
        p2.add(new JButton(new AbstractAction("close JDialog with ESC key") {
            @Override public void actionPerformed(ActionEvent e) {
                final JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(getRootPane()), "title", true);
                Action act = new AbstractAction("OK") {
                    @Override public void actionPerformed(ActionEvent e) {
                        dialog.dispose();
                    }
                };
                InputMap imap = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
                dialog.getRootPane().getActionMap().put("close-it", act);
                dialog.getContentPane().add(makePanel(act));
                dialog.pack();
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(getRootPane());
                dialog.setVisible(true);
            }
        }));
        add(p1);
        add(p2);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setPreferredSize(new Dimension(320, 240));
    }

    private JPanel makePanel(Action act) {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                d.width = Math.max(240, d.width);
                return d;
            }
        };
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 10, 5, 10);
        c.anchor = GridBagConstraints.LINE_START;
        p.add(new JLabel(new DummyIcon()), c);

        c.insets = new Insets(5, 0, 5, 0);
        //p.add(new JLabel("<html>Message<br>aaaaaa<br>aaaaaaaaaaa<br>aaaaaaaaaaaaaaaa"), c);
        p.add(new JLabel("Message"), c);

        c.gridwidth = 2;
        c.gridy = 1;
        c.weightx = 1d;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(new JButton(act), c);

        return p;
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

class DummyIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.RED);
        g2.translate(x, y);
        g2.fillOval(0, 0, 32, 32);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 32;
    }
    @Override public int getIconHeight() {
        return 32;
    }
}
