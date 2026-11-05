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

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(scroll);
    add(horizontalSplit, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  @Override public void updateUI() {
    super.updateUI();
    // Re-read the color on every LookAndFeel change instead of a fixed Color.WHITE
    setBackground(UIManager.getColor("Table.background"));
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
    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT) {
      @Override public void updateUI() {
        super.updateUI();
        // The new SplitPaneUI creates a new divider,
        // so the customization must be applied again after every LookAndFeel change
        customizeDivider(this);
      }
    };
    split.setContinuousLayout(true);
    split.setBorder(BorderFactory.createEmptyBorder());
    EventQueue.invokeLater(() -> split.setDividerLocation(.4));
    return split;
  }

  private static void customizeDivider(JSplitPane split) {
    Color bgc = UIManager.getColor("Table.background");
    split.setBackground(bgc);
    JLabel tripleColon = new JLabel("⫶");
    tripleColon.setForeground(getColor("Label.disabledForeground", Color.GRAY));
    tripleColon.setBorder(BorderFactory.createEmptyBorder(3, 2, 0, 4));
    BasicSplitPaneDivider divider = ((BasicSplitPaneUI) split.getUI()).getDivider();
    divider.setLayout(new BorderLayout());
    divider.setBorder(BorderFactory.createEmptyBorder());
    divider.setBackground(bgc);
    divider.add(tripleColon);
    // Set the size via JSplitPane instead of BasicSplitPaneDivider#setDividerSize(...),
    // JSplitPane#getDividerSize() is used by setDividerLocation(double)
    // and it is propagated to the divider by BasicSplitPaneUI
    split.setDividerSize(tripleColon.getPreferredSize().width);
  }

  private static Color getColor(String key, Color defaultColor) {
    Color color = UIManager.getColor(key);
    return color == null ? defaultColor : color;
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

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = createButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton createButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
