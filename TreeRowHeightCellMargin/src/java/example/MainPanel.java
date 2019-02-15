// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    UIManager.put("Tree.closedIcon", new ColorIcon(Color.RED));
    UIManager.put("Tree.openIcon", new ColorIcon(Color.GREEN));

    JTree tree1 = new JTree();
    tree1.setRowHeight(0);

    JTree tree2 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new CompoundTreeCellRenderer());
        setRowHeight(0);
      }
    };
    // tree.setCellRenderer(new CompoundTreeCellRenderer());

    add(makeTitledPanel("Default", new JScrollPane(tree1)));
    add(makeTitledPanel("Label", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class CompoundTreeCellRenderer extends DefaultTreeCellRenderer {
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel icon = new JLabel();
  private final JLabel text = new JLabel();
  private final Border insideBorder = BorderFactory.createEmptyBorder(1, 2, 1, 2);
  private final Border outsideBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
  private final Border emptyBorder = BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
  private final Border compoundFocusBorder;

  protected CompoundTreeCellRenderer() {
    super();
    Color bsColor = getBorderSelectionColor();
    Color focusBgsColor = new Color(~getBackgroundSelectionColor().getRGB());
    compoundFocusBorder = BorderFactory.createCompoundBorder(new DotBorder(focusBgsColor, bsColor), insideBorder);

    icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
    text.setBorder(emptyBorder);
    text.setOpaque(true);
    renderer.setOpaque(false);
    renderer.add(icon, BorderLayout.WEST);

    JPanel wrap = new JPanel(new GridBagLayout());
    wrap.setOpaque(false);
    wrap.add(text);
    renderer.add(wrap);
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Color bgColor;
    Color fgColor;
    if (selected) {
      bgColor = getBackgroundSelectionColor();
      fgColor = getTextSelectionColor();
    } else {
      bgColor = Optional.ofNullable(getBackgroundNonSelectionColor()).orElse(getBackground());
      fgColor = Optional.ofNullable(getTextNonSelectionColor()).orElse(getForeground());
    }
    text.setForeground(fgColor);
    text.setBackground(bgColor);
    text.setBorder(hasFocus ? compoundFocusBorder : emptyBorder);

    JLabel l = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    text.setText(l.getText());
    icon.setIcon(l.getIcon());

    return renderer;
  }

  // @Override public void paint(Graphics g) { /* Empty painter */ }
}

class DotBorder extends LineBorder {
  private final Color borderSelectionColor;

  protected DotBorder(Color color, Color borderSelectionColor) {
    super(color, 1);
    this.borderSelectionColor = borderSelectionColor;
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(borderSelectionColor);
    g2.drawRect(0, 0, w - 1, h - 1);
    g2.setPaint(getLineColor());
    BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
    g2.dispose();
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillOval(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 24;
  }

  @Override public int getIconHeight() {
    return 24;
  }
}
