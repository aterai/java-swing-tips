// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import javax.swing.*;

public final class MainPanel extends JPanel {
  // private static final String PropertyPrefix = "MenuItem";
  private static final String PRE = "RadioButtonMenuItem.";

  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    info(log);
    JPopupMenu popup = makePopup();
    log.setComponentPopupMenu(popup);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void info(JTextArea log) {
    String key = PRE + "margin";
    log.append(String.format("%s: %s%n", key, UIManager.getInsets(key)));
    log.append(infoInt(PRE + "minimumTextOffset"));
    log.append(infoInt(PRE + "afterCheckIconGap"));
    log.append(infoInt(PRE + "checkIconOffset"));
    Icon icon = getCheckIcon();
    log.append(String.format("%scheckIcon: %s%n", PRE, icon));
    if (icon != null) {
      int w = icon.getIconWidth();
      int h = icon.getIconHeight();
      log.append(String.format("  checkIcon size -> (%dx%d)%n", w, h));
    }
  }

  private static String infoInt(String key) {
    return String.format("%s: %d%n", key, UIManager.getInt(key));
  }

  private static Icon getCheckIcon() {
    return UIManager.getIcon(PRE + "checkIcon");
  }

  private static JPopupMenu makePopup() {
    // UIManager.put("RadioButtonMenuItem.margin", new Insets(2, -31, 2, 2));
    UIManager.put(PRE + "minimumTextOffset", 10);
    UIManager.put(PRE + "afterCheckIconGap", 0);
    UIManager.put(PRE + "checkIconOffset", 0);
    Icon checkIcon = getCheckIcon();
    int height = checkIcon == null ? 22 : checkIcon.getIconHeight();
    UIManager.put(PRE + "checkIcon", new EmptyIcon());
    Dimension d = new Dimension(100, height);
    JPopupMenu popup = new JPopupMenu();
    ButtonGroup bg = new ButtonGroup();
    Arrays.asList(
        makeMenuItem("0.5 pt", .5f, d),
        makeMenuItem("0.75 pt", .75f, d),
        makeMenuItem("1 pt", 1f, d),
        makeMenuItem("1.5 pt", 1.5f, d),
        makeMenuItem("2.25 pt", 2.25f, d),
        makeMenuItem("3 pt", 3f, d)
    ).forEach(m -> {
      popup.add(m);
      bg.add(m);
    });
    return popup;
  }

  private static JMenuItem makeMenuItem(String txt, float width, Dimension d) {
    float px = width * Toolkit.getDefaultToolkit().getScreenResolution() / 72f;
    return new JRadioButtonMenuItem(txt, new LineIcon(new BasicStroke(px), d)) {
      @Override protected void init(String text, Icon icon) {
        super.init(text, icon);
        setHorizontalTextPosition(LEADING);
        setHorizontalAlignment(TRAILING);
      }

      @Override protected void paintComponent(Graphics g) {
        if (isSelected()) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(new Color(0xAA_64_AA_FF, true));
          g2.fillRect(0, 0, getWidth(), getHeight());
          g2.dispose();
        }
        super.paintComponent(g);
      }
    };
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

class LineIcon implements Icon {
  private final Stroke stroke;
  private final Dimension size;

  protected LineIcon(Stroke stroke, Dimension size) {
    this.stroke = stroke;
    this.size = size;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setColor(Color.BLACK);
    g2.setStroke(stroke);
    int yy = y + getIconHeight() / 2;
    g2.drawLine(x + 5, yy, x + getIconWidth() - 5, yy);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return size.width;
  }

  @Override public int getIconHeight() {
    return size.height;
  }
}

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    // empty
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
