// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import javax.swing.plaf.basic.BasicButtonUI;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTabbedPane tabbedPane = new JTabbedPane();
    // TabTitleEditListener l = new TabTitleEditListener(tabbedPane);
    // tabbedPane.addChangeListener(l);
    // tabbedPane.addMouseListener(l);
    IntStream.range(0, 3).forEach(i -> {
      String title = "Tab " + i;
      tabbedPane.addTab(title, new JLabel(title));
      tabbedPane.setTabComponentAt(i, new ButtonTabComponent(tabbedPane));
    });
    tabbedPane.setComponentPopupMenu(new TabTitleRenamePopupMenu());
    add(tabbedPane);
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

// How to Use Tabbed Panes (The Java™ Tutorials > ... > Using Swing Components)
// https://docs.oracle.com/javase/tutorial/uiswing/components/tabbedpane.html
class ButtonTabComponent extends JPanel {
  private final JTabbedPane tabbedPane;

  protected ButtonTabComponent(JTabbedPane tabbedPane) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    this.tabbedPane = Objects.requireNonNull(tabbedPane, "TabbedPane cannot be null");
    JLabel label = new JLabel() {
      @Override public String getText() {
        String txt = null;
        int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          txt = tabbedPane.getTitleAt(i);
        }
        return txt;
      }

      @Override public Icon getIcon() {
        Icon icn = null;
        int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          icn = tabbedPane.getIconAt(i);
        }
        return icn;
      }
    };
    label.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 5));

    JButton button = new TabButton();
    TabButtonHandler handler = new TabButtonHandler();
    button.addActionListener(handler);
    button.addMouseListener(handler);

    add(label);
    add(button);
  }

  @Override public final Component add(Component comp) {
    return super.add(comp);
  }

  @Override public void updateUI() {
    super.updateUI();
    setOpaque(false);
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
  }

  private final class TabButtonHandler extends MouseAdapter implements ActionListener {
    @Override public void actionPerformed(ActionEvent e) {
      int i = tabbedPane.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
        tabbedPane.remove(i);
      }
    }

    @Override public void mouseEntered(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(true);
      }
    }

    @Override public void mouseExited(MouseEvent e) {
      Component component = e.getComponent();
      if (component instanceof AbstractButton) {
        AbstractButton button = (AbstractButton) component;
        button.setBorderPainted(false);
      }
    }
  }
}

final class TabButton extends JButton {
  private static final int SIZE = 17;
  private static final int DELTA = 6;

  @Override public void updateUI() {
    // we don't want to update UI for this button
    // super.updateUI();
    setUI(new BasicButtonUI());
    setToolTipText("close this tab");
    setContentAreaFilled(false);
    setFocusable(false);
    setBorder(BorderFactory.createEtchedBorder());
    setBorderPainted(false);
    setRolloverEnabled(true);
  }

  @Override public Dimension getPreferredSize() {
    return new Dimension(SIZE, SIZE);
  }

  @Override protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g.create();
    g2.setStroke(new BasicStroke(2));
    g2.setPaint(Color.BLACK);
    if (getModel().isRollover()) {
      g2.setPaint(Color.ORANGE);
    }
    if (getModel().isPressed()) {
      g2.setPaint(Color.BLUE);
    }
    g2.drawLine(DELTA, DELTA, getWidth() - DELTA - 1, getHeight() - DELTA - 1);
    g2.drawLine(getWidth() - DELTA - 1, DELTA, DELTA, getHeight() - DELTA - 1);
    g2.dispose();
  }
}

final class TabTitleRenamePopupMenu extends JPopupMenu {
  private final JMenuItem rename;

