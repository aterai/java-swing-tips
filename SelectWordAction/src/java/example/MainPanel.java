// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.Segment;
import javax.swing.text.TextAction;
import javax.swing.text.Utilities;

public final class MainPanel extends JPanel {
  private static final String TEXT = "AA-BB_CC\nAA-bb_CC\naa1-bb2_cc3\naa_(bb)_cc;\n11-22_33";
  private final JTextArea textArea = new JTextArea(TEXT);

  private MainPanel() {
    super(new BorderLayout());

    textArea.getActionMap().put(DefaultEditorKit.selectWordAction, new TextAction(DefaultEditorKit.selectWordAction) {
      @Override public void actionPerformed(ActionEvent e) {
        JTextComponent target = getTextComponent(e);
        if (target != null) {
          try {
            int offs = target.getCaretPosition();
            int begOffs = TextUtils.getWordStart(target, offs);
            int endOffs = TextUtils.getWordEnd(target, offs);
            target.setCaretPosition(begOffs);
            target.moveCaretPosition(endOffs);
          } catch (BadLocationException ex) {
            UIManager.getLookAndFeel().provideErrorFeedback(target);
          }
        }
      }
    });
    JSplitPane split = new JSplitPane();
    split.setResizeWeight(.5);
    split.setLeftComponent(makeTitledPanel("Default", new JTextArea(TEXT)));
    split.setRightComponent(makeTitledPanel("Break words: _ and -", textArea));
    add(split);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.add(new JLabel(title), BorderLayout.NORTH);
    p.add(new JScrollPane(c));
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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

final class TextUtils {
  private TextUtils() {
    /* HideUtilityClassConstructor */
  }

  // @see javax.swint.text.Utilities.getWordStart(...)
  public static int getWordStart(JTextComponent c, int offs) throws BadLocationException {
    Element line = Optional.ofNullable(Utilities.getParagraphElement(c, offs))
        .orElseThrow(() -> new BadLocationException("No word at " + offs, offs));
    Document doc = c.getDocument();
    int lineStart = line.getStartOffset();
    int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
    int offs2 = offs;
    Segment seg = SegmentCache.getSharedSegment();
    doc.getText(lineStart, lineEnd - lineStart, seg);
    if (seg.count > 0) {
      BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
      words.setText(seg);
      int wordPosition = seg.offset + offs - lineStart;
      if (wordPosition >= words.last()) {
        wordPosition = words.last() - 1;
        words.following(wordPosition);
        offs2 = lineStart + words.previous() - seg.offset;
      } else {
        words.following(wordPosition);
        offs2 = lineStart + words.previous() - seg.offset;
        for (int i = offs; i > offs2; i--) {
          char ch = seg.charAt(i - seg.offset);
          if (ch == '_' || ch == '-') {
            offs2 = i + 1;
            break;
          }
        }
      }
    }
    SegmentCache.releaseSharedSegment(seg);
    return offs2;
  }

  // @see javax.swint.text.Utilities.getWordEnd(...)
  public static int getWordEnd(JTextComponent c, int offs) throws BadLocationException {
    Element line = Optional.ofNullable(Utilities.getParagraphElement(c, offs))
        .orElseThrow(() -> new BadLocationException("No word at " + offs, offs));
    Document doc = c.getDocument();
    int lineStart = line.getStartOffset();
    int lineEnd = Math.min(line.getEndOffset(), doc.getLength());
    int offs2 = offs;

    Segment seg = SegmentCache.getSharedSegment();
    doc.getText(lineStart, lineEnd - lineStart, seg);
    if (seg.count > 0) {
      BreakIterator words = BreakIterator.getWordInstance(c.getLocale());
      words.setText(seg);
      int wordPosition = offs - lineStart + seg.offset;
      if (wordPosition >= words.last()) {
        wordPosition = words.last() - 1;
      }
      offs2 = lineStart + words.following(wordPosition) - seg.offset;

      for (int i = offs; i < offs2; i++) {
        char ch = seg.charAt(i - seg.offset);
        if (ch == '_' || ch == '-') {
          offs2 = i;
          break;
        }
      }
    }
    SegmentCache.releaseSharedSegment(seg);
    return offs2;
  }
}

class SegmentCache {
  /**
   * A global cache.
   */
  private static SegmentCache sharedCache = new SegmentCache();

  /**
   * A list of the currently unused Segments.
   */
  private final List<Segment> segments = new ArrayList<>(11);

  /**
   * Returns the shared SegmentCache.
   */
  public static SegmentCache getSharedInstance() {
    return sharedCache;
  }

  /**
   * A convenience method to get a Segment from the shared
   * <code>SegmentCache</code>.
   */
  public static Segment getSharedSegment() {
    return getSharedInstance().getSegment();
  }

  /**
   * A convenience method to release a Segment to the shared
   * <code>SegmentCache</code>.
   */
  public static void releaseSharedSegment(Segment segment) {
    getSharedInstance().releaseSegment(segment);
  }

  // /**
  //  * Creates and returns a SegmentCache.
  //  */
  // public SegmentCache() {
  //   segments = new ArrayList<>(11);
  // }

  /**
   * Returns a <code>Segment</code>. When done, the <code>Segment</code>
   * should be recycled by invoking <code>releaseSegment</code>.
   */
  public Segment getSegment() {
    synchronized (this) {
      int size = segments.size();
      if (size > 0) {
        return segments.remove(size - 1);
      }
    }
    return new CachedSegment();
  }

  /**
   * Releases a Segment. You should not use a Segment after you release it,
   * and you should NEVER release the same Segment more than once, eg:
   * <pre>
   *   segmentCache.releaseSegment(segment);
   *   segmentCache.releaseSegment(segment);
   * </pre>
   * Will likely result in very bad things happening!
   */
  public void releaseSegment(Segment segment) {
    if (segment instanceof CachedSegment) {
      synchronized (this) {
        segment.array = null;
        segment.count = 0;
        segments.add(segment);
      }
    }
  }

  /**
   * CachedSegment is used as a tagging interface to determine if
   * a Segment can successfully be shared.
   */
  private static class CachedSegment extends Segment {
  }
}
