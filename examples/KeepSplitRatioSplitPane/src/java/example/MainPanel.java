// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new GridLayout(0, 1, 0, 0));
    JSplitPane split1 = makeSplitPane("Default(ResizeWeight:0.5)");
    add(split1);
    JSplitPane split2 = makeSplitPane("SplitPaneWrapper(Keep ratio)");
    add(new SplitPaneWrapper(split2));
    List<JSplitPane> list = Arrays.asList(split1, split2);
    JMenu menu = new JMenu("JSplitPane");
    menu.add("resetToPreferredSizes").addActionListener(e ->
        list.forEach(JSplitPane::resetToPreferredSizes));
    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    EventQueue.invokeLater(() -> {
      getRootPane().setJMenuBar(mb);
      list.forEach(s -> s.setDividerLocation(.5));
    });
    setPreferredSize(new Dimension(320, 240));
  }

  private static JSplitPane makeSplitPane(String title) {
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
    split.setBorder(BorderFactory.createTitledBorder(title));
    split.setLeftComponent(makeLabel(Color.CYAN));
    split.setRightComponent(makeLabel(Color.ORANGE));
    split.setResizeWeight(.5);
    // EventQueue.invokeLater(() -> split.setDividerLocation(.5));
    return split;
  }

  private static JLabel makeLabel(Color color) {
    JLabel label = new JLabel(" ", SwingConstants.CENTER);
    label.setOpaque(true);
    label.setBackground(color);
    label.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        label.setText(String.format("%04dpx", label.getWidth()));
      }
    });
    return label;
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

class SplitPaneWrapper extends JPanel {
  private final JSplitPane splitPane;

  protected SplitPaneWrapper(JSplitPane splitPane) {
    super(new BorderLayout());
    this.splitPane = splitPane;
    add(splitPane);
  }

  @Override public void doLayout() {
    int size = getOrientedSize(splitPane);
    int loc = splitPane.getDividerLocation();
    BigDecimal ratio = BigDecimal.valueOf(loc / (double) size)
        .setScale(2, RoundingMode.HALF_UP);
    super.doLayout();
    if (splitPane.isShowing()) {
      EventQueue.invokeLater(() -> {
        int sz = getOrientedSize(splitPane);
        int iv = (int) (.5 + sz * ratio.doubleValue());
        splitPane.setDividerLocation(iv);
      });
    }
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  private static int getOrientedSize(JSplitPane sp) {
    return sp.getOrientation() == JSplitPane.VERTICAL_SPLIT
        ? sp.getHeight() - sp.getDividerSize()
        : sp.getWidth() - sp.getDividerSize();
  }
}
