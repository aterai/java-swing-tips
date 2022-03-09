// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    ComboBoxModel<String> model = makeComboBoxModel();
    JComboBox<String> combo1 = new ToolTipComboBox<>(model);
    add(makeTitledPanel("Overflow ToolTip JComboBox", combo1), BorderLayout.NORTH);

    JComboBox<String> combo2 = new JComboBox<>(model);
    add(makeTitledPanel("Default JComboBox", combo2), BorderLayout.SOUTH);

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createTitledBorder(title));
    box.add(Box.createVerticalStrut(2));
    box.add(c);
    return box;
  }

  private static ComboBoxModel<String> makeComboBoxModel() {
    DefaultComboBoxModel<String> m = new DefaultComboBoxModel<>();
    m.addElement("0123456789/0123456789/0123456789/0123456789/01.jpg");
    m.addElement("abc.tif");
    m.addElement("aaa-bbb-ccc.pdf");
    m.addElement("c:/0123456789/0123456789/0123456789/0123456789/02.mpg");
    m.addElement("http://localhost/0123456789/0123456789/0123456789/0123456789/03.png");
    return m;
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

class ToolTipComboBox<E> extends JComboBox<E> {
  protected ToolTipComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setRenderer(null);
    super.updateUI();
    ListCellRenderer<? super E> renderer = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component r = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      JComponent c = (JComponent) r;
      // Insets i1 = getInsets();
      Insets ins = c.getInsets();
      // int availableWidth = getWidth() - i1.top - i1.bottom - ins.top - ins.bottom;
      Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
      // System.out.println(rect);
      int availableWidth = rect.width - ins.top - ins.bottom;

      String str = Objects.toString(value, "");
      FontMetrics fm = c.getFontMetrics(c.getFont());
      c.setToolTipText(fm.stringWidth(str) > availableWidth ? str : null);

      if (index < 0) {
        // @see BasicComboBoxUI#rectangleForCurrentValue
        // System.out.println(UIManager.getBoolean("ComboBox.squareButton"));
        // int buttonSize = getHeight() - i1.top - i1.bottom; // - ins.top - ins.bottom;
        JButton arrowButton = getArrowButton(this);
        int buttonSize = Objects.nonNull(arrowButton) ? arrowButton.getWidth() : rect.height;
        availableWidth -= buttonSize;
        JTextField tf = (JTextField) getEditor().getEditorComponent();
        availableWidth -= tf.getMargin().left + tf.getMargin().right;
        setToolTipText(fm.stringWidth(str) > availableWidth ? str : null);
      }
      return c;
    });
  }

  private static JButton getArrowButton(Container combo) {
    for (Component c : combo.getComponents()) {
      if (c instanceof JButton) {
        return (JButton) c;
      }
    }
    return null;
  }
}
