// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(createStyleSheet());

    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(htmlEditorKit);
    editor.setEditable(false);

    JButton button = new JButton("open");
    button.addActionListener(e -> {
      JFileChooser fileChooser = new JFileChooser();
      int ret = fileChooser.showOpenDialog(getRootPane());
      if (ret == JFileChooser.APPROVE_OPTION) {
        loadFile(fileChooser.getSelectedFile().toPath(), editor);
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    box.add(Box.createHorizontalGlue());
    box.add(button);

    add(new JScrollPane(editor));
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void loadFile(Path path, JEditorPane editor) {
    try {
      // Files.readAllLines(...) throws a MalformedInputException (an IOException)
      // for a non UTF-8 file, while Files.lines(...) throws an UncheckedIOException
      // lazily from the stream, which is not caught by catch (IOException ex).
      String txt = String.join("\n", Files.readAllLines(path, StandardCharsets.UTF_8));
      editor.setText("<pre>" + JavaSyntaxHighlighter.highlight(txt) + "\n</pre>");
    } catch (IOException ex) {
      editor.setText(ex.getMessage());
    }
  }

  private static StyleSheet createStyleSheet() {
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule(".str{color:#008800}");
    styleSheet.addRule(".kwd{color:#000088}");
    styleSheet.addRule(".com{color:#880000}");
    styleSheet.addRule(".typ{color:#660066}");
    styleSheet.addRule(".lit{color:#006666}");
    styleSheet.addRule(".pun{color:#666600}");
    styleSheet.addRule(".pln{color:#000000}");
    styleSheet.addRule(".tag{color:#000088}");
    styleSheet.addRule(".atn{color:#660066}");
    styleSheet.addRule(".atv{color:#008800}");
    styleSheet.addRule(".dec{color:#660066}");
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

