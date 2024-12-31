// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JPanel p = new JPanel(new GridLayout(2, 1, 25, 5));
    p.add(makeTitledPanel("JComboBox + ComboBoxEditor", makeListEditorComboBox1()));
    p.add(makeTitledPanel("JList + JPopupMenu", makeListEditorComboBox2()));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<ListItem> makeListEditorComboBox1() {
    ListModel<ListItem> model = makeModel();
    JList<ListItem> list = new NewspaperStyleList<>(model);
    int fixedCellWidth = list.getFixedCellWidth();
    int fixedCellHeight = list.getFixedCellHeight();
    Dimension dim = new Dimension(fixedCellWidth * 4, fixedCellHeight);
    JScrollPane scroll = makeScrollPane(list, dim);
    DefaultComboBoxModel<ListItem> cm = new DefaultComboBoxModel<>();
    for (int i = 0; i < model.getSize(); i++) {
      cm.addElement(model.getElementAt(i));
    }
    return new JComboBox<ListItem>(cm) {
      @Override public void updateUI() {
        super.updateUI();
        setMaximumRowCount(4);
        setPrototypeDisplayValue(new ListItem("red", new ColorIcon(Color.RED)));
        EventQueue.invokeLater(() -> {
          ComboPopup popup = (ComboPopup) getAccessibleContext().getAccessibleChild(0);
          JList<?> lst = popup.getList();
          lst.setLayoutOrientation(JList.HORIZONTAL_WRAP);
          lst.setVisibleRowCount(0);
          lst.setFixedCellWidth(fixedCellWidth);
          lst.setFixedCellHeight(fixedCellHeight);
          lst.setOpaque(true);
          lst.setBackground(new Color(0x32_32_32));
          lst.setForeground(Color.WHITE);
        });
        setRenderer(new ListItemListCellRenderer<>());
        setEditable(true);
        setEditor(new ComboBoxEditor() {
          @Override public Component getEditorComponent() {
            return scroll;
          }

          @Override public void setItem(Object anObject) {
            setSelectedIndex(list.getSelectedIndex());
          }

          @Override public Object getItem() {
            return list.getSelectedValue();
          }

          @Override public void selectAll() {
            // System.out.println("selectAll");
          }

          @Override public void addActionListener(ActionListener l) {
            // System.out.println("addActionListener");
          }

          @Override public void removeActionListener(ActionListener l) {
            // System.out.println("removeActionListener");
          }
        });
      }
    };
  }

  private static JPanel makeListEditorComboBox2() {
    ListModel<ListItem> model = makeModel();
    JList<ListItem> list = new NewspaperStyleList<>(model);
    int fcw = list.getFixedCellWidth();
    int fch = list.getFixedCellHeight();
    JScrollPane scroll = makeScrollPane(list, new Dimension(fcw * 3 + 10, fch));
    scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);

    JList<ListItem> dropDownList = new NewspaperStyleList<>(model);
    int ddw = dropDownList.getFixedCellWidth() * 4 + 10;
    JScrollPane dropDownScroll = makeScrollPane(dropDownList, new Dimension(ddw, 0));

    JPopupMenu popup = new JPopupMenu();
    popup.setLayout(new BorderLayout());
    dropDownList.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (popup.isVisible() && e.getClickCount() >= 2) {
          popup.setVisible(false);
        }
      }
    });
    popup.add(dropDownScroll);
    popup.setBorder(BorderFactory.createLineBorder(Color.GRAY));

    JToggleButton dropButton = makeDropDownButton();
    dropButton.addItemListener(e -> {
      AbstractButton b = (AbstractButton) e.getItemSelectable();
      if (e.getStateChange() == ItemEvent.SELECTED) {
        popup.show(b, -scroll.getWidth(), b.getHeight());
      }
    });

    popup.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        dropDownList.setSelectedIndex(list.getSelectedIndex());
        EventQueue.invokeLater(popup::pack);
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        dropButton.setSelected(false);
        list.requestFocusInWindow();
        int i = dropDownList.getSelectedIndex();
        if (i >= 0) {
          list.setSelectedIndex(i);
          list.scrollRectToVisible(list.getCellBounds(i, i));
        }
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        popupMenuWillBecomeInvisible(e);
      }
    });

    JScrollBar verticalScrollBar = scroll.getVerticalScrollBar();
    JPanel verticalBox = new JPanel(new BorderLayout()) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.height = list.getFixedCellHeight();
        return d;
      }
    };
    verticalBox.setOpaque(false);
    verticalBox.add(verticalScrollBar);
    verticalBox.add(dropButton, BorderLayout.SOUTH);

    JPanel panel = new JPanel(new BorderLayout(0, 0));
    panel.add(scroll);
    panel.add(verticalBox, BorderLayout.EAST);
    panel.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    return panel;
  }

  private static JScrollPane makeScrollPane(JList<?> list, Dimension dim) {
    return new JScrollPane(list) {
      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = dim.width;
        d.height = dim.height > 0 ? dim.height : d.height;
        return d;
      }

      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setViewportBorder(BorderFactory.createEmptyBorder());
      }
    };
  }

  private static ListModel<ListItem> makeModel() {
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
    // model.addElement(new ListItem("black", new ColorIcon(Color.BLACK)));
    model.addElement(new ListItem("white", new ColorIcon(Color.WHITE)));
    return model;
  }

  private static JToggleButton makeDropDownButton() {
    return new JToggleButton(new DropDownArrowIcon()) {
      @Override public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setContentAreaFilled(false);
        setFocusPainted(false);
        setFocusable(false);
      }
    };
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class NewspaperStyleList<E extends ListItem> extends JList<E> {
  protected NewspaperStyleList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setSelectionForeground(null); // Nimbus
    setSelectionBackground(null); // Nimbus
    setCellRenderer(null);
    super.updateUI();

    setLayoutOrientation(HORIZONTAL_WRAP);
    setVisibleRowCount(0);
    setFixedCellWidth(62);
    setFixedCellHeight(40);
    setCellRenderer(new ListItemListCellRenderer<>());
    setOpaque(true);
    setBackground(new Color(0x32_32_32));
    setForeground(Color.WHITE);
  }
}

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  protected static final Color SELECTED_COLOR = new Color(0x40_32_64_FF, true);
  private final JLabel label = new JLabel("", null, SwingConstants.CENTER) {
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
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(noFocusBorder);
    label.setOpaque(false);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    renderer.add(label);
    renderer.setOpaque(false);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value.getTitle());
    label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    label.setIcon(value.getIcon());
    if (isSelected) {
      label.setForeground(list.getSelectionForeground());
      label.setBackground(SELECTED_COLOR);
    } else {
      label.setForeground(list.getForeground());
      label.setBackground(list.getBackground());
    }
    return renderer;
  }
}

class ListItem {
  private final Icon icon;
  private final String title;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public Icon getIcon() {
    return icon;
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
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 12;
  }
}

class DropDownArrowIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    if (c instanceof AbstractButton && ((AbstractButton) c).isSelected()) {
      g2.setPaint(Color.LIGHT_GRAY);
    } else {
      g2.setPaint(Color.DARK_GRAY);
    }
    g2.drawLine(2, 3, 6, 3);
    g2.drawLine(3, 4, 5, 4);
    g2.drawLine(4, 5, 4, 5);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 9;
  }

  @Override public int getIconHeight() {
    return 9;
  }
}
