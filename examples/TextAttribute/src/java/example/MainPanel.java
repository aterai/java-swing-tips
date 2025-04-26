// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private static final String TEXT = "0123456789";
  private final JTextField textField0 = new JTextField(TEXT);
  private final JTextField textField1 = new JTextField(TEXT);
  private final JTextField textField2 = new JTextField(TEXT);
  private final JTextArea textArea = new JTextArea(TEXT + "\n" + TEXT);

  private MainPanel() {
    super(new BorderLayout(5, 5));
    Font font = textField2.getFont();
    textField2.setFont(font.deriveFont(16f));
    // TEST:
    // Map<TextAttribute, Object> attrs = new HashMap<>(font.getAttributes());
    // attrs.put(TextAttribute.SIZE, 32);
    // attrs.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_LOW_DOTTED);
    // textField.setFont(font.deriveFont(attrs));
    // textField.setMargin(new Insets(4, 2, 4, 2));

    JComboBox<UnderlineStyle> comboBox = new JComboBox<>(UnderlineStyle.values());
    comboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        Object style = ((UnderlineStyle) e.getItem()).getStyle();
        initUnderline(textField0, style);
        initUnderline(textField1, style);
        initUnderline(textField2, style);
        initUnderline(textArea, style);
      }
    });

    JPanel p = new JPanel(new GridLayout(3, 1, 5, 5));
    p.add(comboBox, BorderLayout.NORTH);
    p.add(textField1, BorderLayout.NORTH);
    p.add(textField2, BorderLayout.NORTH);
    add(p, BorderLayout.NORTH);
    add(textField0, BorderLayout.SOUTH);
    add(new JScrollPane(textArea));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  @SuppressWarnings("PMD.UseConcurrentHashMap")
  private static void initUnderline(JTextComponent tc, Object style) {
    Font font = tc.getFont();
    // ConcurrentHashMap does not allow null to be used as a key or value
    // NullPointerException transform=null*
    Map<TextAttribute, Object> attrs = new HashMap<>(font.getAttributes());
    attrs.put(TextAttribute.UNDERLINE, style);
    tc.setFont(font.deriveFont(attrs));
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

enum UnderlineStyle {
  OFF(-1),
  LOW_DASHED(TextAttribute.UNDERLINE_LOW_DASHED),
  LOW_DOTTED(TextAttribute.UNDERLINE_LOW_DOTTED),
  LOW_GRAY(TextAttribute.UNDERLINE_LOW_GRAY),
  LOW_ONE_PIXEL(TextAttribute.UNDERLINE_LOW_ONE_PIXEL),
  LOW_TWO_PIXEL(TextAttribute.UNDERLINE_LOW_TWO_PIXEL),
  ON(TextAttribute.UNDERLINE_ON);
  private final int style;

  UnderlineStyle(int style) {
    this.style = style;
  }

  public int getStyle() {
    return style;
  }

  @Override public String toString() {
    return "UNDERLINE_" + super.toString();
  }
}
