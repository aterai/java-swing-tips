// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea1 = new JTextArea();
    textArea1.getDocument().addDocumentListener(new FifoDocumentListener(textArea1));
    textArea1.setEditable(false);

    JTextArea textArea2 = new JTextArea();
    Document doc2 = textArea2.getDocument();
    if (doc2 instanceof AbstractDocument) {
      ((AbstractDocument) doc2).setDocumentFilter(new FifoDocumentFilter());
    }

    Timer timer = new Timer(200, e -> {
      String s = LocalDateTime.now(ZoneId.systemDefault()).toString();
      textArea1.append(textArea1.getDocument().getLength() > 0 ? "\n" + s : s);
      textArea2.append(textArea2.getDocument().getLength() > 0 ? "\n" + s : s);
    });

    JButton start = new JButton("Start");
    start.addActionListener(e -> {
      if (!timer.isRunning()) {
        timer.start();
      }
    });

    JButton stop = new JButton("Stop");
    stop.addActionListener(e -> timer.stop());

    JButton clear = new JButton("Clear");
    clear.addActionListener(e -> {
      textArea1.setText("");
      textArea2.setText("");
    });

    addHierarchyListener(e -> {
      boolean b = (e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0;
      if (b && !e.getComponent().isDisplayable()) {
        timer.stop();
      }
    });

    Box box = Box.createHorizontalBox();
    box.add(Box.createHorizontalGlue());
    box.add(start);
    box.add(stop);
    box.add(Box.createHorizontalStrut(5));
    box.add(clear);

    JPanel p = new JPanel(new GridLayout(1, 2));
    p.add(new JScrollPane(textArea1));
    p.add(new JScrollPane(textArea2));

    add(p);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
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
    // frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class FifoDocumentListener implements DocumentListener {
  private static final int MAX_LINES = 10;
  private final JTextComponent textComponent;

  protected FifoDocumentListener(JTextComponent textComponent) {
    this.textComponent = textComponent;
  }

  @Override public void insertUpdate(DocumentEvent e) {
    Document doc = e.getDocument();
    Element root = doc.getDefaultRootElement();
    if (root.getElementCount() <= MAX_LINES) {
      return;
    }
    EventQueue.invokeLater(() -> removeLines(doc, root));
    textComponent.setCaretPosition(doc.getLength());
  }

  private static void removeLines(Document doc, Element root) {
    Element fl = root.getElement(0);
    try {
      doc.remove(0, fl.getEndOffset());
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

  @Override public void removeUpdate(DocumentEvent e) {
    /* not needed */
  }

  @Override public void changedUpdate(DocumentEvent e) {
    /* not needed */
  }
}

class FifoDocumentFilter extends DocumentFilter {
  private static final int MAX_LINES = 10;

  @Override public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException {
    fb.insertString(offset, text, attr);
    Element root = fb.getDocument().getDefaultRootElement();
    if (root.getElementCount() > MAX_LINES) {
      fb.remove(0, root.getElement(0).getEndOffset());
    }
  }
}
