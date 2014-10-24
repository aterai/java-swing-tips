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
    private static final String TEXT = "あいうえお かきくけこ さしすせそ たちつてと なにぬねの はひふへほ まみむめも";
    private final JLabel    lbl1 = new JLabel(TEXT);
    private final JLabel    lbl2 = new WrappedLabel(TEXT);
    private final JLabel    lbl3 = new WrappingLabel(TEXT);
    private final JTextArea lbl4 = new JTextArea(TEXT);
    public MainPanel() {
        super(new GridLayout(4, 1, 0, 0));
        lbl1.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.YELLOW, 5), "JLabel"));
        lbl2.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GREEN,  5), "GlyphVector"));
        lbl3.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.CYAN,   5), "LineBreakMeasurer"));
        lbl4.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.ORANGE, 5), "JTextArea"));

        //lbl2.setFont(new Font(Font.SERIF, Font.TRUETYPE_FONT, 20));
        lbl4.setFont(lbl1.getFont());
        lbl4.setEditable(false);
        lbl4.setLineWrap(true);
        lbl4.setBackground(lbl1.getBackground());

        add(lbl1);
        add(lbl2);
        add(lbl3);
        add(lbl4);
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

class WrappingLabel extends JLabel {
    public WrappingLabel(String text) {
        super(text);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(getForeground());
        Insets i = getInsets();
        float x = i.left;
        float y = i.top;
        int w = getWidth() - i.left - i.right;
        AttributedString as = new AttributedString(getText());
        as.addAttribute(TextAttribute.FONT, getFont());
        AttributedCharacterIterator aci = as.getIterator();
        FontRenderContext frc = g2.getFontRenderContext();
        LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
        while (lbm.getPosition() < aci.getEndIndex()) {
            TextLayout tl = lbm.nextLayout(w);
            tl.draw(g2, x, y + tl.getAscent());
            y += tl.getDescent() + tl.getLeading() + tl.getAscent();
        }
        g2.dispose();
    }
}

class WrappedLabel extends JLabel {
    private GlyphVector gvtext;
    private int width = -1;

    public WrappedLabel() {
        this(null);
    }
    public WrappedLabel(String str) {
        super(str);
    }
    @Override public void doLayout() {
        Insets i = getInsets();
        int w = getWidth() - i.left - i.right;
        if (w != width) {
            Font font = getFont();
            FontMetrics fm = getFontMetrics(font);
            FontRenderContext frc = fm.getFontRenderContext();
            gvtext = getWrappedGlyphVector(getText(), w, font, frc);
            width = w;
        }
        super.doLayout();
    }
    @Override protected void paintComponent(Graphics g) {
        if (gvtext == null) {
            super.paintComponent(g);
        } else {
            Insets i = getInsets();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(Color.RED);
            g2.drawGlyphVector(gvtext, i.left, getFont().getSize() + i.top);
            g2.dispose();
        }
    }
    private GlyphVector getWrappedGlyphVector(String str, float width, Font font, FontRenderContext frc) {
        Point2D gmPos    = new Point2D.Double(0.0d, 0.0d);
        GlyphVector gv   = font.createGlyphVector(frc, str);
        float lineheight = (float) (gv.getLogicalBounds().getHeight());
        float xpos       = 0f;
        float advance    = 0f;
        int   lineCount  = 0;
        GlyphMetrics gm;
        for (int i = 0; i < gv.getNumGlyphs(); i++) {
            gm = gv.getGlyphMetrics(i);
            advance = gm.getAdvance();
            if (xpos < width && width <= xpos + advance) {
                lineCount++;
                xpos = 0f;
            }
            gmPos.setLocation(xpos, lineheight * lineCount);
            gv.setGlyphPosition(i, gmPos);
            xpos = xpos + advance;
        }
        return gv;
    }
}
