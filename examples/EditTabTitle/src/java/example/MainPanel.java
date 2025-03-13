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

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String help = String.join("\n",
        " Start editing: Double-Click, Enter-Key",
        " Commit rename: field-focusLost, Enter-Key",
        "Cancel editing: Esc-Key, title.isEmpty"
    );
    JTextArea area = new JTextArea(help);
    area.setEditable(false);

    JTabbedPane tabbedPane = new EditableTabbedPane();
    tabbedPane.addTab("Shortcuts", new JScrollPane(area));
    tabbedPane.addTab("JLabel", new JLabel("11111111"));
    tabbedPane.addTab("JTree", new JScrollPane(new JTree()));
    tabbedPane.addTab("JButton", new JButton("222222"));
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

class EditableTabbedPane extends JTabbedPane {
  public static final String EDIT_KEY = "rename-tab";
  public static final String START_EDITING = "start-editing";
  public static final String CANCEL_EDITING = "cancel-editing";
  protected final Container glassPane = new JComponent() {
    @Override public void setVisible(boolean flag) {
      super.setVisible(flag);
      setFocusTraversalPolicyProvider(flag);
      setFocusCycleRoot(flag);
    }
  };
  protected final JTextField editor = new JTextField();
  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      getRootPane().setGlassPane(glassPane);
      Rectangle rect = getBoundsAt(getSelectedIndex());
      Component src = EditableTabbedPane.this;
      Point p = SwingUtilities.convertPoint(src, rect.getLocation(), glassPane);
      // rect.setBounds(p.x + 2, p.y + 2, rect.width - 4, rect.height - 4);
      rect.setLocation(p);
      rect.grow(-2, -2);
      editor.setBounds(rect);
      editor.setText(getTitleAt(getSelectedIndex()));
      editor.selectAll();
      glassPane.add(editor);
      glassPane.setVisible(true);
      editor.requestFocusInWindow();
    }
  };
  protected final Action cancelEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      glassPane.setVisible(false);
    }
  };
  protected final Action renameTab = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      String str = editor.getText().trim();
      if (!str.isEmpty()) {
        setTitleAt(getSelectedIndex(), str);
        Optional.ofNullable(getTabComponentAt(getSelectedIndex()))
            .ifPresent(Component::revalidate);
      }
      glassPane.setVisible(false);
    }
  };
  private transient MouseListener listener;

  protected EditableTabbedPane() {
    super();
    editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    InputMap im = editor.getInputMap(WHEN_FOCUSED);
    im.put(enterKey, EDIT_KEY);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL_EDITING);

    ActionMap am = editor.getActionMap();
    am.put(EDIT_KEY, renameTab);
    am.put(CANCEL_EDITING, cancelEditing);

    getInputMap(WHEN_FOCUSED).put(enterKey, START_EDITING);
    getActionMap().put(START_EDITING, startEditing);

    glassPane.setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
      @Override public boolean accept(Component c) {
        return Objects.equals(c, getEditor());
      }
    });
    glassPane.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        JTextField tabEditor = getEditor();
        Optional.ofNullable(tabEditor.getActionMap().get(EDIT_KEY))
            .filter(a -> !tabEditor.getBounds().contains(e.getPoint()))
            .ifPresent(a -> actionPerformed(e.getComponent(), a, EDIT_KEY));
      }
    });
  }

  @Override public void updateUI() {
    removeMouseListener(listener);
    super.updateUI();
    listener = new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          actionPerformed(e.getComponent(), startEditing, START_EDITING);
        }
      }
    };
    addMouseListener(listener);
    EventQueue.invokeLater(() -> SwingUtilities.updateComponentTreeUI(editor));
  }

  private static void actionPerformed(Component c, Action a, String command) {
    a.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, command));
  }

  protected JTextField getEditor() {
    return editor;
  }
}
