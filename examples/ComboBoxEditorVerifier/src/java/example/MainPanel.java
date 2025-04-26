// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String[] model = {"123456", "7890"};
    JComboBox<String> combo = new EditableComboBox(model);
    initComboBoxEditor(combo);
    JPanel p = new JPanel(new GridLayout(5, 1));
    p.add(new JLabel("Default:", SwingConstants.LEFT));
    p.add(new EditableComboBox(model));
    p.add(Box.createVerticalStrut(15));
    p.add(new JLabel("6 >= str.length()", SwingConstants.LEFT));
    p.add(combo);
    add(p, BorderLayout.NORTH);
    setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initComboBoxEditor(JComboBox<String> combo) {
    combo.setEditor(new BasicComboBoxEditor() {
      private Component editorComponent;
      // // @see javax/swing/plaf/synth/SynthComboBoxUI.java
      // @Override public JTextField createEditorComponent() {
      //   JTextField f = new JTextField("", 9); f.setName("ComboBox.textField");
      //   return f;
      // }

      @Override public Component getEditorComponent() {
        // if (editorComponent == null) { editorComponent = makeLayer(); }
        editorComponent = Optional.ofNullable(editorComponent).orElseGet(this::makeLayer);
        return editorComponent;
      }

      private JLayer<JTextComponent> makeLayer() {
        JTextComponent c = (JTextComponent) super.getEditorComponent();
        return new JLayer<>(c, new ValidationLayerUI<>());
      }
    });
    combo.setInputVerifier(new LengthInputVerifier());
    combo.addPopupMenuListener(new SelectItemMenuListener());
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

class EditableComboBox extends JComboBox<String> {
  private static final int MAX_HISTORY = 10;
  private static final String ENTER_PRESSED = "enterPressed";
  private Action defaultAction;

  protected EditableComboBox(String... model) {
    super(model);
  }

  @Override public void updateUI() {
    getActionMap().put(ENTER_PRESSED, null);
    super.updateUI();
    defaultAction = getActionMap().get(ENTER_PRESSED);
    getActionMap().put(ENTER_PRESSED, new EnterAction());
    setEditable(true);
  }

  protected final class EnterAction extends AbstractAction {
    @Override public void actionPerformed(ActionEvent e) {
      boolean isPopupVisible = isPopupVisible();
      DefaultComboBoxModel<String> m = (DefaultComboBoxModel<String>) getModel();
      String s = Objects.toString(getEditor().getItem(), "");
      InputVerifier v = getInputVerifier();
      if (v != null && v.verify((JComboBox<?>) e.getSource()) && m.getIndexOf(s) < 0) {
        setPopupVisible(false);
        updateModel(m, s);
        setPopupVisible(isPopupVisible);
      } else if (v == null && m.getIndexOf(s) < 0) {
        setPopupVisible(false);
        updateModel(m, s);
        setPopupVisible(isPopupVisible);
      } else {
        defaultAction.actionPerformed(e);
      }
    }

    private void updateModel(DefaultComboBoxModel<String> m, String s) {
      m.removeElement(s);
      m.insertElementAt(s, 0);
      if (m.getSize() > MAX_HISTORY) {
        m.removeElementAt(MAX_HISTORY);
      }
      setSelectedIndex(0);
    }
  }
}

// @see https://docs.oracle.com/javase/tutorial/uiswing/examples/misc/FieldValidatorProject/src/FieldValidator.java
class ValidationLayerUI<V extends JTextComponent> extends LayerUI<V> {
  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    // JTextComponent tc = ((JLayer<?>) c).getView();
    Container p = SwingUtilities.getAncestorOfClass(JComboBox.class, c);
    if (p instanceof JComboBox) {
      JComboBox<?> combo = (JComboBox<?>) p;
      Optional.ofNullable(combo.getInputVerifier())
          .filter(iv -> !iv.verify(combo))
          .ifPresent(iv -> new ErrorIcon().paintIcon(c, g, 0, 0));
    }
  }
}

class LengthInputVerifier extends InputVerifier {
  public static final int MAX_LEN = 6;

  @Override public boolean verify(JComponent c) {
    return c instanceof JComboBox && maxLengthCheck((JComboBox<?>) c, MAX_LEN);
  }

  public static boolean maxLengthCheck(JComboBox<?> comboBox, int max) {
    String str = Objects.toString(comboBox.getEditor().getItem(), "");
    return max - str.length() >= 0;
  }
}

class ErrorIcon implements Icon {
  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    int w = c.getWidth();
    int h = c.getHeight();
    int iw = getIconWidth();
    int ih = getIconHeight();
    int pad = 5;
    int cx = w - pad - iw;
    int cy = (h - ih) / 2;
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(cx, cy);
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2.setPaint(Color.RED);
    g2.fillRect(0, 0, iw + 1, ih + 1);
    g2.setPaint(Color.WHITE);
    g2.drawLine(0, 0, iw, ih);
    g2.drawLine(0, ih, iw, 0);
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 8;
  }

  @Override public int getIconHeight() {
    return 8;
  }
}
