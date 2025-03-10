// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultTableModel m1 = new DefaultTableModel(100, 256);
    JTable table = new JTable(m1);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    JPanel panel = new JPanel(new BorderLayout());
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
    panel.add(scroll.getHorizontalScrollBar());

    JRadioButton r1 = new JRadioButton("a", true);
    r1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        table.setModel(m1);
      }
    });

    DefaultTableModel m2 = new DefaultTableModel(50, 8);
    JRadioButton r2 = new JRadioButton("b");
    r2.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        table.setModel(m2);
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder());
    ButtonGroup bg = new ButtonGroup();
    Arrays.asList(r1, r2).forEach(r -> {
      r.setOpaque(false);
      bg.add(r);
      box.add(r);
    });
    box.add(Box.createHorizontalGlue());

    JSplitPane horizontalBox = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    horizontalBox.setBackground(Color.WHITE);
    horizontalBox.setLeftComponent(box);
    horizontalBox.setRightComponent(panel);
    horizontalBox.setContinuousLayout(true);
    horizontalBox.setBorder(BorderFactory.createEmptyBorder());
    EventQueue.invokeLater(() -> horizontalBox.setDividerLocation(.4));

    JLabel tripleColon = new JLabel("⫶");
    tripleColon.setForeground(Color.GRAY);
    tripleColon.setBorder(BorderFactory.createEmptyBorder(3, 2, 0, 4));

    BasicSplitPaneDivider divider = ((BasicSplitPaneUI) horizontalBox.getUI()).getDivider();
    divider.setLayout(new BorderLayout());
    divider.setBorder(BorderFactory.createEmptyBorder());
    divider.setBackground(Color.WHITE);
    divider.add(tripleColon);
    divider.setDividerSize(tripleColon.getPreferredSize().width);

    add(scroll);
    add(horizontalBox, BorderLayout.SOUTH);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
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
