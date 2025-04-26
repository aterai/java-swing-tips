// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.Element;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    String txt = makeSampleText();
    add(makeTitledPanel("Default", new JTextArea(txt)));
    add(makeTitledPanel("Override selectAll", new CustomTextArea(txt)));
    add(makeTitledPanel("move Caret top", new EmacsTextArea(txt)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String makeSampleText() {
    String s = "aaa\nbb bb\nc c c\nddd\n\n1234567890\n";
    return String.join("\n", Collections.nCopies(10, s));
  }

  private static Component makeTitledPanel(String title, JTextArea editor) {
    editor.setCaretPosition(0);
    editor.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    JScrollPane scroll = new JScrollPane(editor);
    scroll.setRowHeaderView(new LineNumberView(editor));
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(scroll);
    return p;
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

class CustomTextArea extends JTextArea {
  private final Action selectAllAct = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      Rectangle r = getVisibleRect();
      getActionMap().get("select-all").actionPerformed(e);
      EventQueue.invokeLater(() -> scrollRectToVisible(r));
    }
  };
  private final Action caretUpAct = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (getSelectedText() == null) {
        getActionMap().get("caret-up").actionPerformed(e);
      } else {
        getCaret().moveDot(getCaret().getMark());
      }
    }
  };
  private final Action caretForwardAct = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (getSelectedText() == null) {
        getActionMap().get("caret-forward").actionPerformed(e);
      } else {
        getCaret().moveDot(getCaret().getMark());
      }
    }
  };

  protected CustomTextArea(String text) {
    super(text);
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(this::initActionMap);
  }

  private void initActionMap() {
    InputMap im = getInputMap(WHEN_FOCUSED);
    ActionMap am = getActionMap();
    String selectAllKey = "select-all2";
    im.put(KeyStroke.getKeyStroke("ctrl A"), selectAllKey);
    am.put(selectAllKey, selectAllAct);
    String caretUpKey = "caret-up";
    im.put(KeyStroke.getKeyStroke("UP"), caretUpKey + "2");
    am.put(caretUpKey + "2", caretUpAct);
    String caretForwardKey = "caret-forward";
    im.put(KeyStroke.getKeyStroke("LEFT"), caretForwardKey + "2");
    am.put(caretForwardKey + "2", caretForwardAct);
  }
}

class EmacsTextArea extends JTextArea {
  private final Action selectAllAct = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      selectAll();
    }
  };
  private final Action caretDownAct = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (getSelectedText() == null) {
        getActionMap().get("caret-down").actionPerformed(e);
      } else {
        getCaret().moveDot(getCaret().getMark());
      }
    }
  };
  private final Action caretBackwardAct = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (getSelectedText() == null) {
        getActionMap().get("caret-backward").actionPerformed(e);
      } else {
        getCaret().moveDot(getCaret().getMark());
      }
    }
  };

  protected EmacsTextArea(String text) {
    super(text);
  }

  @Override public void updateUI() {
    super.updateUI();
    EventQueue.invokeLater(this::initActionMap);
  }

  private void initActionMap() {
    InputMap im = getInputMap(WHEN_FOCUSED);
    ActionMap am = getActionMap();
    String selectAllKey = "select-all2";
    im.put(KeyStroke.getKeyStroke("ctrl A"), selectAllKey);
    am.put(selectAllKey, selectAllAct);
    String caretDownKey = "caret-down";
    im.put(KeyStroke.getKeyStroke("DOWN"), caretDownKey + "2");
    am.put(caretDownKey + "2", caretDownAct);
    String caretBackwardKey = "caret-backward";
    im.put(KeyStroke.getKeyStroke("RIGHT"), caretBackwardKey + "2");
    am.put(caretBackwardKey + "2", caretBackwardAct);
  }

  @Override public void selectAll() {
    Rectangle r = getVisibleRect();
    Document doc = getDocument();
    if (doc != null) {
      setCaretPosition(doc.getLength());
      moveCaretPosition(0);
    }
    EventQueue.invokeLater(() -> scrollRectToVisible(r));
  }
}

class LineNumberView extends JPanel {
  private static final int MARGIN = 5;
  private final JTextArea textArea;

  protected LineNumberView(JTextArea textArea) {
    super();
    this.textArea = textArea;
    textArea.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        repaint();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        repaint();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        /* not needed */
      }
    });
    textArea.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        revalidate();
        repaint();
      }
    });
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(true);
    EventQueue.invokeLater(() -> {
      Insets i = textArea.getMargin();
      setBorder(BorderFactory.createCompoundBorder(
          BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY),
          BorderFactory.createEmptyBorder(i.top, MARGIN, i.bottom, MARGIN - 1)));
      setBackground(textArea.getBackground());
    });
  }

  private int getComponentWidth(FontMetrics fontMetrics) {
    int lineCount = textArea.getLineCount();
    int maxDigits = Math.max(3, Integer.toString(lineCount).length());
    Insets i = getInsets();
    return maxDigits * fontMetrics.stringWidth("0") + i.left + i.right;
  }

  private int getLineAtPoint(int y) {
    Element root = textArea.getDocument().getDefaultRootElement();
    int pos = textArea.viewToModel(new Point(0, y));
    // Java 9: int pos = textArea.viewToModel2D(new Point(0, y));
    return root.getElementIndex(pos);
  }

  @Override public Dimension getPreferredSize() {
    FontMetrics fontMetrics = textArea.getFontMetrics(textArea.getFont());
    return new Dimension(getComponentWidth(fontMetrics), textArea.getHeight());
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setColor(textArea.getBackground());
    Rectangle clip = g2.getClipBounds();
    g2.fillRect(clip.x, clip.y, clip.width, clip.height);

    Font font = textArea.getFont();
    g2.setFont(font);
    FontMetrics fontMetrics = g2.getFontMetrics(font);
    int fontAscent = fontMetrics.getAscent();
    int fontDescent = fontMetrics.getDescent();
    int fontLeading = fontMetrics.getLeading();

    g2.setColor(getForeground());
    int base = clip.y;
    int start = getLineAtPoint(base);
    int end = getLineAtPoint(base + clip.height);
    int y = start * fontMetrics.getHeight();
    int rmg = getInsets().right;
    for (int i = start; i <= end; i++) {
      String text = Integer.toString(i + 1);
      int x = getComponentWidth(fontMetrics) - rmg - fontMetrics.stringWidth(text);
      y += fontAscent;
      g2.drawString(text, x, y);
      y += fontDescent + fontLeading;
    }
    g2.dispose();
  }
}
