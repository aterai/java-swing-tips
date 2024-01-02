// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.plaf.LayerUI;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<String> combo1 = new JComboBox<>(new String[] {"colors", "sports", "food"});
    combo1.setEditable(true);
    combo1.setSelectedIndex(-1);
    // combo1.setRenderer(new DefaultListCellRenderer() {
    //   @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
    //     String str = Objects.toString(value, "- Select category -");
    //     super.getListCellRendererComponent(list, str, index, isSelected, cellHasFocus);
    //     return this;
    //   }
    // });

    String[][] arrays = {
        {"blue", "violet", "red", "yellow"},
        {"basketball", "soccer", "football", "hockey"},
        {"hot dogs", "pizza", "ravioli", "bananas"}
    };
    // JComboBox<String> combo2 = new JComboBox<String>() {
    //   @Override public void updateUI() {
    //     setBorder(null);
    //     super.updateUI();
    //     if (isWindowsLnF()) {
    //       setBorder(BorderFactory.createCompoundBorder(
    //           getBorder(), BorderFactory.createEmptyBorder(0, 2, 0, 0)));
    //     }
    //   }
    //
    // };
    JComboBox<String> combo2 = new JComboBox<String>() {
      @Override public void updateUI() {
        super.updateUI();
        setEditable(true);
        setEditor(new BasicComboBoxEditor() {
          private Component editorComponent;

          @Override public Component getEditorComponent() {
            editorComponent = Optional.ofNullable(editorComponent).orElseGet(() -> {
              JTextComponent tc = (JTextComponent) super.getEditorComponent();
              return new JLayer<>(tc, new PlaceholderLayerUI<>("- Select type -"));
            });
            return editorComponent;
          }
        });
        Border b1 = UIManager.getLookAndFeelDefaults().getBorder("ComboBox.border");
        Border b2 = BorderFactory.createEmptyBorder(0, 2, 0, 0);
        setBorder(BorderFactory.createCompoundBorder(b1, b2));
      }
    };

    combo1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        int idx = ((JComboBox<?>) e.getItemSelectable()).getSelectedIndex();
        combo2.setModel(new DefaultComboBoxModel<>(arrays[idx]));
        combo2.setSelectedIndex(-1);
      }
    });

    JPanel p = new JPanel(new GridLayout(4, 1, 5, 5));
    setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
    p.add(new JLabel("Category"));
    p.add(combo1);
    p.add(new JLabel("Type"));
    p.add(combo2);

    JButton button = new JButton("clear");
    button.addActionListener(e -> {
      combo1.setSelectedIndex(-1);
      combo2.setModel(new DefaultComboBoxModel<>());
    });

    add(p, BorderLayout.NORTH);
    add(button, BorderLayout.SOUTH);
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

class PlaceholderLayerUI<E extends JTextComponent> extends LayerUI<E> {
  private final JLabel hint;

  protected PlaceholderLayerUI(String hintMessage) {
    super();
    this.hint = new JLabel(hintMessage) {
      @Override public void updateUI() {
        super.updateUI();
        String inactive = "TextField.inactiveForeground";
        setForeground(UIManager.getLookAndFeelDefaults().getColor(inactive));
      }
    };
  }

  @Override public void updateUI(JLayer<? extends E> l) {
    super.updateUI(l);
    SwingUtilities.updateComponentTreeUI(hint);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JTextComponent tc = (JTextComponent) ((JLayer<?>) c).getView();
      if (tc.getText().isEmpty() && !tc.hasFocus()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(hint.getForeground());
        // System.out.println("getInsets: " + tc.getInsets());
        // System.out.println("getMargin: " + tc.getMargin());
        // Insets i = tc.getMargin();
        Rectangle r = SwingUtilities.calculateInnerArea(tc, null);
        Dimension d = hint.getPreferredSize();
        int yy = (int) (r.y + (r.height - d.height) / 2d);
        SwingUtilities.paintComponent(g2, hint, tc, r.x, yy, d.width, d.height);
        g2.dispose();
      }
    }
  }

  @Override public void installUI(JComponent c) {
    super.installUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(AWTEvent.FOCUS_EVENT_MASK);
    }
  }

  @Override public void uninstallUI(JComponent c) {
    super.uninstallUI(c);
    if (c instanceof JLayer) {
      ((JLayer<?>) c).setLayerEventMask(0);
    }
  }

  @Override public void processFocusEvent(FocusEvent e, JLayer<? extends E> l) {
    l.getView().repaint();
  }
}
