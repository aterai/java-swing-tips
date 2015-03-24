package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private static final String MYSITE = "http://ateraimemo.com/";
    private final JTextArea textArea = new JTextArea();
    public MainPanel() {
        super(new BorderLayout());
        JEditorPane editor = new JEditorPane("text/html", "<html><a href='" + MYSITE + "'>" + MYSITE + "</a>");
        editor.setOpaque(false);
        editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editor.setEditable(false);
        editor.addHyperlinkListener(new HyperlinkListener() {
            @Override public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED && Desktop.isDesktopSupported()) {
                    try {
                        Desktop.getDesktop().browse(new URI(MYSITE));
                    } catch (IOException | URISyntaxException ex) {
                        ex.printStackTrace();
                    }
                    textArea.setText(e.toString());
                }
            }
        });
        JPanel p = new JPanel();
        p.add(editor);
        p.setBorder(BorderFactory.createTitledBorder("Desktop.getDesktop().browse(URI)"));
        add(p, BorderLayout.NORTH);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 200));
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
