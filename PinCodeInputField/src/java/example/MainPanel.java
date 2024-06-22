// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;
import javax.swing.text.PasswordView;
import javax.swing.text.Segment;
import javax.swing.text.Utilities;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPasswordField password = new JPasswordField(6) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new BasicPasswordFieldUI() {
          @Override public View create(Element elem) {
            return new PasswordView2(elem);
          }
        });
      }
    };

    Box box = Box.createVerticalBox();
    box.add(makePasswordField(new JPasswordField(6)));
    box.add(Box.createVerticalStrut(10));
    box.add(makePasswordField(password));

    JPanel p = new JPanel(new GridBagLayout());
    p.add(box);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private Component makePasswordField(JPasswordField password) {
    password.setCaret(new DefaultCaret() {
      @Override public boolean isSelectionVisible() {
        return false;
      }
    });
    password.setSelectionColor(Color.WHITE);
    password.setFont(password.getFont().deriveFont(30f));
    Document doc = password.getDocument();
    if (doc instanceof AbstractDocument) {
      ((AbstractDocument) doc).setDocumentFilter(new PinCodeDocumentFilter());
    }
    doc.addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) {
        if (e.getDocument().getLength() >= PinCodeDocumentFilter.MAX) {
          JOptionPane.showMessageDialog(getRootPane(), "Try PIN code verification.");
          EventQueue.invokeLater(() -> password.setText(""));
        }
      }

      @Override public void removeUpdate(DocumentEvent e) {
        // Do nothing
      }

      @Override public void changedUpdate(DocumentEvent e) {
        // Do nothing
      }
    });
    return new JLayer<>(password, new PlaceholderLayerUI<>("PIN"));
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

class PinCodeDocumentFilter extends DocumentFilter {
  public static final int MAX = 4;

  @Override public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
    String str = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
    if (str.length() <= MAX && str.matches("\\d+")) {
      super.replace(fb, offset, length, text, attrs);
    }
  }
}

class PasswordView2 extends PasswordView {
  protected PasswordView2(Element elem) {
    super(elem);
  }

  @Override protected int drawUnselectedText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    return drawText(g, x, y, p0, p1);
  }

  // @see drawUnselectedTextImpl(...)
  private int drawText(Graphics g, int x, int y, int p0, int p1) throws BadLocationException {
    Container c = getContainer();
    int j = x;
    if (c instanceof JPasswordField) {
      JPasswordField f = (JPasswordField) c;
      if (f.isEnabled()) {
        g.setColor(f.getForeground());
      } else {
        g.setColor(f.getDisabledTextColor());
      }
      Graphics2D g2 = (Graphics2D) g;
      char echoChar = f.getEchoChar();
      int n = p1 - p0;
      for (int i = 0; i < n; i++) {
        j = i == n - 1 ? drawLastChar(g2, j, y, i) : drawEchoCharacter(g, j, y, echoChar);
      }
    }
    return j;
  }

  private int drawLastChar(Graphics g, int x, int y, int p1) throws BadLocationException {
    Graphics2D g2 = (Graphics2D) g;
    Font font = g2.getFont();
    float fs = font.getSize2D();
    double w = font.getStringBounds("0", g2.getFontRenderContext()).getWidth();
    int sz = (int) ((fs - w) / 2d);
    Document doc = getDocument();
    Segment s = new Segment(); // SegmentCache.getSharedSegment();
    doc.getText(p1, 1, s);
    // int ret = Utilities.drawTabbedText(s, x, y, g, this, p1);
    // SegmentCache.releaseSharedSegment(s);
    return Utilities.drawTabbedText(s, x + sz, y, g, this, p1);
  }
}

class PlaceholderLayerUI<E extends JTextComponent> extends LayerUI<E> {
  private final JLabel hint;

  protected PlaceholderLayerUI(String hintMessage) {
    super();
    this.hint = new JLabel(hintMessage) {
      @Override public void updateUI() {
        super.updateUI();
        String inactive = "TextField.inactiveForeground";
        setForeground(UIManager.getLookAndFeelDefaults().getColor(inactive));
      }
    };
  }

  @Override public void updateUI(JLayer<? extends E> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(hint);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JTextComponent tc = (JTextComponent) ((JLayer<?>) c).getView();
      if (tc.getText().isEmpty()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(hint.getForeground());
        Rectangle r = SwingUtilities.calculateInnerArea(tc, null);
        Dimension d = hint.getPreferredSize();
        int yy = (int) (r.y + (r.height - d.height) / 2d);
        SwingUtilities.paintComponent(g2, hint, tc, r.x, yy, d.width, d.height);
        g2.dispose();
      }
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.FOCUS_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
  }

  @Override public void processFocusEvent(FocusEvent e, JLayer<? extends E> l) {
    l.getView().repaint();
  }
}
