// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TexturePaint texture = ImageUtils.makeImageTexture();
    JTable table = makeTable();
    JScrollPane scroll = new JScrollPane(table) {
      @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(texture);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
        super.paintComponent(g);
      }
    };
    Color alphaZero = table.getBackground();
    scroll.setOpaque(false);
    scroll.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    scroll.setBackground(alphaZero);
    scroll.getViewport().setOpaque(false);
    scroll.getViewport().setBackground(alphaZero);
    scroll.setColumnHeader(new JViewport());
    scroll.getColumnHeader().setOpaque(false);
    scroll.getColumnHeader().setBackground(alphaZero);

    Color color = new Color(0x32_FF_00_00, true);
    JCheckBox check = new JCheckBox("setBackground(new Color(0x32_FF_00_00, true))");
    check.addActionListener(e -> {
      boolean b = ((JCheckBox) e.getSource()).isSelected();
      table.setBackground(b ? color : alphaZero);
    });

    add(check, BorderLayout.NORTH);
    add(scroll);
    setPreferredSize(new Dimension(320, 240));
  }

  private static TableModel makeModel() {
    String[] columnNames = {"String", "Integer", "Boolean"};
    Object[][] data = {
        {"aaa", 12, true}, {"bbb", 5, false}, {"CCC", 92, true}, {"DDD", 0, false}
    };
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return column == 2;
      }

      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
  }

  private JTable makeTable() {
    return new JTable(makeModel()) {
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

      @Override public void updateUI() {
        super.updateUI();
        // setAutoCreateRowSorter(true);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
        setShowVerticalLines(false);
        // setShowHorizontalLines(false);
        setFocusable(false);
        // setCellSelectionEnabled(false);
        setIntercellSpacing(new Dimension(0, 1));
        setRowHeight(24);
        setSelectionForeground(getForeground());
        setSelectionBackground(new Color(0, 0, 100, 50));

        JCheckBox checkBox = new JCheckBox() {
          @Override protected void paintComponent(Graphics g) {
            g.setColor(new Color(0, 0, 100, 50));
            g.fillRect(0, 0, getWidth(), getHeight());
            super.paintComponent(g);
          }
        };
        checkBox.setOpaque(false);
        checkBox.setHorizontalAlignment(SwingConstants.CENTER);
        setDefaultEditor(Boolean.class, new DefaultCellEditor(checkBox));

        setDefaultRenderer(Object.class, new TranslucentObjectRenderer());
        setDefaultRenderer(Boolean.class, new TranslucentBooleanRenderer());
        setOpaque(false);
        Color alphaZero = new Color(0x0, true);
        setBackground(alphaZero);
        // setGridColor(alphaZero);
        getTableHeader().setDefaultRenderer(new TransparentHeader());
        getTableHeader().setOpaque(false);
        getTableHeader().setBackground(alphaZero);
      }
    };
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

class TransparentHeader implements TableCellRenderer {
  private final CompoundBorder border = BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, Color.BLACK),
      BorderFactory.createEmptyBorder(2, 2, 1, 2));
  private final Color alphaZero = new Color(0x0, true);
  private final TableCellRenderer renderer = new DefaultTableCellRenderer();

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = renderer.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel) {
      JLabel l = (JLabel) c;
      l.setText(Objects.toString(value, ""));
      l.setHorizontalAlignment(SwingConstants.CENTER);
      l.setOpaque(false);
      l.setBackground(alphaZero);
      l.setForeground(Color.BLACK);
      l.setBorder(border);
    }
    return c;
  }
}

class TranslucentObjectRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JComponent) {
      // ((JComponent) c).setOpaque(true);
      ((JComponent) c).setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
    }
    return c;
  }
}

class TranslucentBooleanRenderer implements TableCellRenderer {
  private static final Color SELECTION_BGC = new Color(0, 0, 100, 50);
  private final JCheckBox renderer = new JCheckBox() {
    @Override public void updateUI() {
      super.updateUI();
      // NG???: setHorizontalAlignment(SwingConstants.CENTER);
      setBorderPainted(true);
      setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      setOpaque(false);
    }

    @Override protected void paintComponent(Graphics g) {
      if (!isOpaque()) {
        g.setColor(getBackground());
        g.fillRect(0, 0, getWidth(), getHeight());
      }
      super.paintComponent(g);
    }
  };

  // protected TranslucentBooleanRenderer() {
  //   renderer.setHorizontalAlignment(SwingConstants.CENTER);
  // }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      renderer.setForeground(table.getSelectionForeground());
      renderer.setBackground(SELECTION_BGC);
    } else {
      renderer.setForeground(table.getForeground());
      renderer.setBackground(table.getBackground());
    }
    renderer.setSelected(Objects.equals(value, Boolean.TRUE));
    renderer.setHorizontalAlignment(SwingConstants.CENTER);
    return renderer;
  }
}

final class ImageUtils {
  private ImageUtils() {
    /* Singleton */
  }

  public static TexturePaint makeImageTexture() {
    // unkaku_w.png https://www.viva-edo.com/komon/edokomon.html
    String path = "example/unkaku_w.png";
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    BufferedImage bi = Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return ImageIO.read(s);
      } catch (IOException ex) {
        return makeMissingImage();
      }
    }).orElseGet(ImageUtils::makeMissingImage);
    return new TexturePaint(bi, new Rectangle(bi.getWidth(), bi.getHeight()));
  }

  public static BufferedImage makeMissingImage() {
    Icon missingIcon = UIManager.getIcon("OptionPane.errorIcon");
    int w = missingIcon.getIconWidth();
    int h = missingIcon.getIconHeight();
    BufferedImage bi = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = bi.createGraphics();
    missingIcon.paintIcon(null, g2, 0, 0);
    g2.dispose();
    return bi;
  }
}
