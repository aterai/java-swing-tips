// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.swing.*;
import javax.swing.plaf.basic.BasicPasswordFieldUI;
import javax.swing.text.Element;
import javax.swing.text.PasswordView;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    JPasswordField pf1 = new JPasswordField();
    pf1.setEchoChar('★'); // U+2605

    JPasswordField pf2 = new JPasswordField() {
      @Override public void updateUI() {
        super.updateUI();
        setUI(IconPasswordFieldUI.createUI(this));
      }
    };

    add(makeTitledPanel("setEchoChar('★')", pf1));
    add(makeTitledPanel("drawEchoCharacter", pf2));
    setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
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
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class IconPasswordFieldUI extends BasicPasswordFieldUI {
  protected static final StarIcon ICON = new StarIcon();

  public static IconPasswordFieldUI createUI(JPasswordField c) {
    c.setEchoChar('■'); // U+25A0: As wide as a CJK character cell (full width)
    return new IconPasswordFieldUI();
  }

  @Override public View create(Element elem) {
    return new IconPasswordView(elem);
  }

  private static class IconPasswordView extends PasswordView {
    protected IconPasswordView(Element element) {
      super(element);
    }

    @Override protected int drawEchoCharacter(Graphics g, int x, int y, char c) {
      // Graphics2D g2 = (Graphics2D) g.create();
      // g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      // FontMetrics fm = g2.getFontMetrics();
      // int r = fm.charWidth(c) - 4;
      // // g2.setPaint(Color.GRAY);
      // g2.drawRect(x + 2, y + 4 - fm.getAscent(), r, r);
      // // g2.setPaint(Color.GRAY.brighter());
      // g2.fillOval(x + 2, y + 4 - fm.getAscent(), r, r);
      // g2.dispose();
      // return x + fm.charWidth(c);

      FontMetrics fm = g.getFontMetrics();
      ICON.paintIcon(null, g, x, y - fm.getAscent());
      return x + ICON.getIconWidth(); // fm.charWidth(c);
    }

    // // Java 9 warning: [deprecation]
    // // drawEchoCharacter(Graphics,int,int,char) in PasswordView has been deprecated
    // @Override protected float drawEchoCharacter(Graphics2D g, float x, float y, char c) {
    //   Graphics2D g2 = (Graphics2D) g.create();
    //   FontMetrics fm = g2.getFontMetrics();
    //   g2.translate(x, y - fm.getAscent());
    //   ICON.paintIcon(null, g, 0, 0);
    //   g2.dispose();
    //   return x + ICON.getIconWidth();
    // }
  }
}

class StarIcon implements Icon {
  private final Shape star = makeStar(6, 3, 8);

  public Path2D makeStar(int r1, int r2, int vc) {
    double or = Math.max(r1, r2);
    double ir = Math.min(r1, r2);
    double agl = 0d;
    double add = Math.PI / vc;
    Path2D p = new Path2D.Double();
    p.moveTo(or, 0d);
    for (int i = 0; i < vc * 2 - 1; i++) {
      agl += add;
      double r = i % 2 == 0 ? ir : or;
      p.lineTo(r * Math.cos(agl), r * Math.sin(agl));
    }
    p.closePath();
    AffineTransform at = AffineTransform.getRotateInstance(-Math.PI / 2d, or, 0d);
    return new Path2D.Double(p, at);
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.PINK);
    g2.fill(star);
    // g2.setPaint(Color.BLACK);
    // g2.draw(star);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return star.getBounds().width;
  }

  @Override public int getIconHeight() {
    return star.getBounds().height;
  }
}
