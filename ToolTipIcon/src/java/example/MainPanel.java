// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path = "example/wi0124-48.png";
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    Icon icon = Optional.ofNullable(url).map(u -> {
      try (InputStream s = u.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("html.missingImage");
      }
    }).orElseGet(() -> UIManager.getIcon("html.missingImage"));
    JLabel l1 = makeLabel1(icon);
    l1.setToolTipText("Test1");
    JLabel l2 = makeLabel2(icon);
    l2.setToolTipText("Test2");
    JLabel l3 = new JLabel("ToolTip icon using HTML tags"); // align='middle'
    l3.setToolTipText(String.format("<html><img src='%s'>Test3</img>", url));
    Box box = Box.createVerticalBox();
    box.add(l1);
    box.add(Box.createVerticalStrut(20));
    box.add(l2);
    box.add(Box.createVerticalStrut(20));
    box.add(l3);
    box.add(Box.createVerticalGlue());
    add(box);
    setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
    setPreferredSize(new Dimension(320, 240));
  }

  private JLabel makeLabel1(Icon icon) {
    return new JLabel("ToolTip icon using JLabel") {
      @Override public JToolTip createToolTip() {
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
        JToolTip tip = new JToolTip() {
          @Override public Dimension getPreferredSize() {
            // if (getLayout() == null) {
            //   Insets i = getInsets();
            //   Dimension d = iconLabel.getPreferredSize();
            //   d.width += i.left + i.right;
            //   d.height += i.top + i.bottom;
            //   return d;
            return getLayout().preferredLayoutSize(this);
          }

          @Override public void setTipText(String tipText) {
            String oldValue = iconLabel.getText();
            iconLabel.setText(tipText);
            firePropertyChange("tiptext", oldValue, tipText);
          }
        };
        tip.setComponent(this);
        tip.setLayout(new BorderLayout());
        tip.add(iconLabel);
        return tip;
      }
    };
  }

  private JLabel makeLabel2(Icon icon) {
    return new JLabel("ToolTip icon using MatteBorder") {
      @Override public JToolTip createToolTip() {
        JToolTip tip = new JToolTip() {
          @Override public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            Insets i = getInsets();
            d.height = Math.max(d.height, icon.getIconHeight() + i.top + i.bottom);
            return d;
          }
        };
        tip.setComponent(this);
        Border b1 = tip.getBorder();
        Border b2 = BorderFactory.createMatteBorder(0, icon.getIconWidth(), 0, 0, icon);
        Border b3 = BorderFactory.createEmptyBorder(1, 1, 1, 1);
        Border b4 = BorderFactory.createCompoundBorder(b3, b2);
        tip.setBorder(BorderFactory.createCompoundBorder(b1, b4));
        return tip;
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
