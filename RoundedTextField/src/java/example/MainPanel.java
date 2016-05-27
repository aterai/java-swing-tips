package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.geom.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        JTextField textField01 = new JTextField(20) {
            //Unleash Your Creativity with Swing and the Java 2D API!
            //http://java.sun.com/products/jfc/tsc/articles/swing2d/index.html
            @Override protected void paintComponent(Graphics g) {
                if (!isOpaque()) {
                    int w = getWidth();
                    int h = getHeight();
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setPaint(UIManager.getColor("TextField.background"));
                    g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
                    g2.setPaint(Color.GRAY);
                    g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        textField01.setOpaque(false);
        textField01.setBackground(new Color(0x0, true));
        textField01.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        textField01.setText("aaaaaaaaaaa");

        JTextField textField02 = new JTextField(20);
        textField02.setBorder(new RoundedCornerBorder());
        textField02.setText("bbbbbbbbbbbb");

        JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
        p.add(makeTitlePanel(textField01, "Override: JTextField#paintComponent(...)"));
        p.add(makeTitlePanel(textField02, "setBorder(new RoundedCornerBorder())"));
        add(p);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1d;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
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

class RoundedCornerBorder extends AbstractBorder {
    @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int r = height - 1;
        Shape border = new RoundRectangle2D.Double(x, y, width - 1, height - 1, r, r);
        Container parent = c.getParent();
        if (Objects.nonNull(parent)) {
            g2.setPaint(parent.getBackground());
            Area corner = new Area(new Rectangle2D.Double(x, y, width, height));
            corner.subtract(new Area(border));
            g2.fill(corner);
        }
        g2.setPaint(Color.GRAY);
        g2.draw(border);
        g2.dispose();
    }
    @Override public Insets getBorderInsets(Component c) {
        return new Insets(4, 8, 4, 8);
    }
    @Override public Insets getBorderInsets(Component c, Insets insets) {
        insets.set(4, 8, 4, 8);
        return insets;
    }
}
