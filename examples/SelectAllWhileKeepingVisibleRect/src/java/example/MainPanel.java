// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Collections;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.TextAction;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 0));
    String txt = createSampleText();
    add(createTitledPanel("Default", new JTextArea(txt)));
    add(createTitledPanel("Override selectAll", new CustomTextArea(txt)));
    add(createTitledPanel("move Caret top", new EmacsTextArea(txt)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static String createSampleText() {
    String s = "aaa\nbb bb\nc c c\nddd\n\n1234567890\n";
    return String.join("\n", Collections.nCopies(10, s));
  }

  private static Component createTitledPanel(String title, JTextArea editor) {
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
  protected CustomTextArea(String text) {
    super(text);
  }

  @Override public void updateUI() {
    super.updateUI();
    initActionMap();
  }

  private void initActionMap() {
    InputMap im = getInputMap(WHEN_FOCUSED);
    ActionMap am = getActionMap();
    String selectAllKey = "select-all2";
    im.put(KeyStroke.getKeyStroke("ctrl A"), selectAllKey);
    am.put(selectAllKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        Rectangle r = getVisibleRect();
        getActionMap().get(DefaultEditorKit.selectAllAction).actionPerformed(e);
        // Restore the visible rect after the caret has scrolled to the end of the text
        EventQueue.invokeLater(() -> scrollRectToVisible(r));
      }
    });
    // Visual Studio Code: UP and LEFT move the caret to the start of the selection
    String upKey = "caret-up2";
    im.put(KeyStroke.getKeyStroke("UP"), upKey);
    am.put(upKey, new CollapseSelectionAction(DefaultEditorKit.upAction, true));
    String backwardKey = "caret-backward2";
    im.put(KeyStroke.getKeyStroke("LEFT"), backwardKey);
    am.put(backwardKey, new CollapseSelectionAction(DefaultEditorKit.backwardAction, true));
  }
}

class EmacsTextArea extends JTextArea {
  protected EmacsTextArea(String text) {
    super(text);
  }

  @Override public void updateUI() {
    super.updateUI();
    initActionMap();
  }

  private void initActionMap() {
    InputMap im = getInputMap(WHEN_FOCUSED);
    ActionMap am = getActionMap();
    String selectAllKey = "select-all2";
    im.put(KeyStroke.getKeyStroke("ctrl A"), selectAllKey);
    am.put(selectAllKey, new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        selectAll();
      }
    });
    // GNU Emacs: DOWN and RIGHT move the caret to the end of the selection
    String downKey = "caret-down2";
    im.put(KeyStroke.getKeyStroke("DOWN"), downKey);
    am.put(downKey, new CollapseSelectionAction(DefaultEditorKit.downAction, false));
    String forwardKey = "caret-forward2";
    im.put(KeyStroke.getKeyStroke("RIGHT"), forwardKey);
    am.put(forwardKey, new CollapseSelectionAction(DefaultEditorKit.forwardAction, false));
  }

  @Override public void selectAll() {
    Rectangle r = getVisibleRect();
    Document doc = getDocument();
    if (doc != null) {
      // Select from the end to the beginning so that the caret stays at the top
      setCaretPosition(doc.getLength());
      moveCaretPosition(0);
    }
    EventQueue.invokeLater(() -> scrollRectToVisible(r));
  }
}

// Collapses the selection to its start or end, or performs the default caret
// action when nothing is selected.
class CollapseSelectionAction extends TextAction {
  private final String defaultActionName;
  private final boolean toSelectionStart;

  protected CollapseSelectionAction(String defaultActionName, boolean toSelectionStart) {
    super(defaultActionName);
    this.defaultActionName = defaultActionName;
    this.toSelectionStart = toSelectionStart;
  }

  @Override public void actionPerformed(ActionEvent e) {
    JTextComponent c = getTextComponent(e);
    if (c != null) {
      int start = c.getSelectionStart();
      int end = c.getSelectionEnd();
      if (start == end) {
        Optional.ofNullable(c.getActionMap().get(defaultActionName))
            .ifPresent(a -> a.actionPerformed(e));
      } else {
        c.setCaretPosition(toSelectionStart ? start : end);
      }
    }
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
    g2.setColor(getForeground());
    int base = clip.y;
    int start = getLineAtPoint(base);
    int end = getLineAtPoint(base + clip.height);
    FontMetrics fontMetrics = g2.getFontMetrics(font);
    int fontAscent = fontMetrics.getAscent();
    int fontDescent = fontMetrics.getDescent();
    int fontLeading = fontMetrics.getLeading();
    int y = start * fontMetrics.getHeight();
    int right = getComponentWidth(fontMetrics) - getInsets().right;
    for (int i = start; i <= end; i++) {
      String text = Integer.toString(i + 1);
      int x = right - fontMetrics.stringWidth(text);
      y += fontAscent;
      g2.drawString(text, x, y);
      y += fontDescent + fontLeading;
    }
    g2.dispose();
  }
}
