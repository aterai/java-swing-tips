// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BoxView;
import javax.swing.text.ComponentView;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.Element;
import javax.swing.text.IconView;
import javax.swing.text.JTextComponent;
import javax.swing.text.LabelView;
import javax.swing.text.ParagraphView;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JList<ListItem> list = new EditableList<>(makeModel());
    add(new JScrollPane(list));
    setPreferredSize(new Dimension(320, 240));
  }

  private static ListModel<ListItem> makeModel() {
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

class ListItemListCellRenderer<E extends ListItem> implements ListCellRenderer<E> {
  protected static final Color SELECTED_COLOR = new Color(0xAE_16_64_FF, true);
  private final JLabel icon = new JLabel(null, null, SwingConstants.CENTER);
  private final JLabel label = new JLabel(" ", SwingConstants.CENTER);
  private final JPanel renderer = new JPanel(new BorderLayout()) {
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
  private final Border focusBorder = UIManager.getBorder("List.focusCellHighlightBorder");
  private final Border noFocusBorder; // = UIManager.getBorder("List.noFocusBorder");

  protected ListItemListCellRenderer() {
    Border b = UIManager.getBorder("List.noFocusBorder");
    if (Objects.isNull(b)) { // Nimbus???
      Insets i = focusBorder.getBorderInsets(renderer);
      b = BorderFactory.createEmptyBorder(i.top, i.left, i.bottom, i.right);
    }
    noFocusBorder = b;
    renderer.setBorder(noFocusBorder);
    renderer.setOpaque(true);
    label.setForeground(renderer.getForeground());
    label.setBackground(renderer.getBackground());
    label.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
    label.setOpaque(false);
    icon.setOpaque(false);
    renderer.add(icon);
    renderer.add(label, BorderLayout.SOUTH);
  }

  @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
    icon.setIcon(value.getIcon());
    label.setText(value.getTitle());
    renderer.setBorder(cellHasFocus ? focusBorder : noFocusBorder);
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
  private String title;
  private final Icon icon;

  protected ListItem(String title, Icon icon) {
    this.title = title;
    this.icon = icon;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Icon getIcon() {
    return icon;
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
  private int editingIndex = -1;
  private int editorWidth = -1;
  private transient MouseAdapter handler;
  private Window window; // HeavyWeightEditor
  private final JTextPane editor = new JTextPane() {
    @Override public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      d.width = editorWidth;
      return d;
    }
  };
  private final Action startEditing = new StartEditingAction();
  private final Action renameTitle = new RenameAction();

  protected EditableList(ListModel<E> model) {
    super(model);
    initEditor();
    initAction();
    EventQueue.invokeLater(this::initHandler);
  }

  private void initAction() {
    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    InputMap im = editor.getInputMap(WHEN_FOCUSED);
    im.put(enterKey, RENAME);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), RENAME);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL);

    // ActionMap am = editor.getActionMap();
    editor.getActionMap().put(RENAME, renameTitle);
    // glassPane.setVisible(false);
    Action cancelEditing = new AbstractAction() {
      @Override public void actionPerformed(ActionEvent e) {
        // glassPane.setVisible(false);
        window.setVisible(false);
        editingIndex = -1;
      }
    };
    editor.getActionMap().put(CANCEL, cancelEditing);

    getInputMap(WHEN_FOCUSED).put(enterKey, EDITING);
    getActionMap().put(EDITING, startEditing);
  }

  private void initEditor() {
    editor.setBorder(BorderFactory.createLineBorder(Color.GRAY));
    editor.setEditorKit(new WrapEditorKit());
    editor.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    editor.setFont(UIManager.getFont("TextField.font"));
    // editor.setHorizontalAlignment(SwingConstants.CENTER); // JTextField
    // editor.setLineWrap(true); // JTextArea
    StyledDocument doc = editor.getStyledDocument();
    SimpleAttributeSet center = new SimpleAttributeSet();
    StyleConstants.setAlignment(center, StyleConstants.ALIGN_CENTER);
    doc.setParagraphAttributes(0, doc.getLength(), center, false);
    editor.setComponentPopupMenu(new TextComponentPopupMenu());
    editor.getDocument().addDocumentListener(new ResizeHandler());
  }

  private void initHandler() {
    Window windowAncestor = SwingUtilities.getWindowAncestor(this);
    windowAncestor.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        resetEditor(editor);
      }
    });
    windowAncestor.addComponentListener(new ComponentAdapter() {
      @Override public void componentResized(ComponentEvent e) {
        resetEditor(editor);
      }

      @Override public void componentMoved(ComponentEvent e) {
        resetEditor(editor);
      }
    });

    Container c = SwingUtilities.getAncestorOfClass(JScrollPane.class, this);
    if (c != null) {
      c.addMouseWheelListener(e -> {
        if (window != null && window.isVisible() && editingIndex >= 0) {
          ActionEvent ev = new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, "");
          renameTitle.actionPerformed(ev);
        }
      });
    }
  }

  public static void resetEditor(Component editor) {
    Window windowAncestor = SwingUtilities.getWindowAncestor(editor);
    if (windowAncestor != null) {
      windowAncestor.dispose();
    }
  }

  @Override public void updateUI() {
    removeMouseListener(handler);
    removeMouseMotionListener(handler);
    setSelectionForeground(null);
    setSelectionBackground(null);
    setCellRenderer(null);
    super.updateUI();
    setLayoutOrientation(HORIZONTAL_WRAP);
    getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    setVisibleRowCount(0);
    setFixedCellWidth(72);
    setFixedCellHeight(64);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setCellRenderer(new ListItemListCellRenderer<>());
    handler = new EditingHandler();
    addMouseListener(handler);
    addMouseMotionListener(handler);
  }

  private final class StartEditingAction extends AbstractAction {
    @Override public void actionPerformed(ActionEvent e) {
      // getRootPane().setGlassPane(glassPane);
      int idx = getSelectedIndex();
      editingIndex = idx;
      Rectangle rect = getCellBounds(idx, idx);
      // Point p = SwingUtilities.convertPoint(EditableList.this, rect.getLocation(), glassPane);
      // rect.setLocation(p);
      editorWidth = rect.width;
      editor.setText(getSelectedValue().getTitle());
      int rowHeight = editor.getFontMetrics(editor.getFont()).getHeight();
      rect.y += rect.height - rowHeight - 2 - 1;
      rect.height = editor.getPreferredSize().height;
      editor.setBounds(rect);
      editor.selectAll();
      // glassPane.add(editor);
      // glassPane.setVisible(true);
      Point p = new Point(rect.getLocation());
      SwingUtilities.convertPointToScreen(p, EditableList.this);
      if (window == null) {
        window = new JWindow(SwingUtilities.getWindowAncestor(EditableList.this));
        window.setFocusableWindowState(true);
        window.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
        // window.setAlwaysOnTop(true);
        window.add(editor);
      }
      window.setLocation(p);
      window.pack();
      window.setVisible(true);
      editor.requestFocusInWindow();
    }
  }

  private final class RenameAction extends AbstractAction {
    @Override public void actionPerformed(ActionEvent e) {
      String title = editor.getText().trim();
      int index = editingIndex;
      window.setVisible(false);
      if (!title.isEmpty() && index >= 0) {
        E item = getModel().getElementAt(index);
        item.setTitle(title);
        setSelectedIndex(index);
        EventQueue.invokeLater(() -> setSelectedIndex(index));
      }
      editingIndex = -1;
    }
  }

  private final class ResizeHandler implements DocumentListener {
    private int prev = -1;

    private void update() {
      int h = editor.getPreferredSize().height;
      if (prev != h) {
        Rectangle rect = editor.getBounds();
        rect.height = h;
        editor.setBounds(rect);
        window.pack();
        editor.requestFocusInWindow();
      }
      prev = h;
    }

    @Override public void insertUpdate(DocumentEvent e) {
      EventQueue.invokeLater(this::update);
    }

    @Override public void removeUpdate(DocumentEvent e) {
      EventQueue.invokeLater(this::update);
    }

    @Override public void changedUpdate(DocumentEvent e) {
      EventQueue.invokeLater(this::update);
    }
  }

  private final class EditingHandler extends EditingMouseAdapter {
    @Override public void mouseClicked(MouseEvent e) {
      if (isStartEditingEvent(e, editor.getPreferredSize())) {
        Component c = e.getComponent();
        ActionEvent ev = new ActionEvent(c, ActionEvent.ACTION_PERFORMED, "");
        startEditing.actionPerformed(ev);
      }
    }

    @Override public void mousePressed(MouseEvent e) {
      JList<?> list = (JList<?>) e.getComponent();
      startOutside = !contains(list, e.getPoint());
      if (window != null && window.isVisible() && editingIndex >= 0) {
        ActionEvent ev = new ActionEvent(editor, ActionEvent.ACTION_PERFORMED, "");
        renameTitle.actionPerformed(ev);
      } else if (startOutside) {
        clearSelectionAndFocus(list);
      }
    }
  }
}

