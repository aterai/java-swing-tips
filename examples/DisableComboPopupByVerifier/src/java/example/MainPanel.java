// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // https://stackoverflow.com/questions/72240670/prevent-a-jcombobox-popup-from-appearing-passing-inputverifier
    JTextField field = new JTextField();
    field.setInputVerifier(new LengthInputVerifier());

    JComboBox<String> combo2 = new JComboBox<>(makeModel(20));
    combo2.setFocusable(false);

    JComboBox<String> combo3 = new JComboBox<>(makeModel(15));
    combo3.setEditable(true);

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("InputVerifier:", field));
    box.add(Box.createVerticalStrut(4));
    box.add(makeTitledPanel("Default:", new JComboBox<>(makeModel(5))));
    box.add(Box.createVerticalStrut(4));
    box.add(makeTitledPanel("setFocusable(false):", combo2));
    box.add(Box.createVerticalStrut(4));
    box.add(makeTitledPanel("setEditable(true):", combo3));
    box.add(Box.createVerticalStrut(4));
    box.add(makeTitledPanel("Override BasicComboPopup:", makeComboBox()));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox() {
    return new JComboBox<String>(makeModel(10)) {
      @Override public void updateUI() {
        super.updateUI();
        if (getUI() instanceof WindowsComboBoxUI) {
          setUI(new WindowsComboBoxUI() {
            @Override protected ComboPopup createPopup() {
              return new BasicComboPopup2(comboBox);
            }
          });
        } else {
          setUI(new BasicComboBoxUI() {
            @Override protected ComboPopup createPopup() {
              return new BasicComboPopup2(comboBox);
            }
          });
        }
        setEditable(true);
      }
    };
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static ComboBoxModel<String> makeModel(int size) {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    IntStream.range(0, size).forEach(i -> model.addElement("No." + i));
    return model;
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

class LengthInputVerifier extends InputVerifier {
  private static final int MIN_LEN = 5;

  @Override public boolean verify(JComponent c) {
    boolean verified = false;
    if (c instanceof JTextComponent) {
      String str = ((JTextComponent) c).getText();
      verified = str.length() > MIN_LEN;
    }
    if (!verified) {
      UIManager.getLookAndFeel().provideErrorFeedback(c);
      String msg = String.format("Enter at least %s characters.", MIN_LEN);
      JOptionPane.showMessageDialog(c.getRootPane(), msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
    return verified;
  }
}

class BasicComboPopup2 extends BasicComboPopup {
  // private transient MouseListener handler2;

  // @Override public void uninstallingUI() {
  //   super.uninstallingUI();
  //   handler2 = null;
  // }

  // Java 8: protected BasicComboPopup2(JComboBox<?> combo) {
  // Java 9: protected BasicComboPopup2(JComboBox<Object> combo) {
  @SuppressWarnings("unchecked")
  protected BasicComboPopup2(JComboBox combo) {
    super(combo);
  }

  @Override protected MouseListener createMouseListener() {
    // if (Objects.isNull(handler2)) {
    //   handler2 = new Handler2();
    // }
    // return handler2;
    return new Handler2();
  }

  private final class Handler2 extends MouseAdapter {
    @Override public void mousePressed(MouseEvent e) {
      if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
        return;
      }
      Component c = e.getComponent();
      if (hasFocus(comboBox)) {
        togglePopup();
      } else if (c instanceof AbstractButton) {
        ((AbstractButton) c).getModel().setPressed(false);
      }
    }

    @Override public void mouseReleased(MouseEvent e) {
      Component source = (Component) e.getSource();
      Dimension size = source.getSize();
      Rectangle bounds = new Rectangle(0, 0, size.width - 1, size.height - 1);
      if (!bounds.contains(e.getPoint())) {
        MouseEvent newEvent = SwingUtilities.convertMouseEvent(e.getComponent(), e, list);
        Point location = newEvent.getPoint();
        Rectangle r = new Rectangle();
        list.computeVisibleRect(r);
        if (r.contains(location)) {
          if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
            comboBox.getEditor().setItem(list.getSelectedValue());
          }
          comboBox.setSelectedIndex(list.getSelectedIndex());
        }
        comboBox.setPopupVisible(false);
      }
      hasEntered = false;
      stopAutoScrolling();
    }
  }

  private static boolean hasFocus(JComboBox<?> combo) {
    boolean hasFocus = true;
    if (combo.isEditable()) {
      Component comp = combo.getEditor().getEditorComponent();
      if (!(comp instanceof JComponent) || ((JComponent) comp).isRequestFocusEnabled()) {
        hasFocus = comp.hasFocus() || comp.requestFocusInWindow();
      }
    } else if (combo.isRequestFocusEnabled()) {
      hasFocus = combo.hasFocus() || combo.requestFocusInWindow();
    }
    return hasFocus;
  }
}
