// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;

public final class MainPanel extends JPanel {
  private final JCheckBox expandedIcon = new JCheckBox("Tree.expandedIcon", true);
  private final JCheckBox paintLines = new JCheckBox("Tree.paintLines", true);
  private final JButton update = new JButton("update");

  private MainPanel() {
    super(new BorderLayout(2, 2));

    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

    int lci = UIManager.getInt("Tree.leftChildIndent");
    JSpinner leftChildIndent = new JSpinner(new SpinnerNumberModel(lci, -32, 32, 1));

    int rci = UIManager.getInt("Tree.rightChildIndent");
    JSpinner rightChildIndent = new JSpinner(new SpinnerNumberModel(rci, -32, 32, 1));

    Box box1 = Box.createHorizontalBox();
    box1.add(new JLabel(" Tree.leftChildIndent:"));
    box1.add(leftChildIndent);
    box1.add(new JLabel(" Tree.rightChildIndent:"));
    box1.add(rightChildIndent);

    Box box2 = Box.createHorizontalBox();
    box2.add(Box.createHorizontalGlue());
    box2.add(paintLines);
    box2.add(expandedIcon);
    box2.add(update);

    Icon emptyIcon = new EmptyIcon();
    update.addActionListener(e -> {
      UIManager.put("Tree.leftChildIndent", (Integer) leftChildIndent.getValue());
      UIManager.put("Tree.rightChildIndent", (Integer) rightChildIndent.getValue());
      Icon ei;
      Icon ci;
      if (expandedIcon.isSelected()) {
        UIDefaults lnfdef = UIManager.getLookAndFeelDefaults();
        ei = lnfdef.getIcon("Tree.expandedIcon");
        ci = lnfdef.getIcon("Tree.collapsedIcon");
      } else {
        ei = emptyIcon;
        ci = emptyIcon;
      }
      UIManager.put("Tree.expandedIcon", new IconUIResource(ei));
      UIManager.put("Tree.collapsedIcon", new IconUIResource(ci));
      UIManager.put("Tree.paintLines", paintLines.isSelected());
      SwingUtilities.updateComponentTreeUI(this);
    });

    JPanel p = new JPanel(new GridLayout(2, 1, 2, 2));
    p.add(box1);
    p.add(box2);

    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    add(p, BorderLayout.NORTH);
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

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) { /* Empty icon */ }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
