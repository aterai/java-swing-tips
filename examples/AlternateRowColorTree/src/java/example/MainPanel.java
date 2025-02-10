// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 2, 2));
    add(new JScrollPane(new JTree()));
    add(new JScrollPane(new AlternateRowColorTree()));
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
    frame.getContentPane().add(new MainPanel());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class AlternateRowColorTree extends JTree {
  private static final Color SELECTED_COLOR = new Color(0x64_32_64_FF, true);

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(new Color(0xCC_CC_CC));
    IntStream.range(0, getRowCount())
        .filter(i -> i % 2 == 0)
        .mapToObj(this::getRowBounds)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    int[] selections = getSelectionRows();
    if (selections != null) {
      g2.setPaint(SELECTED_COLOR);
      Arrays.stream(selections)
          .mapToObj(this::getRowBounds)
          .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
      super.paintComponent(g);
      if (hasFocus()) {
        Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
          Rectangle r = getRowBounds(getRowForPath(path));
          g2.setPaint(SELECTED_COLOR.darker());
          g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
        });
      }
    }
    super.paintComponent(g);
    g2.dispose();
  }

  @Override public void updateUI() {
    super.updateUI();
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    setCellRenderer(new TransparentTreeCellRenderer());
    setOpaque(false);
  }
}

class TransparentTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final Color ALPHA_OF_ZERO = new Color(0x0, true);

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, false);
    if (c instanceof JComponent) {
      ((JComponent) c).setOpaque(false);
    }
    return c;
  }

  @Override public Color getBackgroundNonSelectionColor() {
    return getBackgroundSelectionColor();
  }

  @Override public Color getBackgroundSelectionColor() {
    return ALPHA_OF_ZERO;
  }
}

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
