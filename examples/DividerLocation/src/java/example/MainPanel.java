// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    split.setTopComponent(new JScrollPane(new JTable(8, 3)));
    split.setBottomComponent(new JScrollPane(new JTree()));

    JRadioButton r0 = new JRadioButton("0.0", true);
    r0.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        split.setResizeWeight(0d);
      }
    });

    JRadioButton r1 = new JRadioButton("0.5");
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        split.setResizeWeight(.5);
      }
    });

    JRadioButton r2 = new JRadioButton("1.0");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        split.setResizeWeight(1d);
      }
    });

    ButtonGroup bg = new ButtonGroup();
    JPanel p = new JPanel();
    p.add(new JLabel("JSplitPane#setResizeWeight: "));
    Stream.of(r0, r1, r2).forEach(r -> {
      bg.add(r);
      p.add(r);
    });
    add(p, BorderLayout.NORTH);
    add(split);
    setPreferredSize(new Dimension(320, 240));

    EventQueue.invokeLater(() -> split.setDividerLocation(.5));
    // TEST: EventQueue.invokeLater(() -> split.setResizeWeight(.5));
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
