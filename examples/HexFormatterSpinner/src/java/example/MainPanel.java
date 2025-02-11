// -*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.text.ParseException;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;

public final class MainPanel extends JPanel {
  // Character.MIN_CODE_POINT: 0x0, Character.MAX_CODE_POINT: 0x10FFFF
  private final SpinnerNumberModel numberModel = new SpinnerNumberModel(
      0x51DE, 0x0, Character.MAX_CODE_POINT, 1);

  private MainPanel() {
    super(new BorderLayout());
    GlyphPaintPanel fontPanel = new GlyphPaintPanel();
    numberModel.addChangeListener(e -> fontPanel.repaint());
    JSpinner spinner = new JSpinner(numberModel);
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
    JFormattedTextField ftf = editor.getTextField();
    ftf.setFont(new Font(Font.MONOSPACED, Font.PLAIN, ftf.getFont().getSize()));
    ftf.setFormatterFactory(makeFormatterFactory());

    JRadioButton exMi = new JRadioButton(FontPaint.IPA_EX_MINCHO.toString());
    exMi.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setFontPaintFlag(fontPanel, EnumSet.of(FontPaint.IPA_EX_MINCHO));
      }
    });

    JRadioButton mjMi = new JRadioButton(FontPaint.IPA_MJ_MINCHO.toString());
    mjMi.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setFontPaintFlag(fontPanel, EnumSet.of(FontPaint.IPA_MJ_MINCHO));
      }
    });

    JRadioButton both = new JRadioButton("Both", true);
    both.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        setFontPaintFlag(fontPanel, EnumSet.allOf(FontPaint.class));
      }
    });

    JPanel p = new JPanel();
    ButtonGroup bg = new ButtonGroup();
    Stream.of(exMi, mjMi, both).forEach(b -> {
      p.add(b);
      bg.add(b);
    });

    add(spinner, BorderLayout.NORTH);
    add(fontPanel);
    add(p, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void setFontPaintFlag(GlyphPaintPanel p, Set<FontPaint> fp) {
    p.setFontPaintFlag(fp);
    p.repaint();
  }

  public String getCharacterString() {
    int code = numberModel.getNumber().intValue();
    // char[] ca = Character.toChars(code);
    // int len = Character.charCount(code);
    // https://docs.oracle.com/javase/tutorial/i18n/text/usage.html
    return String.valueOf(Character.toChars(code)); // , 0, len);
    // if (code < 0x10000) {
    //   str = Character.toString((char) code);
    // } else {
    //   int x = code - 0x10000;
    //   char[] ca = new char[2];
    //   ca[0] = (char) (Math.floor(x / 0x400) + 0xD800);
    //   ca[1] = (char) (x % 0x400 + 0xDC00);
    //   str = String.valueOf(ca, 0, 2);
    // }
  }

  private enum FontPaint {
    IPA_EX_MINCHO, IPA_MJ_MINCHO
  }

  private final class GlyphPaintPanel extends JPanel {
    private Set<FontPaint> fontPaintFlag = EnumSet.allOf(FontPaint.class);
    private final Font ipaEx = new Font("IPAexMincho", Font.PLAIN, 200);
    private final Font ipaMj = new Font("IPAmjMincho", Font.PLAIN, 200);

    public void setFontPaintFlag(Set<FontPaint> flg) {
      fontPaintFlag = flg;
    }

    // public Set<FontPaint> getFontPaintFlag() {
    //   return fontPaintFlag;
    // }

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
      double cx = getWidth() / 2d - b.getCenterX();
      double cy = getHeight() / 2d - b.getCenterY();
      AffineTransform toCenterAt = AffineTransform.getTranslateInstance(cx, cy);

      g2.setPaint(Color.YELLOW);
      g2.draw(toCenterAt.createTransformedShape(b));

      Shape s1 = toCenterAt.createTransformedShape(exShape);
      Shape s2 = toCenterAt.createTransformedShape(mjShape);

      if (fontPaintFlag.contains(FontPaint.IPA_EX_MINCHO)) {
        g2.setPaint(Color.CYAN);
        g2.fill(s1);
      }
      if (fontPaintFlag.contains(FontPaint.IPA_MJ_MINCHO)) {
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

  private static DefaultFormatterFactory makeFormatterFactory() {
    DefaultFormatter formatter = new DefaultFormatter() {
      @Override public Object stringToValue(String text) throws ParseException {
        Pattern pattern = Pattern.compile("^\\s*(\\p{XDigit}{1,6})\\s*$");
        Matcher matcher = pattern.matcher(text);
        if (matcher.find()) {
          Integer iv = Integer.valueOf(text, 16);
          if (iv <= Character.MAX_CODE_POINT) {
            return iv;
          }
        }
        Toolkit.getDefaultToolkit().beep();
        throw new ParseException(text, 0);
        // try {
        //   return Integer.valueOf(text, 16);
        // } catch (NumberFormatException ex) {
        //   Toolkit.getDefaultToolkit().beep();
        // }
      }

      @Override public String valueToString(Object value) {
        return String.format("%06X", (Integer) value);
      }
    };
    formatter.setValueClass(Integer.class);
    formatter.setOverwriteMode(true);
    return new DefaultFormatterFactory(formatter);
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      Logger.getGlobal().severe(ex::getMessage);
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
