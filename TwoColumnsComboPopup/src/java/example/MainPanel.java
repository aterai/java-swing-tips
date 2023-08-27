// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.accessibility.Accessible;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    ComboBoxModel<String> model = makeModel();
    int rowCount = (model.getSize() + 1) / 2;
    JComboBox<String> combo = new JComboBox<String>(model) {
      @Override public Dimension getPreferredSize() {
        Insets i = getInsets();
        Dimension d = super.getPreferredSize();
        int w = Math.max(100, d.width);
        int h = d.height;
        int buttonWidth = 20; // ???
        return new Dimension(buttonWidth + w + i.left + i.right, h + i.top + i.bottom);
      }

      @Override public void updateUI() {
        super.updateUI();
        setMaximumRowCount(rowCount);
        setPrototypeDisplayValue("12345");
        Accessible o = getAccessibleContext().getAccessibleChild(0);
        if (o instanceof ComboPopup) {
          JList<?> list = ((ComboPopup) o).getList();
          list.setLayoutOrientation(JList.VERTICAL_WRAP);
          list.setVisibleRowCount(rowCount);
          Border b0 = list.getBorder();
          Border b1 = new ColumnRulesBorder();
          list.setBorder(BorderFactory.createCompoundBorder(b0, b1));
          list.setFixedCellWidth((getPreferredSize().width - 2) / 2);
        }
      }
    };

    add(new JComboBox<>(makeModel()));
    add(combo);

    // JMenu menu = LookAndFeelUtils.createLookAndFeelMenu();
    // JPopupMenu popup = menu.getPopupMenu();
    // popup.setLayout(new GridLayout(0, 2, 8, 0));
    // Border b = BorderFactory.createCompoundBorder(popup.getBorder(), new ColumnRulesBorder());
    // popup.setBorder(b);

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<String> makeModel() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("111");
    model.addElement("2222");
    model.addElement("3");
    model.addElement("44444");
    model.addElement("55555");
    model.addElement("66");
    model.addElement("777");
    model.addElement("8");
    model.addElement("9999");
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
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
        ex.printStackTrace();
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
