// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Objects;
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

    JTextField textField1 = new JTextField();
    initBorderAndAlignment(textField1);
    ((AbstractDocument) textField1.getDocument()).setDocumentFilter(new IntegerDocumentFilter());

    JTextField textField2 = new JTextField();
    initBorderAndAlignment(textField2);
    textField2.setInputVerifier(new IntegerInputVerifier());

    JFormattedTextField textField3 = new JFormattedTextField();
    initBorderAndAlignment(textField3);
    textField3.setFormatterFactory(new NumberFormatterFactory());

    String[] columnNames = {"Default", "DocumentFilter", "InputVerifier", "JFormattedTextField"};
    TableModel model = new DefaultTableModel(columnNames, 10) {
      @Override public Class<?> getColumnClass(int column) {
        return Integer.class;
      }
    };
    JTable table = new JTable(model) {
      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        return c;
      }
    };
    table.getColumnModel().getColumn(1).setCellEditor(new DefaultCellEditor(textField1));
    table.getColumnModel().getColumn(2).setCellEditor(new DefaultCellEditor(textField2) {
      @Override public boolean stopCellEditing() {
        JComponent editor = (JComponent) getComponent();
        boolean isEditValid = editor.getInputVerifier().verify(editor);
        editor.setBorder(isEditValid ? BorderFactory.createEmptyBorder(1, 1, 1, 1)
                                     : BorderFactory.createLineBorder(Color.RED));
        return isEditValid && super.stopCellEditing();
      }
    });
    table.getColumnModel().getColumn(3).setCellEditor(new DefaultCellEditor(textField3) {
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

  private static void initBorderAndAlignment(JTextField textField) {
    textField.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    textField.setHorizontalAlignment(SwingConstants.RIGHT);
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

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with Input Verifiers
class IntegerInputVerifier extends InputVerifier {
  @Override public boolean verify(JComponent c) {
    boolean verified = false;
    if (c instanceof JTextComponent) {
      String txt = ((JTextComponent) c).getText();
      if (txt.isEmpty()) {
        return true;
      }
      try {
        int iv = Integer.parseInt(txt);
        verified = iv >= 0;
      } catch (NumberFormatException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(c);
      }
    }
    return verified;
  }
}

// Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
// http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
// Validating with a Document Filter
class IntegerDocumentFilter extends DocumentFilter {
  @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    if (Objects.nonNull(text)) {
      replace(fb, offset, 0, text, attr);
    }
  }

  @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
    replace(fb, offset, length, "", null);
  }

  @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    Document doc = fb.getDocument();
    int currentLength = doc.getLength();
    String currentContent = doc.getText(0, currentLength);
    String before = currentContent.substring(0, offset);
    String after = currentContent.substring(length + offset, currentLength);
    String newValue = before + Objects.toString(text, "") + after;
    checkInput(newValue, offset);
    fb.replace(offset, length, text, attrs);
  }

  private static int checkInput(String proposedValue, int offset) throws BadLocationException {
    if (proposedValue.isEmpty()) {
      return 0;
    } else {
      try {
        return Integer.parseInt(proposedValue);
      } catch (NumberFormatException ex) {
        throw (BadLocationException) new BadLocationException(proposedValue, offset).initCause(ex);
      }
    }
  }
}

// How to Use Formatted Text Fields (The Java™ Tutorials > Creating a GUI With JFC/Swing > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
class NumberFormatterFactory extends DefaultFormatterFactory {
  private static NumberFormatter numberFormatter = new NumberFormatter();

  static {
    numberFormatter.setValueClass(Integer.class);
    ((NumberFormat) numberFormatter.getFormat()).setGroupingUsed(false);
  }

  protected NumberFormatterFactory() {
    super(numberFormatter, numberFormatter, numberFormatter);
  }
}
