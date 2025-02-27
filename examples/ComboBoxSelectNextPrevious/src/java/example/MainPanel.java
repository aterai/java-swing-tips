// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Objects;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ComboBoxModel<LookAndFeelInfo> model = makeModel();
    JComboBox<LookAndFeelInfo> combo = new LookAndFeelComboBox(model) {
      @Override public void updateUI() {
        super.updateUI();
        String name = getUI().getClass().getName();
        if (name.contains("MetalComboBoxUI") || name.contains("MotifComboBoxUI")) {
          InputMap im = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
          im.put(KeyStroke.getKeyStroke("DOWN"), "selectNext2");
          im.put(KeyStroke.getKeyStroke("UP"), "selectPrevious2");
        }
      }
    };
    JPanel box = new JPanel(new GridLayout(2, 2, 5, 2));
    box.add(new JLabel("MetalComboBoxUI default"));
    box.add(new JLabel("BasicComboBoxUI default"));
    box.add(new LookAndFeelComboBox(model));
    box.add(combo);
    add(box, BorderLayout.NORTH);
    add(new JComboBox<>(new String[] {"Item1", "Item2", "Item3"}), BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboBoxModel<LookAndFeelInfo> makeModel() {
    return new DefaultComboBoxModel<>(UIManager.getInstalledLookAndFeels());
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class LookAndFeelComboBox extends JComboBox<LookAndFeelInfo> {
  private transient ItemListener listener;

  protected LookAndFeelComboBox(ComboBoxModel<LookAndFeelInfo> lnf) {
    super(lnf);
  }

  @Override public void updateUI() {
    removeItemListener(listener);
    setRenderer(null);
    super.updateUI();
    ListCellRenderer<? super LookAndFeelInfo> r = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = r.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
      if (c instanceof JLabel && value != null) {
        ((JLabel) c).setText(value.getName());
      }
      return c;
    });
    listener = e -> {
      Object o = e.getItem();
      if (e.getStateChange() == ItemEvent.SELECTED && o instanceof LookAndFeelInfo) {
        setLookAndFeel(((LookAndFeelInfo) o).getClassName());
      }
    };
    addItemListener(listener);
  }

  private static void setLookAndFeel(String lookAndFeelName) {
    EventQueue.invokeLater(() -> {
      String current = UIManager.getLookAndFeel().getClass().getName();
      if (!Objects.equals(current, lookAndFeelName)) {
        try {
          UIManager.setLookAndFeel(lookAndFeelName);
          updateLookAndFeel();
        } catch (UnsupportedLookAndFeelException ignored) {
          Toolkit.getDefaultToolkit().beep();
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
          Toolkit.getDefaultToolkit().beep();
          ex.printStackTrace();
        }
      }
    });
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
