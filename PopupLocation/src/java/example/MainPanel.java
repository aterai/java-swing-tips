// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.BadLocationException;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout(5, 5));
    JPopupMenu popup = new JPopupMenu();
    popup.add("menu 1");
    popup.add("menu 2");
    popup.add("menu 3");

    JTextField field1 = new JTextField("default JTextField");
    field1.setComponentPopupMenu(popup);

    JTextField field2 = new JTextField("override JTextField#getPopupLocation(MouseEvent)") {
      @Override public Point getPopupLocation(MouseEvent event) {
        Point pt = super.getPopupLocation(event);
        try {
          Rectangle r = modelToView(getCaretPosition());
          // Java 9: Rectangle r = modelToView2D(getCaretPosition()).getBounds();
          if (event == null && r != null) {
            pt.setLocation(r.getLocation());
            pt.translate(0, r.height);
          }
        } catch (BadLocationException ex) {
          UIManager.getLookAndFeel().provideErrorFeedback(this);
        }
        return pt;
      }
    };
    field2.setComponentPopupMenu(popup);

    DefaultListModel<ListItem> model = new DefaultListModel<>();
    model.addElement(new ListItem("red", new ColorIcon(Color.RED)));
    model.addElement(new ListItem("green", new ColorIcon(Color.GREEN)));
    model.addElement(new ListItem("blue", new ColorIcon(Color.BLUE)));
    model.addElement(new ListItem("cyan", new ColorIcon(Color.CYAN)));
    model.addElement(new ListItem("darkGray", new ColorIcon(Color.DARK_GRAY)));
    model.addElement(new ListItem("gray", new ColorIcon(Color.GRAY)));
    model.addElement(new ListItem("lightGray", new ColorIcon(Color.LIGHT_GRAY)));
    model.addElement(new ListItem("magenta", new ColorIcon(Color.MAGENTA)));
    model.addElement(new ListItem("orange", new ColorIcon(Color.ORANGE)));
    model.addElement(new ListItem("pink", new ColorIcon(Color.PINK)));
    model.addElement(new ListItem("yellow", new ColorIcon(Color.YELLOW)));
    model.addElement(new ListItem("black", new ColorIcon(Color.BLACK)));
    model.addElement(new ListItem("white", new ColorIcon(Color.WHITE)));

    JList<ListItem> list = new NewspaperStyleList<>(model);
    list.setComponentPopupMenu(popup);

    JPanel p = new JPanel(new GridLayout(2, 1, 5, 5));
    p.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    p.add(field1);
    p.add(field2);

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
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

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  protected static final Color SELECTED_COLOR = new Color(0xAE_16_64_FF, true);
  private final JLabel icon = new JLabel(null, null, SwingConstants.CENTER);
  private final JLabel label = new JLabel(" ", SwingConstants.CENTER);
  private final JPanel renderer = new JPanel(new BorderLayout()) {
    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (SELECTED_COLOR.equals(getBackground())) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(SELECTED_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    }
  };
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    renderer.setBorder(noFocusBorder);
    renderer.setOpaque(true);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    label.setOpaque(false);
    icon.setOpaque(false);
    renderer.add(icon);
    renderer.add(label, BorderLayout.SOUTH);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    icon.setIcon(value.icon);
    label.setText(value.title);
    renderer.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    if (isSelected) {
      label.setForeground(list.getSelectionForeground());
      renderer.setBackground(SELECTED_COLOR);
    } else {
      label.setForeground(list.getForeground());
      renderer.setBackground(list.getBackground());
    }
    return renderer;
  }
}

class ListItem {
  public final Icon icon;
  public final String title;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setPaint(Color.BLACK);
    g2.drawRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}

class NewspaperStyleList<E extends ListItem> extends JList<E> {
  protected NewspaperStyleList(DefaultListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setSelectionForeground(null);
    setSelectionBackground(null);
    setCellRenderer(null);
    super.updateUI();
    setLayoutOrientation(JList.HORIZONTAL_WRAP);
    getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setVisibleRowCount(0);
    setFixedCellWidth(64);
    setFixedCellHeight(64);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setCellRenderer(new ListItemListCellRenderer<>());
  }

  @Override public Point getPopupLocation(MouseEvent event) {
    if (event == null) {
      int i = getLeadSelectionIndex();
      Rectangle r = getCellBounds(i, i);
      if (r != null) {
        return new Point((int) r.getCenterX(), (int) r.getCenterY());
      }
    }
    return super.getPopupLocation(event);
  }
}
