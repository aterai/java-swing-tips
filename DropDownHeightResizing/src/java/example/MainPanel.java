// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.MouseInputListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new FlowLayout(FlowLayout.LEADING));
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    DefaultListModel<String> m1 = new DefaultListModel<>();
    Stream.of(fonts).map(Font::getFontName).forEach(m1::addElement);
    JList<String> list = new JList<>(m1);
    list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    // JWindow window = new JWindow(SwingUtilities.getWindowAncestor(this));
    // window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
    // window.setFocusableWindowState(true);
    // window.setType(Window.Type.POPUP);
    // window.setAlwaysOnTop(true);
    // window.setSize(240, 120);
    JPopupMenu popup = new JPopupMenu();
    popup.setBorder(BorderFactory.createEmptyBorder());
    popup.setPreferredSize(new Dimension(240, 120));
    popup.pack();
    popup.setSize(240, 120);

    JComboBox<String> combo = makeComboBox(fonts, list, popup);
    list.addListSelectionListener(e -> combo.setSelectedIndex(list.getSelectedIndex()));
    list.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() - 1 > 0) {
          combo.setSelectedIndex(list.getSelectedIndex());
          popup.setVisible(false);
        }
      }
    });
    combo.addItemListener(e -> {
      int idx = combo.getSelectedIndex();
      list.setSelectedIndex(idx);
      list.scrollRectToVisible(list.getCellBounds(idx, idx));
    });

    JScrollPane scroll = new JScrollPane(list);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());
    popup.add(makeResizePanel(scroll));

    // JToggleButton button = new JToggleButton("JToggleButton");
    // button.addActionListener(e -> {
    //   AbstractButton btn = (AbstractButton) e.getSource();
    //   boolean flg = btn.getModel().isSelected();
    //   popup.setVisible(flg);
    //   button.setSelected(flg);
    //   Point p = button.getLocation();
    //   p.y += button.getHeight() - 1;
    //   SwingUtilities.convertPointToScreen(p, button.getParent());
    //   popup.setLocation(p);
    //   popup.requestFocusInWindow();
    // });
    EventQueue.invokeLater(() -> {
      Window frame = SwingUtilities.getWindowAncestor(this);
      frame.addMouseListener(new MouseAdapter() {
        @Override public void mousePressed(MouseEvent e) {
          popup.setVisible(false);
          // button.setSelected(false);
        }
      });
      frame.addComponentListener(new ComponentAdapter() {
        @Override public void componentResized(ComponentEvent e) {
          popup.setVisible(false);
          // button.setSelected(false);
        }

        @Override public void componentMoved(ComponentEvent e) {
          componentResized(e);
        }
      });
    });

    // add(button);
    add(combo);
    setPreferredSize(new Dimension(320, 240));
  }

  private JComboBox<String> makeComboBox(Font[] fonts, JList<String> list, JPopupMenu popup) {
    DefaultComboBoxModel<String> m2 = new DefaultComboBoxModel<>();
    Stream.of(fonts).map(Font::getFontName).forEach(m2::addElement);
    JComboBox<String> combo = new JComboBox<String>(m2) {
      private transient PopupMenuListener handler;
      @Override public void updateUI() {
        removePopupMenuListener(handler);
        super.updateUI();
        handler = new PopupMenuListener() {
          @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
            JComboBox<?> c = (JComboBox<?>) e.getSource();
            EventQueue.invokeLater(() -> {
              list.setSelectedIndex(c.getSelectedIndex());
              popup.setPreferredSize(popup.getSize());
              Point p = c.getLocation();
              p.y += c.getHeight();
              // popup.show(c, p.x, p.y);
              // popup.setInvoker(c);
              SwingUtilities.convertPointToScreen(p, c.getParent());
              popup.setLocation(p);
              popup.setVisible(true);
              popup.requestFocusInWindow();
              EventQueue.invokeLater(list::requestFocusInWindow);
            });
          }

          @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
            // rect.setSize(window.getSize());
          }

          @Override public void popupMenuCanceled(PopupMenuEvent e) {
            // rect.setSize(window.getSize());
          }
        };
        addPopupMenuListener(handler);
      }
    };
    combo.setMaximumRowCount(1);
    return combo;
  }

  private static JPanel makeResizePanel(JScrollPane scroll) {
    JLabel bottom = new JLabel("", new DotIcon(), SwingConstants.CENTER);
    MouseInputListener rwl = new ResizeWindowListener();
    bottom.addMouseListener(rwl);
    bottom.addMouseMotionListener(rwl);
    bottom.setCursor(Cursor.getPredefinedCursor(Cursor.S_RESIZE_CURSOR));
    bottom.setOpaque(true);
    bottom.setBackground(new Color(0xE0_E0_E0));
    bottom.setFocusable(false);

    JPanel resizePanel = new JPanel(new BorderLayout());
    resizePanel.add(scroll);
    resizePanel.add(bottom, BorderLayout.SOUTH);
    resizePanel.add(Box.createHorizontalStrut(240), BorderLayout.NORTH);
    resizePanel.setBorder(BorderFactory.createLineBorder(new Color(0x64_64_64)));
    return resizePanel;
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
    frame.setMinimumSize(new Dimension(100, 100));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ResizeWindowListener extends MouseInputAdapter {
  private final Rectangle rect = new Rectangle();

  @Override public void mousePressed(MouseEvent e) {
    Window w = SwingUtilities.getWindowAncestor(e.getComponent());
    if (w != null) {
      rect.setSize(w.getSize());
    }
  }

  @Override public void mouseDragged(MouseEvent e) {
    Window w = SwingUtilities.getWindowAncestor(e.getComponent());
    if (!rect.isEmpty() && w != null) {
      rect.height += e.getY();
      w.setSize(rect.width, rect.height);
    }
  }
}

class DotIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(Color.GRAY);
    int dots = 4;
    int gap = 4;
    int start = getIconWidth() / 2 - (dots - 1) * 2;
    int h = getIconHeight() / 2;
    for (int i = 0; i < dots; i++) {
      g2.fillRect(start + gap * i, h, 2, 2);
    }
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 5;
  }
}
