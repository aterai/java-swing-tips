// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ListModel<IconItem> list = createIconList();
    int gap = 2;
    Icon icon = list.getElementAt(0).getSmallIcon();
    int iw = gap + icon.getIconWidth();
    int ih = gap + icon.getIconHeight();
    Dimension d = new Dimension(iw * 3 + gap, ih * 3 + gap);
    JList<IconItem> editor = new EditorFromList<>(list, d);
    editor.setFixedCellWidth(iw);
    editor.setFixedCellHeight(ih);
    JTable table = new IconTable(createIconTableModel(list), editor);
    JPanel p = new JPanel(new GridBagLayout());
    p.add(table, new GridBagConstraints());
    p.setBackground(Color.WHITE);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<IconItem> createIconList() {
    DefaultListModel<IconItem> list = new DefaultListModel<>();
    list.addElement(new IconItem("wi0009"));
    list.addElement(new IconItem("wi0054"));
    list.addElement(new IconItem("wi0062"));
    list.addElement(new IconItem("wi0063"));
    list.addElement(new IconItem("wi0064"));
    list.addElement(new IconItem("wi0096"));
    list.addElement(new IconItem("wi0111"));
    list.addElement(new IconItem("wi0122"));
    list.addElement(new IconItem("wi0124"));
    return list;
  }

  private static <E extends IconItem> TableModel createIconTableModel(ListModel<E> list) {
    Object[][] data = {
        {list.getElementAt(0), list.getElementAt(1), list.getElementAt(2)},
        {list.getElementAt(3), list.getElementAt(4), list.getElementAt(5)},
        {list.getElementAt(6), list.getElementAt(7), list.getElementAt(8)},
    };
    String[] columnNames = {"A", "B", "C"};
    return new DefaultTableModel(data, columnNames) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
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
    frame.setResizable(false);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class IconItem {
  private final Icon large;
  private final Icon small;

  protected IconItem(String name) {
    large = createIcon("example/" + name + "-48.png");
    small = createIcon("example/" + name + "-24.png");
  }

  public Icon getLargeIcon() {
    return large;
  }

  public Icon getSmallIcon() {
    return small;
  }

  private static Icon createIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      Icon icn;
      try (InputStream s = url.openStream()) {
        icn = new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        icn = UIManager.getIcon("html.missingImage");
      }
      return icn;
    }).orElseGet(() -> UIManager.getIcon("html.missingImage"));
  }
}

class IconTableCellRenderer extends DefaultTableCellRenderer {
  @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
    Component c = super.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
    if (c instanceof JLabel && value instanceof IconItem) {
      JLabel l = (JLabel) c;
      l.setIcon(((IconItem) value).getLargeIcon());
      l.setText(null);
      l.setHorizontalAlignment(CENTER);
    }
    return c;
  }
}

class IconTable extends JTable {
  private static final int CELL_SIZE = 50;
  private final JList<IconItem> editor;
  private final JComponent glassPane;
  private transient MouseListener handler;

