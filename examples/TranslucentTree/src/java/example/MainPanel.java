// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
// import javax.swing.plaf.nimbus.AbstractRegionPainter; // JDK 1.7.0

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 2, 2));
    JTree tree1 = new TranslucentTree();
    JTree tree2 = new TransparentTree();

    // // NimbusLookAndFeel(SynthLookAndFeel) JDK 1.7.0
    // UIDefaults d = new UIDefaults();
    // String key = "Tree:TreeCell[Enabled+Selected].backgroundPainter";
    // d.put(key, new TransparentTreeCellPainter());
    // tree2.putClientProperty("Nimbus.Overrides", d);
    // tree2.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.FALSE);

    add(makeTranslucentScrollPane(tree1));
    add(makeTranslucentScrollPane(tree2));

    setOpaque(false);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JScrollPane makeTranslucentScrollPane(Component view) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(false);
    return scroll;
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
    JFrame frame = new JFrame("@title@") {
      @Override protected JRootPane createRootPane() {
        return new TransparentRootPane();
      }
    };
    Container contentPane = frame.getContentPane();
    if (contentPane instanceof JComponent) {
      ((JComponent) contentPane).setOpaque(false);
    }
    frame.getContentPane().add(new MainPanel());
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TranslucentTree extends JTree {
  @Override public void updateUI() {
    super.updateUI();
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    setCellRenderer(new TranslucentTreeCellRenderer());
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }
}

class TransparentTree extends JTree {
  // https://ateraimemo.com/Swing/TreeRowSelection.html
  private static final Color SELECTED_COLOR = new Color(0x64_64_64_FF, true);

  @Override protected void paintComponent(Graphics g) {
    int[] sr = getSelectionRows();
    if (sr == null) {
      super.paintComponent(g);
      return;
    }
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(SELECTED_COLOR);
    Arrays.stream(getSelectionRows()).mapToObj(this::getRowBounds)
        .forEach(r -> g2.fillRect(0, r.y, getWidth(), r.height));
    super.paintComponent(g);
    if (hasFocus()) {
      Optional.ofNullable(getLeadSelectionPath()).ifPresent(path -> {
        Rectangle r = getRowBounds(getRowForPath(path));
        g2.setPaint(SELECTED_COLOR.darker());
        g2.drawRect(0, r.y, getWidth() - 1, r.height - 1);
      });
    }
    g2.dispose();
  }

  @Override public void updateUI() {
    super.updateUI();
    UIManager.put("Tree.repaintWholeRow", Boolean.TRUE);
    setCellRenderer(new TransparentTreeCellRenderer());
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
  }
}

// https://ateraimemo.com/Swing/RootPaneBackground.html
class TransparentRootPane extends JRootPane {
  private static final Paint TEXTURE = makeCheckerTexture();

  private static TexturePaint makeCheckerTexture() {
    int cs = 6;
    int sz = cs * cs;
    BufferedImage img = new BufferedImage(sz, sz, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = img.createGraphics();
    g2.setPaint(new Color(0xDC_DC_DC));
    g2.fillRect(0, 0, sz, sz);
    g2.setPaint(new Color(0xC8_C8_C8_C8, true));
    for (int i = 0; i * cs < sz; i++) {
      for (int j = 0; j * cs < sz; j++) {
        if ((i + j) % 2 == 0) {
          g2.fillRect(i * cs, j * cs, cs, cs);
        }
      }
    }
    g2.dispose();
    return new TexturePaint(img, new Rectangle(sz, sz));
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(TEXTURE);
    g2.fillRect(0, 0, getWidth(), getHeight());
    g2.dispose();
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
  }
}

// https://ateraimemo.com/Swing/TreeBackgroundSelectionColor.html
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

class TranslucentTreeCellRenderer extends TransparentTreeCellRenderer {
  private final Color translucentBsc = new Color(0x64_64_64_FF, true);

  @Override public Color getBackgroundSelectionColor() {
    return translucentBsc;
  }
}

// https://ateraimemo.com/Swing/NimbusColorPalette.html
// // JDK 1.7.0
// class TransparentTreeCellPainter extends AbstractRegionPainter {
//   // private PaintContext ctx = null;
//   @Override protected void doPaint(Graphics2D g, JComponent c, int width, int height, Object[] extendedCacheKeys) {
//     // Do nothing
//   }
//   @Override protected final PaintContext getPaintContext() {
//     return null;
//   }
// }
