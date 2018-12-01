package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new GridLayout(2, 1));
        String html = "<html><h2>H2</h2>text<ul><li>list: %s</li></ul></html>";

        JEditorPane editor0 = makeEditorPane();
        editor0.setText(String.format(html, "Default"));

        String url = getClass().getResource("bullet.png").toString();
        JEditorPane editor1 = makeEditorPane();
        HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editor1.getEditorKit();
        StyleSheet styleSheet = htmlEditorKit.getStyleSheet();
        styleSheet.addRule(String.format("ul{list-style-image:url(%s);margin:0px 20px;}", url));
        editor1.setText(String.format(html, "bullet.png"));

        // styleSheet.addRule("ul{list-style-type:circle;margin:0px 20px;}");
        // styleSheet.addRule("ul{list-style-type:disc;margin:0px 20px;}");
        // styleSheet.addRule("ul{list-style-type:square;margin:0px 20px;}");
        // styleSheet.addRule("ul{list-style-type:decimal;margin:0px 20px;}");

        // Pseudo element is not supported in javax.swing.text.html.CSS
        // styleSheet.addRule("ul{list-style-type:none;margin:0px 20px;}");
        // styleSheet.addRule("ul li:before{content: "\u00BB";}");

        add(new JScrollPane(editor0));
        add(new JScrollPane(editor1));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JEditorPane makeEditorPane() {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setEditable(false);
        return editorPane;
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
