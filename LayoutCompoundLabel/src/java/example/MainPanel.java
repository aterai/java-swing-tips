// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    Icon icon = UIManager.getIcon("OptionPane.informationIcon");
    JLabel label = new JLabel("OptionPane.informationIcon", icon, SwingConstants.LEADING) {
      private final Rectangle viewRect = new Rectangle();
      private final Rectangle iconRect = new Rectangle();
      private final Rectangle textRect = new Rectangle();

      @Override public String getToolTipText(MouseEvent e) {
        SwingUtilities.calculateInnerArea(this, viewRect);
        SwingUtilities.layoutCompoundLabel(
            this,
            this.getFontMetrics(this.getFont()),
            this.getText(),
            this.getIcon(),
            this.getVerticalAlignment(),
            this.getHorizontalAlignment(),
            this.getVerticalTextPosition(),
            this.getHorizontalTextPosition(),
            viewRect,
            iconRect,
            textRect,
            this.getIconTextGap());
        String tip = super.getToolTipText(e);
        Point pt = e.getPoint();
        if (tip != null) {
          String type;
          if (iconRect.contains(pt)) {
            type = "Icon";
          } else if (textRect.contains(pt)) {
            type = "Text";
          } else if (viewRect.contains(pt)) {
            type = "InnerArea";
          } else {
            type = "Border";
          }
          tip = String.format("%s: %s", type, tip);
        }
        return tip;
      }
    };
    label.setOpaque(true);
    label.setBackground(Color.GREEN);
    label.setBorder(BorderFactory.createMatteBorder(20, 10, 50, 30, Color.RED));
    label.setToolTipText("ToolTipText");

    JMenuItem item = new IconTooltipItem("Information", icon);
    item.setToolTipText("Information item");
    JMenu menu = new JMenu("Menu");
    menu.add(item);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(menu);
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));

    add(label);
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

class IconTooltipItem extends JMenuItem {
  private static final Rectangle VIEW_RECT = new Rectangle();
  private static final Rectangle ICON_RECT = new Rectangle();
  private static final Rectangle TEXT_RECT = new Rectangle();

  protected IconTooltipItem(String text, Icon icon) {
    super(text, icon);
  }

  @Override public String getToolTipText(MouseEvent e) {
    SwingUtilities.calculateInnerArea(this, VIEW_RECT);
    SwingUtilities.layoutCompoundLabel(
        this,
        this.getFontMetrics(this.getFont()),
        this.getText(),
        this.getIcon(),
        this.getVerticalAlignment(),
        this.getHorizontalAlignment(),
        this.getVerticalTextPosition(),
        this.getHorizontalTextPosition(),
        VIEW_RECT,
        ICON_RECT,
        TEXT_RECT,
        this.getIconTextGap());
    String tip = super.getToolTipText(e);
    if (tip != null) {
      tip = (ICON_RECT.contains(e.getPoint()) ? "Icon: " : "Text: ") + tip;
    }
    return tip;
  }
}
