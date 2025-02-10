// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ItemEvent;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private final JTextArea log = new JTextArea();

  private MainPanel() {
    super(new BorderLayout(5, 5));
    // TEST: System.out.println(UIManager.getBoolean("ComboBox.noActionOnKeyNavigation"));
    // TEST: UIManager.put("ComboBox.noActionOnKeyNavigation", Boolean.TRUE);

    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(new JLabel("ComboBox.isEnterSelectablePopup: false(default)", SwingConstants.LEFT));
    p.add(makeComboBox(false));
    p.add(new JLabel("ComboBox.isEnterSelectablePopup: true", SwingConstants.LEFT));
    p.add(makeComboBox(true));

    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private JComboBox<String> makeComboBox(boolean isEnterSelectable) {
    // UIManager.put("ComboBox.isEnterSelectablePopup", Boolean.TRUE);
    JComboBox<String> combo = new JComboBox<>(new String[] {"aaa", "bbb", "CCC", "DDD"});
    combo.setEditable(true);
    combo.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        String key = "ComboBox.isEnterSelectablePopup";
        UIManager.put(key, isEnterSelectable);
        append("\n" + key + ": " + UIManager.getBoolean(key));
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }
    });
    // combo.addActionListener(e -> append("ActionListener: " + combo.getSelectedItem()));
    combo.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        append("ItemListener: " + e.getItem());
      }
    });
    return combo;
  }

  public void append(String text) {
    log.append(text + "\n");
    log.setCaretPosition(log.getDocument().getLength());
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
