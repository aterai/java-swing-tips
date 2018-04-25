package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.net.*;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(4, 1, 5, 5));

        URL url = getClass().getResource("16x16.png");
        String path = url.toString();
        ImageIcon image = new ImageIcon(url);
        String title1 = String.format("<html><img src='%s' />test", path);
        String title2 = String.format("<html><table cellpadding='0'><tr><td><img src='%s'></td><td>test</td></tr></table></html>", path);
        JLabel label = new JLabel("test");
        label.setIcon(image);

        add(makeComponent(title1, BorderFactory.createTitledBorder(title1)));
        add(makeComponent(title2, BorderFactory.createTitledBorder(title2)));
        add(makeComponent("TitledBorder#paintBorder(...)", new TitledBorder("    test") {
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                super.paintBorder(c, g, x, y, width, height);
                g.drawImage(image.getImage(), 5, 0, c);
            }
        }));
        add(makeComponent("ComponentTitledBorder", new ComponentTitledBorder(label, UIManager.getBorder("TitledBorder.border"))));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeComponent(String str, Border border) {
        JLabel l = new JLabel();
        l.setBorder(border);
        l.putClientProperty("html.disable", Boolean.TRUE);
        l.setText(str);
        return l;
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

class ComponentTitledBorder implements Border, SwingConstants {
    private static final int OFFSET = 5;
    private final Component comp;
    private final Border border;

    protected ComponentTitledBorder(Component comp, Border border) {
        this.comp = comp;
        this.border = border;
        if (comp instanceof JComponent) {
            ((JComponent) comp).setOpaque(true);
        }
    }

    @Override public boolean isBorderOpaque() {
        return true;
    }

    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        if (c instanceof Container) {
            Insets borderInsets = border.getBorderInsets(c);
            Insets insets = getBorderInsets(c);
            int temp = (insets.top - borderInsets.top) / 2;
            border.paintBorder(c, g, x, y + temp, width, height - temp);
            Dimension size = comp.getPreferredSize();
            Rectangle rect = new Rectangle(OFFSET, 0, size.width, size.height);
            SwingUtilities.paintComponent(g, comp, (Container) c, rect);
            comp.setBounds(rect);
        }
    }
    @Override public Insets getBorderInsets(Component c) {
        Dimension size = comp.getPreferredSize();
        Insets insets = border.getBorderInsets(c);
        insets.top = Math.max(insets.top, size.height);
        return insets;
    }
}
