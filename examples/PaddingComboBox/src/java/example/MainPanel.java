// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.Border;

public final class MainPanel extends JPanel {
  private final JPanel panel = new JPanel();
  private final JCheckBox check = new JCheckBox("color");

  private MainPanel() {
    super(new BorderLayout());
    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    check.addActionListener(e -> {
      layoutComboBoxPanel(panel, initComboBoxes(check.isSelected()));
      panel.revalidate();
    });
    layoutComboBoxPanel(panel, initComboBoxes(check.isSelected()));

    Box box = Box.createHorizontalBox();
    box.add(check);
    box.add(Box.createHorizontalGlue());

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(panel);
    add(box, BorderLayout.SOUTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private void layoutComboBoxPanel(JPanel p2, List<JComboBox<?>> list) {
    p2.removeAll();
    p2.setLayout(new GridBagLayout());
    Border inside = BorderFactory.createEmptyBorder(10, 5 + 2, 10, 10 + 2);
    Border outside = BorderFactory.createTitledBorder("JComboBox Padding Test");
    p2.setBorder(BorderFactory.createCompoundBorder(outside, inside));
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(5, 5, 5, 0);
    c.anchor = GridBagConstraints.LINE_END;
    for (int i = 0; i < list.size(); i++) {
      c.gridx = 0;
      c.weightx = 0d;
      c.fill = GridBagConstraints.NONE;
      p2.add(makeLabel(i), c);
      c.gridx = 1;
      c.weightx = 1d;
      c.fill = GridBagConstraints.HORIZONTAL;
      p2.add(list.get(i), c);
    }
    p2.revalidate(); // ??? JDK 1.7.0 Nimbus ???
  }

  private static Component makeLabel(int num) {
    return new JLabel(String.format("%d:", num));
  }

  private List<JComboBox<?>> initComboBoxes(boolean isColor) {
    // if (uiCheck.isSelected()) {
    //   // [JDK-7158712] Synth Property "ComboBox.popupInsets" is ignored - Java Bug System
    //   // https://bugs.openjdk.org/browse/JDK-7158712
    //   UIManager.put("ComboBox.padding", new InsetsUIResource(1, 15, 1, 1));
    // }
    List<JComboBox<?>> list = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      list.add(makeComboBox());
    }

    // ---- 0 ----
    JComboBox<?> cb0 = list.get(0);
    cb0.setEditable(false);
    cb0.setToolTipText("combo.setEditable(false)");

    // ---- 1 ----
    JComboBox<?> cb1 = list.get(1);
    cb1.setEditable(true);
    JTextField ed1 = (JTextField) cb1.getEditor().getEditorComponent();
    ed1.setBorder(BorderFactory.createCompoundBorder(ed1.getBorder(), getPaddingBorder(isColor)));
    cb1.setToolTipText("ed.setBorder(BorderFactory.createCompoundBorder(ed.getBorder(), pad))");

    // ---- 2 ----
    JComboBox<?> cb2 = list.get(2);
    cb2.setEditable(true);
    JTextField ed2 = (JTextField) cb2.getEditor().getEditorComponent();
    ed2.setBorder(getPaddingBorder(isColor));
    cb2.setToolTipText("editor.setBorder(padding);");

    // ---- 3 ----
    JComboBox<?> cb3 = list.get(3);
    cb3.setEditable(true);
    JTextField ed3 = (JTextField) cb3.getEditor().getEditorComponent();
    Insets i = ed3.getInsets();
    ed3.setMargin(new Insets(i.top, i.left + 5, i.bottom, i.right));
    cb3.setToolTipText("var i = ed.getInsets(); ed.setMargin(new Insets(i.left + 5, ...))");

    // ---- 4 ----
    JComboBox<?> cb4 = list.get(4);
    cb4.setEditable(true);
    JTextField ed4 = (JTextField) cb4.getEditor().getEditorComponent();
    Insets m = ed4.getMargin();
    ed4.setMargin(new Insets(m.top, m.left + 5, m.bottom, m.right));
    cb4.setToolTipText("Insets m = ed.getMargin(); ed.setMargin(new Insets(m.left + 5, ...))");

    // ---- 5 ----
    JComboBox<?> cb5 = list.get(5);
    cb5.setEditable(true);
    cb5.setBorder(BorderFactory.createCompoundBorder(cb5.getBorder(), getPaddingBorder(isColor)));
    cb5.setToolTipText("cb.setBorder(BorderFactory.createCompoundBorder(cb.getBorder(), pad))");

    // ---- 6 ----
    JComboBox<?> cb6 = list.get(6);
    cb6.setEditable(true);
    cb6.setBorder(BorderFactory.createCompoundBorder(getPaddingBorder(isColor), cb6.getBorder()));
    cb6.setToolTipText("cb.setBorder(BorderFactory.createCompoundBorder(pad, cb.getBorder()))");

    if (isColor) {
      Color c = new Color(.8f, 1f, .8f);
      for (JComboBox<?> cb : list) {
        cb.setOpaque(true);
        cb.setBackground(c);
        JTextField editor = (JTextField) cb.getEditor().getEditorComponent();
        editor.setOpaque(true);
        editor.setBackground(c);
      }
    }
    return list;
  }

  public static Border getPaddingBorder(boolean isColor) {
    Border b;
    if (isColor) {
      b = BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(1f, .8f, .8f, .5f));
    } else {
      b = BorderFactory.createEmptyBorder(0, 5, 0, 0);
    }
    return b;
  }

  private static JComboBox<String> makeComboBox() {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    model.addElement("11111111111111111111111111111");
    model.addElement("222222222222");
    model.addElement("3333333333");
    model.addElement("444444");
    model.addElement("555");

    return new JComboBox<String>(model) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> r = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = r.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          if (c instanceof JComponent) {
            ((JComponent) c).setBorder(getPaddingBorder(false));
          }
          return c;
        });
        // UIManager.put("ComboBox.editorBorder", BorderFactory.createEmptyBorder(0, 5, 0, 0));
        // ((JComponent) r).setBorder(getPaddingBorder(false));
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

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
