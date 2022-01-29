// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

class EditableTabbedPane extends JTabbedPane {
  protected final Container glassPane = new EditorGlassPane();
  protected final JTextField editor = new JTextField();
  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      getRootPane().setGlassPane(glassPane);
      Rectangle rect = getBoundsAt(getSelectedIndex());
      Point p = SwingUtilities.convertPoint(EditableTabbedPane.this, rect.getLocation(), glassPane);
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

  protected EditableTabbedPane() {
    super();
    editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

    InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    im.put(enterKey, "rename-tab");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");

    ActionMap am = editor.getActionMap();
    am.put("rename-tab", renameTab);
    am.put("cancel-editing", cancelEditing);

    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          Component c = e.getComponent();
          startEditing.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, ""));
        }
      }
    });
    getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, "start-editing");
    getActionMap().put("start-editing", startEditing);
  }

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
            Component c = e.getComponent();
            renameTab.actionPerformed(new ActionEvent(c, ActionEvent.ACTION_PERFORMED, ""));
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
