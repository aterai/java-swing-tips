// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));

    // UIManager.put("TextPane.caretForeground", Color.ORANGE);
    StyleSheet styleSheet = new StyleSheet();
    styleSheet.addRule("body {font-size: 12pt}");
    styleSheet.addRule(".highlight {color: red; background: green}");
    HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    htmlEditorKit.setStyleSheet(styleSheet);
    JEditorPane editor = new JEditorPane();
    // editor.setEditable(false);
    editor.setEditorKit(htmlEditorKit);
    editor.setText(makeTestHtml());
    editor.setCaretColor(null);

    JTextField field = new JTextField("JTextField");
    field.setBackground(Color.GRAY);
    field.setForeground(Color.WHITE);

    JRadioButton r1 = new JRadioButton("RED");
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        field.setCaretColor(Color.RED);
        // editor.setCaretColor(Color.RED);
      }
    });

    JRadioButton r2 = new JRadioButton("null");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        field.setCaretColor(null);
        // editor.setCaretColor(null);
      }
    });

    JRadioButton r3 = new JRadioButton("Lnf default", true);
    r3.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        Color c = UIManager.getLookAndFeelDefaults().getColor("TextField.caretForeground");
        field.setCaretColor(c);
        // c = UIManager.getLookAndFeelDefaults().getColor("TextPane.caretForeground");
        // System.out.println(c);
        // editor.setCaretColor(c);
      }
    });

    ButtonGroup bg = new ButtonGroup();
    Box box = Box.createHorizontalBox();
    Stream.of(r1, r2, r3).forEach(rb -> {
      bg.add(rb);
      box.add(rb);
      box.add(Box.createHorizontalStrut(2));
    });
    box.add(field);

    UIManager.put("TextArea.caretForeground", Color.ORANGE);
    JTextArea area = new JTextArea("TextArea.caretForeground: ORANGE");
    // area.setBackground(Color.GREEN);
    // area.setFont(area.getFont().deriveFont(15.0f));
    // area.setCaretColor(Color.RED);

    JPanel p = new JPanel(new GridLayout(2, 1, 2, 2));
    p.add(new JScrollPane(area));
    p.add(new JScrollPane(editor));

    setBorder(BorderFactory.createTitledBorder("JTextComponent#setCaretColor(...)"));
    add(box, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeTestHtml() {
    return String.join("\n", new String[] {
        "<html><body>",
        "<div>JTextPane#setCaretColor(null)</div>",
        "<div class='highlight'>1111111111</div>",
        "<div>2222222222</div>",
        "</body></html>"
    });
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
