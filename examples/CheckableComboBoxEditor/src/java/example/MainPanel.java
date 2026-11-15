// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JComboBox<ComboItem> combo0 = new JComboBox<ComboItem>(createModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setRenderer(new CheckComboBoxRenderer<>());
      }
    };

    JComboBox<ComboItem> combo1 = new JComboBox<ComboItem>(createModel()) {
      @Override public void updateUI() {
        super.updateUI();
        setEditable(true);
        setRenderer(new CheckComboBoxRenderer<>());
        setEditor(new CheckComboBoxEditor());
      }
    };

    Box box = Box.createVerticalBox();
    box.add(createTitledPanel("setEditable(false), setRenderer(...)", combo0));
    box.add(Box.createVerticalStrut(5));
    box.add(createTitledPanel("setEditable(true), setRenderer(...), setEditor(...)", combo1));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboItem[] createModel() {
    return new ComboItem[] {
        new ComboItem(true, true, "00000"),
        new ComboItem(true, false, "11111"),
        new ComboItem(false, true, "22222"),
        new ComboItem(false, false, "33333"),
    };
  }

  private static Component createTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
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

class ComboItem implements Serializable {
  private static final long serialVersionUID = 1L;
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

  public void setEnabled(boolean isEnabled) {
    enabled = isEnabled;
  }

  public boolean isEditable() {
    return editable;
  }

  public void setEditable(boolean isEditable) {
    editable = isEditable;
  }

  public String getText() {
    return text;
  }

  public void setText(String newText) {
    this.text = newText;
  }

  @Override public int hashCode() {
    return Objects.hash(text);
  }

  @Override public boolean equals(Object o) {
    boolean b1 = this == o;
    boolean b2 = o instanceof ComboItem && Objects.equals(((ComboItem) o).getText(), text);
    return b1 || b2;
  }

  @Override public String toString() {
    return String.format("%s: %b, %b", text, enabled, editable);
  }
}

class CheckComboBoxRenderer<E extends ComboItem> implements ListCellRenderer<E> {
  private static final Color SELECTION_BGC = new Color(0x64_C8_FF);
  private final EditorPanel renderer = new EditorPanel(new ComboItem());

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    renderer.setItem(value);
    // index == -1: the cell is painted in the combo box itself, not in the drop-down list
    boolean highlight = isSelected && index >= 0;
    renderer.setOpaque(highlight);
    renderer.setBackground(highlight ? SELECTION_BGC : list.getBackground());
    return renderer;
  }
}

class CheckComboBoxEditor implements ComboBoxEditor {
  private final EditorPanel editorPanel = new EditorPanel(new ComboItem());

  @Override public void selectAll() {
    editorPanel.selectAll();
  }

  @Override public Object getItem() {
    return editorPanel.getItem();
  }

  @Override public void setItem(Object anObject) {
    // The selected index is checked later because the editor might not
    // have been added to the JComboBox yet when this method is called
    EventQueue.invokeLater(this::updateEditingIndex);
    editorPanel.setItem(anObject instanceof ComboItem ? (ComboItem) anObject : new ComboItem());
  }

  private void updateEditingIndex() {
    Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, getEditorComponent());
    if (c instanceof JComboBox) {
      // Keep the last valid index, since getSelectedIndex() returns -1
      // while the text is being edited
      int idx = ((JComboBox<?>) c).getSelectedIndex();
      if (idx >= 0) {
        editorPanel.setEditingIndex(idx);
      }
    }
  }

  @Override public Component getEditorComponent() {
    return editorPanel;
  }

  @Override public void addActionListener(ActionListener l) {
    editorPanel.addActionListener(l);
  }

  @Override public void removeActionListener(ActionListener l) {
    editorPanel.removeActionListener(l);
  }
}

final class EditorPanel extends JPanel {
  private final JCheckBox enabledCheck = new JCheckBox();
  private final JCheckBox editableCheck = new JCheckBox();
  private final JTextField textField = new JTextField("", 16);
  private final transient ComboItem data;
  private int editingIndex = -1;

  /* default */ EditorPanel(ComboItem data) {
    super();
    this.data = data;
    setItem(data);

    enabledCheck.addActionListener(e -> updateEditingItem(
        item -> item.setEnabled(enabledCheck.isSelected())));
    enabledCheck.setOpaque(false);
    enabledCheck.setFocusable(false);

    editableCheck.addActionListener(e -> updateEditingItem(
        item -> item.setEditable(editableCheck.isSelected())));
    editableCheck.setOpaque(false);
    editableCheck.setFocusable(false);

    textField.addActionListener(e -> updateEditingItem(
        item -> item.setText(textField.getText())));
    textField.setBorder(BorderFactory.createEmptyBorder());
    textField.setOpaque(false);

    setOpaque(false);
    setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

    add(enabledCheck);
    add(editableCheck);
    add(textField);
  }

  // Applies the change to the model item being edited and reselects it
  private void updateEditingItem(Consumer<ComboItem> updater) {
    Container c = SwingUtilities.getAncestorOfClass(JComboBox.class, this);
    if (c instanceof JComboBox) {
      JComboBox<?> combo = (JComboBox<?>) c;
      // getItemAt(...) returns null if the editing index is out of range
      Object o = combo.getItemAt(editingIndex);
      if (o instanceof ComboItem) {
        ComboItem item = (ComboItem) o;
        updater.accept(item);
        setItem(item);
        combo.setSelectedIndex(editingIndex);
      }
    }
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
