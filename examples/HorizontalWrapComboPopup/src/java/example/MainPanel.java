// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
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
    p.add(new IconComboBox(makeModel()), c);
    c.gridx = 0;
    c.gridy = 1;
    c.weightx = 0.0;
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.WEST;
    p.add(new JLabel("PopupMenuListener:"), c);
    c.gridx = 1;
    c.weightx = 1.0;
    p.add(new IconWrapComboBox(makeModel()), c);
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<Icon> makeModel() {
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
  protected static final Icon PROTO = new ColorIcon(Color.DARK_GRAY);
  protected static final int ROW_CNT = 3;

  protected IconComboBox(ComboBoxModel<Icon> model) {
    super(model);
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    int w = PROTO.getIconWidth();
    int h = PROTO.getIconHeight();
    int totalCount = getItemCount();
    int columnCount = totalCount / ROW_CNT + (totalCount % ROW_CNT == 0 ? 0 : 1);
    return new Dimension(w * columnCount + i.left + i.right, h + i.top + i.bottom);
  }

  @Override public void updateUI() {
    super.updateUI();
    setMaximumRowCount(ROW_CNT);
    setPrototypeDisplayValue(PROTO);
    Accessible o = getAccessibleContext().getAccessibleChild(0);
    if (o instanceof ComboPopup) {
      JList<?> list = ((ComboPopup) o).getList();
      list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
      list.setVisibleRowCount(ROW_CNT);
      list.setFixedCellWidth(PROTO.getIconWidth());
      list.setFixedCellHeight(PROTO.getIconHeight());
    }
  }
}

class IconWrapComboBox extends IconComboBox {
  private PopupMenuListener listener;

  protected IconWrapComboBox(ComboBoxModel<Icon> model) {
    super(model);
  }

  @Override public Dimension getPreferredSize() {
    Insets i = getInsets();
    int w = PROTO.getIconWidth();
    int h = PROTO.getIconHeight();
    int buttonWidth = 20; // ???
    return new Dimension(buttonWidth + w + i.left + i.right, h + i.top + i.bottom);
  }

  @Override public void updateUI() {
    setRenderer(null);
    removePopupMenuListener(listener);
    super.updateUI();
    setMaximumRowCount(ROW_CNT);
    setPrototypeDisplayValue(PROTO);
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
    Accessible o = getAccessibleContext().getAccessibleChild(0);
    if (o instanceof ComboPopup) {
      JList<?> list = ((ComboPopup) o).getList();
      list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
      list.setVisibleRowCount(ROW_CNT);
      list.setFixedCellWidth(PROTO.getIconWidth());
      list.setFixedCellHeight(PROTO.getIconHeight());
    }
    listener = new PopupMenuHandler(ROW_CNT, PROTO);
    addPopupMenuListener(listener);
  }
}

class PopupMenuHandler implements PopupMenuListener {
  private final AtomicBoolean adjusting = new AtomicBoolean();
  private final int rowCnt;
  private final Icon proto;

  protected PopupMenuHandler(int rowCnt, Icon proto) {
    this.rowCnt = rowCnt;
    this.proto = proto;
  }

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> combo = (JComboBox<?>) e.getSource();
    Insets i = combo.getInsets();
    int totalCount = combo.getItemCount();
    int columnCount = totalCount / rowCnt + (totalCount % rowCnt == 0 ? 0 : 1);
    int popupWidth = proto.getIconWidth() * columnCount + i.left + i.right;
    Dimension size = combo.getSize();
    if (size.width < popupWidth && !adjusting.get()) {
      adjusting.set(true);
      combo.setSize(popupWidth, size.height);
      combo.showPopup();
      // // Java 8
      // combo.setSize(size);
      // adjusting.set(false);
      // Java 21
      EventQueue.invokeLater(() -> {
        combo.setSize(size);
        adjusting.set(false);
      });
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
