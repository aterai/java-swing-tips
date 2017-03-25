package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import java.text.ParseException;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private final JRadioButton exMi = new JRadioButton(FontPaint.IPAexMincho.toString());
    private final JRadioButton mjMi = new JRadioButton(FontPaint.IPAmjMincho.toString());
    private final JRadioButton both = new JRadioButton("Both", true);
    private final JSpinner spinner  = new JSpinner(new SpinnerNumberModel(0x51DE, 0x0, 0x10FFFF, 1));
    protected EnumSet<FontPaint> fontPaintFlag = EnumSet.allOf(FontPaint.class);

    public MainPanel() {
        super(new BorderLayout());

        JPanel fontPanel = new GlyphPaintPanel();

        spinner.addChangeListener(e -> fontPanel.repaint());
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
        JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
        ftf.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ftf.getFont().getSize()));
        ftf.setFormatterFactory(makeFFactory());

        JPanel p = new JPanel();
        ButtonGroup bg = new ButtonGroup();
        ActionListener al = e -> {
            if (exMi.isSelected()) {
                fontPaintFlag = EnumSet.of(FontPaint.IPAexMincho);
            } else if (mjMi.isSelected()) {
                fontPaintFlag = EnumSet.of(FontPaint.IPAmjMincho);
            } else { //if (both.isSelected()) {
                fontPaintFlag = EnumSet.allOf(FontPaint.class);
            }
            fontPanel.repaint();
        };
        Arrays.asList(exMi, mjMi, both).forEach(b -> {
            p.add(b);
            bg.add(b);
            b.addActionListener(al);
        });

        add(spinner, BorderLayout.NORTH);
        add(fontPanel);
        add(p, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }
    protected String getCharacterString() {
        int code = ((Integer) spinner.getValue()).intValue();
        //char[] ca = Character.toChars(code);
        //int len = Character.charCount(code);
        //http://docs.oracle.com/javase/tutorial/i18n/text/usage.html
        return new String(Character.toChars(code)); //, 0, len);
//         if (code < 0x10000) {
//             str = Character.toString((char) code);
//         } else {
//             int x = code - 0x10000;
//             char[] ca = new char[2];
//             ca[0] = (char) (Math.floor(x / 0x400) + 0xD800);
//             ca[1] = (char) (x % 0x400 + 0xDC00);
//             str = new String(ca, 0, 2);
//         }
    }
    private class GlyphPaintPanel extends JPanel {
        private final Font ipaEx = new Font("IPAexMincho", Font.PLAIN, 200);
        private final Font ipaMj = new Font("IPAmjMincho", Font.PLAIN, 200);
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());

            String str = getCharacterString();

            FontRenderContext frc = g2.getFontRenderContext();
            Shape exShape = new TextLayout(str, ipaEx, frc).getOutline(null);
            Shape mjShape = new TextLayout(str, ipaMj, frc).getOutline(null);

            Rectangle2D b = exShape.getBounds2D();
            AffineTransform toCenterAT = AffineTransform.getTranslateInstance(getWidth() / 2d - b.getCenterX(), getHeight() / 2d - b.getCenterY());

            g2.setPaint(Color.YELLOW);
            g2.draw(toCenterAT.createTransformedShape(b));

            Shape s1 = toCenterAT.createTransformedShape(exShape);
            Shape s2 = toCenterAT.createTransformedShape(mjShape);

            if (fontPaintFlag.contains(FontPaint.IPAexMincho)) {
                g2.setPaint(Color.CYAN);
                g2.fill(s1);
            }
            if (fontPaintFlag.contains(FontPaint.IPAmjMincho)) {
                g2.setPaint(Color.MAGENTA);
                g2.fill(s2);
            }
            if (fontPaintFlag.containsAll(EnumSet.allOf(FontPaint.class))) {
                g2.setClip(s1);
                g2.setPaint(Color.BLACK);
                g2.fill(s2);
            }
            g2.dispose();
        }
    }
    private static DefaultFormatterFactory makeFFactory() {
        DefaultFormatter formatter = new DefaultFormatter() {
            @Override public Object stringToValue(String text) throws ParseException {
                return Integer.valueOf(text, 16);
//                 try {
//                     return Integer.valueOf(text, 16);
//                 } catch (NumberFormatException ex) {
//                     throw new ParseException(text, 0);
//                 }
            }
            //private static final String MASK = "000000";
            @Override public String valueToString(Object value) throws ParseException {
                //String str = MASK + Integer.toHexString((Integer) value).toUpperCase(Locale.ENGLISH);
                //int i = str.length() - MASK.length();
                //return str.substring(i);
                //String s = Integer.toHexString((Integer) value);
                //return String.format("%6S", s).replaceAll(" ", "0");
                return String.format("%06X", (Integer) value);
            }
        };
        formatter.setValueClass(Integer.class);
        formatter.setOverwriteMode(true);
        return new DefaultFormatterFactory(formatter);
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

enum FontPaint { IPAexMincho, IPAmjMincho }
