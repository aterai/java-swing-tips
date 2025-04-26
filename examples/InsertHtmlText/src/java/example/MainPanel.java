// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;

public final class MainPanel extends JPanel {
  private static final String ROW_TEXT = "<tr bgColor='%s'><td>%s</td><td>%s</td></tr>";

  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(htmlEditorKit);
    editor.setText("<html><body>head<table id='log' border='1'></table>tail</body>");
    editor.setEditable(false);
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(Box.createHorizontalGlue());
    box.add(makeInsertAfterStart(editor));
    box.add(Box.createHorizontalStrut(5));
    box.add(makeInsertBeforeEnd(editor));
    add(new JScrollPane(editor));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JButton makeInsertAfterStart(JEditorPane editor) {
    String title = "insertAfterStart";
    JButton button = new JButton(title);
    button.addActionListener(e -> {
      HTMLDocument doc = (HTMLDocument) editor.getDocument();
      LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
      String tag = String.format(ROW_TEXT, "#AEEEEE", title, date);
      try {
        doc.insertAfterStart(doc.getElement("log"), tag);
      } catch (BadLocationException | IOException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(editor);
      }
    });
    return button;
  }

  private static JButton makeInsertBeforeEnd(JEditorPane editor) {
    String title = "insertBeforeEnd";
    JButton button = new JButton(title);
    button.addActionListener(e -> {
      HTMLDocument doc = (HTMLDocument) editor.getDocument();
      LocalDateTime date = LocalDateTime.now(ZoneId.systemDefault());
      String tag = String.format(ROW_TEXT, "#FFFFFF", title, date);
      try {
        doc.insertBeforeEnd(doc.getElement("log"), tag);
      } catch (BadLocationException | IOException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback(editor);
      }
    });
    return button;
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
