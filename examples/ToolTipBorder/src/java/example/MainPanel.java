// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JButton button0 = new JButton("ToolTip1") {
      @Override public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        Border b1 = tip.getBorder();
        Border b2 = BorderFactory.createTitledBorder("TitledBorder ToolTip");
        tip.setBorder(BorderFactory.createCompoundBorder(b1, b2));
        tip.setComponent(this);
        return tip;
      }
    };
    button0.setToolTipText("Test - ToolTipText0");

    JButton button1 = new JButton("ToolTip2") {
      @Override public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        Border b1 = tip.getBorder();
        Border b2 = BorderFactory.createMatteBorder(0, 10, 0, 0, Color.GREEN);
        tip.setBorder(BorderFactory.createCompoundBorder(b1, b2));
        tip.setComponent(this);
        return tip;
      }
    };
    button1.setToolTipText("Test - ToolTipText1");

    JPanel p = new JPanel(new BorderLayout(5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(makeTitledPanel("TitledBorder", button0), BorderLayout.NORTH);
    p.add(makeTitledPanel("MatteBorder", button1), BorderLayout.SOUTH);
    add(p, BorderLayout.NORTH);
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
