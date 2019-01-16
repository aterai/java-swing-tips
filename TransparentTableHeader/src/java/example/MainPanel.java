// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private final Color alphaZero = new Color(0x0, true);
  private final Color color = new Color(255, 0, 0, 50);

  private MainPanel() {
    super(new BorderLayout());

    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
      {"aaa", 12, true}, {"bbb", 5, false},
      {"CCC", 92, true}, {"DDD", 0, false}
    };
    TableModel model = new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return column == 2;
      }

      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JComponent) {
          ((JComponent) c).setOpaque(false);
        }
        return c;
      }

      @Override public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        c.setForeground(Color.BLACK);
        return c;
      }
    };
    // table.setAutoCreateRowSorter(true);
    table.setRowSelectionAllowed(true);
    table.setFillsViewportHeight(true);
    table.setShowVerticalLines(false);
    // table.setShowHorizontalLines(false);
    table.setFocusable(false);
    // table.setCellSelectionEnabled(false);
    table.setIntercellSpacing(new Dimension(0, 1));
    table.setRowHeight(24);
    table.setSelectionForeground(table.getForeground());
    table.setSelectionBackground(new Color(0, 0, 100, 50));

    JCheckBox checkBox = new JCheckBox() {
      @Override protected void paintComponent(Graphics g) {
        g.setColor(new Color(0, 0, 100, 50));
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
      }
    };
    checkBox.setOpaque(false);
    checkBox.setHorizontalAlignment(SwingConstants.CENTER);
    table.setDefaultEditor(Boolean.class, new DefaultCellEditor(checkBox));

    table.setDefaultRenderer(Object.class, new TranslucentObjectRenderer());
    table.setDefaultRenderer(Boolean.class, new TranslucentBooleanRenderer());
    table.setOpaque(false);
    table.setBackground(alphaZero);
    // table.setGridColor(alphaZero);
    table.getTableHeader().setDefaultRenderer(new TransparentHeader());
    table.getTableHeader().setOpaque(false);
    table.getTableHeader().setBackground(alphaZero);

    TexturePaint texture = makeImageTexture();
    JScrollPane scroll = new JScrollPane(table) {
      @Override protected void paintComponent(Graphics g) {
        if (Objects.nonNull(texture)) {
          Graphics2D g2 = (Graphics2D) g.create();
          g2.setPaint(texture);
          g2.fillRect(0, 0, getWidth(), getHeight());
          g2.dispose();
        }
        super.paintComponent(g);
      }
    };
    scroll.setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    scroll.setBackground(alphaZero);
    scroll.getViewport().setOpaque(false);
    scroll.getViewport().setBackground(alphaZero);
    scroll.setColumnHeader(new JViewport());
    scroll.getColumnHeader().setOpaque(false);
    scroll.getColumnHeader().setBackground(alphaZero);

    JCheckBox check = new JCheckBox("setBackground(new Color(255, 0, 0, 50))");
    check.addActionListener(e -> table.setBackground(((JCheckBox) e.getSource()).isSelected() ? color : alphaZero));

    add(check, BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TexturePaint makeImageTexture() {
    // unkaku_w.png http://www.viva-edo.com/komon/edokomon.html
    BufferedImage bi = Optional.ofNullable(MainPanel.class.getResource("unkaku_w.png"))
        .map(url -> {
          try {
            return ImageIO.read(url);
          } catch (IOException ex) {
            return makeMissingImage();
          }
        }).orElseGet(() -> makeMissingImage());
    return new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));
  }

  private static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TransparentHeader extends JLabel implements TableCellRenderer {
  private final Border border = BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
      BorderFactory.createEmptyBorder(2, 2, 1, 2));
  private final Color alphaZero = new Color(0x0, true);

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    this.setText(Objects.toString(value, ""));
    this.setHorizontalAlignment(SwingConstants.CENTER);
    this.setOpaque(false);
    this.setBackground(alphaZero);
    this.setForeground(Color.BLACK);
    this.setBorder(border);
    return this;
  }
}

class TranslucentObjectRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
    // c.setOpaque(true);
    c.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    return c;
  }
}

class TranslucentBooleanRenderer extends JCheckBox implements TableCellRenderer {
  private static final Color SELECTION_BACKGROUND = new Color(0, 0, 100, 50);

  @Override public void updateUI() {
    super.updateUI();
    setHorizontalAlignment(SwingConstants.CENTER);
    setBorderPainted(true);
    setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    setOpaque(false);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    setHorizontalAlignment(SwingConstants.CENTER);
    if (isSelected) {
      setForeground(table.getSelectionForeground());
      super.setBackground(SELECTION_BACKGROUND);
    } else {
      setForeground(table.getForeground());
      setBackground(table.getBackground());
    }
    setSelected(Objects.equals(value, Boolean.TRUE));
    return this;
  }

  @Override protected void paintComponent(Graphics g) {
    if (!isOpaque()) {
      g.setColor(getBackground());
      g.fillRect(0, 0, getWidth(), getHeight());
    }
    super.paintComponent(g);
  }

  // // Overridden for performance reasons. ---->
  // @Override public boolean isOpaque() {
  //   Color back = getBackground();
  //   Component p = getParent();
  //   if (Objects.nonNull(p)) {
  //     p = p.getParent();
  //   } // p should now be the JTable.
  //   boolean colorMatch = Objects.nonNull(back) && Objects.nonNull(p) && back.equals(p.getBackground()) && p.isOpaque();
  //   return !colorMatch && super.isOpaque();
  // }
  // @Override protected void firePropertyChange(String propertyName, Object oldValue, Object newValue) {
  //   // System.out.println(propertyName);
  //   // String literal pool
  //   // if ((propertyName == "font" || propertyName == "foreground") && oldValue != newValue) {
  //   boolean flag = "font".equals(propertyName) || "foreground".equals(propertyName);
  //   if (flag && !Objects.equals(oldValue, newValue)) {
  //     super.firePropertyChange(propertyName, oldValue, newValue);
  //   }
  // }
  // @Override public void firePropertyChange(String propertyName, boolean oldValue, boolean newValue) { /* Overridden for performance reasons. */ }
  // @Override public void repaint(long tm, int x, int y, int width, int height) { /* Overridden for performance reasons. */ }
  // @Override public void repaint(Rectangle r) { /* Overridden for performance reasons. */ }
  // @Override public void repaint() { /* Overridden for performance reasons. */ }
  // @Override public void invalidate() { /* Overridden for performance reasons. */ }
  // @Override public void validate() { /* Overridden for performance reasons. */ }
  // @Override public void revalidate() { /* Overridden for performance reasons. */ }
  // // <---- Overridden for performance reasons.
}
