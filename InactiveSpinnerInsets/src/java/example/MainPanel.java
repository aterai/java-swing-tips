package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout(5, 5));
        //UIManager.put("FormattedTextField.inactiveBackground", Color.RED);
        JSpinner spinner0 = new JSpinner();

        JSpinner spinner1 = new JSpinner();
        JSpinner.DefaultEditor editor1 = (JSpinner.DefaultEditor) spinner1.getEditor();
        editor1.setOpaque(false);
        JTextField field1 = editor1.getTextField();
        field1.setOpaque(false);

//         JSpinner s2 = new JSpinner();
//         s2.setBorder(BorderFactory.createCompoundBorder(
//             BorderFactory.createLineBorder(new Color(127, 157, 185)),
//             BorderFactory.createLineBorder(
//                 UIManager.getColor("FormattedTextField.inactiveBackground"), 2)));

        JSpinner spinner2 = new JSpinner();
        spinner2.setBorder(BorderFactory.createEmptyBorder());
        JSpinner.DefaultEditor editor2 = (JSpinner.DefaultEditor) spinner2.getEditor();
        editor2.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(127, 157, 185)));
        JTextField field2 = editor2.getTextField();
        field2.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 0));

        JSpinner spinner3 = new SimpleBorderSpinner();

        final List<JSpinner> list = Arrays.asList(spinner0, spinner1, spinner2, spinner3);
        Box box = Box.createVerticalBox();
        for (JSpinner s: list) {
            s.setEnabled(false);
        }

        addTestSpinner(box, spinner0, "Default");
        addTestSpinner(box, spinner1, "setOpaque(false)");
        addTestSpinner(box, spinner2, "setBorder(...)");
        addTestSpinner(box, spinner3, "paintComponent, paintChildren");

        setBorder(BorderFactory.createEmptyBorder(2, 20, 2, 20));
        add(box, BorderLayout.NORTH);
        add(new JCheckBox(new AbstractAction("setEnabled") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox cb = (JCheckBox) e.getSource();
                for (JSpinner s: list) {
                    s.setEnabled(cb.isSelected());
                }
            }
        }), BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    private static void addTestSpinner(Box box, JSpinner spinner, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.add(spinner);
        p.setBorder(BorderFactory.createTitledBorder(title));
        box.add(p);
        box.add(Box.createVerticalStrut(2));
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
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class SimpleBorderSpinner extends JSpinner {
    @Override protected void paintComponent(Graphics g) {
        if (getUI() instanceof com.sun.java.swing.plaf.windows.WindowsSpinnerUI) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setPaint(isEnabled() ? UIManager.getColor("FormattedTextField.background")
                                     : UIManager.getColor("FormattedTextField.inactiveBackground"));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.dispose();
        }
    }
    @Override protected void paintChildren(Graphics g) {
        super.paintChildren(g);
        if (!isEnabled() && getUI() instanceof com.sun.java.swing.plaf.windows.WindowsSpinnerUI) {
            Graphics2D g2d = (Graphics2D) g.create();
            Rectangle r = getComponent(0).getBounds();
            r.add(getComponent(1).getBounds());
            r.width--; r.height--;
            g2d.setPaint(UIManager.getColor("FormattedTextField.inactiveBackground"));
            g2d.draw(r);
            g2d.dispose();
        }
    }
}
