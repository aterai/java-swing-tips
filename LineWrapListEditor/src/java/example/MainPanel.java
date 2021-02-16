// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    DefaultListModel<ListItem> model = new DefaultListModel<>();
    model.addElement(new ListItem("red", new ColorIcon(Color.RED)));
    model.addElement(new ListItem("green", new ColorIcon(Color.GREEN)));
    model.addElement(new ListItem("blue", new ColorIcon(Color.BLUE)));
    model.addElement(new ListItem("cyan", new ColorIcon(Color.CYAN)));
    model.addElement(new ListItem("darkGray", new ColorIcon(Color.DARK_GRAY)));
    model.addElement(new ListItem("gray", new ColorIcon(Color.GRAY)));
    model.addElement(new ListItem("lightGray", new ColorIcon(Color.LIGHT_GRAY)));
    model.addElement(new ListItem("magenta", new ColorIcon(Color.MAGENTA)));
    model.addElement(new ListItem("orange", new ColorIcon(Color.ORANGE)));
    model.addElement(new ListItem("pink", new ColorIcon(Color.PINK)));
    model.addElement(new ListItem("yellow", new ColorIcon(Color.YELLOW)));
    model.addElement(new ListItem("black", new ColorIcon(Color.BLACK)));
    model.addElement(new ListItem("white", new ColorIcon(Color.WHITE)));

    JList<ListItem> list = new EditableList<>(model);
    add(new JScrollPane(list));
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

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  protected static final Color SELECTED_COLOR = new Color(0xAE_16_64_FF, true);
  private final JLabel label = new JLabel("", null, SwingConstants.CENTER) {
    @Override protected void paintComponent(Graphics g) {
      super.paintComponent(g);
      if (SELECTED_COLOR.equals(getBackground())) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(SELECTED_COLOR);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
      }
    }
  };
  private final JPanel renderer = new JPanel(new BorderLayout());
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    label.setVerticalTextPosition(SwingConstants.BOTTOM);
    label.setHorizontalTextPosition(SwingConstants.CENTER);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(noFocusBorder);
    label.setOpaque(false);
    renderer.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    renderer.add(label);
    renderer.setOpaque(true);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    label.setText(value.title);
    label.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
    label.setIcon(value.icon);
    if (isSelected) {
      label.setForeground(list.getSelectionForeground());
      renderer.setBackground(SELECTED_COLOR);
    } else {
      label.setForeground(list.getForeground());
      renderer.setBackground(list.getBackground());
    }
    return renderer;
  }
}

class ListItem {
  public final Icon icon;
  public final String title;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }
}

class ColorIcon implements Icon {
  private final Color color;

  protected ColorIcon(Color color) {
    this.color = color;
  }

  @Override public void paintIcon(Component c, Graphics g, int x, int y) {
    Graphics2D g2 = (Graphics2D) g.create();
    g2.translate(x, y);
    g2.setPaint(color);
    g2.fillRect(0, 0, getIconWidth(), getIconHeight());
    g2.setPaint(Color.BLACK);
    g2.drawRect(0, 0, getIconWidth(), getIconHeight());
    g2.dispose();
  }

  @Override public int getIconWidth() {
    return 32;
  }

  @Override public int getIconHeight() {
    return 32;
  }
}

