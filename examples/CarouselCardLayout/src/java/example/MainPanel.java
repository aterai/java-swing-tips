// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    CardLayout cardLayout = new CardLayout(50, 5);

    JPanel cards = new JPanel(cardLayout) {
      @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (Component c : getComponents()) {
          if (c.isVisible()) {
            paintSideComponents(g, getComponentZOrder(c));
            return;
          }
        }
      }

      private void paintSideComponents(Graphics g, int current) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setComposite(AlphaComposite.SrcOver.derive(.5f));
        Insets insets = getInsets();
        int hgap = cardLayout.getHgap();
        int vgap = cardLayout.getVgap();
        int cw = getWidth() - (hgap * 2 + insets.left + insets.right);
        // int ch = getHeight() - (vgap * 2 + insets.top + insets.bottom);
        int gap = 10;
        int nc = getComponentCount();

        g2.translate(hgap + insets.left - cw - gap, vgap + insets.top);
        Component prev = getComponent(current > 0 ? current - 1 : nc - 1);
        prev.print(g2);

        g2.translate((cw + gap) * 2, 0);
        Component next = getComponent((current + 1) % nc);
        next.print(g2);

        g2.dispose();
      }
    };
    cards.add(new JScrollPane(new JTree()), "JTree");
    cards.add(new JSplitPane(), "JSplitPane");
    cards.add(new JScrollPane(new JTable(9, 3)), "JTable");
    cards.add(new JButton("JButton"), "JButton");

    JButton prevButton = new JButton("Previous");
    prevButton.addActionListener(e -> {
      cardLayout.previous(cards);
      cards.repaint();
    });

    JButton nextButton = new JButton("Next");
    nextButton.addActionListener(e -> {
      cardLayout.next(cards);
      cards.repaint();
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(prevButton);
    box.add(Box.createHorizontalGlue());
    box.add(nextButton);

    add(cards);
    add(box, BorderLayout.SOUTH);
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
