// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();

    JSpinner spinner0 = new JSpinner(new SpinnerNumberModel(5, 0, 10, 1));
    box.add(makeTitledPanel("stepSize: 1", spinner0));
    box.add(Box.createVerticalStrut(10));

    JSpinner spinner1 = new JSpinner(new SpinnerNumberModel(5, 0, 10, -1));
    box.add(makeTitledPanel("stepSize: -1", spinner1));
    box.add(Box.createVerticalStrut(10));

    String[] scale = {
        "AAA", "AA+", "AA", "AA-", "A+", "A", "A-",
        "BBB+", "BBB", "BBB-", "BB+", "BB", "BB-", "B+", "B", "B-",
        "CCC+", "CCC", "CCC-", "CC", "R", "D"
    };
    JSpinner spinner2 = new JSpinner(new SpinnerListModel(scale));
    box.add(makeTitledPanel("SpinnerListModel", spinner2));
    box.add(Box.createVerticalStrut(10));

    JSpinner spinner3 = new JSpinner(new SpinnerListModel(scale) {
      @Override public Object getNextValue() {
        return super.getPreviousValue();
      }

      @Override public Object getPreviousValue() {
        return super.getNextValue();
      }
    });
    box.add(makeTitledPanel("Reverse direction SpinnerListModel", spinner3));

    add(box, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
