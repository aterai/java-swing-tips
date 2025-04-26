// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.View;
import javax.swing.text.html.HTMLDocument;

public final class MainPanel extends JPanel {
  private static final String LINK = "https://ateraimemo.com/";
  private static final String HTML_TEXT = String.join("\n",
      "<html><body>",
      "html tag: <br /><a href='" + LINK + "'>" + LINK + "</a>",
      "</body></html>"
  );
  private static String tooltip;

  private MainPanel() {
    super(new BorderLayout());
    JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    sp.setResizeWeight(.5);
    sp.setTopComponent(new JScrollPane(makeEditorPane(false)));
    sp.setBottomComponent(new JScrollPane(makeEditorPane(true)));
    add(sp);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JEditorPane makeEditorPane(boolean editable) {
    JEditorPane editor = new JEditorPane();
    editor.setEditable(editable);
    editor.setContentType("text/html");
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setText(HTML_TEXT);
    editor.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        JOptionPane.showMessageDialog(editor, "Clicked on the link " + e.getURL());
      } else if (e.getEventType() == HyperlinkEvent.EventType.ENTERED) {
        tooltip = editor.getToolTipText();
        // URL url = e.getURL();
        // editor.setToolTipText(Objects.nonNull(url) ? url.toExternalForm() : null);
        String txt = Optional.ofNullable(e.getURL()).map(URL::toExternalForm).orElse(null);
        editor.setToolTipText(txt);
      } else if (e.getEventType() == HyperlinkEvent.EventType.EXITED) {
        editor.setToolTipText(tooltip);
      }
    });

    HTMLDocument doc = (HTMLDocument) editor.getDocument();
    Style s = doc.addStyle("button", null);
    StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
    HyperlinkButton button = new HyperlinkButton(LINK);
    String msg = "Clicked on the link " + button.getText();
    button.addActionListener(e -> JOptionPane.showMessageDialog(editor, msg));
    button.setToolTipText("button: " + button.getText());
    button.setOpaque(false);
    StyleConstants.setComponent(s, button);
    try {
      doc.insertString(doc.getLength(), "\n----\nJButton:\n", null);
      doc.insertString(doc.getLength(), LINK + "\n", doc.getStyle("button"));
      // doc.insertString(doc.getLength(), "\n", null);
    } catch (BadLocationException ex) {
      // should never happen
      RuntimeException wrap = new StringIndexOutOfBoundsException(ex.offsetRequested());
      wrap.initCause(ex);
      throw wrap;
    }
    return editor;
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

// class URILabel extends JLabel {
//   private final String href;
//   protected URILabel(String h) {
//     super(String.format("<html><a href='%s'>%s</a>", h, h));
//     href = str;
//     setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//     addMouseListener(new MouseAdapter() {
//       @Override public void mousePressed(MouseEvent e) {
//         UIManager.getLookAndFeel().provideErrorFeedback(e.getComponent());
//       }
//     });
//   }
// }

class HyperlinkButton extends JButton {
  private static final String UI_CLASS_ID = "LinkViewButtonUI";

  protected HyperlinkButton() {
    this(null, null);
  }

  protected HyperlinkButton(Icon icon) {
    this(null, icon);
  }

  protected HyperlinkButton(String text) {
    this(text, null);
  }

  protected HyperlinkButton(Action a) {
    this();
    super.setAction(a);
  }

  protected HyperlinkButton(String text, Icon icon) {
    super(text, icon);
  }

  // @Override public String getUIClassID() {
  //   return UI_CLASS_ID;
  // }

  // @Override public void setUI(LinkViewButtonUI ui) {
  //   super.setUI(ui);
  // }

  @Override public void updateUI() {
    super.updateUI();
    if (Objects.nonNull(UIManager.get(UI_CLASS_ID))) {
      setUI((LinkViewButtonUI) UIManager.getUI(this));
    } else {
      setUI(BasicLinkViewButtonUI.createUI(this));
    }
    setForeground(Color.BLUE);
    setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
  }

  @Override public LinkViewButtonUI getUI() {
    return BasicLinkViewButtonUI.createUI(this);
  }
}

class LinkViewButtonUI extends BasicButtonUI {
  /* ButtonUI */
}

class BasicLinkViewButtonUI extends LinkViewButtonUI {
  private static final LinkViewButtonUI LINKVIEW_UI = new BasicLinkViewButtonUI();
  // private final Dimension size = new Dimension();
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  public static LinkViewButtonUI createUI(JButton b) {
    // b.setForeground(Color.BLUE);
    // b.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    // b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    return LINKVIEW_UI;
  }

  @Override public void paint(Graphics g, JComponent c) {
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
        null, // icon != null ? icon : getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        viewRect,
        iconRect,
        textRect,
        0); // b.getText() == null ? 0 : b.getIconTextGap());

    if (c.isOpaque()) {
      g.setColor(b.getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }

    ButtonModel m = b.getModel();
    boolean isRollover = b.isRolloverEnabled() && m.isRollover();
    if (!m.isSelected() && !m.isPressed() && !m.isArmed() && isRollover) {
      g.setColor(Color.BLUE);
      int yh = viewRect.y + viewRect.height;
      g.drawLine(viewRect.x, yh, viewRect.x + viewRect.width, yh);
    }
    Object o = c.getClientProperty(BasicHTML.propertyKey);
    if (o instanceof View) {
      ((View) o).paint(g, textRect);
    } else {
      paintText(g, b, textRect, text);
    }
  }
}
