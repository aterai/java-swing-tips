// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.Optional;
import javax.swing.*;
import javax.swing.text.html.FormSubmitEvent;
import javax.swing.text.html.HTMLEditorKit;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // System.out.println(UIManager.getString("IsindexView.prompt"));
    // UIManager.put("IsindexView.prompt", "Search1: ");
    JEditorPane editor = new JEditorPane();
    HTMLEditorKit kit = new HTMLEditorKit();
    kit.setAutoFormSubmission(false);
    editor.setEditorKit(kit);
    editor.setEditable(false);
    editor.addHyperlinkListener(e -> {
      if (e instanceof FormSubmitEvent) {
        String data = ((FormSubmitEvent) e).getData();
        String charset = Charset.defaultCharset().toString();
        try {
          String para = URLDecoder.decode(data, charset);
          JOptionPane.showMessageDialog(editor.getRootPane(), para);
        } catch (UnsupportedEncodingException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(editor);
          ex.printStackTrace();
        }
      }
    });
    editor.setText(makeHtml());
    add(new JScrollPane(editor));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeHtml() {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    String path = "example/16x16.png";
    String src = Optional.ofNullable(cl.getResource(path)).map(URL::toString).orElse("not found");
    // https://docs.oracle.com/javase/8/docs/api/javax/swing/text/html/FormView.html
    return String.join(
        "\n",
        "<html><body><form id='form1' action='#'>",
        "<div>Username: <input type='text' id='username' name='username'></div>",
        "<div>Password: <input type='password' id='password' name='password'></div>",
        "<input type='submit' value='Submit'>",
        "</form><br/><hr/>",
        "<form id='form2' action='#'>",
        "<div>button: <input type='button' value='JButton'></div>",
        "<div>checkbox: <input type='checkbox' id='checkbox1' name='checkbox1'></div>",
        "<div>image: <input type='image' id='image1' name='image1' src='" + src + "'></div>",
        "<div>password: <input type='password' id='password1' name='password1'></div>",
        "<div>radio: <input type='radio' id='radio1' name='radio1'></div>",
        "<div>reset: <input type='reset' id='reset1' name='reset1'></div>",
        "<div>submit: <input type='submit' id='submit1' name='submit1'></div>",
        "<div>text: <input type='text' id='text1' name='text1'></div>",
        "<div>file: <input type='file' id='file1' name='file1'></div>",
        "<div><isindex id='search1' name='search1' action='#'></div>",
        "<div><isindex id='search2' name='search2' prompt='search: ' action='#'></div>",
        "</form><br/><hr/>",
        "<form id='form3' action='#'>",
        "<div><select name='select1' size='5' multiple>",
        "  <option value='' selected='selected'>selected</option>",
        "  <option value='option1'>option1</option>",
        "  <option value='option2'>option2</option>",
        "</select></div><br/>",
        "<div><select name='select2'>",
        "  <option value='option0'>option0</option>",
        "  <option value='option1'>option1</option>",
        "  <option value='option2'>option2</option>",
        "</select></div><br/>",
        "<div><textarea name='textarea1' cols='50' rows='5'></div>",
        "</form>",
        "</body></html>");
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
