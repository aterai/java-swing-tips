// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.text.PlainDocument;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    String txt1 = "\n1\taaa\n12\tbbb\n123\tccc";
    JTextPane textPane = new JTextPane();
    textPane.setText("JTextPane:" + txt1);

    JTextArea textArea = new JTextArea("JTextArea:" + txt1);
    textArea.setTabSize(4);

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.add(new JScrollPane(textPane));
    p.add(new JScrollPane(textArea));

    String txt2 = "aaa\tbbb\tccc";
    JTextField field1 = new JTextField(txt2, 20);
    initActionInputMap(field1);

    int tabSize = 4;
    Document doc = new PlainDocument();
    doc.putProperty(PlainDocument.tabSizeAttribute, tabSize);
    JTextField field2 = new JTextField(doc, txt2, 20);
    initActionInputMap(field2);

    SpinnerNumberModel model = new SpinnerNumberModel(tabSize, -2, 12, 1);
    model.addChangeListener(e -> setTabSize(field2, model.getNumber().intValue()));
    JSpinner spinner = new JSpinner(model);

    Box box = Box.createVerticalBox();
    box.add(field1);
    box.add(Box.createVerticalStrut(5));
    box.add(field2);
    box.add(Box.createVerticalStrut(5));
    box.add(spinner);

    add(p);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initActionInputMap(JTextComponent editor) {
    String mapKey = "insert-horizontal-tab";
    editor.getActionMap().put(mapKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        try {
          editor.getDocument().insertString(editor.getCaretPosition(), "\t", null);
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(editor);
        }
      }
    });
    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK);
    editor.getInputMap(WHEN_FOCUSED).put(keyStroke, mapKey);
  }

  private static void setTabSize(JTextComponent editor, int size) {
    Document doc = editor.getDocument();
    if (doc != null) {
      doc.putProperty(PlainDocument.tabSizeAttribute, size);
      // int old = getTabSize(doc);
      // firePropertyChange("tabSize", old, size);
      //     BasicTextUI#modelChanged();
      editor.setEditable(false);
      editor.setEditable(true);
      // or: editor.firePropertyChange("font", 0, 1);
    }
  }

  // private static int getTabSize(Document doc) {
  //     int size = 8;
  //     // Document doc = getDocument();
  //     if (doc != null) {
  //       Integer i = (Integer) doc.getProperty(PlainDocument.tabSizeAttribute);
  //       if (i != null) {
  //         size = i;
  //       }
  //     }
  //     return size;
  // }

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
