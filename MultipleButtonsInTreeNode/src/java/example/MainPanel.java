// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.Objects;
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

class ButtonPanel extends JPanel {
  protected final DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
  protected final JButton b1 = new ColorButton(new ColorIcon(Color.RED));
  protected final JButton b2 = new ColorButton(new ColorIcon(Color.GREEN));
  protected final JButton b3 = new ColorButton(new ColorIcon(Color.BLUE));

  protected ButtonPanel() {
    super();
    setOpaque(false);
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
    Component c = panel.renderer.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    return panel.remakePanel(c);
  }
}

class ButtonCellEditor extends AbstractCellEditor implements TreeCellEditor {
  private final ButtonPanel panel = new ButtonPanel();

  protected ButtonCellEditor() {
    super();
    panel.b1.addActionListener(e -> {
      System.out.println("b1: " + panel.renderer.getText());
      stopCellEditing();
    });
    panel.b2.addActionListener(e -> {
      System.out.println("b2: " + panel.renderer.getText());
      stopCellEditing();
    });
    panel.b3.addActionListener(e -> {
      System.out.println("b3: " + panel.renderer.getText());
      stopCellEditing();
    });
    // panel.renderer.addMouseListener(new MouseAdapter() {
    //   @Override public void mousePressed(MouseEvent e) {
    //     System.out.println("label");
    //     stopCellEditing();
    //   }
    // });
  }

  @Override public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
    Component c = panel.renderer.getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
    return panel.remakePanel(c);
  }

  @Override public Object getCellEditorValue() {
    return panel.renderer.getText();
  }

  @Override public boolean isCellEditable(EventObject e) {
    // return e instanceof MouseEvent;
    Object source = e.getSource();
    if (!(source instanceof JTree) || !(e instanceof MouseEvent)) {
      return false;
    }
    JTree tree = (JTree) source;
    Point p = ((MouseEvent) e).getPoint();
    TreePath path = tree.getPathForLocation(p.x, p.y);
    if (Objects.isNull(path)) {
      return false;
    }
    Rectangle r = tree.getPathBounds(path);
    if (Objects.isNull(r)) {
      return false;
    }
    // r.width = panel.getButtonAreaWidth();
    // return r.contains(p);
    if (r.contains(p)) {
      TreeNode node = (TreeNode) path.getLastPathComponent();
      int row = tree.getRowForLocation(p.x, p.y);
      Component c = tree.getCellRenderer().getTreeCellRendererComponent(tree, " ", true, true, node.isLeaf(), row, true);
      c.setBounds(r);
      c.setLocation(0, 0);
      // tree.doLayout();
      tree.revalidate();
      p.translate(-r.x, -r.y);
      Component o = SwingUtilities.getDeepestComponentAt(c, p.x, p.y);
      if (o instanceof JButton) {
        return true;
      }
    }
    return false;
  }
}

class ColorButton extends JButton {
  protected ColorButton(ColorIcon icon) {
    super(icon);
    setPressedIcon(new ColorIcon(icon.color.darker()));
    setFocusable(false);
    setFocusPainted(false);
    setBorderPainted(false);
    setContentAreaFilled(false);
    setBorder(BorderFactory.createEmptyBorder());
  }
//   @Override public Dimension getPreferredSize() {
//     Icon icon = getIcon();
//     return new Dimension(icon.getIconWidth(), icon.getIconHeight());
//   }
}

class ColorIcon implements Icon {
  protected final Color color;

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
}
