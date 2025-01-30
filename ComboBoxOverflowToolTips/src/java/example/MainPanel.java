// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
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

class ToolTipComboBox<E> extends JComboBox<E> {
  protected ToolTipComboBox(ComboBoxModel<E> model) {
    super(model);
  }

  @Override public void updateUI() {
    setRenderer(null);
    super.updateUI();
    ListCellRenderer<? super E> renderer = getRenderer();
    setRenderer((list, value, index, isSelected, cellHasFocus) -> {
      Component c = renderer.getListCellRendererComponent(
          list, value, index, isSelected, cellHasFocus);
      if (c instanceof JComponent) {
        setToolTipText((JComponent) c, value, index < 0);
      }
      return c;
    });
  }

  private void setToolTipText(JComponent c, E value, boolean isComboBody) {
    Insets ins = c.getInsets();
    Rectangle rect = SwingUtilities.calculateInnerArea(this, null);
    int availableWidth = rect.width - ins.top - ins.bottom;
    String str = Objects.toString(value, "");
    FontMetrics fm = c.getFontMetrics(c.getFont());
    c.setToolTipText(fm.stringWidth(str) > availableWidth ? str : null);
    if (isComboBody) {
      int buttonSize = Stream.of(getComponents())
          .filter(JButton.class::isInstance)
          .findFirst()
          .map(Component::getWidth)
          .orElse(rect.height);
      availableWidth -= buttonSize;
      JTextField tf = (JTextField) getEditor().getEditorComponent();
      availableWidth -= tf.getMargin().left + tf.getMargin().right;
      setToolTipText(fm.stringWidth(str) > availableWidth ? str : null);
    }
  }
}
