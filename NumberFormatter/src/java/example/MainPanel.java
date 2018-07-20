package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(3, 1));

        JSpinner spinner = new JSpinner(makeSpinnerNumberModel());
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) spinner.getEditor();
        JFormattedTextField.AbstractFormatter formatter = editor.getTextField().getFormatter();
        if (formatter instanceof DefaultFormatter) {
            ((DefaultFormatter) formatter).setAllowsInvalid(false);
        }

        add(makeTitledPanel("Default", new JSpinner(makeSpinnerNumberModel())));
        add(makeTitledPanel("NumberFormatter#setAllowsInvalid(false)", spinner));
        add(makeTitledPanel("BackgroundColor", new WarningSpinner(makeSpinnerNumberModel())));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static SpinnerNumberModel makeSpinnerNumberModel() {
        return new SpinnerNumberModel(Long.valueOf(10), Long.valueOf(0), Long.valueOf(99999), Long.valueOf(1));
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

class WarningSpinner extends JSpinner {
    protected WarningSpinner(SpinnerNumberModel model) {
        super(model);
        JSpinner.NumberEditor editor = (JSpinner.NumberEditor) getEditor();
        JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
        ftf.setFormatterFactory(makeFFactory(model));
        ftf.getDocument().addDocumentListener(new DocumentListener() {
            private final Color errorBackground = new Color(255, 200, 200);
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
                EventQueue.invokeLater(() -> ftf.setBackground(ftf.isEditValid() ? Color.WHITE : errorBackground));
            }
        });
    }
    private static DefaultFormatterFactory makeFFactory(SpinnerNumberModel m) { // DecimalFormatSymbols dfs) {
        NumberFormat format = new DecimalFormat("####0"); // , dfs);
        NumberFormatter editFormatter = new NumberFormatter(format) {
            // @Override protected DocumentFilter getDocumentFilter() {
            //     return new IntegerDocumentFilter();
            // }
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
}

//     private static JSpinner makeSpinner2(SpinnerNumberModel m) {
//         JSpinner s = new JSpinner(m);
//         JSpinner.NumberEditor editor = (JSpinner.NumberEditor) s.getEditor();
//         JFormattedTextField ftf = (JFormattedTextField) editor.getTextField();
//         ftf.setFormatterFactory(makeFFactory2(m));
//         ftf.addFocusListener(new FocusAdapter() {
//             @Override public void focusLost(FocusEvent e) {
//                 // JTextComponent textField = (JTextComponent) e.getSource();
//                 System.out.println(ftf.getText());
// //                 try {
// //                     ftf.commitEdit();
// //                 } catch (Exception ex) {
// //                     ex.printStackTrace();
// //                 }
//                 Long value = (Long) m.getValue();
//                 System.out.println(value);
//                 Long max = (Long) m.getMaximum();
//                 Long min = (Long) m.getMinimum();
//                 if (max.compareTo(value) < 0) {
//                     m.setValue(max);
//                 } else if (min.compareTo(value) > 0) {
//                     m.setValue(min);
//                 }
//             }
//         });
//         return s;
//     }
//     private static DefaultFormatterFactory makeFFactory2(SpinnerNumberModel m) {
//         NumberFormatter formatter = new NumberFormatter(new DecimalFormat("########0"));
//         formatter.setAllowsInvalid(false);
//         formatter.setCommitsOnValidEdit(true);
//         formatter.setValueClass(Long.class);
//         return new DefaultFormatterFactory(formatter);
//     }

// class IntegerDocumentFilter extends DocumentFilter {
//     @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
//         if (Objects.nonNull(text)) {
//             replace(fb, offset, 0, text, attr);
//         }
//     }
//     @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
//         replace(fb, offset, length, "", null);
//     }
//     @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
//         Document doc = fb.getDocument();
//         int currentLength = doc.getLength();
//         String currentContent = doc.getText(0, currentLength);
//         String before = currentContent.substring(0, offset);
//         String after = currentContent.substring(length + offset, currentLength);
//         String newValue = before + Objects.toString(text, "") + after;
//         // currentValue =
//         checkInput(newValue, offset);
//         fb.replace(offset, length, text, attrs);
//     }
//     private static int checkInput(String proposedValue, int offset) throws BadLocationException {
//         int newValue = 0;
//         if (!proposedValue.isEmpty()) {
//             try {
//                 newValue = Integer.parseInt(proposedValue);
//             } catch (NumberFormatException ex) {
//                 throw new BadLocationException(proposedValue, offset);
//             }
//         }
//         return newValue;
//     }
// }
