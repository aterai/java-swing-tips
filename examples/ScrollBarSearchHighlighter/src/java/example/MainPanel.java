// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsScrollBarUI;
import java.awt.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.*;
import javax.swing.plaf.ScrollBarUI;
import javax.swing.plaf.metal.MetalScrollBarUI;
import javax.swing.plaf.synth.SynthScrollBarUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private static final HighlightPainter HIGHLIGHT = new DefaultHighlightPainter(Color.YELLOW);
  private static final String PATTERN = "Swing";
  private static final String TEXT = String.join("\n",
      "Trail: Creating a GUI with JFC/Swing",
      "Lesson: Learning Swing by Example",
      "This lesson explains the concepts you need to",
      " use Swing components in building a user interface.",
      " First we examine the simplest Swing application you can write.",
      " Then we present several progressively complicated examples of creating",
      " user interfaces using components in the javax.swing package.",
      " We cover several Swing components, such as buttons, labels, and text areas.",
      " The handling of events is also discussed,",
      " as are layout management and accessibility.",
      " This lesson ends with a set of questions and exercises",
      " so you can test yourself on what you've learned.",
      "https://docs.oracle.com/javase/tutorial/uiswing/learn/index.html");

  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea();
    textArea.setEditable(false);
    textArea.setText(TEXT + TEXT + TEXT);

    JScrollPane scroll = new JScrollPane(textArea);
    scroll.setVerticalScrollBar(new HighlightScrollBar());

    JLabel label = new JLabel(new HighlightIcon(textArea, scroll.getVerticalScrollBar()));
    scroll.setRowHeaderView(label);

    JCheckBox check = new JCheckBox("LineWrap");
    check.addActionListener(e -> {
      JCheckBox cb = (JCheckBox) e.getSource();
      textArea.setLineWrap(cb.isSelected());
    });

    JButton highlight = new JButton("highlight");
    highlight.addActionListener(e -> {
      setHighlight(textArea, PATTERN);
      repaint();
    });

    JButton clear = new JButton("clear");
    clear.addActionListener(e -> {
      textArea.getHighlighter().removeAllHighlights();
      scroll.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.add(check);
    box.add(Box.createHorizontalGlue());
    box.add(highlight);
    box.add(Box.createHorizontalStrut(2));
    box.add(clear);

    add(box, BorderLayout.SOUTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  public static void setHighlight(JTextComponent jtc, String pattern) {
    Highlighter highlighter = jtc.getHighlighter();
    highlighter.removeAllHighlights();
    Document doc = jtc.getDocument();
    try {
      String text = doc.getText(0, doc.getLength());
      Matcher matcher = Pattern.compile(pattern).matcher(text);
      while (matcher.find()) {
        int start = matcher.start();
        int end = matcher.end();
        if (start < end) {
          highlighter.addHighlight(start, end, HIGHLIGHT);
        }
      }
    } catch (BadLocationException | PatternSyntaxException ex) {
      UIManager.getLookAndFeel().provideErrorFeedback(jtc);
    }
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

class HighlightScrollBar extends JScrollBar {
  protected HighlightScrollBar() {
    super(VERTICAL);
  }

  @Override public void updateUI() {
    super.updateUI();
    ScrollBarUI ui = getUI();
    if (ui instanceof WindowsScrollBarUI) {
      setUI(new WindowsHighlightScrollBarUI());
    } else if (!(ui instanceof SynthScrollBarUI)) {
      setUI(new MetalHighlightScrollBarUI());
    }
    setUnitIncrement(10);
  }

  //   @Override public Dimension getPreferredSize() {
  //     Dimension d = super.getPreferredSize();
  //     d.width += 4; // getInsets().left;
  //     return d;
  //   }
}

class HighlightIcon implements Icon {
  private static final Color THUMB_COLOR = new Color(0x32_00_00_FF, true);
  private final JTextComponent textArea;
  private final JScrollBar scrollbar;

  protected HighlightIcon(JTextComponent textArea, JScrollBar scrollbar) {
    this.textArea = textArea;
    this.scrollbar = scrollbar;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    BoundedRangeModel range = scrollbar.getModel();
    int trackHeight = getIconHeight();
    int viewHeight = range.getMaximum() - range.getMinimum();
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y + scrollbar.getInsets().top);
    g2.setPaint(Color.RED);
    HighlightMarkPainter.paintMarks(g2, textArea, getIconWidth(), trackHeight, viewHeight);
    if (scrollbar.isVisible()) {
      g2.setPaint(THUMB_COLOR);
      int thumbY = HighlightMarkPainter.scale(range.getValue(), trackHeight, viewHeight);
      int thumbHeight = HighlightMarkPainter.scale(range.getExtent(), trackHeight, viewHeight);
      g2.fillRect(0, thumbY, getIconWidth(), thumbHeight);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 4;
  }

  @Override public int getIconHeight() {
    Container viewport = SwingUtilities.getAncestorOfClass(JViewport.class, textArea);
    return viewport == null ? scrollbar.getHeight() : viewport.getHeight();
  }
}

final class HighlightMarkPainter {
  private static final int MARK_HEIGHT = 2;

  private HighlightMarkPainter() {
    /* Singleton */
  }

  public static int scale(int value, int trackHeight, int viewHeight) {
    return viewHeight <= 0 ? 0 : (int) (value * trackHeight / (double) viewHeight);
  }

  public static void paintMarks(
      Graphics g, JTextComponent textArea, int width, int trackHeight, int viewHeight) {
    try {
      for (Highlighter.Highlight h : textArea.getHighlighter().getHighlights()) {
        // Java 9: Rectangle r = textArea.modelToView2D(h.getStartOffset()).getBounds();
        Rectangle r = textArea.modelToView(h.getStartOffset());
        g.fillRect(0, scale(r.y, trackHeight, viewHeight), width, MARK_HEIGHT);
      }
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
  }

  public static void paintTrackMarks(Graphics g, JComponent c, Rectangle trackBounds) {
    Container scroll = SwingUtilities.getAncestorOfClass(JScrollPane.class, c);
    if (scroll instanceof JScrollPane) {
      Component view = ((JScrollPane) scroll).getViewport().getView();
      if (view instanceof JTextComponent) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(trackBounds.x, trackBounds.y);
        g2.setPaint(Color.YELLOW);
        JTextComponent tc = (JTextComponent) view;
        paintMarks(g2, tc, trackBounds.width, trackBounds.height, tc.getHeight());
        g2.dispose();
      }
    }
  }
}

class WindowsHighlightScrollBarUI extends WindowsScrollBarUI {
  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    super.paintTrack(g, c, trackBounds);
    HighlightMarkPainter.paintTrackMarks(g, c, trackBounds);
  }
}

class MetalHighlightScrollBarUI extends MetalScrollBarUI {
  @Override protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
    super.paintTrack(g, c, trackBounds);
    HighlightMarkPainter.paintTrackMarks(g, c, trackBounds);
  }
}
