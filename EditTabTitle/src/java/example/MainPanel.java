// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Objects;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    String help = String.join("\n", Arrays.asList(
        " Start editing: Double-Click, Enter-Key",
        " Commit rename: field-focusLost, Enter-Key",
        "Cancel editing: Esc-Key, title.isEmpty"));
    JTextArea area = new JTextArea(help);
    area.setEditable(false);

    JTabbedPane tabbedPane = new EditableTabbedPane();
    tabbedPane.addTab("Shortcuts", new JScrollPane(area));
    tabbedPane.addTab("badfasdf", new JLabel("bbbbbbbbbbbafasdf"));
    tabbedPane.addTab("cccc", new JScrollPane(new JTree()));
    tabbedPane.addTab("ddddddd", new JButton("dadfasdfasd"));
    add(tabbedPane);
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
      if (!editor.getText().trim().isEmpty()) {
        setTitleAt(getSelectedIndex(), editor.getText());
        Component c = getTabComponentAt(getSelectedIndex());
        if (c instanceof JComponent) {
          ((JComponent) c).revalidate();
        }
      }
      glassPane.setVisible(false);
    }
  };

  protected EditableTabbedPane() {
    super();
    editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));

    InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-tab");
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");

    ActionMap am = editor.getActionMap();
    am.put("rename-tab", renameTab);
    am.put("cancel-editing", cancelEditing);

    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          startEditing.actionPerformed(new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, ""));
        }
      }
    });
    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
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
            renameTab.actionPerformed(new ActionEvent(e.getComponent(), ActionEvent.ACTION_PERFORMED, ""));
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
