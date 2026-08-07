// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private static final int POPUP_WIDTH = 240;
  private static final int POPUP_HEIGHT = 120;
  private static final Color GRIP_BACKGROUND = new Color(0xE0_E0_E0);
  private static final Color BORDER_COLOR = new Color(0x64_64_64);

  private MainPanel() {
    super(new FlowLayout(FlowLayout.LEADING));
    Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    DefaultListModel<String> fontListModel = new DefaultListModel<>();
    Stream.of(allFonts).map(Font::getFontName).forEach(fontListModel::addElement);
    JList<String> fontList = new JList<>(fontListModel);
    fontList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    JPopupMenu popupMenu = new JPopupMenu();
    popupMenu.setBorder(BorderFactory.createEmptyBorder());
    popupMenu.setPopupSize(POPUP_WIDTH, POPUP_HEIGHT);

    JComboBox<String> fontComboBox = createFontComboBox(allFonts, fontList, popupMenu);
    // Selecting an item in the list updates the combo box selection to match.
    fontList.addListSelectionListener(
        e -> fontComboBox.setSelectedIndex(fontList.getSelectedIndex()));
    fontList.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() - 1 > 0) {
          fontComboBox.setSelectedIndex(fontList.getSelectedIndex());
          popupMenu.setVisible(false);
        }
      }
    });
    fontComboBox.addItemListener(e -> {
      int idx = fontComboBox.getSelectedIndex();
      fontList.setSelectedIndex(idx);
      fontList.scrollRectToVisible(fontList.getCellBounds(idx, idx));
    });

    JScrollPane scrollPane = new JScrollPane(fontList);
    scrollPane.setBorder(BorderFactory.createEmptyBorder());
    scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    popupMenu.add(createResizablePopupContentPanel(scrollPane));

    // JToggleButton button = new JToggleButton("JToggleButton");
    // button.addActionListener(e -> {
    //   AbstractButton btn = (AbstractButton) e.getSource();
    //   boolean flg = btn.getModel().isSelected();
    //   popupMenu.setVisible(flg);
    //   button.setSelected(flg);
    //   Point p = button.getLocation();
    //   p.y += button.getHeight() - 1;
    //   SwingUtilities.convertPointToScreen(p, button.getParent());
    //   popupMenu.setLocation(p);
    //   popupMenu.requestFocusInWindow();
    // });
    // add(button);
    add(fontComboBox);
    setPreferredSize(new Dimension(320, 240));
  }

  private JComboBox<String> createFontComboBox(
      Font[] fonts, JList<String> fontList, JPopupMenu popupMenu) {
    DefaultComboBoxModel<String> fontComboBoxModel = new DefaultComboBoxModel<>();
    Stream.of(fonts).map(Font::getFontName).forEach(fontComboBoxModel::addElement);
    JComboBox<String> fontComboBox = new JComboBox<String>(fontComboBoxModel) {
      private transient PopupMenuListener listener;

      @Override public void updateUI() {
        removePopupMenuListener(listener);
        super.updateUI();
        listener = new ComboBoxPopupMenuHandler(fontList, popupMenu);
        addPopupMenuListener(listener);
      }

      @Override public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.min(d.width, POPUP_WIDTH);
        return d;
      }
    };
    // Show only the current selection;
    // the actual list is rendered inside the custom popup.
    fontComboBox.setMaximumRowCount(1);
    return fontComboBox;
  }

  private static JPanel createResizablePopupContentPanel(JScrollPane scrollPane) {
    JLabel resizeGripLabel = new JLabel("", new ResizeGripIcon(), SwingConstants.CENTER);
    MouseInputListener resizeHandler = new PopupMenuResizeHandler();
    resizeGripLabel.addMouseListener(resizeHandler);
    resizeGripLabel.addMouseMotionListener(resizeHandler);
    resizeGripLabel.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
    resizeGripLabel.setOpaque(true);
    resizeGripLabel.setBackground(GRIP_BACKGROUND);
    resizeGripLabel.setFocusable(false);

    JPanel contentPanel = new JPanel(new BorderLayout());
    contentPanel.add(scrollPane);
    contentPanel.add(resizeGripLabel, BorderLayout.SOUTH);
    // Reserve a fixed width so the popup does not shrink narrower
    // than this while resizing.
    contentPanel.add(Box.createHorizontalStrut(POPUP_WIDTH), BorderLayout.NORTH);
    contentPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
    return contentPanel;
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

// Synchronizes the popup's font list with the combo box and shows the popup below it.
class ComboBoxPopupMenuHandler implements PopupMenuListener {
  private final JList<String> fontList;
  private final JPopupMenu popupMenu;

  protected ComboBoxPopupMenuHandler(JList<String> fontList, JPopupMenu popupMenu) {
    this.fontList = fontList;
    this.popupMenu = popupMenu;
  }

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    Object source = e.getSource();
    if (source instanceof JComboBox<?>) {
      JComboBox<?> comboBox = (JComboBox<?>) source;
      fontList.setSelectedIndex(comboBox.getSelectedIndex());
      EventQueue.invokeLater(() -> popupMenu.show(comboBox, 0, comboBox.getHeight()));
    }
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    // rect.setSize(window.getSize());
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    // rect.setSize(window.getSize());
  }
}

