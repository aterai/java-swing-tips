// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final Color EVEN_BGC = new Color(0xAA_DD_FF_FF, true);

  private MainPanel() {
    super(new BorderLayout());
    String key = "List.lockToPositionOnScroll";
    // UIManager.put(key, Boolean.FALSE);
    JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key));
    check.addActionListener(e -> UIManager.put(key, ((JCheckBox) e.getSource()).isSelected()));
    add(check, BorderLayout.NORTH);
    add(new JScrollPane(makeList()));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JList<String> makeList() {
    DefaultListModel<String> model = new DefaultListModel<>();
    IntStream.range(0, 1000).mapToObj(Objects::toString).forEach(model::addElement);
    JList<String> list = new JList<String>(model) {
      @Override public void updateUI() {
        setCellRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> r = getCellRenderer();
        setCellRenderer((l, v, i, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(l, v, i, isSelected, cellHasFocus);
          c.setForeground(isSelected ? l.getSelectionForeground() : l.getForeground());
          c.setBackground(isSelected ? l.getSelectionBackground() : getBgc(l, i));
          return c;
        });
      }
    };
    list.setFixedCellHeight(64);
    return list;
  }

  private static Color getBgc(JComponent c, int index) {
    return index % 2 == 0 ? EVEN_BGC : c.getBackground();
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
