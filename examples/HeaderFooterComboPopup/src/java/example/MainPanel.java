// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] items = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg"};
    JComboBox<String> combo = new JComboBox<String>(items) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsComboBoxUI) {
          setUI(new WindowsComboBoxUI() {
            @Override protected ComboPopup createPopup() {
              return new HeaderFooterComboPopup(comboBox);
            }
          });
        } else {
          setUI(new BasicComboBoxUI() {
            @Override protected ComboPopup createPopup() {
              return new HeaderFooterComboPopup(comboBox);
            }
          });
        }
      }
    };
    combo.setMaximumRowCount(4);
    add(combo, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
    setPreferredSize(new Dimension(320, 240));
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

class HeaderFooterComboPopup extends BasicComboPopup {
  // Java 8: protected HeaderFooterComboPopup(JComboBox<?> combo) {
  // Java 9: protected HeaderFooterComboPopup(JComboBox<Object> combo) {
  @SuppressWarnings("unchecked")
  protected HeaderFooterComboPopup(JComboBox combo) {
    super(combo);
  }

  @Override protected void configurePopup() {
    // BasicComboPopup#configurePopup() sets a vertical BoxLayout
    // and adds the scroller that wraps the list.
    super.configurePopup();
    add(createHeader(), 0);
    add(createFooter());
  }

  protected JComponent createHeader() {
    JLabel header = new JLabel("History", SwingConstants.CENTER);
    header.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
    // The JLabel constructor sets LEFT_ALIGNMENT; match the CENTER_ALIGNMENT
    // of the scroller and the footer so the BoxLayout does not shift it.
    header.setAlignmentX(CENTER_ALIGNMENT);
    // A JLabel does not stretch in a BoxLayout unless its maximum
    // width is unbounded.
    int height = header.getPreferredSize().height;
    header.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
    return header;
  }

  protected JComponent createFooter() {
    int modifiers = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    JMenuItem footer = new JMenuItem("Show All Bookmarks");
    footer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, modifiers));
    footer.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor(comboBox);
      JOptionPane.showMessageDialog(w, "Bookmarks");
    });
    return footer;
  }
}
