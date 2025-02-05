// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String very = String.join(" ", Collections.nCopies(10, "very"));
    String txt = "A " + very + " long tooltip that must be line wrap";
    JButton b1 = new JButton("JToolTip(Default)");
    b1.setToolTipText(txt + ": 1");
    JButton b2 = makeButton("LineWrapToolTip: Long");
    b2.setToolTipText(txt + ": 2");
    JButton b3 = makeButton("LineWrapToolTip: Short");
    b3.setToolTipText("ToolTipText: 3");
    JTextField field = makeTextField(txt + ": 4");
    field.setToolTipText(field.getText());
    Box box = Box.createVerticalBox();
    box.add(b1);
    box.add(Box.createVerticalStrut(10));
    box.add(b2);
    box.add(Box.createVerticalStrut(10));
    box.add(b3);
    box.add(Box.createVerticalGlue());
    add(box, BorderLayout.WEST);
    add(field, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeButton(String title) {
    return new JButton(title) {
      private transient JToolTip tip;

      @Override public JToolTip createToolTip() {
        if (tip == null) {
          tip = new LineWrapToolTip();
          tip.setComponent(this);
        }
        return tip;
      }

      @Override public String getToolTipText(MouseEvent e) {
        String tipText = super.getToolTipText(e);
        EventQueue.invokeLater(() ->
            Optional.ofNullable(SwingUtilities.getWindowAncestor(tip))
                .filter(w -> w.getType() == Window.Type.POPUP)
                .ifPresent(Window::pack));
        return tipText;
      }
    };
  }

  private JTextField makeTextField(String txt) {
    JTextField field = new JTextField(20) {
      private transient JToolTip tip;

      @Override public JToolTip createToolTip() {
        if (tip == null) {
          tip = new LineWrapToolTip();
          tip.setComponent(this);
        }
        return tip;
      }

      @Override public String getToolTipText(MouseEvent e) {
        String tipText = getText();
        EventQueue.invokeLater(() ->
            Optional.ofNullable(SwingUtilities.getWindowAncestor(tip))
                .filter(w -> w.getType() == Window.Type.POPUP)
                .ifPresent(Window::pack));
        return tipText;
      }
    };
    field.setText(txt);
    return field;
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

class LineWrapToolTip extends JToolTip {
  private static final double JAVA17 = 17.0;
  private static final JLabel MEASURER = new JLabel(" ");
  private static final int TIP_WIDTH = 200;
  private final JTextArea textArea = new JTextArea(0, 20);

  protected LineWrapToolTip() {
    super();
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setOpaque(true);
    // textArea.setColumns(20);
    LookAndFeel.installColorsAndFont(
        textArea, "ToolTip.background", "ToolTip.foreground", "ToolTip.font");
    setLayout(new BorderLayout());
    add(textArea);
  }

  @Override public final void setLayout(LayoutManager mgr) {
    super.setLayout(mgr);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public Dimension getPreferredSize() {
    Dimension d = getLayout().preferredLayoutSize(this);
    Dimension dim;
    String version = System.getProperty("java.specification.version");
    if (Double.parseDouble(version) >= JAVA17) {
      dim = getTextAreaSize17(d);
    } else {
      dim = getTextAreaSize8(d);
    }
    return dim;
  }

  private Dimension getTextAreaSize8(Dimension d) {
    Font font = textArea.getFont();
    MEASURER.setFont(font);
    MEASURER.setText(textArea.getText());
    Insets i = getInsets();
    int pad = getTextAreaPaddingWidth(i);
    // d.width = Math.min(d.width, MEASURER.getPreferredSize().width + pad);
    d.width = Math.min(TIP_WIDTH, MEASURER.getPreferredSize().width + pad);

    // JDK-8226513 JEditorPane is shown with incorrect size - Java Bug System
    // https://bugs.openjdk.org/browse/JDK-8226513
    AttributedString as = new AttributedString(textArea.getText());
    as.addAttribute(TextAttribute.FONT, font);
    AttributedCharacterIterator aci = as.getIterator();
    FontMetrics fm = textArea.getFontMetrics(font);
    FontRenderContext frc = fm.getFontRenderContext();
    LineBreakMeasurer lbm = new LineBreakMeasurer(aci, frc);
    float y = 0f;
    while (lbm.getPosition() < aci.getEndIndex()) {
      TextLayout tl = lbm.nextLayout(TIP_WIDTH);
      y += tl.getDescent() + tl.getLeading() + tl.getAscent();
    }
    d.height = (int) y + getTextAreaPaddingHeight(i);
    return d;
  }

  private Dimension getTextAreaSize17(Dimension d) {
    MEASURER.setFont(textArea.getFont());
    MEASURER.setText(textArea.getText());
    int pad = getTextAreaPaddingWidth(getInsets());
    d.width = Math.min(d.width, MEASURER.getPreferredSize().width + pad);
    return d;
  }

  private int getTextAreaPaddingWidth(Insets i) {
    // @see BasicTextUI.java
    // margin required to show caret in the rightmost position
    int caretMargin = -1;
    Object property = UIManager.get("Caret.width");
    if (property instanceof Number) {
      caretMargin = ((Number) property).intValue();
    }
    property = textArea.getClientProperty("caretWidth");
    if (property instanceof Number) {
      caretMargin = ((Number) property).intValue();
    }
    if (caretMargin < 0) {
      caretMargin = 1;
    }
    Insets ti = textArea.getInsets();
    return i.left + i.right + ti.left + ti.right + caretMargin;
    // Insets tm = textArea.getMargin();
    // return i.left + i.right + ti.left + ti.right + tm.left + tm.right;
  }

  private int getTextAreaPaddingHeight(Insets i) {
    Insets ti = textArea.getInsets();
    return i.top + i.bottom + ti.top + ti.bottom;
  }

  @Override public void setTipText(String tipText) {
    String oldValue = textArea.getText();
    textArea.setText(tipText);
    firePropertyChange("tiptext", oldValue, tipText);
    if (!Objects.equals(oldValue, tipText)) {
      revalidate();
      repaint();
    }
  }

  @Override public String getTipText() {
    return Optional.ofNullable(textArea).map(JTextArea::getText).orElse(null);
  }
}
