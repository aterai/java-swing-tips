// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1, 5, 5));
    Border b0 = BorderFactory.createLineBorder(Color.GRAY);
    TitledBorder b1 = BorderFactory.createTitledBorder(b0, "TitledBorder");
    b1.setTitleJustification(TitledBorder.CENTER);

    JPanel p1 = new JPanel();
    p1.setBorder(b1);

    Border raisedBevel = BorderFactory.createRaisedBevelBorder();
    Border topLine = BorderFactory.createMatteBorder(10, 0, 0, 0, Color.GRAY.brighter());
    Border loweredBevel = BorderFactory.createLoweredBevelBorder();
    Border compound1 = BorderFactory.createCompoundBorder(raisedBevel, topLine);
    Border compound2 = BorderFactory.createCompoundBorder(compound1, loweredBevel);
    TitledBorder b2 = BorderFactory.createTitledBorder(compound2, "CompoundBorder");
    b2.setTitleJustification(TitledBorder.CENTER);

    JPanel p2 = new JPanel();
    p2.setBorder(b2);

    add(p1);
    add(p2);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
