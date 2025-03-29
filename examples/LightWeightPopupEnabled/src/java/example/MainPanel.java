// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super();
    // // Java 9:
    // PopupFactory.setSharedInstance(new PopupFactory() {
    //   @Override public Popup getPopup(Component owner, Component contents, int x, int y) throws IllegalArgumentException {
    //     // @param isHeavyWeightPopup true if Popup should be heavy-weight,
    //     // protected Popup getPopup(..., boolean isHeavyWeightPopup) ...
    //     return super.getPopup(owner, contents, x, y, true);
    //   }
    // });

    JComboBox<String> combo0 = makeComboBox();
    // combo0.setLightWeightPopupEnabled(true); // Default
    JComboBox<String> combo1 = makeComboBox();
    combo1.setLightWeightPopupEnabled(false);
    Box box = Box.createVerticalBox();
    box.add(combo0);
    box.add(combo1);
    box.add(Box.createVerticalGlue());

    JPopupMenu popup0 = makePopupMenu();
    // popup0.setLightWeightPopupEnabled(true); // Default
    JLabel label0 = makeLabel("setLightWeightPopupEnabled: true", Color.ORANGE);
    label0.setComponentPopupMenu(popup0);

    JPopupMenu popup1 = makePopupMenu();
    popup1.setLightWeightPopupEnabled(false);
    JLabel label1 = makeLabel("setLightWeightPopupEnabled: false", Color.PINK);
    label1.setComponentPopupMenu(popup1);

    JComponent glass = new JPanel(new GridLayout(4, 1, 5, 5)) {
      private final Color backgroundColor = new Color(0x64_64_64_C8, true);
      @Override protected void paintComponent(Graphics g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        super.paintComponent(g);
      }
    };
    glass.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    glass.setOpaque(false);
    glass.add(box);
    glass.add(label0);
    glass.add(label1);
    EventQueue.invokeLater(() -> {
      getRootPane().setGlassPane(glass);
      getRootPane().getGlassPane().setVisible(true);
    });
    setPreferredSize(new Dimension(320, 240));
  }

  private JComboBox<String> makeComboBox() {
    String[] model = {"Item1", "Item2", "Item3"};
    return new JComboBox<>(model);
  }

  private static JPopupMenu makePopupMenu() {
    JPopupMenu popup = new JPopupMenu();
    popup.add("JMenuItem");
    popup.addSeparator();
    popup.add(new JCheckBoxMenuItem("JCheckBoxMenuItem"));
    popup.add(new JRadioButtonMenuItem("JRadioButtonMenuItem"));
    JMenu menu = new JMenu("JMenu");
    menu.add("JMenuItem1");
    menu.add("JMenuItem2");
    popup.add(menu);
    return popup;
  }

  private static JLabel makeLabel(String title, Color color) {
    JLabel label = new JLabel(title);
    label.setOpaque(true);
    label.setBackground(color);
    label.setToolTipText("ToolTipText");
    return label;
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
