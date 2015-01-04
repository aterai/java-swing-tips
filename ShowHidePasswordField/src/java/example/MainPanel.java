package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.*;
//import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    public MainPanel() {
        super(new GridLayout(3, 1, 0, 10));
        add(makeTitlePanel(makeShowHidePasswordField(false), "CardLayout"));
        add(makeTitlePanel(makeShowHidePasswordField(true), "CardLayout + ASCII only filter"));

        JPasswordField pf = new JPasswordField(24);
        pf.setText("abcdefghijklmn");
        AbstractDocument doc = (AbstractDocument) pf.getDocument();
        doc.setDocumentFilter(new ASCIIOnlyDocumentFilter());

        JTextField tf = new JTextField(24);
        tf.setFont(FONT);
        tf.enableInputMethods(false);
        tf.setDocument(doc);

        final CardLayout cardLayout = new CardLayout();
        final JPanel p = new JPanel(cardLayout);
        p.setAlignmentX(Component.RIGHT_ALIGNMENT);

        p.add(pf, PasswordField.HIDE.toString());
        p.add(tf, PasswordField.SHOW.toString());

        AbstractButton b = new JToggleButton(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                AbstractButton c = (AbstractButton) e.getSource();
                PasswordField s = c.isSelected() ? PasswordField.SHOW : PasswordField.HIDE;
                cardLayout.show(p, s.toString());
            }
        });
        b.setFocusable(false);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 4));
        b.setAlignmentX(Component.RIGHT_ALIGNMENT);
        b.setAlignmentY(Component.CENTER_ALIGNMENT);
        b.setIcon(new ColorIcon(Color.GREEN));
        b.setRolloverIcon(new ColorIcon(Color.BLUE));
        b.setSelectedIcon(new ColorIcon(Color.RED));
        b.setRolloverSelectedIcon(new ColorIcon(Color.ORANGE));

        JPanel panel = new JPanel() {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        panel.setLayout(new OverlayLayout(panel));
        panel.add(b);
        panel.add(p);

        add(makeTitlePanel(panel, "CardLayout + OverlayLayout"));

        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JComponent makeShowHidePasswordField(boolean ascii) {
        JPasswordField pf = new JPasswordField(24);
        pf.setText("abcdefghijklmn");
        AbstractDocument doc = (AbstractDocument) pf.getDocument();
        if (ascii) {
            doc.setDocumentFilter(new ASCIIOnlyDocumentFilter());
        }

        JTextField tf = new JTextField(24);
        tf.setFont(FONT);
        tf.enableInputMethods(false);
        tf.setDocument(doc);

        final CardLayout cardLayout = new CardLayout();
        final JPanel p = new JPanel(cardLayout);

        p.add(pf, PasswordField.HIDE.toString());
        p.add(tf, PasswordField.SHOW.toString());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(p);
        panel.add(new JCheckBox(new AbstractAction("show passwords") {
            @Override public void actionPerformed(ActionEvent e) {
                JCheckBox c = (JCheckBox) e.getSource();
                PasswordField s = c.isSelected() ? PasswordField.SHOW : PasswordField.HIDE;
                cardLayout.show(p, s.toString());
            }
        }), BorderLayout.SOUTH);
        return panel;
    }
    private JComponent makeTitlePanel(JComponent cmp, String title) {
        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1d;
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

enum PasswordField {
    SHOW, HIDE;
}

class ASCIIOnlyDocumentFilter extends DocumentFilter {
    //private static Pattern pattern = Pattern.compile("\\A\\p{ASCII}*\\z");
    private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();

    @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if (string == null) {
            return;
        } else {
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
        String newValue = before + (text == null ? "" : text) + after;
        checkInput(newValue, offset);
        fb.replace(offset, length, text, attrs);
    }
    //In Java, is it possible to check if a String is only ASCII? - Stack Overflow
    //http://stackoverflow.com/questions/3585053/in-java-is-it-possible-to-check-if-a-string-is-only-ascii
    private static void checkInput(String proposedValue, int offset) throws BadLocationException {
        if (proposedValue.length() > 0 && !asciiEncoder.canEncode(proposedValue)) {
            throw new BadLocationException(proposedValue, offset);
        }
//         for (char c: proposedValue.toCharArray()){
//             if (((int) c) > 127) {
//                 throw new BadLocationException(proposedValue, offset);
//             }
//         }
//         //if (proposedValue.length() > 0 && !proposedValue.chars().allMatch(c -> c < 128)) { //JDK 8
//         Matcher m = pattern.matcher(proposedValue);
//         if (proposedValue.length() > 0 && !m.find()) {
//             throw new BadLocationException(proposedValue, offset);
//         }
    }
}

class ColorIcon implements Icon {
    private final Color color;
    public ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        g.translate(x, y);
        g.setColor(color);
        g.fillRect(1, 1, 11, 11);
        g.translate(-x, -y);
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
