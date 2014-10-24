package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.util.Locale;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextField field1 = new JTextField("asdfasdfasdfasdfasdf");
    private final JTextField field2 = new JTextField();
    public MainPanel() {
        super(new GridLayout(2, 1));
        ((AbstractDocument) field2.getDocument()).setDocumentFilter(new FirstCharToUpperCaseDocumentFilter(field2));
        field2.setText("asdfasdfasdfasdfasdf");

        add(makeTitlePanel(field1, "Default"));
        add(makeTitlePanel(field2, "FirstCharToUpperCase"));
        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.insets  = new Insets(5, 5, 5, 5);
        p.add(cmp, c);
        p.setBorder(BorderFactory.createTitledBorder(title));
        return p;
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
class FirstCharToUpperCaseDocumentFilter extends DocumentFilter {
    private final JTextComponent textArea;
    public FirstCharToUpperCaseDocumentFilter(JTextComponent textArea) {
        super();
        this.textArea = textArea;
    }
    @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
        if (text == null) {
            return;
        }
        replace(fb, offset, 0, text, attrs);
    }
    @Override public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
        Document doc = fb.getDocument();
        if (offset == 0 && doc.getLength() - length > 0) {
            fb.replace(0, length + 1, doc.getText(length, 1).toUpperCase(Locale.ENGLISH), null);
            textArea.setCaretPosition(0);
        } else {
            fb.remove(offset, length);
        }
    }
    @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        String str = text;
        if (offset == 0 && text != null && text.length() > 0) {
            str = text.substring(0, 1).toUpperCase(Locale.ENGLISH) + text.substring(1);
        }
        fb.replace(offset, length, str, attrs);
    }
}
