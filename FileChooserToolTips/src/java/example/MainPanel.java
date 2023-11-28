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
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(makeButton1());
    p.add(makeButton2());
    p.add(makeButton3());
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private JButton makeButton1() {
    JButton button = new JButton("Default");
    button.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(log.getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });
    return button;
  }

  private JButton makeButton2() {
    JButton button = new JButton("JList tooltips");
    button.addActionListener(e -> {
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
    return button;
  }

  private JButton makeButton3() {
    JButton button = new JButton("JTable tooltips");
    button.addActionListener(e -> {
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
    return button;
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
      Rectangle rect = table.getCellRect(row, column, false);
      rect.width -= i.left + i.right;
      l.setToolTipText(isClipped(l, rect) ? l.getText() : table.getToolTipText());
    }
    return c;
  }

  private static boolean isClipped(JLabel label, Rectangle viewR) {
    Rectangle iconR = new Rectangle();
    Rectangle textR = new Rectangle();
    String str = SwingUtilities.layoutCompoundLabel(
        label,
        label.getFontMetrics(label.getFont()),
        label.getText(),
        label.getIcon(),
        label.getVerticalAlignment(),
        label.getHorizontalAlignment(),
        label.getVerticalTextPosition(),
        label.getHorizontalTextPosition(),
        viewR,
        iconR,
        textR,
        label.getIconTextGap());
    return !Objects.equals(label.getText(), str);
  }
}
