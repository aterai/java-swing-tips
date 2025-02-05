// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextPane textPane = new JTextPane();
    textPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    FontMetrics fm = textPane.getFontMetrics(textPane.getFont());
    float charWidth = fm.charWidth('m');
    float tabWidth = charWidth * 4f;
    TabStop[] tabs = new TabStop[10];
    for (int j = 0; j < tabs.length; j++) {
      tabs[j] = createTabStop((j + 1) * tabWidth);
    }
    TabSet tabSet = new TabSet(tabs);
    // MutableAttributeSet attributes = new SimpleAttributeSet();
    MutableAttributeSet attributes = textPane.getStyle(StyleContext.DEFAULT_STYLE);
    StyleConstants.setTabSet(attributes, tabSet);
    // int length = textPane.getDocument().getLength();
    // textPane.getStyledDocument().setParagraphAttributes(0, length, attributes, false);
    textPane.setParagraphAttributes(attributes, false);
    textPane.setText("JTextPane\n0123\n\t4567\n\t\t89ab\n");

    JTextArea textArea = new JTextArea();
    textArea.setTabSize(4);
    textArea.setText("JTextArea\n0123\n\t4567\n\t\t89ab\n");

    add(new JScrollPane(textArea), BorderLayout.NORTH);
    add(new JScrollPane(textPane));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TabStop createTabStop(float pos) {
    return new TabStop(pos);
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
