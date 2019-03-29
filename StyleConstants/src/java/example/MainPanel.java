// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public final class MainPanel extends JPanel {
  private final JTextPane jtp = new JTextPane();
  private final JButton ok = new JButton("Test");
  private final JButton err = new JButton("Error");
  private final JButton clr = new JButton("Clear");

  public MainPanel() {
    super(new BorderLayout(5, 5));
    ok.addActionListener(e -> append("Test test test test", true));
    err.addActionListener(e -> append("Error error error error", false));
    clr.addActionListener(e -> jtp.setText(""));
    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(ok);
    box.add(err);
    box.add(Box.createHorizontalStrut(5));
    box.add(clr);

    jtp.setEditable(false);
    StyledDocument doc = jtp.getStyledDocument();
    // Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
    Style def = doc.getStyle(StyleContext.DEFAULT_STYLE);

    // Style regular = doc.addStyle("regular", def);
    // StyleConstants.setForeground(def, Color.BLACK);

    Style error = doc.addStyle("error", def);
    StyleConstants.setForeground(error, Color.RED);

    JScrollPane scroll = new JScrollPane(jtp);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scroll.getVerticalScrollBar().setUnitIncrement(25);

    add(scroll);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  // private static final String SEPARATOR = "\n";
  // private void append_(String str, boolean flg) {
  //   MutableAttributeSet sas = null;
  //   if (!flg) {
  //     // sas = new SimpleAttributeSet(jtp.getCharacterAttributes());
  //     sas = new SimpleAttributeSet();
  //     StyleConstants.setForeground(sas, Color.RED);
  //     // StyleConstants.setBold(sas, true);
  //     // StyleConstants.setFontFamily(sas, Font.MONOSPACED);
  //     // StyleConstants.setFontSize(sas, 32);
  //     // StyleConstants.setForeground(sas, Color.GREEN);
  //   }
  //   try {
  //     Document doc = jtp.getDocument();
  //     doc.insertString(doc.getLength(), str + SEPARATOR, sas);
  //     jtp.setCaretPosition(doc.getLength());
  //   } catch (BadLocationException ex) {
  //     throw new RuntimeException(ex); // should never happen
  //   }
  // }

  private void append(String str, boolean flg) {
    String style = flg ? StyleContext.DEFAULT_STYLE : "error";
    StyledDocument doc = jtp.getStyledDocument();
    try {
      doc.insertString(doc.getLength(), str + "\n", doc.getStyle(style));
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
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
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
