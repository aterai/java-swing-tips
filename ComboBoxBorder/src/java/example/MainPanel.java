package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        UIManager.put("ComboBox.foreground", Color.WHITE);
        UIManager.put("ComboBox.background", Color.BLACK);
        UIManager.put("ComboBox.selectionForeground", Color.CYAN);
        UIManager.put("ComboBox.selectionBackground", Color.BLACK);

        UIManager.put("ComboBox.buttonDarkShadow", Color.BLACK);
        UIManager.put("ComboBox.buttonBackground", Color.WHITE);
        UIManager.put("ComboBox.buttonHighlight", Color.WHITE);
        UIManager.put("ComboBox.buttonShadow", Color.WHITE);

        UIManager.put("ComboBox.border", BorderFactory.createLineBorder(Color.WHITE));
        UIManager.put("ComboBox.editorBorder", BorderFactory.createLineBorder(Color.GREEN));

        UIManager.put("TitledBorder.titleColor", Color.WHITE);
        UIManager.put("TitledBorder.border", BorderFactory.createEmptyBorder());

        JComboBox<String> combo00 = makeComboBox();
        JComboBox<String> combo01 = makeComboBox();
        JComboBox<String> combo02 = makeComboBox();

        combo01.setUI(new BasicComboBoxUI());
        combo02.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton(new ArrowIcon()); // .createArrowButton();
                b.setBackground(Color.BLACK);
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                b.setBorder(BorderFactory.createEmptyBorder());
                return b;
            }
        });

        combo02.addMouseListener(new MouseAdapter() {
            private ButtonModel getButtonModel(MouseEvent e) {
                JComboBox cb = (JComboBox) e.getComponent();
                JButton b = (JButton) cb.getComponent(0);
                return b.getModel();
            }
            @Override public void mouseEntered(MouseEvent e) {
                getButtonModel(e).setRollover(true);
            }
            @Override public void mouseExited(MouseEvent e) {
                getButtonModel(e).setRollover(false);
            }
            @Override public void mousePressed(MouseEvent e) {
                getButtonModel(e).setPressed(true);
            }
            @Override public void mouseReleased(MouseEvent e) {
                getButtonModel(e).setPressed(false);
            }
        });

        Object o = combo00.getAccessibleContext().getAccessibleChild(0);
        ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.WHITE));
        o = combo01.getAccessibleContext().getAccessibleChild(0);
        ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.WHITE));
        o = combo02.getAccessibleContext().getAccessibleChild(0);
        ((JComponent) o).setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1, Color.WHITE));

        Box box = Box.createVerticalBox();
        box.add(makeTitledPanel("MetalComboBoxUI:", combo00));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("BasicComboBoxUI:", combo01));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("BasicComboBoxUI#createArrowButton():", combo02));
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(box, BorderLayout.NORTH);
        setOpaque(true);
        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        p.setOpaque(true);
        p.setBackground(Color.BLACK);
        return p;
    }
    private static JComboBox<String> makeComboBox() {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
        model.addElement("1234");
        model.addElement("5555555555555555555555");
        model.addElement("6789000000000");
        return new JComboBox<>(model);
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ArrowIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(Color.WHITE);
        int shift = 0;
        if (c instanceof AbstractButton) {
            ButtonModel m = ((AbstractButton) c).getModel();
            if (m.isPressed()) {
                shift = 1;
            } else {
                if (m.isRollover()) {
                    g2.setPaint(Color.WHITE);
                } else {
                    g2.setPaint(Color.BLACK);
                }
            }
        }
        g2.translate(x, y + shift);
        g2.drawLine(2, 3, 6, 3);
        g2.drawLine(3, 4, 5, 4);
        g2.drawLine(4, 5, 4, 5);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 9;
    }
    @Override public int getIconHeight() {
        return 9;
    }
}
