// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.LabelView;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    MutableAttributeSet attr = new SimpleAttributeSet();
    StyleConstants.setForeground(attr, Color.RED);
    StyleConstants.setFontSize(attr, 32);

    MutableAttributeSet a = new SimpleAttributeSet();
    StyleConstants.setLineSpacing(a, .5f);
    // StyleConstants.setSpaceAbove(a, 5f);
    // StyleConstants.setSpaceBelow(a, 5f);
    // StyleConstants.setLeftIndent(a, 5f);
    // StyleConstants.setRightIndent(a, 5f);
    JTextPane editor1 = new JTextPane();
    editor1.setParagraphAttributes(a, false);
    setSampleText(editor1, attr);

    // StyleSheet styleSheet = new StyleSheet();
    // styleSheet.addRule("body {font-size: 24pt; line-height: 2.0}"); // XXX
    // HTMLEditorKit htmlEditorKit = new HTMLEditorKit();
    // htmlEditorKit.setStyleSheet(styleSheet);
    // editor1.setEditorKit(htmlEditorKit);
    // editor1.setText("<html><body>123<br />***<br />111<font size='32'>123<br />999</font>");

    JTextPane editor2 = new JTextPane();
    editor2.setEditorKit(new BottomInsetEditorKit());
    setSampleText(editor2, attr);

    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setTopComponent(new JScrollPane(editor1));
    sp.setBottomComponent(new JScrollPane(editor2));
    sp.setResizeWeight(.5);
    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private static void setSampleText(JTextPane textPane, MutableAttributeSet attr) {
    textPane.setText("12341234\n1234 567890 5555 66666 77777\n88 999999 ");
    try {
      StyledDocument doc = textPane.getStyledDocument();
      doc.insertString(doc.getLength(), "134500698\n", attr);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    // StyledDocument doc = new DefaultStyledDocument();
    // MutableAttributeSet a = new SimpleAttributeSet();
    // StyleConstants.setLineSpacing(a, .5f);
    // doc.setParagraphAttributes(0, doc.getLength() - 1, a, false);
    // textPane.setStyledDocument(doc);
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

class BottomInsetEditorKit extends StyledEditorKit {
  @Override public ViewFactory getViewFactory() {
    return new BottomInsetViewFactory();
  }
}

class BottomInsetViewFactory implements ViewFactory {
  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public View create(Element elem) {
    switch (elem.getName()) {
      // case AbstractDocument.ContentElementName:
      //   return new LabelView(elem);
      case AbstractDocument.ParagraphElementName:
        return new ParagraphView(elem) {
          @Override protected short getBottomInset() {
            return 5;
          }
        };
      case AbstractDocument.SectionElementName:
        return new BoxView(elem, View.Y_AXIS);
      case StyleConstants.ComponentElementName:
        return new ComponentView(elem);
      case StyleConstants.IconElementName:
        return new IconView(elem);
      default:
        return new LabelView(elem);
    }
  }
}
