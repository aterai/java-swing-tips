// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.DimensionUIResource;
import javax.swing.table.DefaultTableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(new DefaultTableModel(15, 3)) {
      @Override public void updateUI() {
        ColorUIResource reset = new ColorUIResource(Color.RED);
        setSelectionForeground(reset);
        setSelectionBackground(reset);
        super.updateUI();
        UIDefaults def = UIManager.getLookAndFeelDefaults();
        Object showGrid = def.get("Table.showGrid");
        Color gridColor = def.getColor("Table.gridColor");
        if (showGrid == null && gridColor != null) {
          setShowGrid(true);
          setIntercellSpacing(new DimensionUIResource(1, 1));
          createDefaultRenderers();
        }
      }
    };
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setAutoCreateRowSorter(true);

    JScrollPane scroll = new JScrollPane(table);
    // scroll.setBorder(new BorderUIResource(BorderFactory.createLineBorder(Color.BLUE, 5)));
    add(scroll);

    String key = "Table.scrollPaneBorder";
    JCheckBox check = new JCheckBox(key, Objects.nonNull(UIManager.getBorder(key))) {
      @Override public void updateUI() {
        super.updateUI();
        boolean b = Objects.nonNull(UIManager.getLookAndFeelDefaults().getBorder(key));
        updateTableScrollPane(scroll, key, b);
        setSelected(b);
      }
    };
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      updateTableScrollPane(scroll, key, b);
    });
    add(check, BorderLayout.SOUTH);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    setPreferredSize(new Dimension(320, 240));
  }

  public static void updateTableScrollPane(Component scroll, String key, boolean lnf) {
    Border border;
    if (lnf) {
      border = UIManager.getLookAndFeelDefaults().getBorder(key);
    } else {
      border = new BorderUIResource(BorderFactory.createEmptyBorder());
    }
    UIManager.put(key, border);
    SwingUtilities.updateComponentTreeUI(scroll);
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
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
        ex.printStackTrace();
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
