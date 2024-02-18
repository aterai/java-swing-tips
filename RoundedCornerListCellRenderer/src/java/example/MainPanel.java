// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"111", "2222", "33333"};
    JComboBox<String> combo1 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new RoundedCornerListCellRenderer<>());
      }
    };

    JComboBox<String> combo2 = new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new RoundedCornerListCellRenderer<>());
        setEditable(true);
      }
    };

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("Default JComboBox", new JComboBox<>(model)));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("RoundedCornerListCellRenderer", combo1));
    box.add(Box.createVerticalStrut(10));
    box.add(makeTitledPanel("RoundedCornerListCellRenderer(editable)", combo2));
    add(box, BorderLayout.NORTH);
    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));
    setPreferredSize(new Dimension(320, 240));
  }

  // // Not work?
  // // https://ateraimemo.com/Swing/RendererUseListColors.html
  // private static void nimbusDerivedColor(JComboBox<?> combo) {
  //   UIDefaults d = new UIDefaults();
  //   d.put("List.rendererUseListColors", Boolean.TRUE);
  //   d.put("ComboBox.rendererUseListColors", Boolean.TRUE);
  //   d.put("ComboBox:\"ComboBox.renderer\"[Selected].textForeground", Color.WHITE);
  //   combo.putClientProperty("Nimbus.Overrides", d);
  // }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
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

class RoundedCornerListCellRenderer<E> implements ListCellRenderer<E> {
  private static final Icon GAP = new GapIcon();
  private final DefaultListCellRenderer renderer = new DefaultListCellRenderer() {
    @Override protected void paintComponent(Graphics g) {
      if (getIcon() != null) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setPaint(getBackground());
        Rectangle r = SwingUtilities.calculateInnerArea(this, null);
        g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10f, 10f));
        super.paintComponent(g2);
        g2.dispose();
      } else {
        super.paintComponent(g);
      }
    }
  };

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c = renderer.getListCellRendererComponent(
        list, value, index, isSelected, cellHasFocus);
    if (c instanceof JLabel) {
      JLabel label = (JLabel) c;
      label.setOpaque(false);
      label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
      label.setIconTextGap(0);
      label.setIcon(index >= 0 ? GAP : null);
      // Nimbus DerivedColor bug?
      Color fgc = isSelected
          ? new Color(list.getSelectionForeground().getRGB())
          : list.getForeground();
      label.setForeground(fgc);
    }
    return c;
  }
}

// class RoundedCornerListCellRenderer2 extends DefaultListCellRenderer {
//   private static final Icon GAP = new GapIcon();
//
//   @Override public String getName() {
//     String name = super.getName();
//     return name == null ? "ComboBox.renderer" : name;
//   }
//
//   @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
//     setName("ComboBox.listRenderer");
//     Component c = super.getListCellRendererComponent(
//         list, value, index, isSelected, cellHasFocus);
//     if (c instanceof JLabel) {
//       JLabel label = (JLabel) c;
//       label.setOpaque(false);
//       label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
//       label.setIconTextGap(0);
//       label.setIcon(index >= 0 ? GAP : null);
//       Color fgc = isSelected
//           ? new Color(list.getSelectionForeground().getRGB())
//           : list.getForeground();
//       label.setForeground(fgc);
//     }
//     return c;
//   }
//
//   @Override protected void paintComponent(Graphics g) {
//     if (getIcon() != null) {
//       Graphics2D g2 = (Graphics2D) g.create();
//       g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//       g2.setPaint(getBackground());
//       Rectangle r = SwingUtilities.calculateInnerArea(this, null);
//       g2.fill(new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 10f, 10f));
//       super.paintComponent(g2);
//       g2.dispose();
//     } else {
//       super.paintComponent(g);
//     }
//   }
// }

class GapIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    /* Empty paint */
  }

  @Override public int getIconWidth() {
    return 2;
  }

  @Override public int getIconHeight() {
    return 18;
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