// Resizes the enclosing JPopupMenu (and its underlying heavyweight/lightweight
// popup window) vertically while the grip label is dragged.
class PopupMenuResizeHandler extends MouseInputAdapter {
  private final Rectangle newSize = new Rectangle();
  private final Point dragStartPoint = new Point();
  private final Dimension dragStartSize = new Dimension();

  @Override public void mousePressed(MouseEvent e) {
    Container popup = SwingUtilities.getAncestorOfClass(JPopupMenu.class, e.getComponent());
    newSize.setSize(popup.getSize());
    dragStartSize.setSize(popup.getSize());
    dragStartPoint.setLocation(e.getComponent().getLocationOnScreen());
  }

  @Override public void mouseDragged(MouseEvent e) {
    newSize.height = dragStartSize.height + e.getLocationOnScreen().y - dragStartPoint.y;
    Container c = SwingUtilities.getAncestorOfClass(JPopupMenu.class, e.getComponent());
    if (c instanceof JPopupMenu) {
      JPopupMenu popupMenu = (JPopupMenu) c;
      popupMenu.setPreferredSize(newSize.getSize());
      Window window = SwingUtilities.getWindowAncestor(popupMenu);
      if (window != null && window.getType() == Window.Type.POPUP) {
        // Popup$HeavyWeightWindow
        window.setSize(newSize.width, newSize.height);
      } else {
        // Popup$LightWeightWindow
        popupMenu.pack();
      }
    }
    // Container p = popupMenu.getTopLevelAncestor();
    // if (p instanceof JWindow && ((Window) p).getType() == Window.Type.POPUP) {
    //   p.setSize(rect.width, rect.height);
    // } else {
    //   popupMenu.pack();
    // }
  }
}

// A row of dots used as a visual grip for the resizable bottom edge of the popup.
class ResizeGripIcon implements Icon {
  private static final int ICON_WIDTH = 32;
  private static final int ICON_HEIGHT = 5;
  private static final int DOT_COUNT = 4;
  private static final int DOT_GAP = 4;
  private static final int DOT_SIZE = 2;

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.GRAY);
    int start = getIconWidth() / 2 - (DOT_COUNT - 1) * 2;
    int centerY = getIconHeight() / 2;
    for (int i = 0; i < DOT_COUNT; i++) {
      g2.fillRect(start + DOT_GAP * i, centerY, DOT_SIZE, DOT_SIZE);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return ICON_WIDTH;
  }

  @Override public int getIconHeight() {
    return ICON_HEIGHT;
  }
}
