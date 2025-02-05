// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicGraphicsUtils;
import javax.swing.tree.DefaultTreeCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 3));
    JTree tree1 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new MarginTreeCellRenderer());
      }
    };
    // tree1.setCellRenderer(new MarginTreeCellRenderer());

    JTree tree2 = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        setCellRenderer(new CompoundTreeCellRenderer());
      }
    };
    // tree2.setCellRenderer(new CompoundTreeCellRenderer());

    add(makeTitledPanel("Default", new JScrollPane(new JTree())));
    add(makeTitledPanel("Margin", new JScrollPane(tree1)));
    add(makeTitledPanel("Label", new JScrollPane(tree2)));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class MarginTreeCellRenderer extends DefaultTreeCellRenderer {
  private static final int MARGIN = 2; // < 3
  private boolean drawsFocusBorder;
  private boolean drawDashedFocus;
  private boolean fillBackground;
  private Color treeBgsColor;
  private Color focusBgsColor;
  // private boolean selected;
  // private boolean hasFocus;

  @Override public void updateUI() {
    super.updateUI();
    drawsFocusBorder = UIManager.getBoolean("Tree.drawsFocusBorderAroundIcon");
    drawDashedFocus = UIManager.getBoolean("Tree.drawDashedFocusIndicator");
    fillBackground = UIManager.getBoolean("Tree.rendererFillBackground");
    setOpaque(fillBackground);
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    this.hasFocus = hasFocus;
    this.selected = selected;
    return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, false);
  }

  @Override public void paint(Graphics g) {
    if (!getComponentOrientation().isLeftToRight()) {
      super.paint(g);
      return;
    }

    Color bgc;
    if (selected) {
      bgc = getBackgroundSelectionColor();
    } else {
      bgc = Optional.ofNullable(getBackgroundNonSelectionColor()).orElse(getBackground());
    }

    int imgOffset = -1;
    int w = getWidth();
    int h = getHeight();
    if (Objects.nonNull(bgc) && fillBackground) {
      imgOffset = getLabelStartPosition();
      g.setColor(bgc);
      g.fillRect(imgOffset - MARGIN, 0, w + MARGIN - imgOffset, h);
    }

    // g.translate(MARGIN, 0);
    // boolean flag = selected;
    // selected = false;
    super.paint(g);
    // g.translate(-2, 0);
    // selected = flag;

    if (hasFocus) {
      if (drawsFocusBorder) {
        imgOffset = 0;
      } else if (imgOffset == -1) {
        imgOffset = getLabelStartPosition();
      }
      g.setColor(bgc);
      g.fillRect(imgOffset - MARGIN, getY(), MARGIN + 1, h);
      paintFocusRect(g, imgOffset - MARGIN, getY(), w + MARGIN - imgOffset, h, bgc);
    }
  }

  private void paintFocusRect(Graphics g, int x, int y, int w, int h, Color notColor) {
    Color bsColor = getBorderSelectionColor();
    boolean b = selected || !drawDashedFocus;
    if (Objects.nonNull(bsColor) && b) {
      g.setColor(bsColor);
      g.drawRect(x, y, w - 1, h - 1);
    }
    if (drawDashedFocus && Objects.nonNull(notColor)) {
      if (!notColor.equals(treeBgsColor)) {
        treeBgsColor = notColor;
        focusBgsColor = new Color(~notColor.getRGB());
      }
      g.setColor(focusBgsColor);
      BasicGraphicsUtils.drawDashedRect(g, x, y, w, h);
    }
  }

  private int getLabelStartPosition() {
    return Optional.ofNullable(getIcon())
        .filter(icon -> Objects.nonNull(getText()))
        .map(icon -> icon.getIconWidth() + Math.max(0, getIconTextGap() - 1))
        .orElse(0);
  }
}

class CompoundTreeCellRenderer extends DefaultTreeCellRenderer {
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final JLabel icon = new JLabel();
  private final JLabel text = new JLabel();
  private boolean isSynth;

  protected CompoundTreeCellRenderer() {
    super();
    icon.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
    text.setOpaque(true);
    renderer.setOpaque(false);
    renderer.add(icon, BorderLayout.WEST);
    renderer.add(text);
  }

  @Override public void updateUI() {
    super.updateUI();
    isSynth = getUI().getClass().getName().contains("Synth");
  }

  private Border makeEmptyBorder(Border insideBorder) {
    Border outsideBorder = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    return BorderFactory.createCompoundBorder(outsideBorder, insideBorder);
  }

  private Border makeFocusBorder(Border insideBorder) {
    Border focusBorder;
    if (isSynth) {
      focusBorder = makeEmptyBorder(insideBorder);
    } else {
      Color bsColor = getBorderSelectionColor();

      boolean drawDashedFocus = UIManager.getBoolean("Tree.drawDashedFocusIndicator");
      Border b;
      if (drawDashedFocus) {
        b = new DotBorder(new Color(~getBackgroundSelectionColor().getRGB()), bsColor);
      } else {
        b = BorderFactory.createLineBorder(bsColor);
      }
      focusBorder = BorderFactory.createCompoundBorder(b, insideBorder);
    }
    return focusBorder;
  }

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    Color bgc;
    Color fgc;
    if (selected) {
      bgc = getBackgroundSelectionColor();
      fgc = getTextSelectionColor();
      text.setOpaque(!isSynth);
    } else {
      bgc = Optional.ofNullable(getBackgroundNonSelectionColor()).orElse(getBackground());
      fgc = Optional.ofNullable(getTextNonSelectionColor()).orElse(getForeground());
      text.setOpaque(false);
    }
    text.setForeground(fgc);
    text.setBackground(bgc);

    Border ib = BorderFactory.createEmptyBorder(1, 2, 1, 2);
    text.setBorder(hasFocus ? makeFocusBorder(ib) : makeEmptyBorder(ib));

    Component c = super.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      text.setText(l.getText());
      icon.setIcon(l.getIcon());
    }
    return renderer;
  }
}

class DotBorder extends LineBorder {
  private final Color selectionColor;

  protected DotBorder(Color color, Color selectionColor) {
    super(color, 1);
    this.selectionColor = selectionColor;
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(selectionColor);
    g2.drawRect(0, 0, w - 1, h - 1);
    g2.setPaint(getLineColor());
    BasicGraphicsUtils.drawDashedRect(g2, 0, 0, w, h);
    g2.dispose();
  }
}
