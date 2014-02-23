package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    public MainPanel(final JFrame frame) {
        super(new GridLayout(2, 1));
        JPanel p1 = new JPanel();
        p1.setBorder(BorderFactory.createTitledBorder("JOptionPane"));
        p1.add(new JButton(new AbstractAction("JOptionPane.showMessageDialog") {
            @Override public void actionPerformed(ActionEvent evt) {
                JOptionPane.showMessageDialog(frame, "showMessageDialog");
            }
        }));
        JPanel p2 = new JPanel();
        p2.setBorder(BorderFactory.createTitledBorder("JDialog"));
        p2.add(new JButton(new AbstractAction("Default") {
            @Override public void actionPerformed(ActionEvent evt) {
                final JDialog dialog = new JDialog(frame, "title", true);
                AbstractAction act = new AbstractAction("OK") {
                    @Override public void actionPerformed(ActionEvent evt) {
                        dialog.dispose();
                    }
                };
                dialog.getContentPane().add(makePanel(act));
                dialog.pack();
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            }
        }));
        p2.add(new JButton(new AbstractAction("close JDialog with ESC key") {
            @Override public void actionPerformed(ActionEvent evt) {
                final JDialog dialog = new JDialog(frame, "title", true);
                AbstractAction act = new AbstractAction("OK") {
                    @Override public void actionPerformed(ActionEvent evt) {
                        dialog.dispose();
                    }
                };
                InputMap imap = dialog.getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
                imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close-it");
                dialog.getRootPane().getActionMap().put("close-it", act);
                dialog.getContentPane().add(makePanel(act));
                dialog.pack();
                dialog.setResizable(false);
                dialog.setLocationRelativeTo(frame);
                dialog.setVisible(true);
            }
        }));
        add(p1); add(p2);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setPreferredSize(new Dimension(320, 200));
    }

    private JPanel makePanel(Action act) {
        JPanel panel = new JPanel(new GridBagLayout()) {
            @Override public Dimension getPreferredSize() {
                return new Dimension(256, 64);
            }
        };
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridwidth  = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.BOTH;
        panel.add(new JLabel("i"), c);

        c.gridwidth  = 2;
        c.gridheight = 2;
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Message"), c);

        c.gridwidth  = 3;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        panel.add(new JButton(act), c);

        return panel;
    }

    public static void main(String[] args) {
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
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
