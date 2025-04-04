// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(makeStyleSheet());
    JEditorPane editor1 = makeEditorPane(htmlEditorKit);
    JEditorPane editor2 = makeEditorPane(htmlEditorKit);
    editor2.setSelectedTextColor(null);
    editor2.setSelectionColor(new Color(0x64_88_AA_AA, true));
    // TEST: editor2.setSelectionColor(null);
    add(new JScrollPane(editor1));
    add(new JScrollPane(editor2));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane(HTMLEditorKit htmlEditorKit) {
    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);
    editor.setBackground(new Color(0xEE_EE_EE));
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Optional.ofNullable(cl.getResource("example/test.html"))
        .ifPresent(url -> {
          try {
            editor.setPage(url);
          } catch (IOException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(editor);
            editor.setText(ex.getMessage());
          }
        });
    return editor;
  }

  private static StyleSheet makeStyleSheet() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str {color:#008800}");
    styleSheet.addRule(".kwd {color:#000088}");
    styleSheet.addRule(".com {color:#880000}");
    styleSheet.addRule(".typ {color:#660066}");
    styleSheet.addRule(".lit {color:#006666}");
    styleSheet.addRule(".pun {color:#666600}");
    styleSheet.addRule(".pln {color:#000000}");
    styleSheet.addRule(".tag {color:#000088}");
    styleSheet.addRule(".atn {color:#660066}");
    styleSheet.addRule(".atv {color:#008800}");
    styleSheet.addRule(".dec {color:#660066}");
    return styleSheet;
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
