// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"123456", "7890", "a"};
    JComboBox<String> comboBox0 = new JComboBox<>(model);
    comboBox0.setEditable(true);
    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(new JLabel("Default:", SwingConstants.LEFT));
    p.add(comboBox0);
    p.add(Box.createVerticalStrut(15));
    p.add(new JLabel("popupMenuWillBecomeVisible:", SwingConstants.LEFT));
    p.add(makeComboBox1(model));
    p.add(Box.createVerticalStrut(15));
    p.add(new JLabel("+enterPressed Action:", SwingConstants.LEFT));
    p.add(makeComboBox2(model));
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox1(String... model) {
    return new JComboBox<String>(model) {
      private transient PopupMenuListener handler;

      @Override public void updateUI() {
        removePopupMenuListener(handler);
        super.updateUI();
        setEditable(true);
        handler = new SelectItemMenuListener();
        addPopupMenuListener(handler);
      }
    };
  }

  private static JComboBox<String> makeComboBox2(String... model) {
    return new JComboBox<String>(model) {
      private static final int MAX_HISTORY = 10;
      private static final String ENTER_PRESSED = "enterPressed";
      private transient PopupMenuListener handler;

      @Override public void updateUI() {
        removePopupMenuListener(handler);
        getActionMap().put(ENTER_PRESSED, null);
        super.updateUI();
        Action defaultAction = getActionMap().get(ENTER_PRESSED);
        Action a = new AbstractAction() {
          @Override public void actionPerformed(ActionEvent e) {
            boolean isPopupVisible = isPopupVisible();
            setPopupVisible(false);
            DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) getModel();
            String str = Objects.toString(getEditor().getItem(), "");
            if (m.getIndexOf(str) < 0) {
              m.removeElement(str);
              m.insertElementAt(str, 0);
              if (m.getSize() > MAX_HISTORY) {
                m.removeElementAt(MAX_HISTORY);
              }
              setSelectedIndex(0);
              setPopupVisible(isPopupVisible);
            } else {
              defaultAction.actionPerformed(e);
            }
          }
        };
        getActionMap().put(ENTER_PRESSED, a);
        setEditable(true);
        handler = new SelectItemMenuListener();
        addPopupMenuListener(handler);
      }
    };
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

class SelectItemMenuListener implements PopupMenuListener {
  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> c = (JComboBox<?>) e.getSource();
    c.setSelectedItem(c.getEditor().getItem());
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }
}
