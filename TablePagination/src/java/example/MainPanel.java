// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.plaf.basic.BasicRadioButtonUI;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.View;

public final class MainPanel extends JPanel {
  private static final int LR_PAGE_SIZE = 5;
  private final Box box = Box.createHorizontalBox();
  private final String[] columnNames = {"Year", "String", "Comment"};
  private final DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
    @Override public Class<?> getColumnClass(int column) {
      return column == 0 ? Integer.class : Object.class;
    }
  };
  private final transient TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);

  private MainPanel() {
    super(new BorderLayout());
    JTable table = new JTable(model);
    table.setFillsViewportHeight(true);
    table.setIntercellSpacing(new Dimension());
    table.setShowGrid(false);
    table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
    table.setRowSorter(sorter);

    IntStream.rangeClosed(1, 2016)
        .mapToObj(i -> new Object[] {i, "Test: " + i, i % 2 == 0 ? "" : "comment..."})
        .forEach(model::addRow);

    initLinkBox(100, 1);
    box.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(table));
    setPreferredSize(new Dimension(320, 240));
  }

  private void initLinkBox(int itemsPerPage, int currentPageIndex) {
    // assert currentPageIndex > 0;
    sorter.setRowFilter(new RowFilter<TableModel, Integer>() {
      @Override public boolean include(Entry<? extends TableModel, ? extends Integer> entry) {
        int ti = currentPageIndex - 1;
        int ei = entry.getIdentifier();
        return ti * itemsPerPage <= ei && ei < ti * itemsPerPage + itemsPerPage;
      }
    });

    int startPageIndex = currentPageIndex - LR_PAGE_SIZE;
    if (startPageIndex <= 0) {
      startPageIndex = 1;
    }

    // #if 0 // BUG
    // int maxPageIndex = (model.getRowCount() / itemsPerPage) + 1;
    // #else
    /* "maxPageIndex" gives one blank page if the module of the division is not zero.
     *   pointed out by erServi
     * e.g. rowCount=100, maxPageIndex=100
     */
    int rowCount = model.getRowCount();
    int v = rowCount % itemsPerPage == 0 ? 0 : 1;
    int maxPageIndex = rowCount / itemsPerPage + v;
    // #endif
    int endPageIndex = currentPageIndex + LR_PAGE_SIZE - 1;
    if (endPageIndex > maxPageIndex) {
      endPageIndex = maxPageIndex;
    }

    box.removeAll();
    if (startPageIndex >= endPageIndex) {
      // if I only have one page, Y don't want to see pagination buttons
      // suggested by erServi
      return;
    }

    ButtonGroup bg = new ButtonGroup();
    boolean flag1 = currentPageIndex > 1;
    Arrays.asList(
        makePrevNextButton(itemsPerPage, 1, "|<", flag1),
        makePrevNextButton(itemsPerPage, currentPageIndex - 1, "<", flag1)
    ).forEach(b -> {
      box.add(b);
      bg.add(b);
    });

    box.add(Box.createHorizontalGlue());
    for (int i = startPageIndex; i <= endPageIndex; i++) {
      JRadioButton c = makeRadioButton(itemsPerPage, currentPageIndex, i);
      box.add(c);
      bg.add(c);
    }
    box.add(Box.createHorizontalGlue());

    boolean flag2 = currentPageIndex < maxPageIndex;
    Arrays.asList(
        makePrevNextButton(itemsPerPage, currentPageIndex + 1, ">", flag2),
        makePrevNextButton(itemsPerPage, maxPageIndex, ">|", flag2)
    ).forEach(b -> {
      box.add(b);
      bg.add(b);
    });
    box.revalidate();
    box.repaint();
  }

  private JRadioButton makeRadioButton(int itemsPerPage, int current, int target) {
    JRadioButton radio = new JRadioButton(Objects.toString(target)) {
      @Override protected void fireStateChanged() {
        ButtonModel bm = getModel();
        if (bm.isEnabled()) {
          if (bm.isPressed() && bm.isArmed()) {
            setForeground(Color.GREEN);
          } else if (bm.isSelected()) {
            setForeground(Color.RED);
          }
          // } else if (isRolloverEnabled() && bm.isRollover()) {
          //   setForeground(Color.BLUE);
          // }
        } else {
          setForeground(Color.GRAY);
        }
        super.fireStateChanged();
      }

      @Override public void updateUI() {
        super.updateUI();
        setUI(new LinkViewRadioButtonUI());
        setForeground(Color.BLUE);
        fireStateChanged();
      }
    };
    if (target == current) {
      radio.setSelected(true);
    }
    radio.addActionListener(e -> initLinkBox(itemsPerPage, target));
    return radio;
  }

  private JRadioButton makePrevNextButton(int itemsPerPage, int tgt, String txt, boolean flg) {
    JRadioButton radio = new JRadioButton(txt) {
      @Override public void updateUI() {
        super.updateUI();
        setUI(new LinkViewRadioButtonUI());
        setForeground(Color.BLUE);
      }
    };
    radio.setEnabled(flg);
    radio.addActionListener(e -> initLinkBox(itemsPerPage, tgt));
    return radio;
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

class LinkViewRadioButtonUI extends BasicRadioButtonUI {
  // private static final LinkViewRadioButtonUI radioButtonUI = new LinkViewRadioButtonUI();
  // private boolean defaults_initialized = false;
  private static final Rectangle VIEW_RECT = new Rectangle();
  private static final Rectangle ICON_RECT = new Rectangle();
  private static final Rectangle TEXT_RECT = new Rectangle();

  // public static ComponentUI createUI(JComponent b) {
  //   return radioButtonUI;
  // }

  // @Override protected void installDefaults(AbstractButton b) {
  //   super.installDefaults(b);
  //   if (!defaults_initialized) {
  //     icon = null; // UIManager.getIcon(getPropertyPrefix() + "icon");
  //     defaults_initialized = true;
  //   }
  // }

  // @Override protected void uninstallDefaults(AbstractButton b) {
  //   super.uninstallDefaults(b);
  //   defaults_initialized = false;
  // }

  @Override public Icon getDefaultIcon() {
    return null;
  }

  // [UnsynchronizedOverridesSynchronized]
  // Unsynchronized method paint overrides synchronized method in BasicRadioButtonUI
  @SuppressWarnings("PMD.AvoidSynchronizedAtMethodLevel")
  @Override public synchronized void paint(Graphics g, JComponent c) {
    if (!(c instanceof AbstractButton)) {
      return;
    }
    AbstractButton b = (AbstractButton) c;
    Font f = b.getFont();
    g.setFont(f);

    if (c.isOpaque()) {
      g.setColor(c.getBackground());
      g.fillRect(0, 0, c.getWidth(), c.getHeight());
    }

    SwingUtilities.calculateInnerArea(c, VIEW_RECT);
    ICON_RECT.setBounds(0, 0, 0, 0);
    TEXT_RECT.setBounds(0, 0, 0, 0);

    String text = SwingUtilities.layoutCompoundLabel(
        c, c.getFontMetrics(f),
        b.getText(),
        null, // altIcon != null ? altIcon : getDefaultIcon(),
        b.getVerticalAlignment(),
        b.getHorizontalAlignment(),
        b.getVerticalTextPosition(),
        b.getHorizontalTextPosition(),
        VIEW_RECT,
        ICON_RECT,
        TEXT_RECT,
        0); // b.getText() == null ? 0 : b.getIconTextGap());

    // // Changing Component State During Painting (an infinite repaint loop)
    // // pointed out by Peter
    // // note: http://today.java.net/pub/a/today/2007/08/30/debugging-swing.html#changing-component-state-during-the-painting
    // // b.setForeground(Color.BLUE);
    // if (!model.isEnabled()) {
    //   // b.setForeground(Color.GRAY);
    // } else if (model.isPressed() && model.isArmed() || model.isSelected()) {
    //   // b.setForeground(Color.BLACK);
    // } else if (b.isRolloverEnabled() && model.isRollover()) {

    ButtonModel m = b.getModel();
    g.setColor(c.getForeground());
    boolean isRollover = b.isRolloverEnabled() && m.isRollover();
    if (!m.isSelected() && !m.isPressed() && !m.isArmed() && isRollover) {
      int vy = VIEW_RECT.y + VIEW_RECT.height;
      g.drawLine(VIEW_RECT.x, vy, VIEW_RECT.x + VIEW_RECT.width, vy);
    }
    Object o = c.getClientProperty(BasicHTML.propertyKey);
    if (o instanceof View) {
      ((View) o).paint(g, TEXT_RECT);
    } else {
      paintText(g, c, TEXT_RECT, text);
    }
  }
}
