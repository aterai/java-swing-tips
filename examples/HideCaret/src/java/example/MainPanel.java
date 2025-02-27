// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.text.Caret;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Highlighter;

public final class MainPanel extends JPanel {
  private final JTextArea textArea = new JTextArea("11111111111111\n22222\n3333");

  private MainPanel() {
    super(new BorderLayout());
    Caret hidingCaret = new DefaultCaret() {
      @Override public boolean isVisible() {
        return false;
      }
    };
    Caret defaultCaret = textArea.getCaret();
    JCheckBox check1 = new JCheckBox("Hide Caret");
    check1.addActionListener(e -> textArea.setCaret(isSelected(e) ? hidingCaret : defaultCaret));

    Highlighter highlighter = textArea.getHighlighter();
    JCheckBox check2 = new JCheckBox("Hide Highlighter");
    check2.addActionListener(e -> textArea.setHighlighter(isSelected(e) ? null : highlighter));

    JCheckBox check3 = new JCheckBox("Editable", true);
    check3.addActionListener(e -> textArea.setEditable(isSelected(e)));

    JCheckBox check4 = new JCheckBox("Focusable", true);
    check4.addActionListener(e -> textArea.setFocusable(isSelected(e)));

    JPanel p1 = new JPanel();
    p1.add(check1);
    p1.add(check2);

    JPanel p2 = new JPanel();
    p2.add(check3);
    p2.add(check4);

    JPanel p = new JPanel(new BorderLayout(0, 0));
    p.add(p1, BorderLayout.NORTH);
    p.add(p2, BorderLayout.SOUTH);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(textArea));
    add(new JTextField(), BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static boolean isSelected(ActionEvent e) {
    return ((JCheckBox) e.getSource()).isSelected();
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