  /* default */ TabTitleRenamePopupMenu() {
    super();
    JTextField textField = new JTextField(10);
    textField.addAncestorListener(new FocusAncestorListener());

    rename = add("rename");
    rename.addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      int idx = t.getSelectedIndex();
      String title = t.getTitleAt(idx);
      textField.setText(title);
      int ret = JOptionPane.showConfirmDialog(
          t, textField, "Rename", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
      if (ret == JOptionPane.OK_OPTION) {
        String str = textField.getText().trim();
        Component c = t.getTabComponentAt(idx);
        if (!str.isEmpty() && c != null) {
          t.setTitleAt(idx, str);
          c.revalidate();
        }
      }
    });
    addSeparator();
    add("new tab").addActionListener(e -> {
      JTabbedPane t = (JTabbedPane) getInvoker();
      int count = t.getTabCount();
      String title = "Tab " + count;
      t.addTab(title, new JLabel(title));
      t.setTabComponentAt(count, new ButtonTabComponent(t));
    });
    add("close all").addActionListener(e -> ((JTabbedPane) getInvoker()).removeAll());
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane t = (JTabbedPane) c;
      rename.setEnabled(t.indexAtLocation(x, y) >= 0);
      super.show(c, x, y);
    }
  }
}

class FocusAncestorListener implements AncestorListener {
  @Override public void ancestorAdded(AncestorEvent e) {
    e.getComponent().requestFocusInWindow();
  }

  @Override public void ancestorMoved(AncestorEvent e) {
    /* not needed */
  }

  @Override public void ancestorRemoved(AncestorEvent e) {
    /* not needed */
  }
}

// class TabTitleEditListener extends MouseAdapter implements ChangeListener {
//   protected final JTextField editor = new JTextField();
//   protected final JTabbedPane tabbedPane;
//   private int editing_idx = -1;
//   private int len = -1;
//   private Dimension dim;
//   private Component tabComponent = null; // <----add----
//
//   protected TabTitleEditListener(JTabbedPane tabbedPane) {
//     super();
//     this.tabbedPane = tabbedPane;
//     editor.setBorder(BorderFactory.createEmptyBorder());
//     editor.addFocusListener(new FocusAdapter() {
//       @Override public void focusLost(FocusEvent e) {
//         renameTabTitle();
//       }
//     });
//     editor.addKeyListener(new KeyAdapter() {
//       @Override public void keyPressed(KeyEvent e) {
//         if (e.getKeyCode() == KeyEvent.VK_ENTER) {
//           renameTabTitle();
//         } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
//           cancelEditing();
//         } else {
//           editor.setPreferredSize((editor.getText().length() > len) ? null : dim);
//           tabbedPane.revalidate();
//         }
//       }
//     });
//     tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(
//       KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
//     tabbedPane.getActionMap().put("start-editing", new AbstractAction() {
//       @Override public void actionPerformed(ActionEvent e) {
//         startEditing();
//       }
//     });
//   }
//
//   @Override public void stateChanged(ChangeEvent e) {
//     renameTabTitle();
//   }
//
//   @Override public void mouseClicked(MouseEvent e) {
//     int i = tabbedPane.getSelectedIndex();
//     Rectangle r = tabbedPane.getUI().getTabBounds(tabbedPane, i);
//     if (Objects.nonNull(r) && rect.contains(e.getPoint()) && e.getClickCount() - 2 >= 0) {
//       startEditing();
//     } else {
//       renameTabTitle();
//     }
//   }
//
//   private void startEditing() {
//     editing_idx = tabbedPane.getSelectedIndex();
//     tabComponent = tabbedPane.getTabComponentAt(editing_idx); // <----add----
//     tabbedPane.setTabComponentAt(editing_idx, editor);
//     editor.setVisible(true);
//     editor.setText(tabbedPane.getTitleAt(editing_idx));
//     editor.selectAll();
//     editor.requestFocusInWindow();
//     len = editor.getText().length();
//     dim = editor.getPreferredSize();
//     editor.setMinimumSize(dim);
//   }
//
//   private void cancelEditing() {
//     if (editing_idx >= 0) {
//       tabbedPane.setTabComponentAt(editing_idx, tabComponent); // <----add----
//       editor.setVisible(false);
//       editing_idx = -1;
//       len = -1;
//       editor.setPreferredSize(null);
//       tabbedPane.requestFocusInWindow();
//     }
//   }
//
//   private void renameTabTitle() {
//     String title = editor.getText().trim();
//     if (editing_idx >= 0 && !title.isEmpty()) {
//       tabbedPane.setTitleAt(editing_idx, title);
//     }
//     cancelEditing();
//   }
// }
