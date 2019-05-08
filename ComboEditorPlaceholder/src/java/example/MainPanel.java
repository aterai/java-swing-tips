// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.Optional;
import javax.swing.*;
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
    //   private boolean isWindowsLnF() {
    //     return getUI().getClass().getName().contains("WindowsComboBoxUI");
    //   }
    // };
    JComboBox<String> combo2 = new JComboBox<>();
    combo2.setEditable(true);

    combo1.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        int idx = ((JComboBox<?>) e.getItemSelectable()).getSelectedIndex();
        combo2.setModel(new DefaultComboBoxModel<>(arrays[idx]));
        combo2.setSelectedIndex(-1);
      }
    });

    combo2.setEditor(new BasicComboBoxEditor() {
      private Component editorComponent;

      @Override public Component getEditorComponent() {
        editorComponent = Optional.ofNullable(editorComponent)
            .orElseGet(() -> {
              JTextComponent tc = (JTextComponent) super.getEditorComponent();
              return new JLayer<>(tc, new PlaceholderLayerUI<>("- Select type -"));
            });
        return editorComponent;
      }
    });
    combo2.setBorder(BorderFactory.createCompoundBorder(
        combo2.getBorder(), BorderFactory.createEmptyBorder(0, 2, 0, 0)));

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

  public static void main(String... args) {
    EventQueue.invokeLater(new Runnable() {
      @Override public void run() {
        createAndShowGui();
      }
    });
  }

  public static void createAndShowGui() {
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

class PlaceholderLayerUI<E extends JTextComponent> extends LayerUI<E> {
  private static final Color INACTIVE = UIManager.getColor("TextField.inactiveForeground");
  private final JLabel hint;

  public PlaceholderLayerUI(String hintMessage) {
    super();
    this.hint = new JLabel(hintMessage);
    hint.setForeground(INACTIVE);
  }

  @Override public void paint(Graphics g, JComponent c) {
    super.paint(g, c);
    if (c instanceof JLayer) {
      JTextComponent tc = (JTextComponent) ((JLayer<?>) c).getView();
      if (tc.getText().length() == 0 && !tc.hasFocus()) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(INACTIVE);
        // System.out.println("getInsets: " + tc.getInsets());
        // System.out.println("getMargin: " + tc.getMargin());
        Insets i = tc.getMargin();
        Dimension d = hint.getPreferredSize();
        SwingUtilities.paintComponent(g2, hint, tc, i.left, i.top, d.width, d.height);
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
