// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final String P1 = String.join(" ",
      "Trail: Creating a GUI with JFC/Swing",
      "Lesson: Learning Swing by Example",
      "This lesson explains the concepts you need to",
      "use Swing components in building a user interface.");
  private static final String P2 =
      " First we examine the simplest Swing application you can write.";
  private static final String P3 = String.join(" ",
      " Then we present several progressively complicated examples of creating",
      "user interfaces using components in the javax.swing package.");
  private static final String P4 =
      " We cover several Swing components, such as buttons, labels, and text areas.";
  private static final String P5 = String.join(" ",
      " The handling of events is also discussed,",
      "as are layout management and accessibility.");
  private static final String P6 = String.join(" ",
      " This lesson ends with a set of questions and exercises",
      "so you can test yourself on what you've learned.");

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTextArea textArea = new JTextArea(String.join("\n", P1, P2, P3, P4, P5, P6));
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    JCheckBox check1 = new JCheckBox("line wrap:", true);
    check1.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      textArea.setLineWrap(b);
    });
    JCheckBox check2 = new JCheckBox("wrap style word:", true);
    check2.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      textArea.setWrapStyleWord(b);
    });
    Box box = Box.createHorizontalBox();
    box.add(check1);
    box.add(Box.createHorizontalStrut(5));
    box.add(check2);
    add(new JScrollPane(textArea));
    add(box, BorderLayout.SOUTH);
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
      Logger.getGlobal().severe(ex::getMessage);
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
