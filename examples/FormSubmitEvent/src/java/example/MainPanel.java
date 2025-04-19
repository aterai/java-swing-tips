// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLEditorKit;

public final class MainPanel extends JPanel {
  private final JTextArea logger = new JTextArea();

  private MainPanel() {
    super(new GridLayout(2, 1, 5, 5));
    logger.setEditable(false);
    JEditorPane editor = new JEditorPane();
    HTMLEditorKit kit = new HTMLEditorKit();
    kit.setAutoFormSubmission(false);
    editor.setEditorKit(kit);
    editor.setEditable(false);
    String form = "<form action='#'><input type='text' name='word' value='12345' /></form>";
    editor.setText("<html><h1>Form test</h1>" + form);
    editor.addHyperlinkListener(e -> {
      if (e instanceof FormSubmitEvent) {
        decodeFormInfo((FormSubmitEvent) e);
      }
    });
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(editor));
    add(new JScrollPane(logger));
    setPreferredSize(new Dimension(320, 240));
  }

  private void decodeFormInfo(FormSubmitEvent e) {
    String data = e.getData();
    append(data);
    String charset = Charset.defaultCharset().toString();
    append("default charset: " + charset);
    try {
      append(URLDecoder.decode(data, charset));
    } catch (UnsupportedEncodingException ex) {
      append(ex.getMessage());
    }
  }

  private void append(String msg) {
    logger.append(msg + "\n");
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
