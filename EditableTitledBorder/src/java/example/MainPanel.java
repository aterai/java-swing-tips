// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import java.util.Optional;
import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;

public final class MainPanel extends JPanel {
  private static final String INFO = " Start editing: Double-Click\n"
      + " Commit rename: field-focusLost, Enter-Key\n"
      + "Cancel editing: Esc-Key, title.isEmpty\n";

  private MainPanel() {
    super(new GridLayout(0, 1, 5, 5));

    JScrollPane l1 = new JScrollPane(new JTree());
    l1.setBorder(new EditableTitledBorder("JTree aaaaaaaaaaaaa", l1));

    JScrollPane l2 = new JScrollPane(new JTextArea(INFO));
    l2.setBorder(new EditableTitledBorder(null, "JTextArea", TitledBorder.RIGHT, TitledBorder.BOTTOM, l2));

    add(l1);
    add(l2);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class EditableTitledBorder extends TitledBorder implements MouseListener {
  protected final Container glassPane = new EditorGlassPane();
  protected final JTextField editor = new JTextField();
  protected final JLabel dummy = new JLabel();
  protected final Rectangle rect = new Rectangle();
  protected Component comp;

  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (comp instanceof JComponent) {
        Optional.ofNullable(((JComponent) comp).getRootPane())
          .ifPresent(r -> r.setGlassPane(glassPane));
      }
      glassPane.removeAll();
      glassPane.add(editor);
      glassPane.setVisible(true);

      Point p = SwingUtilities.convertPoint(comp, rect.getLocation(), glassPane);
      rect.setLocation(p);
      rect.grow(2, 2);
      editor.setBounds(rect);
      editor.setText(getTitle());
      editor.selectAll();
      editor.requestFocusInWindow();
    }
  };
  protected final Action cancelEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      glassPane.setVisible(false);
    }
  };
  protected final Action renameTitle = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (!editor.getText().trim().isEmpty()) {
        setTitle(editor.getText());
      }
      glassPane.setVisible(false);
    }
  };

  protected EditableTitledBorder(String title, Component c) {
    this(null, title, LEADING, DEFAULT_POSITION, null, null, c);
  }

  protected EditableTitledBorder(Border border, Component c) {
    this(border, "", LEADING, DEFAULT_POSITION, null, null, c);
  }

  protected EditableTitledBorder(Border border, String title, Component c) {
    this(border, title, LEADING, DEFAULT_POSITION, null, null, c);
  }

  protected EditableTitledBorder(Border border, String title, int titleJustification, int titlePosition, Component c) {
    this(border, title, titleJustification, titlePosition, null, null, c);
  }

  protected EditableTitledBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont, Component c) {
    this(border, title, titleJustification, titlePosition, titleFont, null, c);
  }

  @SuppressWarnings("checkstyle:linelength")
  protected EditableTitledBorder(Border border, String title, int titleJustification, int titlePosition, Font titleFont, Color titleColor, Component c) {
    super(border, title, titleJustification, titlePosition, titleFont, titleColor);
    this.comp = c;
    comp.addMouseListener(this);
    editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-title");
    editor.getActionMap().put("rename-title", renameTitle);
    editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");
    editor.getActionMap().put("cancel-editing", cancelEditing);
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  private JLabel getLabel(Component c) {
    this.dummy.setText(getTitle());
    this.dummy.setFont(getFont(c));
    // this.dummy.setForeground(getColor(c));
    this.dummy.setComponentOrientation(c.getComponentOrientation());
    this.dummy.setEnabled(c.isEnabled());
    return this.dummy;
  }

  // Checkstyle False Positive: OverloadMethodsDeclarationOrder
  // private static Insets getBorderInsets(Border border, Component c, Insets insets) {
  @SuppressWarnings("PMD.AvoidReassigningParameters")
  private static Insets makeBorderInsets(Border border, Component c, Insets insets) {
    if (Objects.isNull(border)) {
      insets.set(0, 0, 0, 0);
    } else if (border instanceof AbstractBorder) {
      AbstractBorder ab = (AbstractBorder) border;
      insets = ab.getBorderInsets(c, insets);
    } else {
      Insets i = border.getBorderInsets(c);
      insets.set(i.top, i.left, i.bottom, i.right);
    }
    return insets;
  }

  private int getJustification(Component c) {
    int justification = getTitleJustification();
    if (justification == LEADING || justification == DEFAULT_JUSTIFICATION) {
      return c.getComponentOrientation().isLeftToRight() ? LEFT : RIGHT;
    }
    if (justification == TRAILING) {
      return c.getComponentOrientation().isLeftToRight() ? RIGHT : LEFT;
    }
    return justification;
  }

  @SuppressWarnings("PMD.CyclomaticComplexity")
  private Rectangle getTitleBounds(Component c, int x, int y, int width, int height) {
    String title = getTitle();
    if (Objects.nonNull(title) && !title.isEmpty()) {
      Border border = getBorder();
      int edge = border instanceof TitledBorder ? 0 : EDGE_SPACING;
      JLabel label = getLabel(c);
      Dimension size = label.getPreferredSize();
      Insets insets = makeBorderInsets(border, c, new Insets(0, 0, 0, 0));

      int labelY = y;
      int labelH = size.height;
      int position = getTitlePosition();
      switch (position) {
        case ABOVE_TOP:
          insets.left = 0;
          insets.right = 0;
          break;
        case TOP:
          insets.top = edge + insets.top / 2 - labelH / 2;
          if (insets.top >= edge) {
            labelY += insets.top;
          }
          break;
        case BELOW_TOP:
          labelY += insets.top + edge;
          break;
        case ABOVE_BOTTOM:
          labelY += height - labelH - insets.bottom - edge;
          break;
        case BOTTOM:
          labelY += height - labelH;
          insets.bottom = edge + (insets.bottom - labelH) / 2;
          if (insets.bottom >= edge) {
            labelY -= insets.bottom;
          }
          break;
        case BELOW_BOTTOM:
          insets.left = 0;
          insets.right = 0;
          labelY += height - labelH;
          break;
        default:
          break;
      }
      insets.left += edge + TEXT_INSET_H;
      insets.right += edge + TEXT_INSET_H;

      int labelX = x;
      int labelW = width - insets.left - insets.right;
      if (labelW > size.width) {
        labelW = size.width;
      }
      switch (getJustification(c)) {
        case LEFT:
          labelX += insets.left;
          break;
        case RIGHT:
          labelX += width - insets.right - labelW;
          break;
        case CENTER:
          labelX += (width - labelW) / 2;
          break;
        default:
          break;
      }
      return new Rectangle(labelX, labelY, labelW, labelH);
    }
    return new Rectangle();
  }

  @Override public void mouseClicked(MouseEvent e) {
    boolean isDoubleClick = e.getClickCount() >= 2;
    if (isDoubleClick) {
      Component src = e.getComponent();
      Dimension dim = src.getSize();
      rect.setBounds(getTitleBounds(src, 0, 0, dim.width, dim.height));
      if (rect.contains(e.getPoint())) {
        startEditing.actionPerformed(new ActionEvent(src, ActionEvent.ACTION_PERFORMED, ""));
      }
    }
  }

  @Override public void mouseEntered(MouseEvent e) { /* not needed */ }

  @Override public void mouseExited(MouseEvent e) { /* not needed */ }

  @Override public void mousePressed(MouseEvent e) { /* not needed */ }

  @Override public void mouseReleased(MouseEvent e) { /* not needed */ }

  protected JTextField getEditorTextField() {
    return editor;
  }

  private class EditorGlassPane extends JComponent {
    protected EditorGlassPane() {
      super();
      setOpaque(false);
      setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
        @Override public boolean accept(Component c) {
          return Objects.equals(c, getEditorTextField());
        }
      });
      addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
          if (!getEditorTextField().getBounds().contains(e.getPoint())) {
            renameTitle.actionPerformed(new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, ""));
          }
        }
      });
    }

    @Override public void setVisible(boolean flag) {
      super.setVisible(flag);
      setFocusTraversalPolicyProvider(flag);
      setFocusCycleRoot(flag);
    }
  }
}
