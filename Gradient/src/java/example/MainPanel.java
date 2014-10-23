package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JPanel p = new JPanel(new GridLayout(2, 1));
        p.add(makeTestPanel("JSeparator", new JSeparator()));
        p.add(makeTestPanel("GradientSeparator", new GradientSeparator()));

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        box.add(Box.createHorizontalStrut(10));
        box.add(new JSeparator(JSeparator.VERTICAL));
        box.add(Box.createHorizontalStrut(10));
        box.add(new GradientSeparator(JSeparator.VERTICAL));
        box.add(Box.createHorizontalStrut(10));

        add(p);
        add(box, BorderLayout.EAST);
        setPreferredSize(new Dimension(320, 240));
    }

    private static JPanel makeTestPanel(String title, JSeparator sp) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridheight = 1;

        c.gridwidth = 3;
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(makeTitlePanel(title, sp), c);

        c.gridwidth = 1;
        c.gridx = 2;
        c.gridy = 1;
        c.weightx = 0.0;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.NONE;
        p.add(new JButton("dummy"), c);

        c.insets = new Insets(2, 10, 2, 2);
        c.gridwidth = 2;
        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.EAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(new JTextField(), c);

        return p;
    }

    private static JPanel makeTitlePanel(String title, JSeparator separator) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(2, 2, 2, 2);
        c.gridheight = 1;
        c.gridy = 0;

        c.gridwidth = 1;
        c.gridx = 0;
        p.add(new JLabel(title), c);

        c.gridwidth = 2;
        c.gridx = 1;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        p.add(separator, c);

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

class GradientSeparator extends JSeparator {
    public GradientSeparator() {
        super();
    }
    public GradientSeparator(int orientation) {
        super(orientation);
    }
    @Override public void updateUI() {
        super.updateUI();
        setUI(GradientSeparatorUI.createUI(this));
    }
}

class GradientSeparatorUI extends BasicSeparatorUI {
    private Color bgc, ssc, shc;

    public static ComponentUI createUI(JComponent c) {
        return new GradientSeparatorUI();
    }
    private void updateColors(JComponent j) {
        Color c = UIManager.getColor("Panel.background");
        bgc = c instanceof ColorUIResource ? c : j.getBackground();
        c = UIManager.getColor("Separator.shadow");
        ssc = c instanceof ColorUIResource ? c : j.getBackground().brighter();
        c = UIManager.getColor("Separator.highlight");
        shc = c instanceof ColorUIResource ? c : j.getBackground().darker();
    }
    @Override public void installUI(JComponent c) {
        super.installUI(c);
        updateColors(c);
    }
    @Override public void paint(Graphics g, JComponent c) {
        if (c instanceof JSeparator) {
            Graphics2D g2 = (Graphics2D) g.create();
            Dimension s = c.getSize();
            JSeparator js = (JSeparator) c;
            if (js.getOrientation() == JSeparator.VERTICAL) {
                g2.setPaint(new GradientPaint(0, 0, ssc, 0, s.height, bgc, true));
                g2.fillRect(0, 0, 1, s.height);
                g2.setPaint(new GradientPaint(0, 0, shc, 0, s.height, bgc, true));
                g2.fillRect(1, 0, 1, s.height);
            } else {
                g2.setPaint(new GradientPaint(0, 0, ssc, s.width, 0, bgc, true));
                g2.fillRect(0, 0, s.width, 1);
                g2.setPaint(new GradientPaint(0, 0, shc, s.width, 0, bgc, true));
                g2.fillRect(0, 1, s.width, 1);
            }
            g2.dispose();
        }
    }
}
