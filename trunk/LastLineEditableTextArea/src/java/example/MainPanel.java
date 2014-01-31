package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private static final String str = "Can only edit last line, version 0.0\n";
    private MainPanel() {
        super(new BorderLayout());
        JTextArea textArea = new JTextArea();
        textArea.setMargin(new Insets(2,5,2,2));
        textArea.setText(str + NonEditableLineDocumentFilter.PROMPT);
        ((AbstractDocument)textArea.getDocument()).setDocumentFilter(new NonEditableLineDocumentFilter());

        add(new JScrollPane(textArea));
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

class NonEditableLineDocumentFilter extends DocumentFilter {
    public static final String LB = "\n";
    public static final String PROMPT = "> ";
    @Override public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
        if(string == null) {
            return;
        }else{
            replace(fb, offset, 0, string, attr);
        }
    }
    @Override public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
        replace(fb, offset, length, "", null);
    }
    @Override public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
        Document doc = fb.getDocument();
        Element root = doc.getDefaultRootElement();
        int count = root.getElementCount();
        int index = root.getElementIndex(offset);
        Element cur = root.getElement(index);
        int promptPosition = cur.getStartOffset()+PROMPT.length();
        if(index==count-1 && offset-promptPosition>=0) {
            String str = text;
            if(LB.equals(str)) {
                String line = doc.getText(promptPosition, offset-promptPosition);
                String[] args = line.split(" ");
                String cmd = args[0];
                if(cmd.isEmpty()) {
                    str = String.format("%n%s", PROMPT);
                }else{
                    str = String.format("%n%s: command not found%n%s", cmd, PROMPT);
                }
            }
            fb.replace(offset, length, str, attrs);
        }
    }
}
