// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final JTextField leftTextField = new JTextField();
  private final JTextField rightTextField = new JTextField();

  private MainPanel() {
    super(new BorderLayout());
    leftTextField.setEditable(false);
    rightTextField.setEditable(false);

    DefaultComboBoxModel<PairItem> model = new DefaultComboBoxModel<>();
    model.addElement(new PairItem("aaa", "846876"));
    model.addElement(new PairItem("bbb bbb", "123456"));
    model.addElement(new PairItem("cc cc cc", "iop.23456789"));
    model.addElement(new PairItem("dd dd dd", "64345424684"));
    model.addElement(new PairItem("eee eee", "98765432210"));
    JComboBox<PairItem> combo = new JComboBox<>(model);
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        initTextField((PairItem) e.getItem());
      }
    });
    initTextField(combo.getItemAt(combo.getSelectedIndex()));

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(leftTextField);
    box.add(Box.createVerticalStrut(2));
    box.add(rightTextField);
    box.add(Box.createVerticalStrut(5));
    box.add(combo);
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void initTextField(PairItem item) {
    leftTextField.setText(item.getLeftText());
    rightTextField.setText(item.getRightText());
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

class PairItem {
  private final String leftText;
  private final String rightText;

  protected PairItem(String strLeft, String strRight) {
    leftText = strLeft;
    rightText = strRight;
  }

  public String getHtmlText() {
    String td1 = String.format("<td align='left'>%s</td>", leftText);
    String td2 = String.format("<td align='right'>%s</td>", rightText);
    return String.format("<html><table width='290'><tr>%s%s</tr></table>", td1, td2);
  }

  public String getLeftText() {
    return leftText;
  }

  public String getRightText() {
    return rightText;
  }

  @Override public String toString() {
    return getHtmlText();
  }
}
