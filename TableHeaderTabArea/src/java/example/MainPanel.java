// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    TableHeaderTabbedPane tabs = new TableHeaderTabbedPane();
    tabs.setBorder(BorderFactory.createTitledBorder("CardLayout+JTableHeader"));
    tabs.addTab("dddd", new JScrollPane(new JTree()));
    tabs.addTab("eeee", new JLabel("kkk"));
    tabs.addTab("ffff", new JLabel("llllllllll"));
    tabs.addTab("gggg", new JButton("mmmmmm"));
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
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

class TableHeaderTabbedPane extends JPanel {
  protected final CardLayout cardLayout = new CardLayout();
  protected final JPanel tabPanel = new JPanel(new GridLayout(1, 0, 0, 0));
  protected final JPanel contentsPanel = new JPanel(cardLayout);
  protected final TableColumnModel model;
  private final JTableHeader header;
  protected Object selectedColumn;
  protected int rolloverColumn = -1;

  protected TableHeaderTabbedPane() {
    super(new BorderLayout());

    int left = 1;
    int right = 3;
    tabPanel.setBorder(BorderFactory.createEmptyBorder(1, left, 0, right));
    contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, left, 2, right));

    JTable table = new JTable(new DefaultTableModel(null, new String[] {}));
    // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    header = table.getTableHeader();
    model = (TableColumnModel) header.getColumnModel();

    MouseAdapter handler = new TableHeaderMouseInputHandler();
    header.addMouseListener(handler);
    header.addMouseMotionListener(handler);

    TabButton l = new TabButton();
    header.setDefaultRenderer(new TableCellRenderer() {
      @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        l.setText(Objects.toString(value, ""));
        l.setSelected(Objects.equals(value, selectedColumn) || Objects.equals(column, rolloverColumn));
        return l;
      }
    });

    JScrollPane sp = new JScrollPane();
    JViewport vp = new JViewport() {
      @Override public Dimension getPreferredSize() {
        return new Dimension();
      }
    };
    vp.setView(table);
    sp.setViewport(vp);

    // JPanel wrapPanel = new JPanel(new BorderLayout());
    // wrapPanel.add(sp);
    // add(wrapPanel, BorderLayout.NORTH);
    add(sp, BorderLayout.NORTH);
    add(contentsPanel);
  }

  public void addTab(String title, Component comp) {
    contentsPanel.add(comp, title);
    TableColumn tc = new TableColumn(model.getColumnCount(), 75, header.getDefaultRenderer(), null);
    tc.setHeaderValue(title);
    model.addColumn(tc);
    if (Objects.isNull(selectedColumn)) {
      cardLayout.show(contentsPanel, title);
      selectedColumn = title;
    }
  }

  private class TableHeaderMouseInputHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      int idx = ((JTableHeader) e.getComponent()).columnAtPoint(e.getPoint());
      if (idx < 0) {
        return;
      }
      Object title = model.getColumn(idx).getHeaderValue();
      cardLayout.show(contentsPanel, Objects.toString(title));
      selectedColumn = title;
    }

    @Override public void mouseEntered(MouseEvent e) {
      updateRolloverColumn(e);
    }

    @Override public void mouseMoved(MouseEvent e) {
      updateRolloverColumn(e);
    }

    @Override public void mouseDragged(MouseEvent e) {
      rolloverColumn = -1;
      updateRolloverColumn(e);
    }

    @Override public void mouseExited(MouseEvent e) {
      // int oldRolloverColumn = rolloverColumn;
      rolloverColumn = -1;
    }

    // @see BasicTableHeaderUI.MouseInputHandler
    private void updateRolloverColumn(MouseEvent e) {
      JTableHeader h = (JTableHeader) e.getComponent();
      if (Objects.isNull(h.getDraggedColumn()) && h.contains(e.getPoint())) {
        int col = h.columnAtPoint(e.getPoint());
        if (col != rolloverColumn) {
          // int oldRolloverColumn = rolloverColumn;
          rolloverColumn = col;
          // rolloverColumnUpdated(oldRolloverColumn, rolloverColumn);
        }
      }
    }
  }
}

class TabButton extends JRadioButton {
  private static final String UI_CLASS_ID = "TabViewButtonUI";
  private Color textColor; // = Color.WHITE;
  private Color pressedTextColor; // = Color.WHITE.darker();
  private Color rolloverTextColor; // = Color.WHITE;
  private Color rolloverSelectedTextColor; // = Color.WHITE;
  private Color selectedTextColor; // = Color.WHITE;

  @Override public void updateUI() {
    if (Objects.nonNull(UIManager.get(getUIClassID()))) {
      setUI((TabViewButtonUI) UIManager.getUI(this));
    } else {
      setUI(new BasicTabViewButtonUI());
    }
  }

  @Override public String getUIClassID() {
    return UI_CLASS_ID;
  }
  // @Override public void setUI(TabViewButtonUI ui) {
  //   super.setUI(ui);
  // }

  @Override public TabViewButtonUI getUI() {
    return (TabViewButtonUI) ui;
  }

  protected TabButton() {
    super(null, null);
  }

  protected TabButton(Icon icon) {
    super(null, icon);
  }

  protected TabButton(String text) {
    super(text, null);
  }

  protected TabButton(Action a) {
    super(a);
  }

  protected TabButton(String text, Icon icon) {
    super(text, icon);
  }

  @Override protected void fireStateChanged() {
    ButtonModel model = getModel();
    if (model.isEnabled()) {
      if (model.isPressed() && model.isArmed()) {
        setForeground(getPressedTextColor());
      } else if (model.isSelected()) {
        setForeground(getSelectedTextColor());
      } else if (isRolloverEnabled() && model.isRollover()) {
        setForeground(getRolloverTextColor());
      } else {
        setForeground(getTextColor());
      }
    } else {
      setForeground(Color.GRAY);
    }
    super.fireStateChanged();
  }

  public Color getTextColor() {
    return textColor;
  }

  public Color getPressedTextColor() {
    return pressedTextColor;
  }

  public Color getRolloverTextColor() {
    return rolloverTextColor;
  }

  public Color getRolloverSelectedTextColor() {
    return rolloverSelectedTextColor;
  }

  public Color getSelectedTextColor() {
    return selectedTextColor;
  }

  public void setTextColor(Color color) {
    textColor = color;
  }

  public void setPressedTextColor(Color color) {
    pressedTextColor = color;
  }

  public void setRolloverTextColor(Color color) {
    rolloverTextColor = color;
  }

  public void setRolloverSelectedTextColor(Color color) {
    rolloverSelectedTextColor = color;
  }

  public void setSelectedTextColor(Color color) {
    selectedTextColor = color;
  }
}
