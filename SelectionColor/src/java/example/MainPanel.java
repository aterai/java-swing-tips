// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ByteLookupTable;
import java.awt.image.LookupOp;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private static final String PATTERN = "[Ff]rame";
  private static final Color SELECTION_COLOR = new Color(0xC8_64_64_FF, true);
  private static final Color HIGHLIGHT_COLOR = new Color(0x64_FF_FF_32, true);
  private final transient HighlightPainter highlightPainter = new DefaultHighlightPainter(HIGHLIGHT_COLOR);
  private final JEditorPane area = new JEditorPane();

  public MainPanel() {
    super(new BorderLayout());

    JCheckBox check = new JCheckBox("setSelectionColor(#C86464FF)", true);
    check.addActionListener(e -> {
      JCheckBox c = (JCheckBox) e.getSource();
      // https://docs.oracle.com/javase/8/docs/api/javax/swing/text/JTextComponent.html#setSelectionColor-java.awt.Color-
      // DOUBT?: Setting the color to null is the same as setting Color.white.
      area.setSelectionColor(c.isSelected() ? SELECTION_COLOR : null);
    });

    // https://ateraimemo.com/Swing/StyleSheet.html
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".highlight {color: blue; background: #FF5533; opacity: 0.5;}"); // INCOMPLETE: opacity
    // INCOMPLETE: styleSheet.addRule(".highlight {background: rgba(255, 100, 100, 0.6); opacity: 0.5;}");
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    area.setEditorKit(htmlEditorKit);
    area.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    area.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    area.setOpaque(false);
    area.setForeground(new Color(0xC8_C8_C8));
    area.setSelectedTextColor(Color.WHITE);
    area.setBackground(new Color(0x0, true)); // Nimbus
    area.setSelectionColor(SELECTION_COLOR);
    area.setText("<html><pre>" + String.join("<br />", Arrays.asList(
        "public static void createAndShowGui() {",
        "  <span class='highlight'>JFrame</span> frame = new JFrame();",
        "  frame.setDefaultCloseOperation(EXIT_ON_CLOSE);",
        "  frame.getContentPane().add(new MainPanel());",
        "  frame.pack();",
        "  frame.setLocationRelativeTo(null);",
        "  frame.setVisible(true);",
        "}")));

    // TEST: https://ateraimemo.com/Swing/DrawsLayeredHighlights.html
    // DefaultHighlighter dh = (DefaultHighlighter) area.getHighlighter();
    // dh.setDrawsLayeredHighlights(false);

    JToggleButton button = new JToggleButton("highlight");
    button.addActionListener(e -> {
      JToggleButton t = (JToggleButton) e.getSource();
      if (t.isSelected()) {
        setHighlight(area, PATTERN);
      } else {
        area.getHighlighter().removeAllHighlights();
      }
    });

    URL url = getClass().getResource("tokeidai.jpg");
    BufferedImage bi = getFilteredImage(url);
    JScrollPane scroll = new JScrollPane(area);
    scroll.getViewport().setOpaque(false);
    scroll.setViewportBorder(new CentredBackgroundBorder(bi));
    scroll.getVerticalScrollBar().setUnitIncrement(25);
    add(scroll);

    Box box = Box.createHorizontalBox();
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(button);
    box.add(Box.createHorizontalStrut(2));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private BufferedImage getFilteredImage(URL url) {
    BufferedImage image = Optional.ofNullable(url)
        .map(u -> {
          try {
            return ImageIO.read(u);
          } catch (IOException ex) {
            return makeMissingImage();
          }
        }).orElseGet(() -> makeMissingImage());

    BufferedImage dest = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
    byte[] b = new byte[256];
    for (int i = 0; i < b.length; i++) {
      b[i] = (byte) (i * .2f);
    }
    BufferedImageOp op = new LookupOp(new ByteLookupTable(0, b), null);
    op.filter(image, dest);
    return dest;
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }

  // https://ateraimemo.com/Swing/Highlighter.html
  private void setHighlight(JTextComponent jtc, String pattern) {
    Highlighter highlighter = jtc.getHighlighter();
    highlighter.removeAllHighlights();
    Document doc = jtc.getDocument();
    try {
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      int pos = 0;
      while (matcher.find(pos) && !matcher.group().isEmpty()) {
        int start = matcher.start();
        int end = matcher.end();
        highlighter.addHighlight(start, end, highlightPainter);
        pos = end;
      }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(jtc);
    }
    jtc.repaint();
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// https://community.oracle.com/thread/1395763 How can I use TextArea with Background Picture ?
// https://ateraimemo.com/Swing/CentredBackgroundBorder.html
class CentredBackgroundBorder implements Border {
  private final BufferedImage image;

  protected CentredBackgroundBorder(BufferedImage image) {
    this.image = image;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    int cx = (width - image.getWidth()) / 2;
    int cy = (height - image.getHeight()) / 2;
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.drawRenderedImage(image, AffineTransform.getTranslateInstance(cx, cy));
    g2.dispose();
  }

  @Override public Insets getBorderInsets(Component c) {
    return new Insets(0, 0, 0, 0);
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }
}
