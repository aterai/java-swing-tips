// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

    JComboBox<String> combo = new JComboBox<String>(makeModel()) {
      private transient PopupMenuListener listener;
      @Override public void updateUI() {
        removePopupMenuListener(listener);
        super.updateUI();
        if (getUI() instanceof WindowsComboBoxUI) {
          setUI(new RightPopupWindowsComboBoxUI());
        } else {
          setUI(new RightPopupBasicComboBoxUI());
        }
        listener = new RightPopupMenuListener();
        addPopupMenuListener(listener);
      }
    };

    JPanel p = new JPanel(new GridLayout(2, 2, 5, 5));
    p.add(new JComboBox<>(makeModel()));
    p.add(new JLabel("<- default"));
    p.add(combo);
    p.add(new JLabel("<- RightPopupMenuListener"));

    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    MutableComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("111");
    model.addElement("2222");
    model.addElement("33333");
    model.addElement("444444");
    model.addElement("5555555");
    model.addElement("66666666");
    return model;
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

class RightPopupMenuListener implements PopupMenuListener {
  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> combo = (JComboBox<?>) e.getSource();
    Accessible a = combo.getAccessibleContext().getAccessibleChild(0);
    // Or Accessible a = combo.getUI().getAccessibleChild(combo, 0);
    if (a instanceof JPopupMenu) {
      EventQueue.invokeLater(() -> {
        Point pt = new Point(combo.getSize().width, 0);
        SwingUtilities.convertPointToScreen(pt, combo);
        ((JPopupMenu) a).setLocation(pt);
      });
    }
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }
}

class RightPopupWindowsComboBoxUI extends WindowsComboBoxUI {
  @Override protected JButton createArrowButton() {
    String path = "example/14x14.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    Icon icon = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("html.missingImage");
      }
    }).orElseGet(() -> UIManager.getIcon("html.missingImage"));
    JButton button = new JButton(icon) {
      @Override public Dimension getPreferredSize() {
        return new Dimension(14, 14);
      }
    };
    button.setRolloverIcon(makeRolloverIcon(icon));
    button.setFocusPainted(false);
    button.setContentAreaFilled(false);
    return button;
  }

  private static Icon makeRolloverIcon(Icon srcIcon) {
    int w = srcIcon.getIconWidth();
    int h = srcIcon.getIconHeight();
    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    srcIcon.paintIcon(null, g2, 0, 0);
    float[] scaleFactors = {1.2f, 1.2f, 1.2f, 1f};
    float[] offsets = {0f, 0f, 0f, 0f};
    BufferedImageOp op = new RescaleOp(scaleFactors, offsets, g2.getRenderingHints());
    g2.dispose();
    return new ImageIcon(op.filter(img, null));
  }
}

class RightPopupBasicComboBoxUI extends BasicComboBoxUI {
  @Override protected JButton createArrowButton() {
    JButton button = super.createArrowButton();
    if (button instanceof BasicArrowButton) {
      ((BasicArrowButton) button).setDirection(SwingConstants.EAST);
    }
    return button;
  }
}
