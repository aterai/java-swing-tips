// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.IconUIResource;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(makeModel());
    table.setAutoCreateRowSorter(true);

    JButton clearButton = new JButton("clear SortKeys");
    clearButton.addActionListener(e -> table.getRowSorter().setSortKeys(null));

    add(makeRadioPane(table), BorderLayout.NORTH);
    add(clearButton, BorderLayout.SOUTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private Box makeRadioPane(JTable table) {
    ButtonGroup bg = new ButtonGroup();
    ItemListener handler = e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        String name = bg.getSelection().getActionCommand();
        SortIconType type = SortIconType.valueOf(name);
        UIManager.put("Table.ascendingSortIcon", type.getAscendingIcon());
        UIManager.put("Table.descendingSortIcon", type.getDescendingIcon());
        table.getTableHeader().repaint();
      }
    };
    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
    box.add(new JLabel("Table Sort Icon: "));
    Arrays.asList(SortIconType.values()).forEach(type -> {
      String name = type.name();
      boolean selected = type == SortIconType.DEFAULT;
      JRadioButton radio = new JRadioButton(name, selected);
      radio.addItemListener(handler);
      radio.setActionCommand(name);
      box.add(radio);
      bg.add(radio);
    });
    box.add(Box.createHorizontalGlue());
    return box;
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

class EmptyIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty icon */
  }

  @Override public int getIconWidth() {
    return 0;
  }

  @Override public int getIconHeight() {
    return 0;
  }
}

@SuppressWarnings("ImmutableEnumChecker")
enum SortIconType {
  DEFAULT(
      UIManager.getLookAndFeelDefaults().getIcon("Table.ascendingSortIcon"),
      UIManager.getLookAndFeelDefaults().getIcon("Table.descendingSortIcon")
  ),
  EMPTY(
      new EmptyIcon(),
      new EmptyIcon()
  ),
  CUSTOM(
      makeIcon("example/ascending.png"),
      makeIcon("example/descending.png")
  );
  private final Icon ascending;
  private final Icon descending;

  SortIconType(Icon ascending, Icon descending) {
    this.ascending = ascending;
    this.descending = descending;
  }

  public Icon getAscendingIcon() {
    return ascending;
  }

  public Icon getDescendingIcon() {
    return descending;
  }

  private static Icon makeIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    ImageIcon icon = Optional.ofNullable(cl.getResource(path))
        .map(ImageIcon::new)
        .orElseGet(() -> new ImageIcon(makeMissingImage()));
    return new IconUIResource(icon);
  }

  private static Image makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("html.missingImage");
    int iw = missingIcon.getIconWidth();
    int ih = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, (16 - iw) / 2, (16 - ih) / 2);
    g2.dispose();
    return bi;
  }
}
