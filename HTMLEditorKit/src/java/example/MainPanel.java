package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JTextPane textPane = new JTextPane();

    public MainPanel() {
        super(new BorderLayout());
        textPane.setComponentPopupMenu(new HtmlColorPopupMenu());
        // textPane.setEditorKit(new HTMLEditorKit());
        textPane.setContentType("text/html");
        textArea.setText(textPane.getText());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("JTextPane", new JScrollPane(textPane));
        tabbedPane.addTab("JTextArea", new JScrollPane(textArea));
        tabbedPane.addChangeListener(e -> {
            JTabbedPane t = (JTabbedPane) e.getSource();
            boolean isHtmlMode = t.getSelectedIndex() == 0;
            if (isHtmlMode) {
                textPane.setText(textArea.getText());
                // textPane.setText("");
                // HTMLEditorKit hek = (HTMLEditorKit) textPane.getEditorKit();
                // HTMLDocument doc = (HTMLDocument) textPane.getStyledDocument();
                // hek.insertHTML(doc, 0, textArea.getText(), 0, 0, null);
            } else {
                String str = textPane.getText();
                textArea.setText(str);
                // // Removing HTML from a Java String - Stack Overflow
                // // https://stackoverflow.com/questions/240546/removing-html-from-a-java-string
                // // Test >>>>
                // ParserDelegator delegator = new ParserDelegator();
                // StringBuilder s = new StringBuilder();
                // delegator.parse(new StringReader(str), new HTMLEditorKit.ParserCallback() {
                //     @Override public void handleText(char[] text, int pos) {
                //         s.append(text);
                //     }
                // }, Boolean.TRUE);
                // System.out.println(s.toString());
                // // <<<<
            }
            t.revalidate();
        });
        add(tabbedPane);
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

class HtmlColorPopupMenu extends JPopupMenu {
    protected HtmlColorPopupMenu() {
        super();
        MutableAttributeSet red = new SimpleAttributeSet();
        StyleConstants.setForeground(red, Color.RED);
        MutableAttributeSet green = new SimpleAttributeSet();
        StyleConstants.setForeground(green, Color.GREEN);
        MutableAttributeSet blue = new SimpleAttributeSet();
        StyleConstants.setForeground(blue, Color.BLUE);

        add("Red").addActionListener(e -> {
            JTextPane t = (JTextPane) getInvoker();
            StyledDocument doc = t.getStyledDocument();
            int start = t.getSelectionStart();
            int end = t.getSelectionEnd();
            doc.setCharacterAttributes(start, end - start, red, false);
        });
        add("Green").addActionListener(e -> {
            JTextPane t = (JTextPane) getInvoker();
            StyledDocument doc = t.getStyledDocument();
            int start = t.getSelectionStart();
            int end = t.getSelectionEnd();
            doc.setCharacterAttributes(start, end - start, green, false);
        });
        add("Blue").addActionListener(e -> {
            JTextPane t = (JTextPane) getInvoker();
            StyledDocument doc = t.getStyledDocument();
            int start = t.getSelectionStart();
            int end = t.getSelectionEnd();
            doc.setCharacterAttributes(start, end - start, blue, false);
        });
    }
    @Override public void show(Component c, int x, int y) {
        if (c instanceof JTextPane) {
            JTextPane t = (JTextPane) c;
            int start = t.getSelectionStart();
            int end = t.getSelectionEnd();
            boolean flag = end - start > 0;
            for (MenuElement me: getSubElements()) {
                me.getComponent().setEnabled(flag);
            }
            super.show(c, x, y);
        }
    }
}
