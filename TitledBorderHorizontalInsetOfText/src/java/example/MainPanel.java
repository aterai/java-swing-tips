package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
// import java.awt.geom.Path2D;
// import java.util.Objects;
import javax.swing.*;
import javax.swing.border.*;
// import javax.swing.plaf.basic.BasicHTML;

public final class MainPanel extends JPanel {
    private static final String TITLE = "TitledBorder Test";
    private MainPanel() {
        super(new GridLayout(0, 1, 5, 5));
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        add(makeComp("override TitledBorder#paintBorder(...)", new TitledBorder(TITLE + "1") {
            @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                super.paintBorder(c, g, x + 10, y, width, height);
            }
        }));
        add(makeComp("override TitledBorder#getBorderInsets(...)", new TitledBorder(TITLE + "2") {
            @Override public Insets getBorderInsets(Component c, Insets insets) {
                Insets i = super.getBorderInsets(c, insets);
                i.left += 10;
                return i;
            }
        }));

        JLabel label = new JLabel(TITLE + "3", null, SwingConstants.LEFT);
        label.setBorder(new EmptyBorder(0, 5, 0, 5));
        add(makeComp("ComponentTitledBorder + EmptyBorder", new ComponentTitledBorder(label, UIManager.getBorder("TitledBorder.border"))));

        add(makeComp("TitledBorder2: copied from TitledBorder", new TitledBorder2(TITLE + "4")));

        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeComp(String str, Border border) {
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

// @see http://www.jroller.com/santhosh/entry/component_titled_border
// @see http://ateraimemo.com/Swing/ComponentTitledBorder.html
class ComponentTitledBorder implements Border, SwingConstants {
    protected static final int OFFSET = 10;
    protected final Component comp;
    protected final Border border;

    protected ComponentTitledBorder(Component comp, Border border) {
        this.comp   = comp;
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
            int v = Math.max(0, (insets.top - borderInsets.top) / 2);
            border.paintBorder(c, g, x, y + v, width, height - v);
            Dimension size = comp.getPreferredSize();
            Rectangle rect = new Rectangle(OFFSET, 0, size.width, size.height);
            comp.setBounds(rect);
            SwingUtilities.paintComponent(g, comp, (Container) c, rect);
        }
    }
    @Override public Insets getBorderInsets(Component c) {
        Dimension size = comp.getPreferredSize();
        Insets insets = border.getBorderInsets(c);
        insets.top = Math.max(insets.top, size.height);
        return insets;
    }
}
