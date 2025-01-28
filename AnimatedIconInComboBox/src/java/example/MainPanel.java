// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String path1 = "example/favicon.png";
    String path2 = "example/animated.gif";

    JComboBox<Icon> combo = new JComboBox<>();
    Icon[] icons = {makeIcon(path1), makeAnimatedIcon(path2, combo, 1)};
    combo.setModel(new DefaultComboBoxModel<>(icons));

    JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    p.add(new JLabel("Default ImageIcon"));
    p.add(new JComboBox<>(new Icon[] {makeIcon(path1), makeIcon(path2)}));
    p.add(new JLabel("ImageIcon#setImageObserver(ImageObserver)"));
    p.add(combo);
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public static Icon makeAnimatedIcon(String path, JComboBox<?> combo, int row) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    return Optional.ofNullable(url)
        .map(ImageIcon::new)
        .<Icon>map(icon -> {
          // Wastefulness: icon.setImageObserver(combo);
          icon.setImageObserver((img, flags, x, y, w, h) -> {
            if (combo.isShowing() && (flags & (FRAMEBITS | ALLBITS)) != 0) {
              repaintComboBox(combo, row);
            }
            return (flags & (ALLBITS | ABORT)) == 0;
          });
          return icon;
        })
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
  }

  private static Icon makeIcon(String path) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    return Optional.ofNullable(url)
        .<Icon>map(ImageIcon::new)
        .orElseGet(() -> UIManager.getIcon("html.missingImage"));
  }

  public static void repaintComboBox(JComboBox<?> combo, int row) {
    if (combo.getSelectedIndex() == row) {
      combo.repaint();
    }
    Accessible a = combo.getAccessibleContext().getAccessibleChild(0);
    if (a instanceof ComboPopup) {
      JList<?> list = ((ComboPopup) a).getList();
      if (list.isShowing()) {
        list.repaint(list.getCellBounds(row, row));
      }
    }
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
