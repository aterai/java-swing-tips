// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"aaa", "bbb", "ccc", "ddd", "eee", "fff", "ggg"};
    JComboBox<String> combo = new JComboBox<String>(model) {
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

class HeaderFooterComboPopup extends BasicComboPopup {
  protected transient JLabel header;
  protected transient JMenuItem footer;

  // Java 8: protected HeaderFooterComboPopup(JComboBox<?> combo) {
  // Java 9: protected HeaderFooterComboPopup(JComboBox<Object> combo) {
  @SuppressWarnings("unchecked")
  protected HeaderFooterComboPopup(JComboBox combo) {
    super(combo);
  }

  @Override protected void configurePopup() {
    // setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    // setBorderPainted(true);
    // setBorder(LIST_BORDER);
    // setOpaque(false);
    // add(scroller);
    // setDoubleBuffered(true);
    // setFocusable(false);
    super.configurePopup();
    configureHeader();
    configureFooter();
    add(header, 0);
    add(footer);
    // or
    // setLayout(new BorderLayout());
    // add(header, BorderLayout.NORTH);
    // add(scroller);
    // add(footer, BorderLayout.SOUTH);
  }

  protected void configureHeader() {
    header = new JLabel("History");
    header.setBorder(BorderFactory.createEmptyBorder(4, 5, 4, 0));
    header.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
    header.setAlignmentX(1f);
  }

  protected void configureFooter() {
    int modifiers = InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK;
    footer = new JMenuItem("Show All Bookmarks");
    footer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, modifiers));
    footer.addActionListener(e -> {
      Window w = SwingUtilities.getWindowAncestor(getInvoker());
      JOptionPane.showMessageDialog(w, "Bookmarks");
    });
  }
}
