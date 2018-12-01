package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String u1F60x = "😀😁😂😃😄😅😆😇😈😉😊😋😌😍😎😏";
        String u1F61x = "😐😑😒😓😔😕😖😗😘😙😚😛😜😝😞😟";
        String u1F62x = "😠😡😢😣😤😥😦😧😨😩😪😫😬😭😮😯";
        String u1F63x = "😰😱😲😳😴😵😶😷😸😹😺😻😼😽😾😿";
        String u1F64x = "🙀🙁🙂　　🙅🙆🙇🙈🙉🙊🙋🙌🙍🙎🙏";

        JTextField label = new JTextField();
        label.setEditable(false);
        label.setFont(label.getFont().deriveFont(32f));

        JTextArea textArea = new JTextArea(String.join("\n", Arrays.asList(u1F60x, u1F61x, u1F62x, u1F63x, u1F64x)));
        textArea.addCaretListener(e -> {
            try {
                int dot = e.getDot();
                int mark = e.getMark();
                if (dot - mark == 0) {
                    Document doc = textArea.getDocument();
                    String txt = doc.getText(dot, 1);
                    int code = txt.codePointAt(0);
                    if (Character.isHighSurrogate((char) code)) {
                        txt = doc.getText(dot, 2);
                        code = txt.codePointAt(0);
                        // code = Character.toCodePoint((char) code, (char) doc.getText(dot + 1, 1).codePointAt(0));
                        // txt = new String(Character.toChars(code));
                    }
                    label.setText(String.format("%s: U+%04X", txt, code));
                } else {
                    label.setText("");
                }
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        });

        add(new JScrollPane(textArea));
        add(label, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
