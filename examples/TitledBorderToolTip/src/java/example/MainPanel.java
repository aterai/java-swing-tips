// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(1, 2, 5, 5));
    JPanel panel1 = new JPanel() {
      private final JLabel label = new JLabel();
      @Override public String getToolTipText(MouseEvent e) {
        return getBorder() instanceof TitledBorder
            ? getText(e, (TitledBorder) getBorder()) : super.getToolTipText(e);
      }

      private String getText(MouseEvent e, TitledBorder titledBorder) {
        label.setText(titledBorder.getTitle());
        label.setFont(titledBorder.getTitleFont());
        Rectangle r = SwingUtilities.calculateInnerArea(this, null);
        r.y = 0;
        r.height = titledBorder.getBorderInsets(this).top;
        String tipText = super.getToolTipText(e);
        if (label.getPreferredSize().width > r.width && r.contains(e.getPoint())) {
          tipText = label.getText();
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
