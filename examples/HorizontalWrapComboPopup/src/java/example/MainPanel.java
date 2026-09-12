// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.gridheight = 1;
    c.gridwidth = 1;
    c.gridx = 0;
    c.gridy = 0;
    c.weightx = 0.0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.WEST;
    JPanel p = new JPanel(new GridBagLayout());
    p.add(new JLabel("PreferredSize:"), c);
    c.gridx = 1;
    c.weightx = 1.0;
    p.add(new IconComboBox(createModel()), c);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.WEST;
    p.add(new JLabel("PopupMenuListener:"), c);
    c.gridx = 1;
    c.weightx = 1.0;
    p.add(new IconWrapComboBox(createModel()), c);
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<Icon> createModel() {
    DefaultComboBoxModel<Icon> model = new DefaultComboBoxModel<>();
    model.addElement(new ColorIcon(Color.RED));
    model.addElement(new ColorIcon(Color.GREEN));
    model.addElement(new ColorIcon(Color.BLUE));
    model.addElement(new ColorIcon(Color.ORANGE));
    model.addElement(new ColorIcon(Color.CYAN));
    model.addElement(new ColorIcon(Color.PINK));
    model.addElement(new ColorIcon(Color.YELLOW));
    model.addElement(new ColorIcon(Color.MAGENTA));
    model.addElement(new ColorIcon(Color.GRAY));
    return model;
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

class IconComboBox extends JComboBox<Icon> {
  protected static final Icon PROTOTYPE = new ColorIcon(Color.DARK_GRAY);
  protected static final int ROW_COUNT = 3;

  protected IconComboBox(ComboBoxModel<Icon> model) {
    super(model);
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    int w = PROTOTYPE.getIconWidth() * getColumnCount(getItemCount(), ROW_COUNT);
    int h = PROTOTYPE.getIconHeight();
    return new Dimension(w + i.left + i.right, h + i.top + i.bottom);
  }

  @Override public void updateUI() {
    super.updateUI();
    setMaximumRowCount(ROW_COUNT);
    setPrototypeDisplayValue(PROTOTYPE);
    Accessible o = getAccessibleContext().getAccessibleChild(0);
    if (o instanceof ComboPopup) {
      JList<?> list = ((ComboPopup) o).getList();
      list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
      list.setVisibleRowCount(ROW_COUNT);
      list.setFixedCellWidth(PROTOTYPE.getIconWidth());
      list.setFixedCellHeight(PROTOTYPE.getIconHeight());
    }
  }

  // Number of columns needed to lay out itemCount cells in rowCount rows
  public static int getColumnCount(int itemCount, int rowCount) {
    return (itemCount + rowCount - 1) / rowCount;
  }
}

class IconWrapComboBox extends IconComboBox {
  private transient PopupMenuListener listener;

  protected IconWrapComboBox(ComboBoxModel<Icon> model) {
    super(model);
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    int w = PROTOTYPE.getIconWidth();
    int h = PROTOTYPE.getIconHeight();
    int buttonWidth = 20; // ???
    return new Dimension(buttonWidth + w + i.left + i.right, h + i.top + i.bottom);
  }

  @Override public void updateUI() {
    setRenderer(null);
    removePopupMenuListener(listener);
    super.updateUI();
    ListCellRenderer<? super Icon> r = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = r.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel) {
        JLabel l = (JLabel) c;
        l.setIcon(value);
        l.setBorder(BorderFactory.createEmptyBorder());
      }
      return c;
    });
    listener = new WidePopupMenuListener(ROW_COUNT, PROTOTYPE);
    addPopupMenuListener(listener);
  }
}

class WidePopupMenuListener implements PopupMenuListener {
  private final int rowCount;
  private final Icon prototype;

  protected WidePopupMenuListener(int rowCount, Icon prototype) {
    this.rowCount = rowCount;
    this.prototype = prototype;
  }

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> combo = (JComboBox<?>) e.getSource();
    Insets i = combo.getInsets();
    int columnCount = IconComboBox.getColumnCount(combo.getItemCount(), rowCount);
    int popupWidth = prototype.getIconWidth() * columnCount + i.left + i.right;
    Dimension size = combo.getSize();
    if (size.width < popupWidth) {
      // Temporarily widen the combo box so that BasicComboPopup#getPopupLocation()
      // sizes the popup from the widened bounds. The nested showPopup() fires this
      // listener again, but the width check above prevents infinite recursion.
      combo.setSize(popupWidth, size.height);
      combo.showPopup();
      // // Java 8
      // combo.setSize(size);
      // Java 21: the outer BasicComboPopup#show() still calls getPopupLocation()
      // after this listener returns, so restoring the size synchronously would
      // shrink the already visible popup back to the combo box width.
      EventQueue.invokeLater(() -> combo.setSize(size));
    }
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
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
    g2.fillRect(1, 1, getIconWidth() - 2, getIconHeight() - 2);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}
