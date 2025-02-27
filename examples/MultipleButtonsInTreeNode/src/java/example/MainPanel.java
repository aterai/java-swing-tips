// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        setCellEditor(null);
        super.updateUI();
        setCellRenderer(new ButtonCellRenderer());
        setCellEditor(new ButtonCellEditor());
        setRowHeight(0);
      }
    };
    tree.setEditable(true);
    add(new JScrollPane(tree));
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

final class ButtonPanel extends JPanel {
  public final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  public final JButton b1 = new ColorButton(new ColorIcon(Color.RED));
  public final JButton b2 = new ColorButton(new ColorIcon(Color.GREEN));
  public final JButton b3 = new ColorButton(new ColorIcon(Color.BLUE));

  // public ButtonPanel() {
  //   super();
  //   b1.addActionListener(e -> System.out.println("b1: " + renderer.getText()));
  //   b2.addActionListener(e -> System.out.println("b2: " + renderer.getText()));
  //   b3.addActionListener(e -> System.out.println("b3: " + renderer.getText()));
  // }

  @Override public boolean isOpaque() {
    return false;
  }

  public Component remakePanel(Component c) {
    removeAll();
    Stream.of(b1, b2, b3, c).forEach(this::add);
    return this;
  }

  // public int getButtonAreaWidth() {
  //   int hgap = ((FlowLayout) getLayout()).getHgap();
  //   return Arrays.asList(b1, b2, b3).stream()
  //     .mapToInt(b -> b.getPreferredSize().width + hgap)
  //     .sum();
  // }
}

class ButtonCellRenderer implements TreeCellRenderer {
  private final ButtonPanel panel = new ButtonPanel();

  @Override public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
    TreeCellRenderer r = panel.renderer;
    Component c = r.getTreeCellRendererComponent(
        tree, value, selected, expanded, leaf, row, hasFocus);
    return panel.remakePanel(c);
  }
}

class ButtonCellEditor extends AbstractCellEditor implements TreeCellEditor {
  private final ButtonPanel panel = new ButtonPanel();

  protected ButtonCellEditor() {
    super();
    ActionListener al = e -> stopCellEditing();
    panel.b1.addActionListener(al);
    panel.b2.addActionListener(al);
    panel.b3.addActionListener(al);
    // panel.renderer.addMouseListener(new MouseAdapter() {
    //   @Override public void mousePressed(MouseEvent e) {
    //     stopCellEditing();
    //   }
    // });
  }

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
    TreeCellRenderer r = panel.renderer;
    Component c = r.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    return panel.remakePanel(c);
  }

  @Override public Object getCellEditorValue() {
    return panel.renderer.getText();
  }

  @Override public boolean isCellEditable(EventObject e) {
    // return e instanceof MouseEvent;
    return e instanceof MouseEvent && getDeepestButtonAt((MouseEvent) e);
  }

  private static boolean getDeepestButtonAt(MouseEvent e) {
    Point pt = e.getPoint();
    return Optional.ofNullable(e.getComponent())
        .filter(JTree.class::isInstance)
        .map(JTree.class::cast)
        .map(tree -> {
          TreePath path = tree.getPathForLocation(pt.x, pt.y);
          Rectangle rect = tree.getPathBounds(path);
          return contains(path, rect, pt) && getButtonAt(tree, path, rect, pt);
        })
        .orElse(false);
  }

  private static boolean contains(TreePath path, Rectangle rect, Point pt) {
    return path != null && rect != null && rect.contains(pt);
  }

  private static boolean getButtonAt(JTree tree, TreePath path, Rectangle rect, Point pt) {
    TreeNode node = (TreeNode) path.getLastPathComponent();
    int row = tree.getRowForLocation(pt.x, pt.y);
    TreeCellRenderer r = tree.getCellRenderer();
    boolean leaf = node.isLeaf();
    Component c = r.getTreeCellRendererComponent(tree, " ", true, true, leaf, row, true);
    c.setBounds(rect);
    c.setLocation(0, 0);
    // tree.doLayout();
    tree.revalidate();
    pt.translate(-rect.x, -rect.y);
    return SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y) instanceof JButton;
  }
}

class ColorButton extends JButton {
  protected ColorButton(ColorIcon icon) {
    super(icon);
    setPressedIcon(new ColorIcon(icon.getColor().darker()));
  }

  @Override public void updateUI() {
    super.updateUI();
    setFocusable(false);
    setFocusPainted(false);
    setBorderPainted(false);
    setContentAreaFilled(false);
    setBorder(BorderFactory.createEmptyBorder());
  }

  @Override public final void setPressedIcon(Icon pressedIcon) {
    super.setPressedIcon(pressedIcon);
  }

  // @Override public Dimension getPreferredSize() {
  //   Icon icon = getIcon();
  //   return new Dimension(icon.getIconWidth(), icon.getIconHeight());
  // }
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
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 8;
  }

  @Override public int getIconHeight() {
    return 8;
  }

  public Color getColor() {
    return color;
  }
}
