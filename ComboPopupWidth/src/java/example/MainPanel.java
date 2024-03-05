// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> combo00 = makeComboBox();
    combo00.setEditable(false);

    JComboBox<String> combo01 = makeComboBox();
    combo01.setEditable(true);

    JComboBox<String> combo02 = makeComboBox();
    combo02.setEditable(false);
    combo02.addPopupMenuListener(new WidePopupMenuListener());

    JComboBox<String> combo03 = makeComboBox();
    combo03.setEditable(true);
    combo03.addPopupMenuListener(new WidePopupMenuListener());

    int g = 5;
    JPanel p = new JPanel(new GridLayout(4, 2, g, g));
    p.add(combo00);
    p.add(new JLabel("<- normal"));
    p.add(combo01);
    p.add(new JLabel("<- normal, editable"));
    p.add(combo02);
    p.add(new JLabel("<- wide"));
    p.add(combo03);
    p.add(new JLabel("<- wide, editable"));
    setBorder(BorderFactory.createEmptyBorder(g, g, g, g));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox() {
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

// https://community.oracle.com/thread/1368300 How to widen the drop-down list in a JComboBox
class WidePopupMenuListener implements PopupMenuListener {
  private static final int POPUP_MIN_WIDTH = 300;
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> combo = (JComboBox<?>) e.getSource();
    Dimension size = combo.getSize();
    if (size.width >= POPUP_MIN_WIDTH || adjusting.get()) {
      return;
    }
    adjusting.set(true);
    combo.setSize(POPUP_MIN_WIDTH, size.height);
    combo.showPopup();
    // // Java 8
    // combo.setSize(size);
    // adjusting.set(false);
    // Java 21
    EventQueue.invokeLater(() -> {
      combo.setSize(size);
      adjusting.set(false);
    });
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }
}
