// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Utilities;

public final class MainPanel extends JPanel {
  private static final String TEXT = String.join("\n",
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
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html");
  private static final String HELP = String.join("\n",
      "cursor-end: ctrl E",
      "caret-end: ctrl END(default)",
      "caret-end-line: END(default)",
      "caret-next-word: ctrl RIGHT, ctrl KP_RIGHT(default)",
      "caret-end-paragraph: ctrl P",
      "caret-end-word: ctrl D",
      "--------\n");

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JTextArea textArea = new JTextArea(HELP + TEXT);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    getActionMap(textArea).put("cursor-end", new CursorEndAction());
    InputMap im = textArea.getInputMap(WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke("ctrl E"), "cursor-end");
    im.put(KeyStroke.getKeyStroke("ctrl P"), "caret-end-paragraph");
    im.put(KeyStroke.getKeyStroke("ctrl D"), "caret-end-word");
    JCheckBox check = new JCheckBox("line wrap:", true);
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      textArea.setLineWrap(b);
      textArea.setWrapStyleWord(b);
    });
    add(new JScrollPane(textArea));
    add(check, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ActionMap getActionMap(JTextArea textArea) {
    return textArea.getActionMap();
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

class CursorEndAction extends AbstractAction {
  @Override public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src instanceof JTextComponent) {
      JTextComponent target = (JTextComponent) src;
      int offs = target.getCaretPosition();
      int rowEndOffs = getRowEndOffsets(target, offs);
      if (rowEndOffs == offs) {
        int length = target.getDocument().getLength();
        int end = Utilities.getParagraphElement(target, offs).getEndOffset();
        target.setCaretPosition(Math.min(length, end - 1));
      } else {
        target.setCaretPosition(rowEndOffs);
      }
    }
  }

  private static int getRowEndOffsets(JTextComponent target, int curOffs) {
    int rowEndOffs = -1;
    try {
      rowEndOffs = Utilities.getRowEnd(target, curOffs);
    } catch (BadLocationException ex) {
      // Logger.getGlobal().severe(ex::getMessage);
      UIManager.getLookAndFeel().provideErrorFeedback(target);
    }
    return rowEndOffs;
  }
}
