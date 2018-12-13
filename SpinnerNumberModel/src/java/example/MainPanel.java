package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final List<SpinnerNumberModel> list1 = Arrays.asList(
      new SpinnerNumberModel(Byte.valueOf((byte) Byte.MAX_VALUE),
                             Byte.valueOf((byte) 0),
                             Byte.valueOf((byte) Byte.MAX_VALUE),
                             Byte.valueOf((byte) 1)),
      new SpinnerNumberModel(Short.valueOf((short) Short.MAX_VALUE),
                             Short.valueOf((short) 0),
                             Short.valueOf((short) Short.MAX_VALUE),
                             Short.valueOf((short) 1)),
      new SpinnerNumberModel(Integer.MAX_VALUE,
                             0,
                             Integer.MAX_VALUE,
                             1),
      new SpinnerNumberModel(Long.valueOf(Long.MAX_VALUE),
                             Long.valueOf(0),
                             Long.valueOf(Long.MAX_VALUE),
                             Long.valueOf(1)));
  private final List<SpinnerNumberModel> list2 = Arrays.asList(
      new SpinnerNumberModel(Long.valueOf(Byte.MAX_VALUE),
                             Long.valueOf(0),
                             Long.valueOf(Byte.MAX_VALUE),
                             Long.valueOf(1)),
      new SpinnerNumberModel(Long.valueOf(Short.MAX_VALUE),
                             Long.valueOf(0),
                             Long.valueOf(Short.MAX_VALUE),
                             Long.valueOf(1)),
      new SpinnerNumberModel(Long.valueOf(Integer.MAX_VALUE),
                             Long.valueOf(0),
                             Long.valueOf(Integer.MAX_VALUE),
                             Long.valueOf(1)),
      new SpinnerNumberModel(Long.valueOf(Long.MAX_VALUE),
                             Long.valueOf(0),
                             Long.valueOf(Long.MAX_VALUE),
                             Long.valueOf(1)));

  public MainPanel() {
    super(new BorderLayout());
    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("Byte, Short, Integer, Long", makeJSpinnerListPanel(list1)));
    box.add(makeTitledPanel("Long.valueOf", makeJSpinnerListPanel(list2)));
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box makeJSpinnerListPanel(List<SpinnerNumberModel> list) {
    Box box = Box.createVerticalBox();
    list.stream().map(JSpinner::new).forEach(spinner -> {
      box.add(spinner);
      box.add(Box.createVerticalStrut(2));
    });
    return box;
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
