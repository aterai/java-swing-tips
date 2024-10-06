// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 5, 5));
    JPanel panel1 = new JPanel() {
      private final JLabel label = new JLabel();
      @Override public String getToolTipText(MouseEvent e) {
        Border b = getBorder();
        return b instanceof TitledBorder ? getString(e, (TitledBorder) b) : null;
      }

      private String getString(MouseEvent e, TitledBorder titledBorder) {
        // int edge = 2; // EDGE_SPACING;
        Insets i = titledBorder.getBorderInsets(this);
        String title = titledBorder.getTitle();
        label.setFont(titledBorder.getTitleFont());
        label.setText(title);
        Dimension size = label.getPreferredSize();
        int labelX = i.left;
        int labelY = 0;
        int labelW = getSize().width - i.left - i.right;
        int labelH = i.top;
        String tipText = null;
        if (size.width > labelW) {
          Rectangle r = new Rectangle(labelX, labelY, labelW, labelH);
          if (r.contains(e.getPoint())) {
            tipText = title;
          }
        }
        return tipText;
      }
    };
    panel1.setBorder(BorderFactory.createTitledBorder("Override JPanel#getToolTipText(...)"));
    panel1.setToolTipText("JPanel: ToolTipText");

    JPanel panel2 = new JPanel();
    panel2.setBorder(BorderFactory.createTitledBorder("Default TitledBorder on JPanel"));
    panel2.setToolTipText("JPanel");

    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    add(panel1);
    add(panel2);
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
