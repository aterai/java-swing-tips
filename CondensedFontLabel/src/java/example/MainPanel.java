package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
    private static final String TEXT = "1234567890 ABCDEFG HIJKLMN OPQRSTU VWXYZ";
    private final JTextArea textArea = new JTextArea(TEXT) {
        @Override public void updateUI() {
            super.updateUI();
            setEditable(false);
            setLineWrap(true);
            setWrapStyleWord(true);
            setOpaque(false);
            setBackground(new Color(0x0, true));
        }
    };
    private final JLabel lbl1 = new WrappedLabel(TEXT);
    private final JLabel lbl2 = new WrappingLabel(TEXT);

    public MainPanel() {
        super(new GridLayout(0, 1));

        Border b = BorderFactory.createLineBorder(Color.GRAY,  5);
        textArea.setBorder(BorderFactory.createTitledBorder(b, "JTextArea(condensed: 0.9)"));
        lbl1.setBorder(BorderFactory.createTitledBorder(b, "GlyphVector(condensed: 0.9)"));
        lbl2.setBorder(BorderFactory.createTitledBorder(b, "LineBreakMeasurer(condensed: 0.9)"));

        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18).deriveFont(AffineTransform.getScaleInstance(.9, 1d));
//         //TEST:
//         Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);
//         if (font.isTransformed()) {
//             font = font.deriveFont(AffineTransform.getScaleInstance(.9, 1d));
//         }
        for (JComponent c: Arrays.asList(textArea, lbl1, lbl2)) {
            c.setFont(font);
            add(c);
        }
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
    //TEST: private AffineTransform at = AffineTransform.getScaleInstance(.9, 1d);
    protected WrappingLabel(String text) {
        super(text);
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getForeground());
        g2.setFont(getFont());
        Insets i = getInsets();
        float x = i.left;
        float y = i.top;
        int w = getWidth() - i.left - i.right;

        AttributedString as = new AttributedString(getText());
        as.addAttribute(TextAttribute.FONT, getFont()); //TEST: .deriveFont(at));
        //TEST: as.addAttribute(TextAttribute.TRANSFORM, at);
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
    //TEST: private AffineTransform at = AffineTransform.getScaleInstance(.9, 1d);

    protected WrappedLabel(String str) {
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
        if (Objects.nonNull(gvtext)) {
            Insets i = getInsets();
            Graphics2D g2 = (Graphics2D) g.create();
            g2.drawGlyphVector(gvtext, i.left, getFont().getSize() + i.top);
            g2.dispose();
        } else {
            super.paintComponent(g);
        }
    }
    private GlyphVector getWrappedGlyphVector(String str, double width, Font font, FontRenderContext frc) {
        Point2D gmPos    = new Point2D.Float();
        GlyphVector gv   = font.createGlyphVector(frc, str);
        float lineheight = (float) gv.getLogicalBounds().getHeight();
        float xpos       = 0f;
        float advance    = 0f;
        int   lineCount  = 0;
        GlyphMetrics gm;

        for (int i = 0; i < gv.getNumGlyphs(); i++) {
            //TEST: gv.setGlyphTransform(i, at);
            gm = gv.getGlyphMetrics(i);
            advance = gm.getAdvance();
            if (xpos < width && width <= xpos + advance) {
                lineCount++;
                xpos = 0f;
            }
            gmPos.setLocation(xpos, lineheight * lineCount);
            gv.setGlyphPosition(i, gmPos);
            xpos += advance;
        }
        return gv;
    }
}
