// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.metal.MetalBorders;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JToolBar toolBar1 = new JToolBar("Customized ToolBarBorder") {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(new ToolBarDragBorder());
      }
    };
    toolBar1.add(new JLabel("<- Customized Border"));
    toolBar1.addSeparator();
    toolBar1.add(new JRadioButton("JRadioButton"));
    toolBar1.add(new JToggleButton("JToggleButton"));

    JToolBar toolBar2 = new JToolBar("default");
    toolBar2.add(new JLabel("<- Default Border"));
    toolBar2.addSeparator();
    toolBar2.add(new JCheckBox("JCheckBox"));
    toolBar2.add(new JButton("JButton"));

    add(toolBar1, BorderLayout.NORTH);
    add(toolBar2, BorderLayout.SOUTH);
    add(new JScrollPane(new JTextArea()));
    // add(Box.createHorizontalStrut(0), BorderLayout.WEST);
    // add(Box.createHorizontalStrut(0), BorderLayout.EAST);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
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

class ToolBarDragBorder extends MetalBorders.ToolBarBorder {
  private static final Icon DRAG_ICON = new ToolBarDragIcon();

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    if (!(c instanceof JToolBar)) {
      return;
    }
    JToolBar tb = (JToolBar) c;
    if (tb.isFloatable()) {
      if (tb.getOrientation() == HORIZONTAL) {
        int cy = (h - DRAG_ICON.getIconHeight()) / 2;
        DRAG_ICON.paintIcon(c, g, x, y + cy);
      } else { // vertical
        super.paintBorder(c, g, x, y, w, h);
      }
    }
  }
}

class ToolBarDragIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.GRAY);
    int x2 = getIconWidth() / 2 - 1;
    int y2 = getIconHeight() / 2 - 1;
    g2.fillRect(x2, y2 - 6, 2, 2);
    g2.fillRect(x2, y2 - 2, 2, 2);
    g2.fillRect(x2, y2 + 2, 2, 2);
    g2.fillRect(x2, y2 + 6, 2, 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 14;
  }

  @Override public int getIconHeight() {
    return 16;
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
