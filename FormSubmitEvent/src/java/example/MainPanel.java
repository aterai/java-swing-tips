// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import javax.swing.*;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1, 5, 5));

    JTextArea logger = new JTextArea();
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
        String data = ((FormSubmitEvent) e).getData();
        logger.append(data + "\n");

        String charset = Charset.defaultCharset().toString();
        logger.append("default charset: " + charset + "\n");

        try {
          String txt = URLDecoder.decode(data, charset);
          logger.append(txt + "\n");
        } catch (UnsupportedEncodingException ex) {
          ex.printStackTrace();
          logger.append(ex.getMessage() + "\n");
        }
      }
    });

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(new JScrollPane(editor));
    add(new JScrollPane(logger));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
