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
  private static final String HELP = String.join("\n",
      " Start editing: Double-Click",
      " Commit rename: field-focusLost, Enter-Key",
      "Cancel editing: Esc-Key, title.isEmpty");

  private MainPanel() {
    super(new GridLayout(0, 1, 5, 5));
    JScrollPane c1 = new JScrollPane(new JTree()) {
      @Override public void updateUI() {
        Border b = getBorder();
        String title = "JTree 111111111111111";
        if (b instanceof TitledBorder) {
          title = ((TitledBorder) b).getTitle();
        }
        setBorder(null);
        super.updateUI();
        setBorder(new EditableTitledBorder(title, this));
      }
    };

    JScrollPane c2 = new JScrollPane(new JTextArea(HELP)) {
      @Override public void updateUI() {
        Border b = getBorder();
        String title = "JTextArea";
        if (b instanceof TitledBorder) {
          title = ((TitledBorder) b).getTitle();
        }
        setBorder(null);
        super.updateUI();
        setBorder(new EditableTitledBorder(
            null, title, TitledBorder.RIGHT, TitledBorder.BOTTOM, this));
      }
    };

    add(c1);
    add(c2);
    setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
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

class EditableTitledBorder extends TitledBorder {
  protected final JTextField editor = new JTextField();
  protected final JLabel renderer = new JLabel();
  protected final Rectangle rect = new Rectangle();
  protected final Container glassPane = new JLabel() { // NG? : JComponent() {
    private transient MouseListener listener;
    @Override public void updateUI() {
      removeMouseListener(listener);
      super.updateUI();
      setOpaque(false);
      setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
        @Override public boolean accept(Component c) {
          return Objects.equals(c, editor);
        }
      });
      listener = new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
          if (!editor.getBounds().contains(e.getPoint())) {
            Component c = e.getComponent();
            renameTitle.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, ""));
          }
        }
      };
      addMouseListener(listener);
    }

    @Override public void setVisible(boolean flag) {
      super.setVisible(flag);
      setFocusTraversalPolicyProvider(flag);
      setFocusCycleRoot(flag);
    }
  };
  protected Component comp;

  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (comp instanceof JComponent) {
        Optional.ofNullable(((JComponent) comp).getRootPane())
            .ifPresent(r -> r.setGlassPane(glassPane));
      }
      Point p = SwingUtilities.convertPoint(comp, rect.getLocation(), glassPane);
      rect.setLocation(p);
      // Insets i = editor.getInsets();
      // rect.grow(i.top + i.bottom, i.left + i.right);
      rect.grow(2, 2);
      editor.setBounds(rect);
      editor.setText(getTitle());
      glassPane.removeAll();
      glassPane.add(editor);
      glassPane.setVisible(true);
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
      String str = editor.getText().trim();
      if (!str.isEmpty()) {
        setTitle(str);
      }
      glassPane.setVisible(false);
    }
  };

  protected EditableTitledBorder(String title, Component c) {
    this(null, title, LEADING, DEFAULT_POSITION, null, null, c);
  }

  // protected EditableTitledBorder(Border border, Component c) {
  //   this(border, "", LEADING, DEFAULT_POSITION, null, null, c);
  // }

  // protected EditableTitledBorder(Border border, String title, Component c) {
  //   this(border, title, LEADING, DEFAULT_POSITION, null, null, c);
  // }

  protected EditableTitledBorder(
      Border border,
      String title,
      int justification,
      int pos,
      Component c) {
    this(border, title, justification, pos, null, null, c);
  }

  // protected EditableTitledBorder(
  //     Border border,
  //     String title,
  //     int justification,
  //     int pos,
  //     Font font,
  //     Component c) {
  //   this(border, title, justification, pos, font, null, c);
  // }

  protected EditableTitledBorder(
      Border border,
      String title,
      int justification,
      int pos,
      Font font,
      Color color,
      Component c) {
    super(border, title, justification, pos, font, color);
    this.comp = c;
    comp.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          Component src = e.getComponent();
          rect.setBounds(getTitleBounds(src));
          if (rect.contains(e.getPoint())) {
            startEditing.actionPerformed(new ActionEvent(src, ActionEvent.ACTION_PERFORMED, ""));
          }
        }
      }
    });
    InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
    ActionMap am = editor.getActionMap();
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-title");
    am.put("rename-title", renameTitle);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");
    am.put("cancel-editing", cancelEditing);
  }

  @Override public boolean isBorderOpaque() {
    return true;
  }

  private JLabel getLabel2(Component c) {
    renderer.setText(getTitle());
    renderer.setFont(getFont(c));
    // renderer.setForeground(getColor(c));
    renderer.setComponentOrientation(c.getComponentOrientation());
    renderer.setEnabled(c.isEnabled());
    return renderer;
  }

  private int getJustification2(Component c) {
    int justification = getTitleJustification();
    if (justification == LEADING || justification == DEFAULT_JUSTIFICATION) {
      justification = c.getComponentOrientation().isLeftToRight() ? LEFT : RIGHT;
    } else if (justification == TRAILING) {
      justification = c.getComponentOrientation().isLeftToRight() ? RIGHT : LEFT;
    }
    return justification;
  }

  private Rectangle getLabelBounds(Component c) {
    // @see public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
    Border border = getBorder();
    int edge = border instanceof TitledBorder ? 0 : EDGE_SPACING;
    Insets i = getBorderInsets(border, c);
    JLabel label = getLabel2(c);
    Dimension size = label.getPreferredSize();
    Rectangle r = new Rectangle(c.getWidth() - i.left - i.right, size.height);
    calcPosition(c, edge, i, r);
    calcJustification(c, size, i, r);
    return r;
  }

  protected Rectangle getTitleBounds(Component c) {
    return Optional.ofNullable(getTitle())
        .filter(s -> !s.isEmpty())
        .map(s -> getLabelBounds(c))
        .orElseGet(Rectangle::new);
  }

  private void calcPosition(Component c, int edge, Insets insets, Rectangle lblR) {
    switch (getTitlePosition()) {
      case ABOVE_TOP:
        insets.left = 0;
        insets.right = 0;
        break;
      case TOP:
        insets.top = edge + insets.top / 2 - lblR.height / 2;
        if (insets.top >= edge) {
          lblR.y += insets.top;
        }
        break;
      case BELOW_TOP:
        lblR.y += insets.top + edge;
        break;
      case ABOVE_BOTTOM:
        lblR.y += c.getHeight() - lblR.height - insets.bottom - edge;
        break;
      case BOTTOM:
        lblR.y += c.getHeight() - lblR.height;
        insets.bottom = edge + (insets.bottom - lblR.height) / 2;
        if (insets.bottom >= edge) {
          lblR.y -= insets.bottom;
        }
        break;
      case BELOW_BOTTOM:
        insets.left = 0;
        insets.right = 0;
        lblR.y += c.getHeight() - lblR.height;
        break;
      default:
        break;
    }
    insets.left += edge + TEXT_INSET_H;
    insets.right += edge + TEXT_INSET_H;
  }

  private void calcJustification(Component c, Dimension size, Insets insets, Rectangle lblR) {
    if (lblR.width > size.width) {
      lblR.width = size.width;
    }
    switch (getJustification2(c)) {
      case LEFT:
        lblR.x += insets.left;
        break;
      case RIGHT:
        lblR.x += c.getWidth() - insets.right - lblR.width;
        break;
      case CENTER:
        lblR.x += (c.getWidth() - lblR.width) / 2;
        break;
      default:
        break;
    }
  }

  // @SuppressWarnings("PMD.AvoidReassigningParameters")
  // @see private Insets getBorderInsets(Border border, Component c, Insets insets) {
  private static Insets getBorderInsets(Border border, Component c) {
    Insets insets = new Insets(0, 0, 0, 0);
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
}
