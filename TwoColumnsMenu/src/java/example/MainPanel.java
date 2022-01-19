// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JMenu menu1 = new JMenu("File");
    // menu1.getPopupMenu().setLayout(new GridLayout(0, 2, 2, 0));
    menu1.add("New");
    menu1.add("Open");
    menu1.addSeparator();
    menu1.add("Exit");

    JMenu menu2 = LookAndFeelUtil.createLookAndFeelMenu();
    JPopupMenu popup = menu2.getPopupMenu();
    popup.setLayout(new GridLayout(0, 2, 8, 0));
    Border b = BorderFactory.createCompoundBorder(popup.getBorder(), new ColumnRulesBorder());
    popup.setBorder(b);

    JMenuBar mb = new JMenuBar();
    mb.add(menu1);
    mb.add(menu2);

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

class ColumnRulesBorder implements Border {
  private final Insets insets = new Insets(0, 0, 0, 0);
  private final JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
  private final Container renderer = new JPanel();

  @Override public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
    if (c instanceof JComponent) {
      Rectangle r = SwingUtilities.calculateInnerArea((JComponent) c, null);
      int sw = separator.getPreferredSize().width;
      int sh = r.height;
      int sx = (int) (r.getCenterX() - sw / 2d);
      int sy = (int) r.getMinY();
      Graphics2D g2 = (Graphics2D) g.create();
      SwingUtilities.paintComponent(g2, separator, renderer, sx, sy, sw, sh);
      g2.dispose();
    }
  }

  @Override public Insets getBorderInsets(Component c) {
    return insets;
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }
}

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtil() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup lafGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
      menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafGroup));
    }
    return menu;
  }

  private static JMenuItem createLookAndFeelItem(String laf, String lafClass, ButtonGroup bg) {
    JMenuItem lafItem = new JRadioButtonMenuItem(laf, lafClass.equals(lookAndFeel));
    lafItem.setActionCommand(lafClass);
    lafItem.setHideActionText(true);
    lafItem.addActionListener(e -> {
      ButtonModel m = bg.getSelection();
      try {
        setLookAndFeel(m.getActionCommand());
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
        UIManager.getLookAndFeel().provideErrorFeedback((Component) e.getSource());
      }
    });
    bg.add(lafItem);
    return lafItem;
  }

  private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
    String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
    if (!oldLookAndFeel.equals(lookAndFeel)) {
      UIManager.setLookAndFeel(lookAndFeel);
      LookAndFeelUtil.lookAndFeel = lookAndFeel;
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
