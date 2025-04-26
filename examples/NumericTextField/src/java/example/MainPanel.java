// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;
import javax.swing.text.PlainDocument;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = new JTextField("1000");
    field1.setHorizontalAlignment(SwingConstants.RIGHT);
    field1.setInputVerifier(new IntegerInputVerifier());

    JTextField field2 = new JTextField();
    field2.setDocument(new IntegerDocument());
    field2.setText("2000");

    JTextField field3 = new JTextField();
    Document doc = field3.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new IntegerDocumentFilter());
    }
    field3.setText("3000");

    JFormattedTextField field4 = new JFormattedTextField();
    field4.setFormatterFactory(new NumberFormatterFactory());
    field4.setHorizontalAlignment(SwingConstants.RIGHT);
    field4.setValue(4000);

    JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
    ((JSpinner.NumberEditor) spinner.getEditor()).getFormat().setGroupingUsed(false);
    spinner.setValue(5000);

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder("TextField"));
    box.add(new JLabel("InputVerifier"));
    box.add(field1);
    box.add(Box.createVerticalStrut(10));

    box.add(new JLabel("Custom Document"));
    box.add(field2);
    box.add(Box.createVerticalStrut(10));

    box.add(new JLabel("DocumentFilter"));
    box.add(field3);
    box.add(Box.createVerticalStrut(10));

    box.add(new JLabel("FormatterFactory"));
    box.add(field4);
    box.add(Box.createVerticalStrut(10));

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createTitledBorder("Spinner"));
    p.add(new JLabel("SpinnerNumberModel"), BorderLayout.NORTH);
    p.add(spinner);

    add(box, BorderLayout.NORTH);
    add(Box.createRigidArea(new Dimension(320, 16)));
    add(p, BorderLayout.SOUTH);
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

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// https://web.archive.org/web/20050523001117/http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with Input Verifiers
class IntegerInputVerifier extends InputVerifier {
  @Override public boolean verify(JComponent c) {
    boolean verified = false;
    if (c instanceof JTextComponent) {
      JTextComponent textField = (JTextComponent) c;
      try {
        Integer.parseInt(textField.getText());
        verified = true;
      } catch (NumberFormatException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(c);
        // Toolkit.getDefaultToolkit().beep();
      }
    }
    return verified;
  }
}

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// https://web.archive.org/web/20050523001117/http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with a Custom Document
class IntegerDocument extends PlainDocument {
  @Override public void insertString(int offset, String str, AttributeSet attributes) throws BadLocationException {
    if (Objects.nonNull(str)) {
      String newValue;
      int length = getLength();
      if (length == 0) {
        newValue = str;
      } else {
        String currentContent = getText(0, length);
        StringBuilder currentBuffer = new StringBuilder(currentContent);
        currentBuffer.insert(offset, str);
        newValue = currentBuffer.toString();
      }
      checkInput(newValue, offset);
      super.insertString(offset, str, attributes);
    }
  }

  @Override public void remove(int offset, int length) throws BadLocationException {
    int currentLength = getLength();
    String currentContent = getText(0, currentLength);
    String before = currentContent.substring(0, offset);
    String after = currentContent.substring(length + offset, currentLength);
    String newValue = before + after;
    checkInput(newValue, offset);
    super.remove(offset, length);
  }

  private void checkInput(String value, int offset) throws BadLocationException {
    if (!value.isEmpty()) {
      try {
        Integer.parseInt(value);
      } catch (NumberFormatException ex) {
        throw (BadLocationException) new BadLocationException(value, offset).initCause(ex);
      }
    }
  }
}

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// https://web.archive.org/web/20050523001117/http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with a Document Filter
class IntegerDocumentFilter extends DocumentFilter {
  @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    if (Objects.nonNull(text)) {
      replace(fb, offset, 0, text, attr);
    }
  }

  @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
    replace(fb, offset, length, "", null);
  }

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    Document doc = fb.getDocument();
    int currentLength = doc.getLength();
    String currentContent = doc.getText(0, currentLength);
    String before = currentContent.substring(0, offset);
    String after = currentContent.substring(length + offset, currentLength);
    String newValue = before + Objects.toString(text, "") + after;
    checkInput(newValue, offset);
    fb.replace(offset, length, text, attrs);
  }

  private static void checkInput(String value, int offset) throws BadLocationException {
    if (!value.isEmpty()) {
      try {
        Integer.parseInt(value);
      } catch (NumberFormatException ex) {
        throw (BadLocationException) new BadLocationException(value, offset).initCause(ex);
      }
    }
  }
}

// How to Use Formatted Text Fields (The Java™ Tutorials > ... > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
class NumberFormatterFactory extends DefaultFormatterFactory {
  // private static NumberFormat amountEditFormat = NumberFormat.getNumberInstance();
  // private static NumberFormat amountDisplayFormat = NumberFormat.getCurrencyInstance();
  // private static MaskFormatter mf;
  private static final NumberFormatter FORMATTER = new NumberFormatter();

  static {
    // amountDisplayFormat.setMinimumFractionDigits(0);
    // amountEditFormat.setGroupingUsed(false);
    // try {
    //   mf = new MaskFormatter("#######");
    // } catch (ParseException ex) {}
    FORMATTER.setValueClass(Integer.class);
    ((NumberFormat) FORMATTER.getFormat()).setGroupingUsed(false);
  }

  protected NumberFormatterFactory() {
    super(FORMATTER, FORMATTER, FORMATTER);
    // super(new NumberFormatter(amountEditFormat),
    //     new NumberFormatter(amountEditFormat),
    //     new NumberFormatter(amountEditFormat));
    // super(new NumberFormatter(amountDisplayFormat),
    //     new NumberFormatter(amountDisplayFormat),
    //     new NumberFormatter(amountEditFormat));
  }
}
