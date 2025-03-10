// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(2, 1));
    add(makeUI(null, 4));
    add(makeUI("MMMMMMM", 4));
    setPreferredSize(new Dimension(320, 240));
  }

  public Component makeUI(String prototypeValue, int visibleRowCount) {
    String[] model1 = IntStream.range(0, 20).mapToObj(Objects::toString).toArray(String[]::new);
    JList<String> list1 = new JList<>(model1);
    list1.setVisibleRowCount(visibleRowCount);
    list1.setPrototypeCellValue(prototypeValue);

    String[] model2 = {"looooooooooooooong"};
    JList<String> list2 = new JList<>(model2);
    list2.setVisibleRowCount(visibleRowCount);
    list2.setPrototypeCellValue(prototypeValue);

    JList<String> list3 = new JList<String>(model2) {
      @Override public Dimension getPreferredScrollableViewportSize() {
        Dimension d = super.getPreferredScrollableViewportSize();
        d.width = 60;
        return d;
      }
    };
    list3.setVisibleRowCount(visibleRowCount);
    list3.setPrototypeCellValue(prototypeValue);

    JPanel p = new JPanel(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(2, 2, 0, 2);
    c.gridx = 0;
    p.add(new JScrollPane(list1), c);
    c.gridx = 1;
    p.add(new JScrollPane(list2), c);
    c.gridx = 2;
    p.add(new JScrollPane(list3), c);

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
