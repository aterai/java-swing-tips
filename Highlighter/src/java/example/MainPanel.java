package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.text.*;

public class MainPanel extends JPanel {
    private static final String PATTERN = "Swing";
    private static final String INIT_TXT =
      "Trail: Creating a GUI with JFC/Swing\n" +
      "Lesson: Learning Swing by Example\n" +
      "This lesson explains the concepts you need to use Swing components in building a user interface." +
      " First we examine the simplest Swing application you can write." +
      " Then we present several progressively complicated examples of creating user interfaces using components in the javax.swing package." +
      " We cover several Swing components, such as buttons, labels, and text areas." +
      " The handling of events is also discussed, as are layout management and accessibility." +
      " This lesson ends with a set of questions and exercises so you can test yourself on what you?ve learned.\n" +
      "http://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";

    //    private final Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(SystemColor.textHighlight);
    private final transient Highlighter.HighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

    public MainPanel() {
        super(new BorderLayout());
        final JTextArea jta  = new JTextArea();
        jta.setLineWrap(true);
        jta.setText(INIT_TXT);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        box.add(new JButton(new AbstractAction("highlight: "+PATTERN) {
            @Override public void actionPerformed(ActionEvent e) {
                jta.setEditable(false);
                setHighlight(jta, PATTERN);
            }
        }));
        box.add(new JButton(new AbstractAction("clear") {
            @Override public void actionPerformed(ActionEvent e) {
                jta.setEditable(true);
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
            Highlighter highlighter = jtc.getHighlighter();
            Document doc = jtc.getDocument();
            String text = doc.getText(0, doc.getLength());
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            int pos = 0;
            while(matcher.find(pos)) {
                pos = matcher.end();
                highlighter.addHighlight(matcher.start(), pos, highlightPainter);
            }
//             int pos = text.indexOf(pattern);
//             while(pos >= 0) {
//                 int nextp = pos + pattern.length();
//                 jtc.getHighlighter().addHighlight(pos, nextp, highlightPainter);
//                 pos = text.indexOf(pattern, nextp);
//             }
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
