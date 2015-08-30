package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;

public final class MainPanel extends JPanel {
    //private final HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    //private final JEditorPane editor = new JEditorPane();
    private final JEditorPane editor = new JEditorPane("text/html", "");
    public MainPanel() {
        super(new BorderLayout());

        editor.setFont(new Font("Serif", Font.PLAIN, 16));

        //StyleSheet styleSheet = new StyleSheet();
        //styleSheet.addRule("body {font-size: 16pt;}");
        //styleSheet.addRule("h1 {font-size: 64pt;}");
        //htmlEditorKit.setStyleSheet(styleSheet);
        //editor.setEditorKit(htmlEditorKit);

        StringBuilder buf = new StringBuilder(300);
        buf.append("<html>JEditorPane#setFont(new Font('Serif', Font.PLAIN, 16));<br/>");
        HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editor.getEditorKit();
        StyleSheet styles = htmlEditorKit.getStyleSheet();
        //System.out.println(styles);
        Enumeration rules = styles.getStyleNames();
        while (rules.hasMoreElements()) {
            String name = (String) rules.nextElement();
            Style rule = styles.getRule(name);
            if ("body".equals(name)) {
                Enumeration sets = rule.getAttributeNames();
                while (sets.hasMoreElements()) {
                    Object n = sets.nextElement();
                    buf.append(String.format("%s: %s<br />", n, rule.getAttribute(n)));
                }
            }
        }
        editor.setText(buf.toString());

        add(new JCheckBox(new AbstractAction("JEditorPane.HONOR_DISPLAY_PROPERTIES") {
            @Override public void actionPerformed(ActionEvent e) {
                editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, ((JCheckBox) e.getSource()).isSelected());
                //HTMLEditorKit htmlEditorKit = (HTMLEditorKit) editor.getEditorKit();
                //StyleSheet styles = htmlEditorKit.getStyleSheet();
                //styles.addRule("body {font-size: 64pt;}");
            }
        }), BorderLayout.NORTH);
        add(new JScrollPane(editor));
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
