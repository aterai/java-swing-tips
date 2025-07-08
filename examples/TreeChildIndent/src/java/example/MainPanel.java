// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;

public final class MainPanel extends JPanel {
  private final JCheckBox expandedIcon = new JCheckBox("Tree.expandedIcon", true);
  private final JCheckBox paintLines = new JCheckBox("Tree.paintLines", true);

  private MainPanel() {
    super(new BorderLayout(2, 2));
    JTree tree = new JTree();
    tree.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    int lci = UIManager.getInt("Tree.leftChildIndent");
    SpinnerNumberModel leftChildIndent = new SpinnerNumberModel(lci, -32, 32, 1);
    int rci = UIManager.getInt("Tree.rightChildIndent");
    SpinnerNumberModel rightChildIndent = new SpinnerNumberModel(rci, -32, 32, 1);

    Box box1 = Box.createHorizontalBox();
    box1.add(new JLabel(" Tree.leftChildIndent:"));
    box1.add(new JSpinner(leftChildIndent));
    box1.add(new JLabel(" Tree.rightChildIndent:"));
    box1.add(new JSpinner(rightChildIndent));

    Box box2 = Box.createHorizontalBox();
    box2.add(Box.createHorizontalGlue());
    box2.add(paintLines);
    box2.add(expandedIcon);
    JButton update = new JButton("update");
    update.addActionListener(e -> {
      int left = leftChildIndent.getNumber().intValue();
      int right = rightChildIndent.getNumber().intValue();
      updateTreeIconAndIndent(left, right);
    });
    box2.add(update);

    JPanel p = new JPanel(new GridLayout(2, 1, 2, 2));
    p.add(box1);
    p.add(box2);
    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(tree));
    setPreferredSize(new Dimension(320, 240));
  }

  private void updateTreeIconAndIndent(int leftIndent, int rightIndent) {
    UIManager.put("Tree.leftChildIndent", leftIndent);
    UIManager.put("Tree.rightChildIndent", rightIndent);
    Icon ei;
    Icon ci;
    if (expandedIcon.isSelected()) {
      UIDefaults lnfDef = UIManager.getLookAndFeelDefaults();
      ei = lnfDef.getIcon("Tree.expandedIcon");
      ci = lnfDef.getIcon("Tree.collapsedIcon");
    } else {
      Icon emptyIcon = new EmptyIcon();
      ei = emptyIcon;
      ci = emptyIcon;
    }
    UIManager.put("Tree.expandedIcon", new IconUIResource(ei));
    UIManager.put("Tree.collapsedIcon", new IconUIResource(ci));
    UIManager.put("Tree.paintLines", paintLines.isSelected());
    SwingUtilities.updateComponentTreeUI(this);
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
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}
