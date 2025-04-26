// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private static final Color BACKGROUND = new Color(70, 80, 90);
  private static final String[] MODEL = {
      "red", "pink", "orange", "yellow", "green", "magenta", "cyan", "blue"
  };

  private MainPanel() {
    super(new BorderLayout());
    UIManager.put("ComboBox.forceOpaque", Boolean.FALSE);
    // UIManager.put("ComboBox.rendererUseListColors", Boolean.TRUE);
    Insets ins = UIManager.getInsets("ComboBox.padding");
    ins.right = 0;
    UIManager.put("ComboBox.padding", ins);
    JPanel p = new JPanel(new GridLayout(0, 1));
    p.add(makeTitledPanel("ComboPopup", makeComboBox2()));
    p.add(makeTitledPanel("DefaultListCellRenderer", makeComboBox1()));
    p.add(makeTitledPanel("UIDefaults", makeComboBox0()));
    add(p, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static JComboBox<String> makeComboBox0() {
    JComboBox<String> comboBox = new JComboBox<>(MODEL);
    comboBox.setForeground(Color.WHITE);
    comboBox.setBackground(BACKGROUND);
    UIDefaults d1 = new UIDefaults();
    d1.put("ComboBox:\"ComboBox.listRenderer\".background", BACKGROUND);
    d1.put("ComboBox:\"ComboBox.listRenderer\".textForeground", Color.WHITE);
    d1.put("ComboBox:\"ComboBox.listRenderer\"[Selected].background", Color.LIGHT_GRAY);
    d1.put("ComboBox:\"ComboBox.listRenderer\"[Selected].textForeground", Color.BLACK);
    // d1.put("ComboBox:\"ComboBox.listRenderer\".opaque", Boolean.TRUE);
    ListCellRenderer<? super String> renderer = comboBox.getRenderer();
    if (renderer instanceof JComponent) {
      putClientProperty((JComponent) renderer, d1);
    }

    // UIDefaults d2 = new UIDefaults();
    // d2.put("ComboBox.foreground", Color.WHITE);
    // d2.put("ComboBox:\"ComboBox.renderer\"[Selected].background", Color.RED);
    // d2.put("ComboBox:\"ComboBox.renderer\"[Selected].textForeground", Color.BLUE);
    // putClientProperty(comboBox, d2);

    // UIDefaults d3 = new UIDefaults();
    // d3.put(
    //     "ComboBox:\"ComboBox.textField\"[Enabled].backgroundPainter",
    //     (Painter<JComponent>) (g, c, w, h) -> {
    //       g.setPaint(Color.RED);
    //       g.fillRect(0, 0, w, h);
    //     });
    // d3.put(
    //     "ComboBox:\"ComboBox.textField\"[Selected].backgroundPainter",
    //     (Painter<JComponent>) (g, c, w, h) -> {
    //       g.setPaint(Color.CYAN);
    //       g.fillRect(0, 0, w, h);
    //     });
    // d3.put("ComboBox:\"ComboBox.textField\"[Selected].textForeground", Color.BLUE);
    // comboBox0.setEditable(true);
    // Component editor = comboBox0.getEditor().getEditorComponent();
    // if (editor instanceof JComponent) {
    //   putClientProperty((JComponent) editor, d3);
    // }
    return comboBox;
  }

  private static JComboBox<String> makeComboBox1() {
    return new JComboBox<String>(MODEL) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        setRenderer(new ComboRenderer());
        setBackground(BACKGROUND);
      }
    };
  }

  private static JComboBox<String> makeComboBox2() {
    return new JComboBox<String>(MODEL) {
      @Override public void updateUI() {
        setRenderer(null);
        super.updateUI();
        ListCellRenderer<? super String> renderer = getRenderer();
        setRenderer((list, value, index, isSelected, cellHasFocus) -> {
          Component c = renderer.getListCellRendererComponent(
              list, value, index, isSelected, cellHasFocus);
          ((JComponent) c).setOpaque(true); // setOpaque(index < 0) // WindowsLaf
          return c;
        });
        ComboPopup popup = (ComboPopup) getAccessibleContext().getAccessibleChild(0);
        JList<?> list = popup.getList();
        list.setBackground(BACKGROUND);
        list.setForeground(Color.WHITE);
        list.setSelectionBackground(Color.LIGHT_GRAY);
        list.setSelectionForeground(Color.BLACK);
        setBackground(BACKGROUND);
      }
    };
  }

  private static void putClientProperty(JComponent c, UIDefaults d) {
    c.putClientProperty("Nimbus.Overrides", d);
    c.putClientProperty("Nimbus.Overrides.InheritDefaults", Boolean.TRUE);
  }

  private static Component makeTitledPanel(String title, Component cmp) {
    JPanel p = new JPanel(new GridBagLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    GridBagConstraints c = new GridBagConstraints();
    c.weightx = 1d;
    c.fill = GridBagConstraints.HORIZONTAL;
    c.insets = new Insets(5, 5, 5, 5);
    p.add(cmp, c);
    return p;
  }

  private static final class ComboRenderer extends DefaultListCellRenderer {
    @Override public Component getListCellRendererComponent(
        JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
      list.setBackground(BACKGROUND);
      list.setForeground(Color.WHITE);
      list.setSelectionBackground(Color.LIGHT_GRAY);
      list.setSelectionForeground(Color.BLACK);
      return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
  }

  // private static class ComboRenderer2 extends BasicComboBoxRenderer {
  //   @Override public Component getListCellRendererComponent(
  //       JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
  //     Component c = super.getListCellRendererComponent(
  //       list, value, index, isSelected, cellHasFocus);
  //     list.setBackground(BACKGROUND);
  //     list.setForeground(Color.WHITE);
  //     list.setSelectionBackground(Color.LIGHT_GRAY);
  //     list.setSelectionForeground(Color.BLACK);
  //     return c;
  //   }
  // }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
      UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
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
