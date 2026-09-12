// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> combo00 = createComboBox();
    combo00.setEditable(false);

    JComboBox<String> combo01 = createComboBox();
    combo01.setEditable(true);

    JComboBox<String> combo02 = createComboBox();
    combo02.setEditable(false);
    combo02.addPopupMenuListener(new WidePopupMenuListener());

    JComboBox<String> combo03 = createComboBox();
    combo03.setEditable(true);
    combo03.addPopupMenuListener(new WidePopupMenuListener());

    int gap = 5;
    JPanel p = new JPanel(new GridLayout(4, 2, gap, gap));
    p.add(combo00);
    p.add(new JLabel("<- normal"));
    p.add(combo01);
    p.add(new JLabel("<- normal, editable"));
    p.add(combo02);
    p.add(new JLabel("<- wide"));
    p.add(combo03);
    p.add(new JLabel("<- wide, editable"));
    setBorder(BorderFactory.createEmptyBorder(gap, gap, gap, gap));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> createComboBox() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("1111");
    model.addElement("22222222");
    model.addElement("3333333333");
    model.addElement("012345678901234567890123456789012345678901234567890123456789");
    model.addElement("444");
    model.addElement("55555");
    return new JComboBox<>(model);
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

// How to widen the drop-down list in a JComboBox
// https://community.oracle.com/thread/1368300
class WidePopupMenuListener implements PopupMenuListener {
  private static final int POPUP_MIN_WIDTH = 300;

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> combo = (JComboBox<?>) e.getSource();
    Dimension size = combo.getSize();
    if (size.width < POPUP_MIN_WIDTH) {
      // Temporarily widen the combo box so that BasicComboPopup#getPopupLocation()
      // sizes the popup from the widened bounds. The nested showPopup() fires this
      // listener again, but the width check above prevents infinite recursion.
      combo.setSize(POPUP_MIN_WIDTH, size.height);
      combo.showPopup();
      // // Java 8
      // combo.setSize(size);
      // Java 21: the outer BasicComboPopup#show() still calls getPopupLocation()
      // after this listener returns, so restoring the size synchronously would
      // shrink the already visible popup back to the combo box width.
      EventQueue.invokeLater(() -> combo.setSize(size));
    }
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }
}
