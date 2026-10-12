// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.DocumentFilter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    JLabel label = new JLabel() {
      @Override public Dimension getPreferredSize() {
        return new Dimension(32, 32);
      }
    };
    label.setOpaque(true);
    label.setBackground(new Color(0xFF_FF_00_00, true));
    UIManager.put("ColorChooser.rgbHexCodeText", "#RGBA:");
    JButton button = new JButton("open JColorChooser");
    button.addActionListener(e -> {
      Color color = openColorChooser(getRootPane(), label.getBackground());
      if (color != null) {
        label.setBackground(color);
      }
    });
    add(label);
    add(button);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Color openColorChooser(Component parent, Color initColor) {
    JColorChooser cc = new JColorChooser();
    cc.setColor(initColor);
    AbstractColorChooserPanel rgbChooser = getRgbChooser(cc);
    if (rgbChooser != null) {
      // Java 9: if (rgbChooser.isColorTransparencySelectionEnabled()) {
      for (Component c : rgbChooser.getComponents()) {
        if (c instanceof JFormattedTextField) {
          removeFocusListeners(c);
          // c.removePropertyChangeListener("value", (PropertyChangeListener) rgbChooser);
          // javax.swing.colorchooser.ValueFormatter.init(8, true, (JFormattedTextField) c);
          ValueFormatter.init((JFormattedTextField) c);
        }
      }
      // Pass a new array: setting the same array instance does not fire a
      // property change event, so the chooser panels would not be reinstalled
      cc.setChooserPanels(cc.getChooserPanels().clone());
    }
    ColorTracker ok = new ColorTracker(cc);
    String title = "JColorChooser";
    JDialog dialog = JColorChooser.createDialog(parent, title, true, cc, ok, null);
    dialog.addComponentListener(new ComponentAdapter() {
      @Override public void componentHidden(ComponentEvent e) {
        Component c = e.getComponent();
        if (c instanceof Window) {
          ((Window) c).dispose();
        }
      }
    });
    dialog.setVisible(true);
    return ok.getColor();
  }

  private static AbstractColorChooserPanel getRgbChooser(JColorChooser colorChooser) {
    String rgbName = UIManager.getString("ColorChooser.rgbNameText", Locale.getDefault());
    return Arrays.stream(colorChooser.getChooserPanels())
        .filter(p -> Objects.equals(rgbName, p.getDisplayName()))
        .findFirst()
        .orElse(null);
  }

  private static void removeFocusListeners(Component c) {
    for (FocusListener l : c.getFocusListeners()) {
      if (l instanceof JFormattedTextField.AbstractFormatter) {
        c.removeFocusListener(l);
      }
    }
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

class ColorTracker implements ActionListener {
  private final JColorChooser chooser;
  private Color color;

  protected ColorTracker(JColorChooser c) {
    chooser = c;
  }

  @Override public void actionPerformed(ActionEvent e) {
    color = chooser.getColor();
  }

  public Color getColor() {
    return color;
  }
}

// copied from javax/swing/colorchooser/ValueFormatter.java
class ValueFormatter extends JFormattedTextField.AbstractFormatter implements FocusListener {
  private static final int LENGTH = 8;
  // private final int length;
  // private final int radix;
  // private JFormattedTextField text;
  private final transient DocumentFilter filter = new DocumentFilter() {
    @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
      if (isValidLength(fb.getDocument().getLength() - length)) {
        fb.remove(offset, length);
      }
    }

    @Override public void replace(FilterBypass fb, int offset, int length, String txt, AttributeSet set) throws BadLocationException {
      int len = fb.getDocument().getLength() + txt.length() - length;
      if (isValidLength(len) && isValidCode(txt)) {
        fb.replace(offset, length, txt.toUpperCase(Locale.ENGLISH), set);
      }
    }

    @Override public void insertString(FilterBypass fb, int offset, String txt, AttributeSet set) throws BadLocationException {
      if (isValidLength(fb.getDocument().getLength() + txt.length()) && isValidCode(txt)) {
        fb.insertString(offset, txt.toUpperCase(Locale.ENGLISH), set);
      }
    }
  };

  // protected ValueFormatter(int length, boolean hex) {
  //   this.length = length;
  //   this.radix = hex ? 16 : 10;
  // }

  public static void init(JFormattedTextField text) {
    ValueFormatter formatter = new ValueFormatter();
    text.setColumns(9);
    text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, text.getFont().getSize()));
    text.setFormatterFactory(new DefaultFormatterFactory(formatter));
    text.setHorizontalAlignment(SwingConstants.RIGHT);
    text.setMinimumSize(text.getPreferredSize());
    text.addFocusListener(formatter);
  }

  private static boolean isValidLength(int len) {
    return 0 <= len && len <= LENGTH;
  }

  private static boolean isValidCode(String code) {
    return code.chars().allMatch(c -> Character.digit(c, 16) >= 0);
  }

  @Override public Object stringToValue(String text) throws ParseException {
    // The DocumentFilter allows 0 to 8 digits, so an incomplete code must be
    // rejected here, otherwise substring(...) throws StringIndexOutOfBoundsException
    if (text == null || text.length() != LENGTH) {
      throw new ParseException("illegal length", 0);
    }
    try {
      // Integer.valueOf(argb, 16) throws NumberFormatException if argb > 0x7FFFFFFF
      return Integer.parseUnsignedInt(rgbaToArgb(text), 16);
    } catch (NumberFormatException nfe) {
      ParseException pe = new ParseException("illegal format", 0);
      pe.initCause(nfe);
      throw pe;
    }
  }

  @Override public String valueToString(Object object) throws ParseException {
    if (object instanceof Integer) {
      String argb = String.format(Locale.ENGLISH, "%08X", (Integer) object);
      return argbToRgba(argb);
    }
    throw new ParseException("illegal object", 0);
  }

  private static String argbToRgba(String argb) {
    return argb.substring(2) + argb.substring(0, 2);
  }

  private static String rgbaToArgb(String rgba) {
    return rgba.substring(6) + rgba.substring(0, 6);
  }

  @Override protected DocumentFilter getDocumentFilter() {
    return filter;
  }

  @Override public void focusGained(FocusEvent e) {
    Object source = e.getSource();
    if (source instanceof JFormattedTextField) {
      JFormattedTextField text = (JFormattedTextField) source;
      SwingUtilities.invokeLater(text::selectAll);
    }
  }

  @Override public void focusLost(FocusEvent e) {
    // Do nothing
  }
}
