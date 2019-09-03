// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

public final class MainPanel extends JPanel {
  private static final String MYSITE = "https://ateraimemo.com/";
  private static final String HREF = String.format("<html><a href='%s'>%s</a>", MYSITE, MYSITE);

  private MainPanel() {
    super(new GridLayout(3, 1));
    add(makeUrlPanel("Default", HREF));

    // Customize detault html link color in java swing - Stack Overflow
    // https://stackoverflow.com/questions/26749495/customize-detault-html-link-color-in-java-swing
    HTMLEditorKit kit = new HTMLEditorKit();
    StyleSheet styleSheet = kit.getStyleSheet();
    styleSheet.addRule("a{color:#FF0000;}");
    add(makeUrlPanel("styleSheet.addRule(\"a{color:#FF0000;}\")", HREF));

    add(makeUrlPanel("<a style='color:#00FF00'...", String.format("<html><a style='color:#00FF00' href='%s'>%s</a>", MYSITE, MYSITE)));

    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeUrlPanel(String title, String href) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));

    JEditorPane editor = new JEditorPane("text/html", href);
    editor.setOpaque(false);
    editor.setEditable(false);
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);

    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 0);

    c.gridx = 0;
    c.anchor = GridBagConstraints.LINE_END;
    p.add(new JLabel("JLabel:"), c);
    p.add(new JLabel("JEditorPane:"), c);

    c.gridx = 1;
    c.weightx = 1d;
    c.anchor = GridBagConstraints.LINE_START;
    p.add(new JLabel(href), c);
    p.add(editor, c);

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

// class URILabel extends JLabel {
//   protected URILabel(String h) {
//     super(String.format("<html><a href='%s'>%s</a>", h, h));
//     setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//     addMouseListener(new MouseAdapter() {
//       @Override public void mousePressed(MouseEvent e) {
//         Toolkit.getDefaultToolkit().beep();
//       }
//     });
//   }
// }
//
// class HyperlinkButton extends JButton {
//   private static final String UI_CLASS_ID = "LinkViewButtonUI";
//
// //   @Override public String getUIClassID() {
// //     return UI_CLASS_ID;
// //   }
// //
// //   @Override public void setUI(LinkViewButtonUI ui) {
// //     super.setUI(ui);
// //   }
//
//   @Override public void updateUI() {
//     super.updateUI();
//     if (UIManager.get(UI_CLASS_ID) == null) {
//       setUI(BasicLinkViewButtonUI.createUI(this));
//     } else {
//       setUI((LinkViewButtonUI) UIManager.getUI(this));
//     }
//     setForeground(Color.BLUE);
//     setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
//     setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//   }
//
//   public LinkViewButtonUI getUI() {
//     return BasicLinkViewButtonUI.createUI(this);
//   }
//
//   protected HyperlinkButton() {
//     this(null, null);
//   }
//
//   protected HyperlinkButton(Icon icon) {
//     this(null, icon);
//   }
//
//   protected HyperlinkButton(String text) {
//     this(text, null);
//   }
//
//   protected HyperlinkButton(Action a) {
//     this();
//     super.setAction(a);
//   }
//
//   protected HyperlinkButton(String text, Icon icon) {
//     super(text, icon);
//   }
// }
//
// class LinkViewButtonUI extends BasicButtonUI {
//   /* ButtonUI */
// }
//
// class BasicLinkViewButtonUI extends LinkViewButtonUI {
//   private static final LinkViewButtonUI LINKVIEW_BUTTON_UI = new BasicLinkViewButtonUI();
//   private final Dimension size = new Dimension();
//   private final Rectangle viewRect = new Rectangle();
//   private final Rectangle iconRect = new Rectangle();
//   private final Rectangle textRect = new Rectangle();
//
//   public static LinkViewButtonUI createUI(JButton b) {
//     // b.setForeground(Color.BLUE);
//     // b.setBorder(BorderFactory.createEmptyBorder(0, 0, 2, 0));
//     // b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
//     return LINKVIEW_BUTTON_UI;
//   }
//
//   @Override public synchronized void paint(Graphics g, JComponent c) {
//     if (!(c instanceof AbstractButton)) {
//       return;
//     }
//     AbstractButton b = (AbstractButton) c;
//     Font f = c.getFont();
//     g.setFont(f);
//     FontMetrics fm = c.getFontMetrics(f);
//
//     Insets i = c.getInsets();
//     b.getSize(size);
//     viewRect.setBounds(i.left, i.top, size.width - i.left - i.right, size.height - i.top - i.bottom);
//     iconRect.setBounds(0, 0, 0, 0);
//     textRect.setBounds(0, 0, 0, 0);
//
//     String text = SwingUtilities.layoutCompoundLabel(
//       c, fm, b.getText(), null, // altIcon != null ? altIcon : getDefaultIcon(),
//       b.getVerticalAlignment(), b.getHorizontalAlignment(),
//       b.getVerticalTextPosition(), b.getHorizontalTextPosition(),
//       viewRect, iconRect, textRect,
//       0); // b.getText() == null ? 0 : b.getIconTextGap());
//
//     if (c.isOpaque()) {
//       g.setColor(b.getBackground());
//       g.fillRect(0, 0, size.width, size.height);
//     }
//
//     ButtonModel n = b.getModel();
//     if (!n.isSelected() && !m.isPressed() && !m.isArmed() && b.isRolloverEnabled() && m.isRollover()) {
//       g.setColor(Color.BLUE);
//       g.drawLine(viewRect.x, viewRect.y + viewRect.height,
//            viewRect.x + viewRect.width, viewRect.y + viewRect.height);
//     }
//     View v = (View) c.getClientProperty(BasicHTML.propertyKey);
//     if (v == null) {
//       paintText(g, b, textRect, text);
//     } else {
//       v.paint(g, textRect);
//     }
//   }
// }
