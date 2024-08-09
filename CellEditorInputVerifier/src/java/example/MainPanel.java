// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextField field1 = new JTextField();
    initBorderAndAlignment(field1);
    Document doc1 = field1.getDocument();
    if (doc1 instanceof AbstractDocument) {
      ((AbstractDocument) doc1).setDocumentFilter(new IntegerDocumentFilter());
    }

    JTextField field2 = new JTextField();
    initBorderAndAlignment(field2);
    field2.setInputVerifier(new IntegerInputVerifier());

    JFormattedTextField field3 = new JFormattedTextField();
    initBorderAndAlignment(field3);
    field3.setFormatterFactory(new NumberFormatterFactory());

    TableModel model = makeModel();
    JTable table = new JTable(model) {
      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JComponent) {
          ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        }
        return c;
      }
    };
    table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(field1));
    table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(field2) {
      @Override public boolean stopCellEditing() {
        JComponent editor = (JComponent) getComponent();
        boolean isEditValid = editor.getInputVerifier().verify(editor);
        editor.setBorder(isEditValid ? BorderFactory.createEmptyBorder(1, 1, 1, 1)
                                     : BorderFactory.createLineBorder(Color.RED));
        return isEditValid && super.stopCellEditing();
      }
    });
    table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(field3) {
      @Override public boolean stopCellEditing() {
        JFormattedTextField editor = (JFormattedTextField) getComponent();
        boolean isEditValid = editor.isEditValid();
        editor.setBorder(isEditValid ? BorderFactory.createEmptyBorder(1, 1, 1, 1)
                                     : BorderFactory.createLineBorder(Color.RED));
        return isEditValid && super.stopCellEditing();
      }
    });

    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"Default", "DocumentFilter", "InputVerifier", "JFormattedTextField"};
    return new DefaultTableModel(columnNames, 10) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }
    };
  }

  private static void initBorderAndAlignment(JTextField textField) {
    textField.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    textField.setHorizontalAlignment(SwingConstants.RIGHT);
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
    return Optional.ofNullable(c)
        .filter(JTextComponent.class::isInstance)
        .map(JTextComponent.class::cast)
        .map(JTextComponent::getText)
        .filter(txt -> !txt.isEmpty())
        .map(txt -> {
          boolean verified = false;
          try {
            int iv = Integer.parseInt(txt);
            verified = iv >= 0;
          } catch (NumberFormatException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(c);
          }
          return verified;
        })
        .orElse(false);
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
  private static final NumberFormatter FORMATTER = new NumberFormatter();

  static {
    FORMATTER.setValueClass(Integer.class);
    ((NumberFormat) FORMATTER.getFormat()).setGroupingUsed(false);
  }

  protected NumberFormatterFactory() {
    super(FORMATTER, FORMATTER, FORMATTER);
  }
}
