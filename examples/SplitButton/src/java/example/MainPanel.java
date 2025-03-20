// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new FlowLayout(FlowLayout.LEADING));
    JComboBox<ComboItem> combo = new JComboBox<ComboItem>(makeModel()) {
      private transient PopupMenuListener listener;

      @Override public void updateUI() {
        removePopupMenuListener(listener);
        super.updateUI();
        setPrototypeDisplayValue(new ComboItem("*Create a merge commit*", ""));
        setRenderer(new CheckComboBoxRenderer<>(this));
        // ComboPopup popup = (ComboPopup) getAccessibleContext().getAccessibleChild(0);
        // JList<?> list = popup.getList();
        // list.setFixedCellHeight(-1);
        listener = new WidePopupMenuListener();
        addPopupMenuListener(listener);
      }
    };
    add(combo);
    setPreferredSize(new Dimension(320, 240));
  }

  private static ComboItem[] makeModel() {
    // https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/incorporating-changes-from-a-pull-request/merging-a-pull-request
    return new ComboItem[] {
        new ComboItem(
            "Create a merge commit",
            "All commits from this branch\nwill be added to the base branch\nvia a merge commit."
        ),
        new ComboItem(
            "Squash and merge",
            "The 1 commit from this branch\nwill be added to the base branch."
        ),
        new ComboItem(
            "Rebase and merge",
            "The 1 commit from this branch\nwill be rebased and added to the\nbase branch."
        )
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

class ComboItem {
  private final String title;
  private final String description;

  protected ComboItem(String title, String description) {
    this.title = title;
    this.description = description;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  @Override public String toString() {
    return title;
  }
}

final class EditorPanel extends JPanel {
  private final JCheckBox checkBox = new JCheckBox();
  private final JLabel label = new JLabel();
  private final JTextArea textArea = new JTextArea();

  /* default */ EditorPanel(ComboItem data) {
    super(new BorderLayout());
    setItem(data);
    checkBox.setOpaque(false);
    checkBox.setFocusable(false);
    Box box = Box.createVerticalBox();
    box.add(checkBox);
    box.add(Box.createVerticalGlue());
    add(box, BorderLayout.WEST);

    label.setFont(label.getFont().deriveFont(Font.BOLD, 14f));
    textArea.setBorder(BorderFactory.createEmptyBorder());
    textArea.setOpaque(false);
    textArea.setFont(textArea.getFont().deriveFont(12f));
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    p.setOpaque(false);
    p.add(label, BorderLayout.NORTH);
    p.add(textArea);
    add(p);
    setOpaque(false);
  }

  public void setSelected(boolean b) {
    checkBox.setSelected(b);
  }

  public void setItem(ComboItem item) {
    label.setText(item.getTitle());
    textArea.setText(item.getDescription());
  }
}

class CheckComboBoxRenderer<E extends ComboItem> implements ListCellRenderer<E> {
  private static final Color SELECTED_BGC = new Color(0xC0_E8_FF);
  private final EditorPanel renderer;
  private final JLabel label = new JLabel();
  private final JComboBox<ComboItem> combo;

  protected CheckComboBoxRenderer(JComboBox<ComboItem> combo) {
    this.combo = combo;
    ComboItem proto = Optional.ofNullable(combo.getPrototypeDisplayValue())
        .orElseGet(() -> new ComboItem("", ""));
    renderer = new EditorPanel(proto);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    Component c;
    if (index >= 0) {
      renderer.setItem(value);
      if (isSelected) {
        renderer.setSelected(true);
        renderer.setOpaque(true);
        renderer.setBackground(SELECTED_BGC);
      } else {
        renderer.setSelected(combo.getSelectedIndex() == index);
        renderer.setOpaque(false);
        renderer.setBackground(Color.WHITE);
      }
      c = renderer;
    } else {
      label.setOpaque(false);
      label.setText(Objects.toString(value, ""));
      c = label;
    }
    return c;
  }
}

class WidePopupMenuListener implements PopupMenuListener {
  private static final int POPUP_MIN_WIDTH = 260;
  private final AtomicBoolean adjusting = new AtomicBoolean();

  @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    JComboBox<?> combo = (JComboBox<?>) e.getSource();
    Dimension size = combo.getSize();
    if (size.width >= POPUP_MIN_WIDTH || adjusting.get()) {
      return;
    }
    adjusting.set(true);
    combo.setSize(POPUP_MIN_WIDTH, size.height);
    combo.showPopup();
    EventQueue.invokeLater(() -> {
      combo.setSize(size);
      adjusting.set(false);
    });
  }

  @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    /* not needed */
  }

  @Override public void popupMenuCanceled(PopupMenuEvent e) {
    /* not needed */
  }
}
