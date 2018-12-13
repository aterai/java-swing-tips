package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Objects;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTree tree = new JTree() {
      @Override public void updateUI() {
        setCellRenderer(null);
        setCellEditor(null);
        super.updateUI();
        // ???#1: JDK 1.6.0 bug??? Nimbus LnF
        setCellRenderer(new CheckBoxNodeRenderer());
        setCellEditor(new CheckBoxNodeEditor());
      }
    };
    TreeModel model = tree.getModel();
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();
    // Java 9: Collections.list(root.breadthFirstEnumeration()).stream()
    Collections.list((Enumeration<?>) root.breadthFirstEnumeration()).stream()
      .filter(DefaultMutableTreeNode.class::isInstance)
      .map(DefaultMutableTreeNode.class::cast)
      .forEach(n -> n.setUserObject(new CheckBoxNode(Objects.toString(n.getUserObject(), ""), Status.DESELECTED)));

    model.addTreeModelListener(new CheckBoxStatusUpdateListener());

    tree.setEditable(true);
    tree.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    tree.expandRow(0);
    // tree.setToggleClickCount(1);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class TriStateCheckBox extends JCheckBox {
  @Override public void updateUI() {
    Icon currentIcon = getIcon();
    setIcon(null);
    super.updateUI();
    if (Objects.nonNull(currentIcon)) {
      setIcon(new IndeterminateIcon());
    }
    setOpaque(false);
  }
}

class IndeterminateIcon implements Icon {
  private static final Color FOREGROUND = new Color(50, 20, 255, 200); // TEST: UIManager.getColor("CheckBox.foreground");
  private static final int SIDE_MARGIN = 4;
  private static final int HEIGHT = 2;
  private final Icon icon = UIManager.getIcon("CheckBox.icon");

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    icon.paintIcon(c, g2, 0, 0);
    g2.setPaint(FOREGROUND);
    g2.fillRect(SIDE_MARGIN, (getIconHeight() - HEIGHT) / 2, getIconWidth() - SIDE_MARGIN - SIDE_MARGIN, HEIGHT);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return icon.getIconWidth();
  }

  @Override public int getIconHeight() {
    return icon.getIconHeight();
  }
}
