package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private static final String INIT_TXT =
        "Trail: Creating a GUI with JFC/Swing\n"
      + "Lesson: Learning Swing by Example\n"
      + "This lesson explains the concepts you need to\n"
      + " use Swing components in building a user interface.\n"
      + " First we examine the simplest Swing application you can write.\n"
      + " Then we present several progressively complicated examples of creating\n"
      + " user interfaces using components in the javax.swing package.\n"
      + " We cover several Swing components, such as buttons, labels, and text areas.\n"
      + " The handling of events is also discussed,\n"
      + " as are layout management and accessibility.\n"
      + " This lesson ends with a set of questions and exercises\n"
      + " so you can test yourself on what you've learned.\n"
      + "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html\n";

    private MainPanel() {
        super(new BorderLayout());

        JTextArea textArea1 = new JTextArea("JTextArea#setMargin(Insets)\n\n" + INIT_TXT);
        textArea1.setMargin(new Insets(5, 5, 5, 5));
        JScrollPane scroll1 = new JScrollPane(textArea1);

        JTextArea textArea2 = new JTextArea("JScrollPane#setViewportBorder(...)\n\n" + INIT_TXT);
        textArea2.setMargin(new Insets(0, 0, 0, 1));
        JScrollPane scroll2 = new JScrollPane(textArea2) {
            @Override public void updateUI() {
                //setViewportBorder(null);
                super.updateUI();
                EventQueue.invokeLater(() -> setViewportBorder(BorderFactory.createLineBorder(getViewport().getView().getBackground(), 5)));
            }
        };
        // scroll2.setViewportBorder(BorderFactory.createLineBorder(textArea2.getBackground(), 5));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(.5);
        splitPane.setLeftComponent(scroll1);
        splitPane.setRightComponent(scroll2);
        add(splitPane);

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
