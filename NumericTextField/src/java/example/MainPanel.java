package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.text.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JTextField textField1 = new JTextField("1000");
        textField1.setHorizontalAlignment(SwingConstants.RIGHT);
        textField1.setInputVerifier(new IntegerInputVerifier());

        JTextField textField2 = new JTextField();
        textField2.setDocument(new IntegerDocument());
        textField2.setText("2000");

        JTextField textField3 = new JTextField();
        ((AbstractDocument) textField3.getDocument()).setDocumentFilter(new IntegerDocumentFilter());
        textField3.setText("3000");

        JFormattedTextField textField4 = new JFormattedTextField();
        textField4.setFormatterFactory(new NumberFormatterFactory());
        textField4.setHorizontalAlignment(SwingConstants.RIGHT);
        textField4.setValue(4000);

        JSpinner spinner = new JSpinner(new SpinnerNumberModel(0, 0, Integer.MAX_VALUE, 1));
        ((JSpinner.NumberEditor) spinner.getEditor()).getFormat().setGroupingUsed(false);
        spinner.setValue(5000);

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder("TextField"));
        box.add(new JLabel("InputVerifier"));
        box.add(textField1);
        box.add(Box.createVerticalStrut(10));

        box.add(new JLabel("Custom Document"));
        box.add(textField2);
        box.add(Box.createVerticalStrut(10));

        box.add(new JLabel("DocumentFilter"));
        box.add(textField3);
        box.add(Box.createVerticalStrut(10));

        box.add(new JLabel("FormatterFactory"));
        box.add(textField4);
        box.add(Box.createVerticalStrut(10));

        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.setBorder(BorderFactory.createTitledBorder("Spinner"));
        p.add(new JLabel("SpinnerNumberModel"), BorderLayout.NORTH);
        p.add(spinner);

        add(box, BorderLayout.NORTH);
        add(Box.createRigidArea(new Dimension(320, 16)));
        add(p, BorderLayout.SOUTH);
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

//Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
//>http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
//Validating with Input Verifiers
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
                //Toolkit.getDefaultToolkit().beep();
            }
        }
        return verified;
    }
}

//Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
//>http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
//Validating with a Custom Document
class IntegerDocument extends PlainDocument {
    private int currentValue;
    public int getValue() {
        return currentValue;
    }
    @Override public void insertString(int offset, String str, AttributeSet attributes) throws BadLocationException {
        if (Objects.nonNull(str)) {
            String newValue;
            int length = getLength();
            if (length == 0) {
                newValue = str;
            } else {
                String currentContent = getText(0, length);
                StringBuffer currentBuffer = new StringBuffer(currentContent);
                currentBuffer.insert(offset, str);
                newValue = currentBuffer.toString();
            }
            currentValue = checkInput(newValue, offset);
            super.insertString(offset, str, attributes);
        }
    }
    @Override public void remove(int offset, int length) throws BadLocationException {
        int currentLength = getLength();
        String currentContent = getText(0, currentLength);
        String before = currentContent.substring(0, offset);
        String after = currentContent.substring(length + offset, currentLength);
        String newValue = before + after;
        currentValue = checkInput(newValue, offset);
        super.remove(offset, length);
    }
    private int checkInput(String proposedValue, int offset) throws BadLocationException {
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

//Validating Text and Filtering Documents and Accessibility and the Java Access Bridge Tech Tips
//>http://java.sun.com/developer/JDCTechTips/2005/tt0518.html
//Validating with a Document Filter
class IntegerDocumentFilter extends DocumentFilter {
    //int currentValue = 0;
    @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (Objects.nonNull(string)) {
            replace(fb, offset, 0, string, attr);
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

//How to Use Formatted Text Fields
//http://docs.oracle.com/javase/tutorial/uiswing/components/formattedtextfield.html
class NumberFormatterFactory extends DefaultFormatterFactory {
    //private static NumberFormat amountEditFormat = NumberFormat.getNumberInstance();
    //private static NumberFormat amountDisplayFormat = NumberFormat.getCurrencyInstance();
    //private static MaskFormatter mf;
    private static NumberFormatter numberFormatter = new NumberFormatter();
    static {
        //amountDisplayFormat.setMinimumFractionDigits(0);
        //amountEditFormat.setGroupingUsed(false);
        //try {
        //    mf = new MaskFormatter("#######");
        //} catch (ParseException ex) {}
        numberFormatter.setValueClass(Integer.class);
        ((NumberFormat) numberFormatter.getFormat()).setGroupingUsed(false);
    }
    protected NumberFormatterFactory() {
        super(numberFormatter, numberFormatter, numberFormatter);
//         super(new NumberFormatter(amountEditFormat),
//               new NumberFormatter(amountEditFormat),
//               new NumberFormatter(amountEditFormat));
//         super(new NumberFormatter(amountDisplayFormat),
//               new NumberFormatter(amountDisplayFormat),
//               new NumberFormatter(amountEditFormat));
    }
}
