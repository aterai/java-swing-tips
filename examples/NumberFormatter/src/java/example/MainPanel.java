// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(3, 1));
    JSpinner spinner = new JSpinner(makeSpinnerNumberModel());
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
    JFormattedTextField.AbstractFormatter formatter = editor.getTextField().getFormatter();
    if (formatter instanceof DefaultFormatter) {
      ((DefaultFormatter) formatter).setAllowsInvalid(false);
    }

    add(makeTitledPanel("Default", new JSpinner(makeSpinnerNumberModel())));
    add(makeTitledPanel("NumberFormatter#setAllowsInvalid(false)", spinner));
    add(makeTitledPanel("BackgroundColor", makeWarningSpinner(makeSpinnerNumberModel())));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSpinner makeWarningSpinner(SpinnerNumberModel model) {
    JSpinner spinner = new JSpinner(model);
    JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
    JFormattedTextField ftf = editor.getTextField();
    ftf.setFormatterFactory(makeFormatterFactory(model));
    ftf.getDocument().addDocumentListener(new WarningDocumentListener(ftf));
    return spinner;
  }

  private static DefaultFormatterFactory makeFormatterFactory(SpinnerNumberModel m) {
    NumberFormat format = new DecimalFormat("####0"); // , dfs);
    NumberFormatter editFormatter = new NumberFormatter(format) {
      @Override public Object stringToValue(String text) throws ParseException {
        try {
          Long.parseLong(text);
        } catch (NumberFormatException ex) {
          throw (ParseException) new ParseException(ex.getMessage(), 0).initCause(ex);
        }
        Object o = format.parse(text);
        if (o instanceof Long) {
          Long val = (Long) format.parse(text);
          Long max = (Long) m.getMaximum();
          Long min = (Long) m.getMinimum();
          if (max.compareTo(val) < 0 || min.compareTo(val) > 0) {
            throw new ParseException("out of bounds", 0);
          }
          return val;
        }
        throw new ParseException("not Long", 0);
      }
    };
    // editFormatter.setAllowsInvalid(false);
    // editFormatter.setCommitsOnValidEdit(true);
    editFormatter.setValueClass(Long.class);
    NumberFormatter displayFormatter = new NumberFormatter(format);
    return new DefaultFormatterFactory(displayFormatter, displayFormatter, editFormatter);
  }

  private static SpinnerNumberModel makeSpinnerNumberModel() {
    // ClassCastException: class Double cannot be cast to class Long
    // return new SpinnerNumberModel(10L, 0L, 99_999L, 1L);
    Long value = 10L;
    Long minimum = 0L;
    Long maximum = 99_999L;
    Long stepSize = 1L;
    return new SpinnerNumberModel(value, minimum, maximum, stepSize);
    // or:
    // return new SpinnerNumberModel(
    //   Long.valueOf(10L), Long.valueOf(0L), Long.valueOf(99_999L), Long.valueOf(1L));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
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

class WarningDocumentListener implements DocumentListener {
  private static final Color ERROR_BGC = new Color(0xFF_C8_C8);
  private final JFormattedTextField field;

  protected WarningDocumentListener(JFormattedTextField field) {
    this.field = field;
  }

  @Override public void changedUpdate(DocumentEvent e) {
    updateEditValid();
  }

  @Override public void insertUpdate(DocumentEvent e) {
    updateEditValid();
  }

  @Override public void removeUpdate(DocumentEvent e) {
    updateEditValid();
  }

  private void updateEditValid() {
    EventQueue.invokeLater(() -> {
      Color c = field.isEditValid() ? Color.WHITE : ERROR_BGC;
      field.setBackground(c);
    });
  }
}

//   private static JSpinner makeSpinner2(SpinnerNumberModel m) {
//     JSpinner s = new JSpinner(m);
//     JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) s.getEditor();
//     JFormattedTextField ftf = editor.getTextField();
//     ftf.setFormatterFactory(makeFFactory2(m));
//     ftf.addFocusListener(new FocusAdapter() {
//       @Override public void focusLost(FocusEvent e) {
//         // JTextComponent textField = (JTextComponent) e.getSource();
//         System.out.println(ftf.getText());
// //         try {
// //           ftf.commitEdit();
// //         } catch (Exception ex) {
// //           ex.printStackTrace();
// //         }
//         Long value = (Long) m.getValue();
//         System.out.println(value);
//         Long max = (Long) m.getMaximum();
//         Long min = (Long) m.getMinimum();
//         if (max.compareTo(value) < 0) {
//           m.setValue(max);
//         } else if (min.compareTo(value) > 0) {
//           m.setValue(min);
//         }
//       }
//     });
//     return s;
//   }
//
//   private static DefaultFormatterFactory makeFFactory2(SpinnerNumberModel m) {
//     NumberFormatter formatter = new NumberFormatter(new DecimalFormat("########0"));
//     formatter.setAllowsInvalid(false);
//     formatter.setCommitsOnValidEdit(true);
//     formatter.setValueClass(Long.class);
//     return new DefaultFormatterFactory(formatter);
//   }

// class IntegerDocumentFilter extends DocumentFilter {
//   @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
//     if (Objects.nonNull(text)) {
//       replace(fb, offset, 0, text, attr);
//     }
//   }
//
//   @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
//     replace(fb, offset, length, "", null);
//   }
//
//   @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//     Document doc = fb.getDocument();
//     int currentLength = doc.getLength();
//     String currentContent = doc.getText(0, currentLength);
//     String before = currentContent.substring(0, offset);
//     String after = currentContent.substring(length + offset, currentLength);
//     String newValue = before + Objects.toString(text, "") + after;
//     // currentValue =
//     checkInput(newValue, offset);
//     fb.replace(offset, length, text, attrs);
//   }
//
//   private static int checkInput(String proposedValue, int offset) throws BadLocationException {
//     int newValue = 0;
//     if (!proposedValue.isEmpty()) {
//       try {
//         newValue = Integer.parseInt(proposedValue);
//       } catch (NumberFormatException ex) {
//         throw new BadLocationException(proposedValue, offset);
//       }
//     }
//     return newValue;
//   }
// }
