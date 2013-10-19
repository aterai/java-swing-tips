package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
//import java.io.StringReader;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
//import javax.swing.text.html.parser.*;

public class MainPanel extends JPanel {
    private final JTextArea textArea = new JTextArea();
    private final JTextPane textPane = new JTextPane();

    public MainPanel() {
        super(new BorderLayout());
        textPane.setComponentPopupMenu(new HTMLColorPopupMenu());
        //textPane.setEditorKit(new HTMLEditorKit());
        textPane.setContentType("text/html");
        textArea.setText(textPane.getText());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("JTextPane", new JScrollPane(textPane));
        tabbedPane.addTab("JTextArea", new JScrollPane(textArea));
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                JTabbedPane t = (JTabbedPane)e.getSource();
                int i = t.getSelectedIndex();
                try{
                    if(i==0) {
                        textPane.setText(textArea.getText());
                        //textPane.setText("");
                        //HTMLEditorKit hek = (HTMLEditorKit)textPane.getEditorKit();
                        //HTMLDocument doc = (HTMLDocument)textPane.getStyledDocument();
                        //hek.insertHTML(doc, 0, textArea.getText(), 0, 0, null);
                    }else{
                        String str = textPane.getText();
                        textArea.setText(str);
                        ////Removing HTML from a Java String - Stack Overflow
                        ////http://stackoverflow.com/questions/240546/removing-html-from-a-java-string
                        ////Test>>>>
                        //ParserDelegator delegator = new ParserDelegator();
                        //final StringBuffer s = new StringBuffer();
                        //delegator.parse(new StringReader(str), new HTMLEditorKit.ParserCallback() {
                        //    @Override public void handleText(char[] text, int pos) {
                        //        s.append(text);
                        //    }
                        //}, Boolean.TRUE);
                        //System.out.println(s.toString());
                        ////<<<<
                    }
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
                t.revalidate();
            }
        });
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class HTMLColorPopupMenu extends JPopupMenu {
    public HTMLColorPopupMenu() {
        super();
        final SimpleAttributeSet red = new SimpleAttributeSet();
        StyleConstants.setForeground(red, Color.RED);
        final SimpleAttributeSet green = new SimpleAttributeSet();
        StyleConstants.setForeground(green, Color.GREEN);
        final SimpleAttributeSet blue = new SimpleAttributeSet();
        StyleConstants.setForeground(blue, Color.BLUE);

        add(new AbstractAction("Red") {
            @Override public void actionPerformed(ActionEvent e) {
                JTextPane t = (JTextPane)getInvoker();
                StyledDocument doc = t.getStyledDocument();
                int start = t.getSelectionStart();
                int end   = t.getSelectionEnd();
                doc.setCharacterAttributes(start, end - start, red, false);
            }
        });
        add(new AbstractAction("Green") {
            @Override public void actionPerformed(ActionEvent e) {
                JTextPane t = (JTextPane)getInvoker();
                StyledDocument doc = t.getStyledDocument();
                int start = t.getSelectionStart();
                int end   = t.getSelectionEnd();
                doc.setCharacterAttributes(start, end - start, green, false);
            }
        });
        add(new AbstractAction("Blue") {
            @Override public void actionPerformed(ActionEvent e) {
                JTextPane t = (JTextPane)getInvoker();
                StyledDocument doc = t.getStyledDocument();
                int start = t.getSelectionStart();
                int end   = t.getSelectionEnd();
                doc.setCharacterAttributes(start, end - start, blue, false);
            }
        });
    }
    @Override public void show(Component c, int x, int y) {
        JTextPane t = (JTextPane)c;
        int start = t.getSelectionStart();
        int end   = t.getSelectionEnd();
        boolean flag = end - start > 0;
        for(MenuElement me: getSubElements()) {
            me.getComponent().setEnabled(flag);
        }
        super.show(c, x, y);
    }
}
