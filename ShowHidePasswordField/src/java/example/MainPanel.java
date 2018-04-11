package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private static final Font FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
    private MainPanel() {
        super(new GridLayout(4, 1, 0, 2));

        JPasswordField pf1 = makePasswordField();
        AbstractButton b1 = new JCheckBox("show passwords");
        b1.addActionListener(e -> {
            AbstractButton c = (AbstractButton) e.getSource();
            pf1.setEchoChar(c.isSelected() ? '\u0000' : (Character) UIManager.get("PasswordField.echoChar"));
        });
        Container p1 = new JPanel(new BorderLayout());
        p1.add(pf1);
        p1.add(b1, BorderLayout.SOUTH);
        add(makeTitledPanel("BorderLayout + JCheckBox", p1));

        JPasswordField pf2 = makePasswordField();
        // AbstractDocument doc = (AbstractDocument) pf2.getDocument();
        // doc.setDocumentFilter(new ASCIIOnlyDocumentFilter());
        AbstractButton b2 = new JToggleButton();
        b2.addActionListener(e -> {
            AbstractButton c = (AbstractButton) e.getSource();
            pf2.setEchoChar(c.isSelected() ? '\u0000' : (Character) UIManager.get("PasswordField.echoChar"));
        });
        initEyeButton(b2);
        Container p2 = makeOverlayLayoutPanel();
        p2.add(b2);
        p2.add(pf2);
        add(makeTitledPanel("OverlayLayout + JToggleButton", p2));

        JPasswordField pf3 = makePasswordField();
        AbstractDocument doc = (AbstractDocument) pf3.getDocument();
        JTextField tf3 = new JTextField(24);
        tf3.setFont(FONT);
        tf3.enableInputMethods(false);
        tf3.setDocument(doc);

        CardLayout cardLayout = new CardLayout();
        Container p3 = new JPanel(cardLayout) {
            @Override public void updateUI() {
                super.updateUI();
                setAlignmentX(Component.RIGHT_ALIGNMENT);
            }
        };
        p3.add(pf3, PasswordField.HIDE.toString());
        p3.add(tf3, PasswordField.SHOW.toString());

        AbstractButton b3 = new JToggleButton();
        b3.addActionListener(e -> {
            AbstractButton c = (AbstractButton) e.getSource();
            PasswordField s = c.isSelected() ? PasswordField.SHOW : PasswordField.HIDE;
            cardLayout.show(p3, s.toString());
        });
        initEyeButton(b3);

        Container pp3 = makeOverlayLayoutPanel();
        pp3.add(b3);
        pp3.add(p3);
        add(makeTitledPanel("CardLayout + JTextField(can copy) + ...", pp3));

        JPasswordField pf4 = makePasswordField();
        AbstractButton b4 = new JButton();
        b4.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) {
                pf4.setEchoChar('\u0000');
            }
            @Override public void mouseReleased(MouseEvent e) {
                pf4.setEchoChar((Character) UIManager.get("PasswordField.echoChar"));
            }
        });
        initEyeButton(b4);
        Container p4 = makeOverlayLayoutPanel();
        p4.add(b4);
        p4.add(pf4);
        add(makeTitledPanel("press and hold down the mouse button", p4));

        setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        setPreferredSize(new Dimension(320, 240));
    }
    private static void initEyeButton(AbstractButton b) {
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
    }
    private static Container makeOverlayLayoutPanel() {
        JPanel p = new JPanel() {
            @Override public boolean isOptimizedDrawingEnabled() {
                return false;
            }
        };
        p.setLayout(new OverlayLayout(p));
        return p;
    }
    private static JPasswordField makePasswordField() {
        JPasswordField pf = new JPasswordField(24);
        pf.setText("abcdefghijklmn");
        pf.setAlignmentX(Component.RIGHT_ALIGNMENT);
        return pf;
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

// class ASCIIOnlyDocumentFilter extends DocumentFilter {
//     // private static Pattern pattern = Pattern.compile("\\A\\p{ASCII}*\\z");
//     private static CharsetEncoder asciiEncoder = Charset.forName("US-ASCII").newEncoder();
//
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
//         checkInput(newValue, offset);
//         fb.replace(offset, length, text, attrs);
//     }
//     // In Java, is it possible to check if a String is only ASCII? - Stack Overflow
//     // https://stackoverflow.com/questions/3585053/in-java-is-it-possible-to-check-if-a-string-is-only-ascii
//     private static void checkInput(String proposedValue, int offset) throws BadLocationException {
//         if (!proposedValue.isEmpty() && !asciiEncoder.canEncode(proposedValue)) {
//             throw new BadLocationException(proposedValue, offset);
//         }
// //         for (char c: proposedValue.toCharArray()) {
// //             if (((int) c) > 127) {
// //                 throw new BadLocationException(proposedValue, offset);
// //             }
// //         }
// //         // if (!proposedValue.isEmpty() && !proposedValue.chars().allMatch(c -> c < 128)) { // JDK 8
// //         Matcher m = pattern.matcher(proposedValue);
// //         if (!proposedValue.isEmpty() && !m.find()) {
// //             throw new BadLocationException(proposedValue, offset);
// //         }
//     }
// }

class ColorIcon implements Icon {
    private final Color color;
    protected ColorIcon(Color color) {
        this.color = color;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(color);
        g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 12;
    }
    @Override public int getIconHeight() {
        return 12;
    }
}
