// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JComboBox<ComboItem> combo0 = new JComboBox<ComboItem>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setRenderer(new CheckComboBoxRenderer<>());
      }
    };

    JComboBox<ComboItem> combo1 = new JComboBox<ComboItem>(makeModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setEditable(true);
        setRenderer(new CheckComboBoxRenderer<>());
        setEditor(new CheckComboBoxEditor());
      }
    };

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("setEditable(false), setRenderer(...)", combo0));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("setEditable(true), setRenderer(...), setEditor(...)", combo1));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboItem[] makeModel() {
    return new ComboItem[] {
      new ComboItem(true, true, "00000"),
      new ComboItem(true, false, "11111"),
      new ComboItem(false, true, "22222"),
      new ComboItem(false, false, "33333")
    };
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class ComboItem {
  private boolean enabled;
  private boolean editable;
  private String text;

  protected ComboItem() {
    this(false, false, "");
  }

  protected ComboItem(boolean enabled, boolean editable, String text) {
    this.enabled = enabled;
    this.editable = editable;
    this.text = text;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean editable) {
    this.editable = editable;
  }

  public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }
}

class CheckComboBoxRenderer<E extends ComboItem> implements ListCellRenderer<E> {
  private static final Color SBGC = new Color(100, 200, 255);
  private final EditorPanel renderer = new EditorPanel(new ComboItem());

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    renderer.setItem(value);
    if (isSelected && index >= 0) {
      renderer.setOpaque(true);
      renderer.setBackground(SBGC);
    } else {
      renderer.setOpaque(false);
      renderer.setBackground(Color.WHITE);
    }
    return renderer;
  }
}

class CheckComboBoxEditor implements ComboBoxEditor {
  private final EditorPanel editor = new EditorPanel(new ComboItem());

  @Override public void selectAll() {
    editor.selectAll();
  }

  @Override public Object getItem() {
    return editor.getItem();
  }

  @Override public void setItem(Object anObject) {
    EventQueue.invokeLater(() -> {
      Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, getEditorComponent());
      if (c instanceof JComboBox) {
        JComboBox<?> combo = (JComboBox<?>) c;
        int idx = combo.getSelectedIndex();
        if (idx >= 0 && idx != editor.getEditingIndex()) {
          System.out.println("setItem: " + idx);
          editor.setEditingIndex(idx);
        }
      }
    });
    if (anObject instanceof ComboItem) {
      editor.setItem((ComboItem) anObject);
    } else {
      editor.setItem(new ComboItem());
    }
  }

  @Override public Component getEditorComponent() {
    return editor;
  }

  @Override public void addActionListener(ActionListener l) {
    // System.out.println("addActionListener: " + l.getClass().getName());
    editor.addActionListener(l);
  }

  @Override public void removeActionListener(ActionListener l) {
    // System.out.println("removeActionListener: " + l.getClass().getName());
    editor.removeActionListener(l);
  }
}

final class EditorPanel extends JPanel {
  private final JCheckBox enabledCheck = new JCheckBox();
  private final JCheckBox editableCheck = new JCheckBox();
  private final JTextField textField = new JTextField("", 16);
  private final transient ComboItem data;
  private int editingIndex = -1;

  protected EditorPanel(ComboItem data) {
    super();
    this.data = data;
    setItem(data);

    enabledCheck.addActionListener(e -> {
      Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, this);
      if (c instanceof JComboBox) {
        JComboBox<?> combo = (JComboBox<?>) c;
        ComboItem item = (ComboItem) combo.getItemAt(editingIndex);
        item.setEnabled(((JCheckBox) e.getSource()).isSelected());
        editableCheck.setEnabled(item.isEnabled());
        textField.setEnabled(item.isEnabled());
        combo.setSelectedIndex(editingIndex);
      }
    });
    enabledCheck.setOpaque(false);
    enabledCheck.setFocusable(false);

    editableCheck.addActionListener(e -> {
      Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, this);
      if (c instanceof JComboBox) {
        JComboBox<?> combo = (JComboBox<?>) c;
        ComboItem item = (ComboItem) combo.getItemAt(editingIndex);
        item.setEditable(((JCheckBox) e.getSource()).isSelected());
        textField.setEditable(item.isEditable());
        combo.setSelectedIndex(editingIndex);
      }
    });
    editableCheck.setOpaque(false);
    editableCheck.setFocusable(false);

    textField.addActionListener(e -> {
      Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, this);
      if (c instanceof JComboBox) {
        JComboBox<?> combo = (JComboBox<?>) c;
        ComboItem item = (ComboItem) combo.getItemAt(editingIndex);
        item.setText(((JTextField) e.getSource()).getText());
        combo.setSelectedIndex(editingIndex);
      }
    });
    textField.setBorder(BorderFactory.createEmptyBorder());
    textField.setOpaque(false);

    setOpaque(false);
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    add(enabledCheck);
    add(editableCheck);
    add(textField);
  }

  public int getEditingIndex() {
    return editingIndex;
  }

  public void setEditingIndex(int idx) {
    this.editingIndex = idx;
  }

  public ComboItem getItem() {
    data.setEnabled(enabledCheck.isSelected());
    data.setEditable(editableCheck.isSelected());
    data.setText(textField.getText());
    return data;
  }

  public void setItem(ComboItem item) {
    enabledCheck.setSelected(item.isEnabled());

    editableCheck.setSelected(item.isEditable());
    editableCheck.setEnabled(item.isEnabled());

    textField.setText(item.getText());
    textField.setEnabled(item.isEnabled());
    textField.setEditable(item.isEditable());
  }

  public void selectAll() {
    textField.requestFocusInWindow();
    textField.selectAll();
  }

  public void addActionListener(ActionListener l) {
    textField.addActionListener(l);
    enabledCheck.addActionListener(l);
    editableCheck.addActionListener(l);
  }

  public void removeActionListener(ActionListener l) {
    textField.removeActionListener(l);
    enabledCheck.removeActionListener(l);
    editableCheck.removeActionListener(l);
  }
}
