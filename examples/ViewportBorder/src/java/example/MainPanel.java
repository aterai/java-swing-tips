// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join("\n", Arrays.asList(
      "Trail: Creating a GUI with JFC/Swing",
      "Lesson: Learning Swing by Example",
      "This lesson explains the concepts you need to",
      " use Swing components in building a user interface.",
      " First we examine the simplest Swing application you can write.",
      " Then we present several progressively complicated examples of creating",
      " user interfaces using components in the javax.swing package.",
      " We cover several Swing components, such as buttons, labels, and text areas.",
      " The handling of events is also discussed,",
      " as are layout management and accessibility.",
      " This lesson ends with a set of questions and exercises",
      " so you can test yourself on what you've learned.",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html"));

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea1 = new JTextArea("JTextArea#setMargin(Insets)\n\n" + TEXT);
    textArea1.setMargin(new Insets(5, 5, 5, 5));
    JScrollPane scroll1 = new JScrollPane(textArea1);

    JTextArea textArea2 = new JTextArea("JScrollPane#setViewportBorder(...)\n\n" + TEXT);
    textArea2.setMargin(new Insets(0, 0, 0, 1));
    JScrollPane scroll2 = new JScrollPane(textArea2) {
      @Override public void updateUI() {
        // setViewportBorder(null);
        super.updateUI();
        EventQueue.invokeLater(() -> {
          Color c = getViewport().getView().getBackground();
          setViewportBorder(BorderFactory.createLineBorder(c, 5));
        });
      }
    };
    // scroll2.setViewportBorder(BorderFactory.createLineBorder(textArea2.getBackground(), 5));

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scroll1, scroll2);
    split.setResizeWeight(.5);
    add(split);

    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
