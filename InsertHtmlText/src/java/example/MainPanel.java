package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Date;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    private static final String HTML_TEXT = "<html><body>head<table id='log' border='1'></table>tail</body></html>";
    private static final String ROW_TEXT = "<tr bgColor='%s'><td>%s</td><td>%s</td></tr>";
    private final HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    private final JEditorPane editor = new JEditorPane();

    private MainPanel() {
        super(new BorderLayout());

        editor.setEditorKit(htmlEditorKit);
        editor.setText(HTML_TEXT);
        editor.setEditable(false);

        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("insertAfterStart") {
            @Override public void actionPerformed(ActionEvent e) {
                HTMLDocument doc = (HTMLDocument) editor.getDocument();
                Element element = doc.getElement("log");
                Date d = new Date();
                String tag = String.format(ROW_TEXT, "#AEEEEE", "insertAfterStart", d.toString());
                try {
                    doc.insertAfterStart(element, tag);
                } catch (BadLocationException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }));
        box.add(Box.createHorizontalStrut(5));
        box.add(new JButton(new AbstractAction("insertBeforeEnd") {
            @Override public void actionPerformed(ActionEvent e) {
                HTMLDocument doc = (HTMLDocument) editor.getDocument();
                Element element = doc.getElement("log");
                Date d = new Date();
                String tag = String.format(ROW_TEXT, "#FFFFFF", "insertBeforeEnd", d.toString());
                try {
                    doc.insertBeforeEnd(element, tag);
                } catch (BadLocationException | IOException ex) {
                    ex.printStackTrace();
                }
            }
        }));
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
