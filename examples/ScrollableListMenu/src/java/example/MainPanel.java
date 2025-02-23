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

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    DefaultListModel<String> model = new DefaultListModel<>();
    Stream.of(fonts).map(Font::getFontName).sorted().forEach(model::addElement);
    JList<String> list = new PopupList<>(model);
    JScrollPane scroll = new JScrollPane(list);
    scroll.getVerticalScrollBar().setUnitIncrement(list.getFixedCellHeight());
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(BorderFactory.createEmptyBorder());

    JMenu subMenu = new JMenu("Font list");
    subMenu.add(scroll);
    JMenu menu = new JMenu("Menu");
    menu.add(subMenu);
    menu.addSeparator();
    menu.add("Item 1");
    menu.add("Item 2");
    JMenuBar mb = new JMenuBar();
    mb.add(menu);
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    add(new JScrollPane(new JTextArea()));
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

class PopupList<E> extends JList<E> {
  private transient PopupListMouseListener listener;

  protected PopupList(ListModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    removeMouseMotionListener(listener);
    super.updateUI();
    setFixedCellHeight(20);
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    listener = new PopupListMouseListener();
    addMouseListener(listener);
    addMouseMotionListener(listener);
    Color selectedBg = new Color(0x91_C9_F7); // UIManager.get("Menu.selectionBackground");
    ListCellRenderer<? super E> renderer = getCellRenderer();
    setCellRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JComponent && listener.isRolloverIndex(index)) {
        c.setBackground(selectedBg);
        c.setForeground(Color.WHITE);
      }
      return c;
    });
  }
}

class PopupListMouseListener extends MouseAdapter {
  private int index = -1;

  public boolean isRolloverIndex(int i) {
    return this.index == i;
  }

  private void setRollover(MouseEvent e) {
    Point pt = e.getPoint();
    Component c = e.getComponent();
    if (c instanceof JList) {
      index = ((JList<?>) c).locationToIndex(pt);
      c.repaint();
    }
  }

  @Override public void mouseMoved(MouseEvent e) {
    setRollover(e);
  }

  @Override public void mouseDragged(MouseEvent e) {
    setRollover(e);
  }

  @Override public void mouseClicked(MouseEvent e) {
    MenuSelectionManager.defaultManager().clearSelectedPath();
  }

  @Override public void mouseExited(MouseEvent e) {
    index = -1;
    e.getComponent().repaint();
  }
}

final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        Logger.getGlobal().severe(ex::getMessage);
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
