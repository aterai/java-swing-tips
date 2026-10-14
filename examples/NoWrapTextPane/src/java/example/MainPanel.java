// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.Element;
import javax.swing.text.ParagraphView;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    int n = 1024 * 1024 - 2;
    String txt = String.join("", Collections.nCopies(n, "a")) + "x\n";
    // Java 11: String txt = "a".repeat(n) + "x\n";

    JEditorPane editorPane = new JEditorPane();
    editorPane.setEditorKit(new NoWrapEditorKit2());

    JTextArea textArea = new JTextArea();

    JButton editorPaneButton = new JButton("JEditorPane");
    JButton textAreaButton = new JButton("JTextArea");

    // JTextPane textPane = new JTextPane() {
    //   // Non Wrapping(Wrap) TextPane : TextField : Swing JFC : Java
    //   // http://www.java2s.com/Code/Java/Swing-JFC/NonWrappingWrapTextPane.htm
    //   @Override public boolean getScrollableTracksViewportWidth() {
    //     Component p = getParent();
    //     if (Objects.isNull(p)) {
    //       return true;
    //     }
    //     return getUI().getPreferredSize(this).width <= p.getSize().width;
    //   }
    // };
    // textPane.setEditorKit(new NoWrapEditorKit1());

    ActionListener longTextListener = e -> {
      if (editorPaneButton.equals(e.getSource())) {
        editorPane.setText(txt);
      } else {
        textArea.setText(txt);
      }
    };
    editorPaneButton.addActionListener(longTextListener);
    textAreaButton.addActionListener(longTextListener);

    JButton clearButton = new JButton("clear all");
    clearButton.addActionListener(e -> {
      editorPane.setText("");
      textArea.setText("");
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(editorPaneButton);
    box.add(textAreaButton);
    box.add(clearButton);

    JPanel p = new JPanel(new GridLayout(2, 1));
    p.add(makeTitledPanel("NoWrapEditorKit(JEditorPane)", editorPane));
    p.add(makeTitledPanel("JTextArea", textArea));

    add(box, BorderLayout.NORTH);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JScrollPane sp = new JScrollPane(c);
    sp.setBorder(BorderFactory.createTitledBorder(title));
    return sp;
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

// https://community.oracle.com/thread/1353861 Disabling word wrap for JTextPane
class NoWrapParagraphView extends ParagraphView {
  protected NoWrapParagraphView(Element elem) {
    super(elem);
  }

  @Override protected SizeRequirements calculateMinorAxisRequirements(int axis, SizeRequirements r) {
    SizeRequirements req = super.calculateMinorAxisRequirements(axis, r);
    req.minimum = req.preferred;
    return req;
  }

  @Override public int getFlowSpan(int index) {
    return Integer.MAX_VALUE;
  }
}

class NoWrapEditorKit2 extends StyledEditorKit {
  @Override public ViewFactory getViewFactory() {
    ViewFactory defaultFactory = super.getViewFactory();
    return elem -> AbstractDocument.ParagraphElementName.equals(elem.getName())
        ? new NoWrapParagraphView(elem)
        : defaultFactory.create(elem);
  }
}
