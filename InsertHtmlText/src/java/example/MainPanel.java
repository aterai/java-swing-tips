package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    private static final String HTML_TEXT = "<html><body>head<table id='log' border='1'></table>tail</body></html>";
    private static final String ROW_TEXT = "<tr bgColor='%s'><td>%s</td><td>%s</td></tr>";

    private MainPanel() {
        super(new BorderLayout());

        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(htmlEditorKit);
        editor.setText(HTML_TEXT);
        editor.setEditable(false);

        JButton insertAfterStart = new JButton("insertAfterStart");
        insertAfterStart.addActionListener(e -> {
            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element element = doc.getElement("log");
            String date = LocalDateTime.now().toString();
            String tag = String.format(ROW_TEXT, "#AEEEEE", "insertAfterStart", date);
            try {
                doc.insertAfterStart(element, tag);
            } catch (BadLocationException | IOException ex) {
                ex.printStackTrace();
            }
        });

        JButton insertBeforeEnd = new JButton("insertBeforeEnd");
        insertBeforeEnd.addActionListener(e -> {
            HTMLDocument doc = (HTMLDocument) editor.getDocument();
            Element element = doc.getElement("log");
            String date = LocalDateTime.now().toString();
            String tag = String.format(ROW_TEXT, "#FFFFFF", "insertBeforeEnd", date);
            try {
                doc.insertBeforeEnd(element, tag);
            } catch (BadLocationException | IOException ex) {
                ex.printStackTrace();
            }
        });

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(Box.createHorizontalGlue());
        box.add(insertAfterStart);
        box.add(Box.createHorizontalStrut(5));
        box.add(insertBeforeEnd);
        add(new JScrollPane(editor));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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
