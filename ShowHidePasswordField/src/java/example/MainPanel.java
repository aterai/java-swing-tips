package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.nio.charset.*;
//import java.util.Objects;
//import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    public MainPanel() {
        super(new GridLayout(2, 1, 0, 10));

        final JPasswordField pf1 = new JPasswordField(24);
        pf1.setText("abcdefghijklmn");
        JPanel p1 = new JPanel(new BorderLayout());
        p1.add(pf1);
        p1.add(new JCheckBox(new AbstractAction("show passwords") {
            @Override public void actionPerformed(ActionEvent e) {
                AbstractButton c = (AbstractButton) e.getSource();
                Character ec = c.isSelected() ? 0 : (Character) UIManager.get("PasswordField.echoChar");
                pf1.setEchoChar(ec);
            }
        }), BorderLayout.SOUTH);
        add(makeTitlePanel(p1, "BorderLayout"));

        JPasswordField pf2 = new JPasswordField(24);
        pf2.setText("abcdefghijklmn");
        pf2.setAlignmentX(Component.RIGHT_ALIGNMENT);
        //AbstractDocument doc = (AbstractDocument) pf2.getDocument();
        //doc.setDocumentFilter(new ASCIIOnlyDocumentFilter());
        AbstractButton b = new JToggleButton(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                AbstractButton c = (AbstractButton) e.getSource();
                Character ec = c.isSelected() ? 0 : (Character) UIManager.get("PasswordField.echoChar");
                pf2.setEchoChar(ec);
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
        b.setToolTipText("show/hide passwords");

        JPanel p2 = new JPanel() {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        p2.setLayout(new OverlayLayout(p2));
        p2.add(b);
        p2.add(pf2);
        add(makeTitlePanel(p2, "OverlayLayout"));

        setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
        setPreferredSize(new Dimension(320, 240));
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

// enum PasswordField {
//     SHOW, HIDE;
// }
//
// class ASCIIOnlyDocumentFilter extends DocumentFilter {
//     //private static Pattern pattern = Pattern.compile("\\A\\p{ASCII}*\\z");
//     private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
//
//     @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
//         if (Objects.isNull(string)) {
//             return;
//         } else {
//             replace(fb, offset, 0, string, attr);
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
//         String newValue = before + (text == null ? "" : text) + after;
//         checkInput(newValue, offset);
//         fb.replace(offset, length, text, attrs);
//     }
//     //In Java, is it possible to check if a String is only ASCII? - Stack Overflow
//     //http://stackoverflow.com/questions/3585053/in-java-is-it-possible-to-check-if-a-string-is-only-ascii
//     private static void checkInput(String proposedValue, int offset) throws BadLocationException {
//         if (proposedValue.length() > 0 && !asciiEncoder.canEncode(proposedValue)) {
//             throw new BadLocationException(proposedValue, offset);
//         }
// //         for (char c: proposedValue.toCharArray()){
// //             if (((int) c) > 127) {
// //                 throw new BadLocationException(proposedValue, offset);
// //             }
// //         }
// //         //if (proposedValue.length() > 0 && !proposedValue.chars().allMatch(c -> c < 128)) { //JDK 8
// //         Matcher m = pattern.matcher(proposedValue);
// //         if (proposedValue.length() > 0 && !m.find()) {
// //             throw new BadLocationException(proposedValue, offset);
// //         }
//     }
// }

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
