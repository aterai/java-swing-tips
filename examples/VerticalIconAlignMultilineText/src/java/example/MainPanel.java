// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsCheckBoxUI;
import java.awt.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.swing.*;
import javax.swing.plaf.basic.BasicCheckBoxUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String txt = "<html>The vertical alignment of this text gets offset when the font changes.";
    JCheckBox check1 = new JCheckBox(txt);
    check1.setVerticalTextPosition(SwingConstants.TOP);

    JCheckBox check2 = new JCheckBox(txt) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsCheckBoxUI) {
          setUI(new WindowsVerticalAlignmentCheckBoxUI());
        } else {
          setUI(new BasicVerticalAlignmentCheckBoxUI());
        }
        setVerticalTextPosition(TOP);
      }
    };

    List<? extends Component> list = Arrays.asList(check1, check2);
    Font font0 = check1.getFont();
    Font font1 = font0.deriveFont(20f);

    JToggleButton button = new JToggleButton("setFont: 24pt");
    button.addActionListener(e -> {
      boolean flag = button.isSelected();
      for (Component c : list) {
        c.setFont(flag ? font1 : font0);
      }
    });

    JPanel p = new JPanel(new GridLayout(1, 2, 2, 2));
    p.add(makeTitledPanel("SwingConstants.TOP", check1));
    p.add(makeTitledPanel("First line center", check2));
    add(p);
    add(button, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
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

final class HtmlViewUtils {
  private HtmlViewUtils() {
    /* Singleton */
  }

  public static int getFirstLineCenterY(String text, AbstractButton c, Rectangle iconRect) {
    int y = 0;
    if (Objects.nonNull(text) && c.getVerticalTextPosition() == SwingConstants.TOP) {
      Object o = c.getClientProperty(BasicHTML.propertyKey);
      if (o instanceof View) {
        View v = (View) o;
        try {
          Element e = v.getElement().getElement(0);
          Shape s = new Rectangle();
          Position.Bias b = Position.Bias.Forward;
          s = v.modelToView(e.getStartOffset(), b, e.getEndOffset(), b, s);
          // System.out.println("v.h: " + s.getBounds());
          y = Math.round(Math.abs(s.getBounds().height - iconRect.height) / 2f);
        } catch (BadLocationException ex) {
          // should never happen
          RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
          wrap.initCause(ex);
          throw wrap;
        }
      }
    }
    return y;
  }
}

class WindowsVerticalAlignmentCheckBoxUI extends WindowsCheckBoxUI {
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method damage overrides synchronized method in DefaultCaret
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void paint(Graphics g, JComponent c) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    Font f = b.getFont();
    g.setFont(f);

    SwingUtilities.calculateInnerArea(c, viewRect);
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);

    String text = SwingUtilities.layoutCompoundLabel(
        c,
        c.getFontMetrics(f),
        b.getText(),
        getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        Objects.nonNull(b.getText()) ? b.getIconTextGap() : 0);

    // // fill background
    // if (c.isOpaque()) {
    //   g.setColor(b.getBackground());
    //   g.fillRect(0, 0, c.getWidth(), c.getHeight());
    // }

    // Paint the radio button
    int y = HtmlViewUtils.getFirstLineCenterY(text, b, iconRect);
    getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y + y);

    // Draw the Text
    if (Objects.nonNull(text)) {
      Object o = c.getClientProperty(BasicHTML.propertyKey);
      if (o instanceof View) {
        ((View) o).paint(g, textRect);
      } else {
        paintText(g, b, textRect, text);
      }
      if (b.hasFocus() && b.isFocusPainted()) {
        paintFocus(g, textRect, b.getSize());
      }
    }
  }

  @Override protected void paintFocus(Graphics g, Rectangle txtRect, Dimension sz) {
    if (txtRect.width > 0 && txtRect.height > 0) {
      super.paintFocus(g, txtRect, sz);
    }
  }
}

class BasicVerticalAlignmentCheckBoxUI extends BasicCheckBoxUI {
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method paint overrides synchronized method in BasicCheckBoxUI
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void paint(Graphics g, JComponent c) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    Font f = b.getFont();
    g.setFont(f);

    SwingUtilities.calculateInnerArea(c, viewRect);
    iconRect.setBounds(0, 0, 0, 0);
    textRect.setBounds(0, 0, 0, 0);

    String text = SwingUtilities.layoutCompoundLabel(
        c,
        c.getFontMetrics(f),
        b.getText(),
        getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        Objects.nonNull(b.getText()) ? b.getIconTextGap() : 0);

    // // fill background
    // if (c.isOpaque()) {
    //   g.setColor(b.getBackground());
    //   g.fillRect(0, 0, c.getWidth(), c.getHeight());
    // }

    // Paint the radio button
    int y = HtmlViewUtils.getFirstLineCenterY(text, b, iconRect);
    getDefaultIcon().paintIcon(c, g, iconRect.x, iconRect.y + y);

    // Draw the Text
    if (Objects.nonNull(text)) {
      Object o = c.getClientProperty(BasicHTML.propertyKey);
      if (o instanceof View) {
        ((View) o).paint(g, textRect);
      } else {
        paintText(g, b, textRect, text);
      }
      if (b.hasFocus() && b.isFocusPainted()) {
        paintFocus(g, textRect, b.getSize());
      }
    }
  }

  @Override protected void paintFocus(Graphics g, Rectangle txtRect, Dimension sz) {
    if (txtRect.width > 0 && txtRect.height > 0) {
      super.paintFocus(g, txtRect, sz);
    }
  }
}
