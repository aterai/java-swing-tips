package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    private static Font makeFont(URL url) {
        Font font = null;
        try(InputStream is = url.openStream()) {
            font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(12.0f);
        }catch(IOException | FontFormatException ex) {
            ex.printStackTrace();
        }
        return font;
    }
    private MainPanel() {
        super(new BorderLayout());
        Font font = makeFont(getClass().getResource("Burnstown Dam.ttf"));
        GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);

        JPanel p = new JPanel(new GridLayout(0,1));
        JLabel label = new JLabel();
        label.setFont(font.deriveFont(Font.PLAIN, 24));
        label.setText("1: setFont(font.deriveFont(Font.PLAIN, 24))");
        p.add(label);
        JLabel label2 = new JLabel();
        label2.setText("<html><font size='+3' face='Burnstown Dam'>2: html,font,size,+3</font></html>");
        p.add(label2);

        StyleSheet styleSheet = new StyleSheet();
        styleSheet.addRule("body {font-size: 24pt; font-family: Burnstown Dam;}");
        //styleSheet.addRule(".highlight {color: red; background: green; font-family: Burnstown Dam.ttf; }");
        HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
        htmlEditorKit.setStyleSheet(styleSheet);
        JEditorPane editor = new JEditorPane();
        editor.setEditorKit(htmlEditorKit);
        editor.setText(makeTestHtml());
        JEditorPane editor2 = new JEditorPane();
        editor2.setFont(font);
        editor2.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor2.setText("4: putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE)");
        JPanel p2 = new JPanel(new GridLayout(0,1));
        p2.add(new JScrollPane(editor));
        p2.add(new JScrollPane(editor2));

        add(p, BorderLayout.NORTH);
        add(p2);
        setPreferredSize(new Dimension(320, 240));
    }
    private static String makeTestHtml() {
        return "<html><body>"
             + "<div>3: StyleSheet,body{font-size:24pt; font-family:Burnstown Dam;}</div>"
             + "</body></html>";
    }
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