class EditingMouseAdapter extends MouseAdapter {
  protected boolean startOutside;

  @Override public void mouseReleased(MouseEvent e) {
    startOutside = false;
  }

  @Override public void mouseDragged(MouseEvent e) {
    JList<?> list = (JList<?>) e.getComponent();
    if (contains(list, e.getPoint())) {
      startOutside = false;
    } else if (startOutside) {
      clearSelectionAndFocus(list);
    }
  }

  protected boolean isStartEditingEvent(MouseEvent e, Dimension dim) {
    JList<?> list = (JList<?>) e.getComponent();
    int idx = list.getSelectedIndex();
    Rectangle rect = list.getCellBounds(idx, idx);
    boolean b = false;
    if (rect != null) {
      rect.y = rect.y + rect.height - dim.height - 2 - 1;
      rect.height = dim.height;
      b = e.getClickCount() >= 2 && rect.contains(e.getPoint());
    }
    return b;
  }

  protected void clearSelectionAndFocus(JList<?> list) {
    list.clearSelection();
    list.getSelectionModel().setAnchorSelectionIndex(-1);
    list.getSelectionModel().setLeadSelectionIndex(-1);
  }

  protected boolean contains(JList<?> list, Point pt) {
    return IntStream.range(0, list.getModel().getSize())
        .mapToObj(i -> list.getCellBounds(i, i))
        .anyMatch(r -> r != null && r.contains(pt));
  }
}

