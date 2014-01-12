package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

public class MainPanel extends JPanel {
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

        JTextArea textArea1 = new JTextArea("JTextArea#setMargin(Insets)\n\n" + initTxt);
        textArea1.setMargin(new Insets(5,5,5,5));
        JScrollPane scroll1 = new JScrollPane(textArea1);

        JTextArea textArea2 = new JTextArea("JScrollPane#setViewportBorder(...)\n\n" + initTxt);
        textArea2.setMargin(new Insets(0,0,0,1));
        JScrollPane scroll2 = new JScrollPane(textArea2);
        scroll2.setViewportBorder(BorderFactory.createLineBorder(textArea2.getBackground(), 5));

        JSplitPane sp = new JSplitPane();
        sp.setResizeWeight(.5);
        sp.setLeftComponent(scroll1);
        sp.setRightComponent(scroll2);
        add(sp);

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
