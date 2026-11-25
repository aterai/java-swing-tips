// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

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
      Logger.getGlobal().severe(ex::getMessage);
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
  private static final Color ALTERNATE_COLOR = new Color(0xCC_CC_CC);
  private transient RepaintHandler handler;

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(ALTERNATE_COLOR);
    IntStream.range(0, getRowCount())
        .filter(i -> i % 2 == 0)
        .mapToObj(this::getRowBounds)
        .filter(Objects::nonNull)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    int[] selectionRows = getSelectionRows();
    if (selectionRows != null) {
      g2.setPaint(SELECTED_COLOR);
      Arrays.stream(selectionRows)
          .mapToObj(this::getRowBounds)
          .filter(Objects::nonNull)
          .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    }
    // Paint the nodes only once, on top of the stripes and the selection rows
    super.paintComponent(g);
    if (hasFocus()) {
      Optional.ofNullable(getLeadSelectionPath()).map(this::getPathBounds).ifPresent(r -> {
        g2.setPaint(SELECTED_COLOR.darker());
        g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
      });
    }
    g2.dispose();
  }

  private void repaintWholeRow(TreePath path) {
    Optional.ofNullable(path)
        .map(this::getPathBounds)
        .ifPresent(r -> repaint(0, r.y, getWidth(), r.height));
  }

  @Override public void updateUI() {
    removeTreeSelectionListener(handler);
    removePropertyChangeListener(LEAD_SELECTION_PATH_PROPERTY, handler);
    removeFocusListener(handler);
    super.updateUI();
    // The selection background and the lead row border are painted across
    // the whole width of the tree, but the TreeUI repaints only the node
    // bounds, so repaint the whole row of the changed paths instead of using
    // the global UIManager.put("Tree.repaintWholeRow", true) setting.
    handler = new RepaintHandler();
    addTreeSelectionListener(handler);
    addPropertyChangeListener(LEAD_SELECTION_PATH_PROPERTY, handler);
    addFocusListener(handler);
    setCellRenderer(new TransparentTreeCellRenderer());
    setOpaque(false);
  }

  private final class RepaintHandler
      implements TreeSelectionListener, PropertyChangeListener, FocusListener {
    @Override public void valueChanged(TreeSelectionEvent e) {
      Arrays.stream(e.getPaths()).forEach(AlternateRowColorTree.this::repaintWholeRow);
    }

    @Override public void propertyChange(PropertyChangeEvent e) {
      repaintWholeRow((TreePath) e.getOldValue());
      repaintWholeRow((TreePath) e.getNewValue());
    }

    @Override public void focusGained(FocusEvent e) {
      repaintWholeRow(getLeadSelectionPath());
    }

    @Override public void focusLost(FocusEvent e) {
      repaintWholeRow(getLeadSelectionPath());
    }
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
    return ALPHA_OF_ZERO;
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
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
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
        Logger.getGlobal().severe(ex::getMessage);
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
