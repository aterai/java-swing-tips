// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Arrays;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultTableModel model1 = new DefaultTableModel(100, 256);
    JTable table = new JTable(model1);
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    JScrollPane scroll = new JScrollPane(table);
    scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

    // Move the horizontal JScrollBar out of the JScrollPane,
    // it still shares the BoundedRangeModel with the viewport
    JPanel scrollBox = new JPanel(new BorderLayout());
    scrollBox.setOpaque(false);
    scrollBox.setBorder(BorderFactory.createEmptyBorder(3, 0, 0, 0));
    scrollBox.add(scroll.getHorizontalScrollBar());

    DefaultTableModel model2 = new DefaultTableModel(50, 8);
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder());
    ButtonGroup group = new ButtonGroup();
    JRadioButton r1 = createRadioButton("a", table, model1);
    r1.setSelected(true);
    JRadioButton r2 = createRadioButton("b", table, model2);
    Arrays.asList(r1, r2).forEach(r -> {
      group.add(r);
      box.add(r);
    });
    box.add(Box.createHorizontalGlue());

    JSplitPane horizontalSplit = createScrollSplitPane();
    horizontalSplit.setLeftComponent(box);
    horizontalSplit.setRightComponent(scrollBox);

    add(scroll);
    add(horizontalSplit, BorderLayout.SOUTH);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JRadioButton createRadioButton(String text, JTable table, TableModel model) {
    JRadioButton radio = new JRadioButton(text);
    radio.setOpaque(false);
    radio.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        table.setModel(model);
      }
    });
    return radio;
  }

  private static JSplitPane createScrollSplitPane() {
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setBackground(Color.WHITE);
    split.setContinuousLayout(true);
    split.setBorder(BorderFactory.createEmptyBorder());
    EventQueue.invokeLater(() -> split.setDividerLocation(.4));

    JLabel tripleColon = new JLabel("⫶");
    tripleColon.setForeground(Color.GRAY);
    tripleColon.setBorder(BorderFactory.createEmptyBorder(3, 2, 0, 4));

    BasicSplitPaneDivider divider = ((BasicSplitPaneUI) split.getUI()).getDivider();
    divider.setLayout(new BorderLayout());
    divider.setBorder(BorderFactory.createEmptyBorder());
    divider.setBackground(Color.WHITE);
    divider.add(tripleColon);
    // Set the size via JSplitPane instead of BasicSplitPaneDivider#setDividerSize(...),
    // JSplitPane#getDividerSize() is used by setDividerLocation(double)
    // and it is propagated to the divider by BasicSplitPaneUI
    split.setDividerSize(tripleColon.getPreferredSize().width);
    return split;
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
