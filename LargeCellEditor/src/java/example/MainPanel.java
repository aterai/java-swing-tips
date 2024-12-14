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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;
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
    ListModel<IconItem> list = makeIconList();
    int ins = 2;
    Icon icon = list.getElementAt(0).getSmallIcon();
    int iw = ins + icon.getIconWidth();
    int ih = ins + icon.getIconHeight();
    Dimension d = new Dimension(iw * 3 + ins, ih * 3 + ins);
    JList<IconItem> editor = new EditorFromList<>(list, d);
    editor.setFixedCellWidth(iw);
    editor.setFixedCellHeight(ih);
    JTable table = new IconTable(makeIconTableModel(list), editor);
    JPanel p = new JPanel(new GridBagLayout());
    p.add(table, new GridBagConstraints());
    p.setBackground(Color.WHITE);
    add(p);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<IconItem> makeIconList() {
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

  private static <E extends IconItem> TableModel makeIconTableModel(ListModel<E> list) {
    Object[][] data = {
        {list.getElementAt(0), list.getElementAt(1), list.getElementAt(2)},
        {list.getElementAt(3), list.getElementAt(4), list.getElementAt(5)},
        {list.getElementAt(6), list.getElementAt(7), list.getElementAt(8)}
    };
    return new DefaultTableModel(data, null) {
      @Override public boolean isCellEditable(int row, int column) {
        return false;
      }

      @Override public int getColumnCount() {
        return 3;
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
      ex.printStackTrace();
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

  protected IconItem(String str) {
    large = makeIcon("example/" + str + "-48.png");
    small = makeIcon("example/" + str + "-24.png");
  }

  public Icon getLargeIcon() {
    return large;
  }

  public Icon getSmallIcon() {
    return small;
  }

  private static Icon makeIcon(String path) {
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return Optional.ofNullable(cl.getResource(path)).map(url -> {
      try (InputStream s = url.openStream()) {
        return new ImageIcon(ImageIO.read(s));
      } catch (IOException ex) {
        return UIManager.getIcon("html.missingImage");
      }
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
  protected static final int CELL_SIZE = 50;
  protected static final int OFFSET = 4;
  protected final JList<IconItem> editor;
  protected final JComponent glassPane = new JComponent() {
    @Override public void setVisible(boolean flag) {
      super.setVisible(flag);
      setFocusTraversalPolicyProvider(flag);
      setFocusCycleRoot(flag);
    }

    @Override protected void paintComponent(Graphics g) {
      g.setColor(new Color(0x64_FF_FF_FF, true));
      int w = getWidth();
      int h = getHeight();
      g.fillRect(0, 0, w, h);
      BufferedImage buffer = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2 = buffer.createGraphics();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .15f));
      g2.setPaint(Color.BLACK);
      Rectangle r = editor.getBounds();
      for (int i = 0; i < OFFSET; i++) {
        g2.fillRoundRect(r.x - i, r.y + OFFSET, r.width + i + i, r.height - OFFSET + i, 5, 5);
      }
      g2.dispose();
      g.drawImage(buffer, 0, 0, this);
    }
  };
  private transient MouseListener handler;

  protected IconTable(TableModel model, JList<IconItem> editor) {
    super(model);
    this.editor = editor;
    KeyStroke key = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
    editor.getInputMap(WHEN_FOCUSED).put(key, "cancel-editing");
    editor.getActionMap().put("cancel-editing", new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        cancelEditing();
      }
    });
    // editor.addKeyListener(new KeyAdapter() {
    //   @Override public void keyPressed(KeyEvent e) {
    //     if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
    //       cancelEditing();
    //     }
    //   }
    // });
    editor.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        Point p = e.getPoint();
        IconItem item = editor.getModel().getElementAt(editor.locationToIndex(p));
        setValueAt(item, getSelectedRow(), getSelectedColumn());
        cancelEditing();
      }
    });

    glassPane.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        // Point pt = e.getPoint();
        // if (!editor.getBounds().contains(pt)) {
        //   cancelEditing();
        // }
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
    getRootPane().setGlassPane(glassPane);
    Dimension d = editor.getPreferredSize();
    editor.setSize(d);

    int sr = getSelectedRow();
    int sc = getSelectedColumn();
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

class EditorFromList<E extends IconItem> extends JList<E> {
  protected int rollOverRowIndex = -1;
  private final Dimension dim;
  private transient RollOverListener handler;

  protected EditorFromList(ListModel<E> model, Dimension dim) {
    super(model);
    this.dim = dim;
  }

  @Override public Dimension getPreferredSize() {
    return dim;
  }

  @Override public void updateUI() {
    removeMouseMotionListener(handler);
    removeMouseListener(handler);
    super.updateUI();
    handler = new RollOverListener();
    addMouseMotionListener(handler);
    addMouseListener(handler);
    setBorder(BorderFactory.createLineBorder(Color.BLACK));
    setLayoutOrientation(HORIZONTAL_WRAP);
    setVisibleRowCount(0);
    JLabel renderer = new JLabel();
    Color selectedColor = new Color(0xC8_C8_FF);
    setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      renderer.setOpaque(true);
      renderer.setHorizontalAlignment(SwingConstants.CENTER);
      if (index == rollOverRowIndex) {
        renderer.setBackground(getSelectionBackground());
      } else if (isSelected) {
        renderer.setBackground(selectedColor);
      } else {
        renderer.setBackground(getBackground());
      }
      renderer.setIcon(value.getSmallIcon());
      return renderer;
    });
  }

  private final class RollOverListener extends MouseAdapter {
    @Override public void mouseExited(MouseEvent e) {
      rollOverRowIndex = -1;
      e.getComponent().repaint();
    }

    @Override public void mouseMoved(MouseEvent e) {
      int row = locationToIndex(e.getPoint());
      if (row != rollOverRowIndex) {
        rollOverRowIndex = row;
        e.getComponent().repaint();
      }
    }
  }
}
