package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public final class MainPanel extends JPanel {
    private final JTextPane jtp = new JTextPane();
    private final JButton ok  = new JButton("Test");
    private final JButton err = new JButton("Error");
    private final JButton clr = new JButton("Clear");

    public MainPanel() {
        super(new BorderLayout(5, 5));
        ok.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                append("Test test test test", true);
            }
        });
        err.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                append("Error error error error", false);
            }
        });
        clr.addActionListener(new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                jtp.setText("");
            }
        });
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(ok);
        box.add(err);
        box.add(Box.createHorizontalStrut(5));
        box.add(clr);

        jtp.setEditable(false);
        StyledDocument doc = jtp.getStyledDocument();
        Style def = StyleContext.getDefaultStyleContext().getStyle(
            StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        //StyleConstants.setForeground(def, Color.BLACK);

        Style error = doc.addStyle("error", regular);
        StyleConstants.setForeground(error, Color.RED);

        JScrollPane scroll = new JScrollPane(jtp);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(25);

        add(scroll);
        add(box, BorderLayout.SOUTH);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setPreferredSize(new Dimension(320, 240));
    }

//     private static final String SEPARATOR = "\n";
//     private void append_(String str, boolean flg) {
//         SimpleAttributeSet sas = null;
//         if (!flg) {
//             //sas = new SimpleAttributeSet(jtp.getCharacterAttributes());
//             sas = new SimpleAttributeSet();
//             StyleConstants.setForeground(sas, Color.RED);
//             //StyleConstants.setBold(sas, true);
//             //StyleConstants.setFontFamily(sas, Font.MONOSPACED);
//             //StyleConstants.setFontSize(sas, 32);
//             //StyleConstants.setForeground(sas, Color.GREEN);
//         }
//         try {
//             Document doc = jtp.getDocument();
//             doc.insertString(doc.getLength(), str + SEPARATOR, sas);
//             jtp.setCaretPosition(doc.getLength());
//         } catch (BadLocationException e) {
//             e.printStackTrace();
//         }
//     }
    private void append(String str, boolean flg) {
        String style = flg ? "regular" : "error";
        StyledDocument doc = jtp.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), str + "\n", doc.getStyle(style));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
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
