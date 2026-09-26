// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<PairItem> combo0 = new JComboBox<>(createModel());
    add(createTitledBox("DefaultComboBox", combo0), BorderLayout.SOUTH);

    JComboBox<PairItem> combo1 = new JComboBox<PairItem>(createModel()) {
      @Override public void updateUI() {
        // setRenderer(null);
        super.updateUI();
        setRenderer(new MultiColumnCellRenderer<>());
      }
    };
    add(createTitledBox("MultiColumnComboBox", combo1), BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Box createTitledBox(String title, JComboBox<?> combo) {
    JTextField leftTextField = new JTextField();
    JTextField rightTextField = new JTextField();
    leftTextField.setEditable(false);
    rightTextField.setEditable(false);
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder(title));
    box.add(Box.createVerticalStrut(2));
    box.add(combo);
    box.add(Box.createVerticalStrut(2));
    box.add(leftTextField);
    box.add(Box.createVerticalStrut(2));
    box.add(rightTextField);
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        updateTextFields((PairItem) e.getItem(), leftTextField, rightTextField);
      }
    });
    updateTextFields((PairItem) combo.getSelectedItem(), leftTextField, rightTextField);
    return box;
  }

  private static void updateTextFields(PairItem item, JTextField left, JTextField right) {
    left.setText(item == null ? "" : item.getLeftText());
    right.setText(item == null ? "" : item.getRightText());
  }

  private static ComboBoxModel<PairItem> createModel() {
    String name = "loooooooooooooooooooooooooooooooooong.1234567890.1234567890";
    DefaultComboBoxModel<PairItem> m = new DefaultComboBoxModel<>();
    m.addElement(new PairItem("ComboBoxModel", "846876"));
    m.addElement(new PairItem("ComboBoxItem", "1111111111111111111111111"));
    m.addElement(new PairItem(name, "test.1234567890.1234567890.1234567890"));
    m.addElement(new PairItem("14234125", "64345424543523452345234523684"));
    m.addElement(new PairItem("DefaultComboBoxModel", "addElement"));
    m.addElement(new PairItem("aaa aaa aa", "ddd"));
    m.addElement(new PairItem("bbb bbb bb", "eee ee"));
    m.addElement(new PairItem("ccc ccc cc", "fff fff"));
    return m;
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
    frame.setMinimumSize(new Dimension(256, 100));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class MultiColumnCellRenderer<E extends PairItem> implements ListCellRenderer<E> {
  private final JLabel leftLabel = new JLabel() {
    @Override public void updateUI() {
      super.updateUI();
      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 0));
    }
  };
  private final JLabel rightLabel = new JLabel() {
    @Override public void updateUI() {
      super.updateUI();
      setOpaque(false);
      setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
      setHorizontalAlignment(RIGHT);
    }

    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 80;
      return d;
    }
  };
  private final JPanel renderer = new JPanel(new BorderLayout()) {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = 0;
      return d;
    }

    @Override public void updateUI() {
      super.updateUI();
      setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
      // TEST:
      // setName("List.cellRenderer");
      // setName("ComboBox.renderer");
      // setName("ComboBox.listRenderer");
    }
  };

  protected MultiColumnCellRenderer() {
    renderer.add(leftLabel);
    renderer.add(rightLabel, BorderLayout.EAST);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    leftLabel.setText(value == null ? "" : value.getLeftText());
    rightLabel.setText(value == null ? "" : value.getRightText());
    leftLabel.setFont(list.getFont());
    rightLabel.setFont(list.getFont());
    Color fgc;
    Color bgc;
    if (index >= 0 && isSelected) {
      fgc = list.getSelectionForeground();
      bgc = list.getSelectionBackground();
    } else {
      fgc = list.getForeground();
      bgc = list.getBackground();
    }
    leftLabel.setForeground(fgc);
    rightLabel.setForeground(blend(fgc, bgc));
    renderer.setBackground(bgc);
    renderer.setOpaque(index >= 0);
    return renderer;
  }

  private static Color blend(Color c1, Color c2) {
    int r = (c1.getRed() + c2.getRed()) / 2;
    int g = (c1.getGreen() + c2.getGreen()) / 2;
    int b = (c1.getBlue() + c2.getBlue()) / 2;
    return new Color(r, g, b);
  }
}

class PairItem {
  private final String leftText;
  private final String rightText;

  protected PairItem(String leftText, String rightText) {
    this.leftText = leftText;
    this.rightText = rightText;
  }

  public String getLeftText() {
    return leftText;
  }

  public String getRightText() {
    return rightText;
  }

  @Override public String toString() {
    return leftText + " / " + rightText;
  }
}