  protected IconTable(TableModel model, JList<IconItem> editor) {
    super(model);
    this.editor = editor;
    this.glassPane = new EditorGlassPane(editor);
    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    editor.getInputMap(WHEN_FOCUSED).put(key, "cancel-editing");
    editor.getActionMap().put("cancel-editing", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        cancelEditing();
      }
    });
    editor.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        int idx = editor.locationToIndex(e.getPoint());
        int row = getSelectedRow();
        int col = getSelectedColumn();
        if (idx >= 0 && row >= 0 && col >= 0) {
          setValueAt(editor.getModel().getElementAt(idx), row, col);
        }
        cancelEditing();
      }
    });

    glassPane.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        // Clicks on the editor JList are consumed by the JList itself,
        // so any click reaching the glass pane is outside the editor.
        cancelEditing();
      }
    });
    glassPane.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
      @Override public boolean accept(Component c) {
        return Objects.equals(c, editor);
      }
    });
    glassPane.add(editor);
    glassPane.setVisible(false);
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    super.updateUI();
    setRowHeight(CELL_SIZE);
    JTableHeader tableHeader = getTableHeader();
    tableHeader.setResizingAllowed(false);
    tableHeader.setReorderingAllowed(false);
    TableColumnModel m = getColumnModel();
    for (int i = 0; i < m.getColumnCount(); i++) {
      TableColumn col = m.getColumn(i);
      col.setMinWidth(CELL_SIZE);
      col.setMaxWidth(CELL_SIZE);
    }
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setDefaultRenderer(Object.class, new IconTableCellRenderer());
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    handler = new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
          startEditing();
        }
      }
    };
    addMouseListener(handler);
  }

  public void startEditing() {
    int sr = getSelectedRow();
    int sc = getSelectedColumn();
    if (sr < 0 || sc < 0) {
      return;
    }
    getRootPane().setGlassPane(glassPane);
    Dimension d = editor.getPreferredSize();
    editor.setSize(d);

    Rectangle r = getCellRect(sr, sc, true);
    Point p = SwingUtilities.convertPoint(this, r.getLocation(), glassPane);
    p.translate((r.width - d.width) / 2, (r.height - d.height) / 2);
    editor.setLocation(p);

    glassPane.setVisible(true);
    editor.setSelectedValue(getValueAt(sr, sc), true);
    editor.requestFocusInWindow();
  }

  protected void cancelEditing() {
    glassPane.setVisible(false);
  }
}

class EditorGlassPane extends JComponent {
  protected static final int OFFSET = 4;
  private final JComponent editor;

  protected EditorGlassPane(JComponent editor) {
    super();
    this.editor = editor;
  }

  @Override public void setVisible(boolean flag) {
    super.setVisible(flag);
    setFocusTraversalPolicyProvider(flag);
    setFocusCycleRoot(flag);
  }

  @Override protected void paintComponent(Graphics g) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setPaint(new Color(0x64_FF_FF_FF, true));
    g2.fillRect(0, 0, getWidth(), getHeight());
    // Draw the translucent drop shadow directly instead of allocating
    // a full-size off-screen BufferedImage on every repaint.
    // SRC_OVER compositing gives the same result as the intermediate buffer.
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .15f));
    g2.setPaint(Color.BLACK);
    Rectangle r = editor.getBounds();
    for (int i = 0; i < OFFSET; i++) {
      g2.fillRoundRect(r.x - i, r.y + OFFSET, r.width + i + i, r.height - OFFSET + i, 5, 5);
    }
    g2.dispose();
  }
}

class EditorFromList<E extends IconItem> extends JList<E> {
  private int rolloverIndex = -1;
  private final Dimension fixedSize;
  private transient RolloverListener handler;

  protected EditorFromList(ListModel<E> model, Dimension fixedSize) {
    super(model);
    this.fixedSize = fixedSize;
  }

  @Override public Dimension getPreferredSize() {
    return fixedSize;
  }

  @Override public void updateUI() {
    removeMouseMotionListener(handler);
    removeMouseListener(handler);
    super.updateUI();
    handler = new RolloverListener();
    addMouseMotionListener(handler);
    addMouseListener(handler);
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setLayoutOrientation(HORIZONTAL_WRAP);
    setVisibleRowCount(0);
    JLabel renderer = new JLabel();
    renderer.setOpaque(true);
    renderer.setHorizontalAlignment(SwingConstants.CENTER);
    Color selectedColor = new Color(0xC8_C8_FF);
    setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      renderer.setBackground(getBackgroundColor(index, isSelected, selectedColor));
      renderer.setIcon(value.getSmallIcon());
      return renderer;
    });
  }

  private Color getBackgroundColor(int index, boolean isSelected, Color selectedColor) {
    Color bgc = getBackground();
    if (index == rolloverIndex) {
      bgc = getSelectionBackground();
    } else if (isSelected) {
      bgc = selectedColor;
    }
    return bgc;
  }

  private final class RolloverListener extends MouseAdapter {
    @Override public void mouseExited(MouseEvent e) {
      rolloverIndex = -1;
      e.getComponent().repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {
      int idx = locationToIndex(e.getPoint());
      if (idx != rolloverIndex) {
        rolloverIndex = idx;
        e.getComponent().repaint();
      }
    }
  }
}
