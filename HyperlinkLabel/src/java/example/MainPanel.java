// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.HyperlinkEvent;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridBagLayout());
    String link = "https://ateraimemo.com/";
    String html = String.format("<html><a href='%s'>%s</a>", link, link);
    JEditorPane editor = new JEditorPane("text/html", html) {
      @Override public void updateUI() {
        super.updateUI();
        setOpaque(false); // editor.setBackground(getBackground());
        setEditable(false); // REQUIRED
        setBackground(new Color(0x0, true)); // Nimbus???
        putClientProperty(HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
      }
    };
    editor.addHyperlinkListener(e -> {
      if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });

    Action browseAction = new AbstractAction(link) {
      @Override public void actionPerformed(ActionEvent e) {
        try {
          if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(new URI(link));
          }
        } catch (IOException | URISyntaxException ex) {
          ex.printStackTrace();
          UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
        }
      }
    };

    Map<String, Component> map = Collections.synchronizedMap(new LinkedHashMap<>(4));
    map.put("JLabel+MouseListener: ", new UrlLabel(link));
    map.put("JButton: ", new JButton(browseAction));
    map.put("JButton+ButtonUI: ", new HyperlinkButton(browseAction));
    map.put("JEditorPane+HyperlinkListener: ", editor);

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 0);
    map.forEach((k, v) -> {
      c.gridx = 0;
      c.anchor = GridBagConstraints.LINE_END;
      add(new JLabel(k), c);
      c.gridx = 1;
      c.anchor = GridBagConstraints.LINE_START;
      add(v, c);
    });

    Border inside = BorderFactory.createEmptyBorder(2, 5 + 2, 2, 5 + 2);
    Border outside = BorderFactory.createTitledBorder("HyperlinkLabel");
    setBorder(BorderFactory.createCompoundBorder(outside, inside));

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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class UrlLabel extends JLabel {
  private transient MouseListener handler;

  protected UrlLabel(String h) {
    super(String.format("<html><a href='%s'>%s", h, h));
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    super.updateUI();
    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    handler = new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        UIManager.getLookAndFeel().provideErrorFeedback(e.getComponent());
      }
    };
    addMouseListener(handler);
  }
}

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
  private static final LinkViewButtonUI LINK_VIEW = new BasicLinkViewButtonUI();
  // private final Dimension size = new Dimension();
  private final Rectangle viewRect = new Rectangle();
  private final Rectangle iconRect = new Rectangle();
  private final Rectangle textRect = new Rectangle();

  public static LinkViewButtonUI createUI(JButton b) {
    // b.setForeground(Color.BLUE);
    // b.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
    // b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    b.setOpaque(false);
    return LINK_VIEW;
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
