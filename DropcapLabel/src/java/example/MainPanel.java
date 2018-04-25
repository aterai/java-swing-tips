package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String TEXT = "This lesson provides an introduction to"
        + " Graphical User Interface (GUI) programming with Swing and the NetBeans IDE."
        + " As you learned in the \"Hello World!\" lesson, the NetBeans IDE is a free, open-source, cross-platform integrated"
        + " development environment with built-in support for the Java programming language.";

    private MainPanel() {
        super(new BorderLayout());
        JLabel label = new DropcapLabel(TEXT);
        label.setFont(new Font(Font.SERIF, Font.PLAIN, 17));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(label);
        setBorder(BorderFactory.createLineBorder(new Color(100, 200, 200, 100), 10));
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

class DropcapLabel extends JLabel {
    protected DropcapLabel(String text) {
        super(text);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        Insets i = getInsets();
        float x0 = i.left;
        float y0 = i.top;

        Font font = getFont();
        String txt = getText();

        FontRenderContext frc = g2.getFontRenderContext();
        Shape shape = new TextLayout(txt.substring(0, 1), font, frc).getOutline(null);

        AffineTransform at1 = AffineTransform.getScaleInstance(5d, 5d);
        Shape s1 = at1.createTransformedShape(shape);
        Rectangle r = s1.getBounds();
        r.grow(6, 2);
        int rw = r.width;
        int rh = r.height;

        AffineTransform at2 = AffineTransform.getTranslateInstance(x0, y0 + rh);
        Shape s2 = at2.createTransformedShape(s1);
        g2.setPaint(getForeground());
        g2.fill(s2);

        float x = x0 + rw;
        float y = y0;
        int w0 = getWidth() - i.left - i.right;
        int w = w0 - rw;

        AttributedString as = new AttributedString(txt.substring(1));
        as.addAttribute(TextAttribute.FONT, font);
        AttributedCharacterIterator aci = as.getIterator();
        LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
        while (lbm.getPosition() < aci.getEndIndex()) {
            TextLayout tl = lbm.nextLayout(w);
            tl.draw(g2, x, y + tl.getAscent());
            y += tl.getDescent() + tl.getLeading() + tl.getAscent();
            if (y0 + rh < y) {
                x = x0;
                w = w0;
            }
        }
        g2.dispose();
    }
}
