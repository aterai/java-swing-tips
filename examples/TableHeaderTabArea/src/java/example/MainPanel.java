// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    TableHeaderTabbedPane tabs = new TableHeaderTabbedPane();
    tabs.setBorder(BorderFactory.createTitledBorder("CardLayout+JTableHeader"));
    tabs.addTab("111", new JScrollPane(new JTree()));
    tabs.addTab("222", new JLabel("55555"));
    tabs.addTab("333", new JLabel("66666"));
    tabs.addTab("444", new JButton("77777"));
    add(tabs);
    setPreferredSize(new Dimension(320, 240));
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

class TableHeaderTabbedPane extends JPanel {
  protected transient Object selectedColumn;
  protected int rolloverColumn = -1;
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel contentsPanel = new JPanel(cardLayout);
  private final JTable table = new JTable(0, 0);
  private final JTableHeader header = table.getTableHeader();

  protected TableHeaderTabbedPane() {
    super(new BorderLayout());
    int left = 1;
    int right = 3;

    JPanel tabPanel = new JPanel(new GridLayout(1, 0, 0, 0));
    tabPanel.setBorder(BorderFactory.createEmptyBorder(1, left, 0, right));
    contentsPanel.setBorder(BorderFactory.createEmptyBorder(4, left, 2, right));

    MouseAdapter handler = new TableHeaderMouseInputHandler();
    header.addMouseListener(handler);
    header.addMouseMotionListener(handler);

    TabButton l = new TabButton();
    header.setDefaultRenderer((table1, value, isSelected, hasFocus, row, column) -> {
      l.setText(Objects.toString(value, ""));
      l.setSelected(Objects.equals(value, selectedColumn) || column == rolloverColumn);
      return l;
    });

    JScrollPane scroll = new JScrollPane();
    JViewport viewport = new JViewport() {
      @Override public Dimension getPreferredSize() {
        return new Dimension();
      }
    };
    viewport.setView(table);
    scroll.setViewport(viewport);

    // JPanel wrapPanel = new JPanel(new BorderLayout());
    // wrapPanel.add(scroll);
    // add(wrapPanel, BorderLayout.NORTH);
    add(scroll, BorderLayout.NORTH);
    add(contentsPanel);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public final void add(Component comp, Object constraints) {
    super.add(comp, constraints);
  }

  public void addTab(String title, Component comp) {
    contentsPanel.add(comp, title);
    TableColumnModel m = header.getColumnModel();
    TableColumn tc = new TableColumn(m.getColumnCount(), 75, header.getDefaultRenderer(), null);
    tc.setHeaderValue(title);
    m.addColumn(tc);
    if (Objects.isNull(selectedColumn)) {
      cardLayout.show(contentsPanel, title);
      selectedColumn = title;
    }
  }

  private final class TableHeaderMouseInputHandler extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      JTableHeader h = (JTableHeader) e.getComponent();
      int idx = h.columnAtPoint(e.getPoint());
      if (idx < 0) {
        return;
      }
      TableColumnModel m = h.getColumnModel();
      Object title = m.getColumn(idx).getHeaderValue();
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
  private Color pressedTc; // = Color.WHITE.darker();
  private Color rolloverTc; // = Color.WHITE;
  private Color rolloverSelTc; // = Color.WHITE;
  private Color selectedTc; // = Color.WHITE;

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
    // super.setAction(a);
    // updateUI();
  }

  protected TabButton(String text, Icon icon) {
    super(text, icon);
    // updateUI();
  }

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

  @Override protected void fireStateChanged() {
    ButtonModel model = getModel();
    if (model.isPressed() && model.isArmed()) {
      setForeground(getPressedTextColor());
    } else if (model.isSelected()) {
      setForeground(getSelectedTextColor());
    } else if (isRolloverEnabled() && model.isRollover()) {
      setForeground(getRolloverTextColor());
    } else if (model.isEnabled()) {
      setForeground(getTextColor());
    } else {
      setForeground(Color.GRAY);
    }
    super.fireStateChanged();
  }

  @Override public Dimension getMinimumSize() {
    Dimension d = super.getMinimumSize();
    d.width = 16;
    return d;
  }

  @Override public String toString() {
    return "TabButton";
  }

  public Color getTextColor() {
    return textColor;
  }

  public Color getPressedTextColor() {
    return pressedTc;
  }

  public Color getRolloverTextColor() {
    return rolloverTc;
  }

  public Color getRolloverSelectedTextColor() {
    return rolloverSelTc;
  }

  public Color getSelectedTextColor() {
    return selectedTc;
  }

  public void setTextColor(Color color) {
    textColor = color;
  }

  public void setPressedTextColor(Color color) {
    pressedTc = color;
  }

  public void setRolloverTextColor(Color color) {
    rolloverTc = color;
  }

  public void setRolloverSelectedTextColor(Color color) {
    rolloverSelTc = color;
  }

  public void setSelectedTextColor(Color color) {
    selectedTc = color;
  }
}
