// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea("★ ☆ ⤾ ⤿ ⥀ ⥁ ⇐ ⇒ ⇦ ⇨ ↺ ↻ ↶ ↷");
    JPopupMenu popup = makePopup();
    textArea.setComponentPopupMenu(popup);
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopup() {
    JPopupMenu popup = new JPopupMenu();
    GridBagConstraints c = new GridBagConstraints();
    popup.setLayout(new GridBagLayout());

    c.weightx = 1d;
    c.weighty = 0d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.anchor = GridBagConstraints.CENTER;

    c.gridy = 0;
    popup.add(makeButton("⇦"), c);
    popup.add(makeButton("⇨"), c);
    popup.add(makeButton("↻"), c);
    popup.add(makeButton("✩"), c);

    c.insets = new Insets(2, 0, 2, 0);
    c.gridwidth = 4;
    c.gridx = 0;
    c.gridy = GridBagConstraints.RELATIVE;
    popup.add(new JSeparator(), c);

    c.insets = new Insets(0, 0, 0, 0);
    popup.add(new JMenuItem("0000000"), c);
    popup.add(new JPopupMenu.Separator(), c);
    popup.add(new JMenuItem("1111"), c);
    popup.add(new JMenuItem("2222222222222222"), c);
    popup.add(new JMenuItem("333333333"), c);

    return popup;
  }

  private static AbstractButton makeButton(String symbol) {
    Icon icon = new SymbolIcon(symbol);
    JMenuItem b = new JMenuItem() {
      private final Dimension dim = new Dimension(icon.getIconWidth(), icon.getIconHeight());
      @Override public Dimension getPreferredSize() {
        return dim;
      }

      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension cd = getSize();
        Dimension pd = getPreferredSize();
        int ix = Math.round((cd.width - pd.width) / 2f);
        int iy = Math.round((cd.height - pd.height) / 2f);
        icon.paintIcon(this, g, ix, iy);
      }
    };
    b.setOpaque(true);
    if (Objects.equals("⇨", symbol)) { // test
      b.setEnabled(false);
      b.setToolTipText("forward");
    }
    return b;
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

class SymbolIcon implements Icon {
  private static final Dimension ICON_SIZE = new Dimension(32, 32);
  private final Font font = new Font(Font.MONOSPACED, Font.BOLD, ICON_SIZE.height);
  private final String str;

  protected SymbolIcon(String str) {
    this.str = str;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.translate(x, y);
    g2.setPaint(c.isEnabled() ? Color.BLACK : Color.GRAY);

    FontRenderContext frc = g2.getFontRenderContext();
    Shape symbol = new TextLayout(str, font, frc).getOutline(null);
    Rectangle2D b = symbol.getBounds2D();
    double cx = getIconWidth() / 2d - b.getCenterX();
    double cy = getIconHeight() / 2d - b.getCenterY();
    AffineTransform toCenterAt = AffineTransform.getTranslateInstance(cx, cy);
    g2.fill(toCenterAt.createTransformedShape(symbol));
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return ICON_SIZE.width;
  }

  @Override public int getIconHeight() {
    return ICON_SIZE.height;
  }
}