final class TextComponentPopupMenu extends JPopupMenu {
  /* default */ TextComponentPopupMenu() {
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
        Action a = m instanceof JMenuItem ? ((JMenuItem) m).getAction() : null;
        if (a instanceof DefaultEditorKit.PasteAction) {
          continue;
        }
        m.setEnabled(hasSelectedText);
      }
      super.show(c, x, y);
    }
  }
}

class WrapEditorKit extends StyledEditorKit {
  @Override public ViewFactory getViewFactory() {
    return new WrapColumnFactory();
  }
}

class WrapColumnFactory implements ViewFactory {
  @SuppressWarnings("PMD.OnlyOneReturn")
  @Override public View create(Element element) {
    String kind = element.getName();
    if (kind != null) {
      switch (kind) {
        case AbstractDocument.ContentElementName:
          return new LabelView(element) {
            @Override public float getMinimumSpan(int axis) {
              return axis == X_AXIS ? 0 : super.getMinimumSpan(axis);
            }
          };
        case AbstractDocument.ParagraphElementName:
          return new ParagraphView(element);
        case AbstractDocument.SectionElementName:
          return new BoxView(element, View.Y_AXIS);
        case StyleConstants.ComponentElementName:
          return new ComponentView(element);
        case StyleConstants.IconElementName:
          return new IconView(element);
        default:
          return new LabelView(element);
      }
    }
    return new LabelView(element);
  }
}

// class WrapLabelView extends LabelView {
//   protected WrapLabelView(Element element) {
//     super(element);
//   }
//
//   // https://stackoverflow.com/questions/30590031/jtextpane-line-wrap-behavior
//   @Override public float getMinimumSpan(int axis) {
//     switch (axis) {
//       case X_AXIS:
//         return 0;
//       case Y_AXIS:
//         return super.getMinimumSpan(axis);
//       default:
//         throw new IllegalArgumentException("Invalid axis: " + axis);
//     }
//   }
// }
