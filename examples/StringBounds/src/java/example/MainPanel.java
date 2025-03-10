// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private final JTextField field = new JTextField("The quick brown fox");
  private final JLabel label = new JLabel(field.getText());
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    log.setEditable(false);
    field.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        update();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        update();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        update();
      }
    });
    update();

    JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
    panel.add(field);
    panel.add(label);

    add(panel, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  public void update() {
    log.setText("");
    String txt = field.getText().trim();
    label.setText(txt);
    if (txt.isEmpty()) {
      return;
    }
    Font font = label.getFont();
    FontRenderContext frc = label.getFontMetrics(font).getFontRenderContext();
    append("Font#getStringBounds(...)", font.getStringBounds(txt, frc));

    TextLayout layout = new TextLayout(txt, font, frc);
    append("TextLayout#getBounds()", layout.getBounds());

    GlyphVector gv = font.createGlyphVector(frc, txt);
    // GlyphVector gv = font.layoutGlyphVector(
    //     frc, txt.toCharArray(), 0, txt.length(), Font.LAYOUT_LEFT_TO_RIGHT);
    append("GlyphVector#getPixelBounds(...)", gv.getPixelBounds(frc, 0, 0));
    append("GlyphVector#getLogicalBounds()", gv.getLogicalBounds());
    append("GlyphVector#getVisualBounds()", gv.getVisualBounds());

    append("JLabel#getPreferredSize()", label.getPreferredSize());
    append("SwingUtilities.layoutCompoundLabel(...)", getCompoundLabelBounds(label).getSize());
  }

  private void append(String s, Object o) {
    log.append(s + ":\n  ");
    if (o instanceof Rectangle2D) {
      Rectangle2D r = (Rectangle2D) o;
      String fmt = "x=%8.4f y=%8.4f w=%8.4f h=%8.4f%n";
      log.append(String.format(fmt, r.getX(), r.getY(), r.getWidth(), r.getHeight()));
    } else {
      log.append(o + "\n");
    }
  }

  private static Rectangle getCompoundLabelBounds(JLabel label) {
    Rectangle viewR = new Rectangle();
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    SwingUtilities.calculateInnerArea(label, viewR);
    SwingUtilities.layoutCompoundLabel(
        label,
        label.getFontMetrics(label.getFont()),
        label.getText(),
        null, // label.getIcon(),
        label.getVerticalAlignment(),
        label.getHorizontalAlignment(),
        label.getVerticalTextPosition(),
        label.getHorizontalTextPosition(),
        viewR,
        iconR,
        textR,
        0); // label.getIconTextGap());
    return textR;
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
      ex.printStackTrace();
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