class EditableList<E extends ListItem> extends JList<E> {
  public static final String RENAME = "rename-title";
  public static final String CANCEL = "cancel-editing";
  public static final String EDITING = "start-editing";
  protected int editingIndex = -1;
  // protected final Container glassPane = new EditorGlassPane();
  // protected final JPopupMenu popup = new JPopupMenu();
  protected final JFrame window = new JFrame();
  protected final JTextArea editor = new JTextArea();
  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      // getRootPane().setGlassPane(glassPane);
      int idx = getSelectedIndex();
      editingIndex = idx;
      Rectangle rect = getCellBounds(idx, idx);
      // Point p = SwingUtilities.convertPoint(EditableList.this, rect.getLocation(), glassPane);
      // rect.setLocation(p);
      editor.setText(getSelectedValue().title);
      // int h = editor.getPreferredSize().height;
      int rowHeight = editor.getFontMetrics(editor.getFont()).getHeight();
      rect.y += rect.height - rowHeight - 2 - 2 - 1;
      // rect.grow(-2, 0);
      rect.height = editor.getPreferredSize().height;
      editor.setBounds(rect);
      editor.selectAll();
      // glassPane.add(editor);
      // glassPane.setVisible(true);
      // popup.show(EditableList.this, rect.x, rect.y);
      Point p = new Point(rect.getLocation());
      SwingUtilities.convertPointToScreen(p, EditableList.this);
      window.setLocation(p);
      window.pack();
      window.setVisible(true);
      editor.requestFocusInWindow();
    }
  };
  protected final Action cancelEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      // glassPane.setVisible(false);
      // popup.setVisible(false);
      window.setVisible(false);
      editingIndex = -1;
    }
  };
  protected final Action renameTitle = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      ListModel<E> m = getModel();
      String title = editor.getText().trim();
      int index = editingIndex; // getSelectedIndex();
      if (!title.isEmpty() && index >= 0 && m instanceof DefaultListModel<?>) {
        @SuppressWarnings("unchecked")
        DefaultListModel<ListItem> model = (DefaultListModel<ListItem>) getModel();
        ListItem item = m.getElementAt(index);
        model.remove(index);
        model.add(index, new ListItem(editor.getText().trim(), item.icon));
        EventQueue.invokeLater(() -> setSelectedIndex(index));
      }
      // glassPane.setVisible(false);
      // popup.setVisible(false);
      window.setVisible(false);
      editingIndex = -1;
    }
  };

  protected EditableList(DefaultListModel<E> model) {
    super(model);
    // popup.addPopupMenuListener(new PopupMenuListener() {
    //   @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
    //     /* not needed */
    //   }
    //
    //   @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
    //     /* not needed */
    //   }
    //
    //   @Override public void popupMenuCanceled(PopupMenuEvent e) {
    //     renameTitle.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, ""));
    //   }
    // });
    // popup.setBorder(BorderFactory.createEmptyBorder());
    // popup.add(editor);
    window.setUndecorated(true);
    window.setAlwaysOnTop(true);
    window.addWindowListener(new WindowAdapter() {
      @Override public void windowDeactivated(WindowEvent e) {
        if (editingIndex >= 0) {
          renameTitle.actionPerformed(new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, ""));
        }
        editingIndex = -1;
      }
    });
    window.add(editor);
    editor.setBorder(BorderFactory.createLineBorder(Color.BLACK));
    // editor.setHorizontalAlignment(SwingConstants.CENTER);
    editor.setLineWrap(true);
    editor.setFont(UIManager.getFont("TextField.font"));
    editor.setComponentPopupMenu(new TextComponentPopupMenu());
    editor.getDocument().addDocumentListener(new DocumentListener() {
      private int prev = -1;
      private void update() {
        EventQueue.invokeLater(() -> {
          int h = editor.getPreferredSize().height;
          if (prev != h) {
            Rectangle rect = editor.getBounds();
            rect.height = h;
            editor.setBounds(rect);
            // popup.pack();
            window.pack();
            editor.requestFocusInWindow();
          }
          prev = h;
        });
      }

      @Override public void insertUpdate(DocumentEvent e) {
        update();
      }

      @Override public void removeUpdate(DocumentEvent e) {
        update();
      }

      @Override public void changedUpdate(DocumentEvent e) {
        update();
      }
    });

    InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), RENAME);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), RENAME);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL);

    ActionMap am = editor.getActionMap();
    am.put(RENAME, renameTitle);
    am.put(CANCEL, cancelEditing);

    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        int idx = getSelectedIndex();
        Rectangle rect = getCellBounds(idx, idx);
        if (rect == null) {
          return;
        }
        int h = editor.getPreferredSize().height;
        rect.y = rect.y + rect.height - h - 2 - 2 - 1;
        rect.height = h;
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick && rect.contains(e.getPoint())) {
          startEditing.actionPerformed(new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, ""));
        }
      }
    });
    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), EDITING);
    getActionMap().put(EDITING, startEditing);
  }

  @Override public void updateUI() {
    setSelectionForeground(null);
    setSelectionBackground(null);
    setCellRenderer(null);
    super.updateUI();
    setLayoutOrientation(JList.HORIZONTAL_WRAP);
    getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setVisibleRowCount(0);
    setFixedCellWidth(64);
    setFixedCellHeight(56);
    setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
    setCellRenderer(new ListItemListCellRenderer<>());
  }

  // protected JTextComponent getEditorTextField() {
  //   return editor;
  // }
  //
  // private class EditorGlassPane extends JComponent {
  //   protected EditorGlassPane() {
  //     super();
  //     setOpaque(false);
  //     setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
  //       @Override public boolean accept(Component c) {
  //         return Objects.equals(c, getEditorTextField());
  //       }
  //     });
  //     addMouseListener(new MouseAdapter() {
  //       @Override public void mouseClicked(MouseEvent e) {
  //         if (!getEditorTextField().getBounds().contains(e.getPoint())) {
  //           renameTitle.actionPerformed(new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, ""));
  //         }
  //       }
  //     });
  //   }
  //
  //   @Override public void setVisible(boolean flag) {
  //     super.setVisible(flag);
  //     setFocusTraversalPolicyProvider(flag);
  //     setFocusCycleRoot(flag);
  //   }
  // }
}

class TextComponentPopupMenu extends JPopupMenu {
  protected TextComponentPopupMenu() {
    super();
    add(new DefaultEditorKit.CutAction());
    add(new DefaultEditorKit.CopyAction());
    add(new DefaultEditorKit.PasteAction());
    add("delete").addActionListener(e -> {
      Component c = getInvoker();
      if (c instanceof JTextComponent) {
        ((JTextComponent) c).replaceSelection(null);
      }
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      for (MenuElement menuElement : getSubElements()) {
        Component m = menuElement.getComponent();
        if (m instanceof JMenuItem && ((JMenuItem) m).getAction() instanceof DefaultEditorKit.PasteAction) {
          continue;
        }
        m.setEnabled(hasSelectedText);
      }
      super.show(c, x, y);
    }
  }
}
