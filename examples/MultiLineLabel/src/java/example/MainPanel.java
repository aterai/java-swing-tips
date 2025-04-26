// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

public final class MainPanel extends JPanel {
  private static final String TEXT = "Quartz glyph job vex'd cwm finks.";

  private MainPanel() {
    super(new GridLayout(3, 1));
    JTextPane label1 = new JTextPane();
    // MutableAttributeSet attr = new SimpleAttributeSet();
    Style attr = label1.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setLineSpacing(attr, -.2f);
    label1.setParagraphAttributes(attr, true);
    label1.setText("JTextPane\n" + TEXT);
    // [XP Style Icons - Download](https://xp-style-icons.en.softonic.com/)
    String path = "example/wi0124-32.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("OptionPane.errorIcon");
      }
    }).orElseGet(() -> UIManager.getIcon("OptionPane.errorIcon"));
    add(makeLeftIcon(label1, icon));

    JTextArea label2 = new JTextArea("JTextArea\n" + TEXT);
    add(makeLeftIcon(label2, icon));

    JLabel label3 = new JLabel("<html>JLabel+html<br>" + TEXT);
    label3.setIcon(icon);
    add(label3);

    setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeLeftIcon(JTextComponent label, Icon icon) {
    label.setForeground(UIManager.getColor("Label.foreground"));
    // label.setBackground(UIManager.getColor("Label.background"));
    label.setOpaque(false);
    label.setEditable(false);
    label.setFocusable(false);
    label.setMaximumSize(label.getPreferredSize());
    label.setMinimumSize(label.getPreferredSize());

    JLabel l = new JLabel(icon);
    l.setCursor(Cursor.getDefaultCursor());
    Box box = Box.createHorizontalBox();
    box.add(l);
    box.add(Box.createHorizontalStrut(2));
    box.add(label);
    box.add(Box.createHorizontalGlue());
    return box;
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
