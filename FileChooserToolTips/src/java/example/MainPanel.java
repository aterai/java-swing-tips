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
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button2 = new JButton("JList tooltips");
    button2.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      descendants(chooser)
          .filter(JList.class::isInstance)
          .map(JList.class::cast)
          .forEach(MainPanel::setCellRenderer);
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton button3 = new JButton("JTable tooltips");
    button3.addActionListener(e -> {
      String key = "viewTypeDetails";
      JFileChooser chooser = new JFileChooser();
      Optional.ofNullable(chooser.getActionMap().get(key)).ifPresent(a -> {
        ActionEvent ae = new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, key);
        a.actionPerformed(ae);
      });
      descendants(chooser)
          .filter(JTable.class::isInstance)
          .map(JTable.class::cast)
          .findFirst()
          .ifPresent(MainPanel::setCellRenderer);
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
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

  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }

  private static void setCellRenderer(JList<?> list) {
    list.setCellRenderer(new TooltipListCellRenderer<>());
  }

  private static void setCellRenderer(JTable table) {
    table.setDefaultRenderer(Object.class, new TooltipTableCellRenderer());
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
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
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    c.setFont(list.getFont());
    // Container v = SwingUtilities.getAncestorOfClass(JViewport.class, list);
    // if (c instanceof JComponent && v instanceof JViewport) {
    //   JComponent l = (JComponent) c;
    //   Rectangle rect = ((JViewport) v).getViewRect();
    //   FontMetrics fm = c.getFontMetrics(c.getFont());
    //   String str = Objects.toString(value, "");
    //   l.setToolTipText(fm.stringWidth(str) > rect.width ? str : null);
    // }
    if (c instanceof JComponent) {
      ((JComponent) c).setToolTipText(Objects.toString(value, null));
    }
    return c;
  }
}

// @see https://ateraimemo.com/Swing/ClippedCellTooltips.html
class TooltipTableCellRenderer implements TableCellRenderer {
  private final TableCellRenderer renderer = new DefaultTableCellRenderer();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = renderer.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    c.setFont(table.getFont());
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      Insets i = l.getInsets();
      Rectangle cr = table.getCellRect(row, column, false);
      cr.width -= i.left + i.right;
      int gap = l.getIconTextGap();
      Optional.ofNullable(l.getIcon()).ifPresent(icon -> cr.width -= icon.getIconWidth() + gap);
      FontMetrics fm = c.getFontMetrics(c.getFont());
      String str = Objects.toString(value, "");
      l.setToolTipText(fm.stringWidth(str) > cr.width ? str : null);
    }
    return c;
  }
}
