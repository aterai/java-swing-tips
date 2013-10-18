package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel{
//    private static final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(SystemColor.textHighlight);
    private static final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);
    private static final String pattern = "Swing";
    private static final String initTxt =
      "Trail: Creating a GUI with JFC/Swing\n" +
      "Lesson: Learning Swing by Example\n" +
      "This lesson explains the concepts you need to use Swing components in building a user interface." +
      " First we examine the simplest Swing application you can write." +
      " Then we present several progressively complicated examples of creating user interfaces using components in the javax.swing package." +
      " We cover several Swing components, such as buttons, labels, and text areas." +
      " The handling of events is also discussed, as are layout management and accessibility." +
      " This lesson ends with a set of questions and exercises so you can test yourself on what you?ve learned.\n" +
      "http://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";
    public MainPanel() {
        super(new BorderLayout());
        final JTextArea jta  = new JTextArea();
        jta.setLineWrap(true);
        jta.setEditable(false);
        jta.setText(initTxt);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("highlight") {
            @Override public void actionPerformed(ActionEvent e) {
                setHighlight(jta, pattern);
            }
        }));
        box.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                jta.getHighlighter().removeAllHighlights();
            }
        }));
        add(new JScrollPane(jta));
        add(box, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
    }

    public void setHighlight(JTextComponent jtc, String pattern) {
        jtc.getHighlighter().removeAllHighlights();
        try{
            Highlighter hilite = jtc.getHighlighter();
            Document doc = jtc.getDocument();
            String text = doc.getText(0, doc.getLength());
            int pos = 0;
            while((pos = text.indexOf(pattern, pos)) >= 0) {
                hilite.addHighlight(pos, pos+pattern.length(), highlightPainter);
                pos += pattern.length();
            }
        }catch(BadLocationException e) {
            e.printStackTrace();
        }
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
