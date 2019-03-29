// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTextArea log = new JTextArea();

    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retvalue = chooser.showOpenDialog(log.getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("JList tooltips");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      stream(chooser)
        .filter(JList.class::isInstance)
        .map(JList.class::cast)
        .forEach(MainPanel::setCellRenderer);
      int retvalue = chooser.showOpenDialog(log.getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button3 = new JButton("JTable tooltips");
    button3.addActionListener(e -> {
      String key = "viewTypeDetails";
      JFileChooser chooser = new JFileChooser();
      Optional.ofNullable(chooser.getActionMap().get(key))
        .ifPresent(a -> a.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, key)));
      stream(chooser)
        .filter(JTable.class::isInstance)
        .map(JTable.class::cast)
        .findFirst()
        .ifPresent(MainPanel::setCellRenderer);
      int retvalue = chooser.showOpenDialog(log.getRootPane());
      if (retvalue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
    p.add(button3);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  public static Stream<Component> stream(Container parent) {
    return Stream.of(parent.getComponents())
      .filter(Container.class::isInstance)
      .map(c -> stream(Container.class.cast(c)))
      .reduce(Stream.of(parent), Stream::concat);
  }

  private static void setCellRenderer(JList<?> list) {
    list.setCellRenderer(new TooltipListCellRenderer<>());
  }

  private static void setCellRenderer(JTable table) {
    table.setDefaultRenderer(Object.class, new TooltipTableCellRenderer());
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see https://ateraimemo.com/Swing/ToolTipOnCellBounds.html
class TooltipListCellRenderer<E> implements ListCellRenderer<E> {
  private final ListCellRenderer<? super E> renderer = new DefaultListCellRenderer();

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    JLabel l = (JLabel) renderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    // Insets i = l.getInsets();
    // Container c = SwingUtilities.getAncestorOfClass(JViewport.class, list);
    // Rectangle rect = c.getBounds();
    // rect.width -= i.left + i.right;
    // FontMetrics fm = l.getFontMetrics(l.getFont());
    // String str = Objects.toString(value, "");
    // l.setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
    l.setToolTipText(Objects.toString(value, null));
    return l;
  }
}

// @see https://ateraimemo.com/Swing/ClippedCellTooltips.html
class TooltipTableCellRenderer implements TableCellRenderer {
  private final TableCellRenderer renderer = new DefaultTableCellRenderer();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JLabel l = (JLabel) renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    Insets i = l.getInsets();
    Rectangle rect = table.getCellRect(row, column, false);
    rect.width -= i.left + i.right;
    Optional.ofNullable(l.getIcon())
      .ifPresent(icon -> rect.width -= icon.getIconWidth() + l.getIconTextGap());
    FontMetrics fm = l.getFontMetrics(l.getFont());
    String str = Objects.toString(value, "");
    l.setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
    return l;
  }
}
