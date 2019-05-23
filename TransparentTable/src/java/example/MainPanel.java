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
import javax.swing.plaf.ColorUIResource;
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
      @Override public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
      }
    };
    JTable table = new JTable(model) {
      @Override public void updateUI() {
        // [JDK-6788475] Changing to Nimbus LAF and back doesn't reset look and feel of JTable completely - Java Bug System
        // https://bugs.openjdk.java.net/browse/JDK-6788475
        // XXX: set dummy ColorUIResource
        setSelectionForeground(new ColorUIResource(Color.RED));
        setSelectionBackground(new ColorUIResource(Color.RED));
        super.updateUI();
        setAutoCreateRowSorter(true);
        setRowSelectionAllowed(true);
        setFillsViewportHeight(true);
        setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        setDefaultRenderer(Boolean.class, new TranslucentBooleanRenderer());
        setOpaque(false);
        setBackground(alphaZero);

        TableModel m = getModel();
        for (int i = 0; i < m.getColumnCount(); i++) {
          TableCellRenderer r = getDefaultRenderer(m.getColumnClass(i));
          if (r instanceof Component) {
            SwingUtilities.updateComponentTreeUI((Component) r);
          }
        }
      }

      @Override public Component prepareEditor(TableCellEditor editor, int row, int column) {
        Component c = super.prepareEditor(editor, row, column);
        if (c instanceof JTextField) {
          JTextField tf = (JTextField) c;
          tf.setOpaque(false);
        } else if (c instanceof JCheckBox) {
          JCheckBox cb = (JCheckBox) c;
          cb.setBackground(getSelectionBackground());
        }
        return c;
      }
    };

    TexturePaint texture = makeImageTexture();
    JScrollPane scroll = new JScrollPane(table) {
      @Override protected JViewport createViewport() {
        return new JViewport() {
          @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setPaint(texture);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
          }
        };
      }
    };
    scroll.getViewport().setOpaque(false);
    scroll.getViewport().setBackground(alphaZero);

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

class TranslucentBooleanRenderer implements TableCellRenderer {
  private final JCheckBox renderer = new JCheckBox() {
    @Override public void updateUI() {
      super.updateUI();
      // NG???: setHorizontalAlignment(SwingConstants.CENTER);
      setBorderPainted(true);
      setBorderPaintedFlat(true);
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

  protected TranslucentBooleanRenderer() {
    renderer.setHorizontalAlignment(SwingConstants.CENTER);
  }

  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    if (isSelected) {
      renderer.setOpaque(true);
      renderer.setForeground(table.getSelectionForeground());
      renderer.setBackground(table.getSelectionBackground());
    } else {
      renderer.setOpaque(false);
      renderer.setForeground(table.getForeground());
      renderer.setBackground(table.getBackground());
    }
    renderer.setSelected(Objects.equals(value, Boolean.TRUE));
    return renderer;
  }
}
