// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    String help = String.join("\n",
        " Start editing: Double-Click, Enter-Key",
        " Commit rename: field-focusLost, Enter-Key",
        "Cancel editing: Esc-Key, title.isEmpty"
    );
    JTabbedPane tabs = new JTabbedPane() {
      private transient TabTitleEditListener listener;
      @Override public void updateUI() {
        removeChangeListener(listener);
        removeMouseListener(listener);
        super.updateUI();
        listener = new TabTitleEditListener(this);
        addChangeListener(listener);
        addMouseListener(listener);
      }
    };
    tabs.addTab("Shortcuts", new JTextArea(help));
    tabs.addTab("JLabel", new JLabel("JLabel"));
    tabs.addTab("JTree", new JScrollPane(new JTree()));
    tabs.addTab("JSplitPane", new JSplitPane());
    add(tabs);
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
    frame.setMinimumSize(new Dimension(256, 100));
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

class TabTitleEditListener extends MouseAdapter implements ChangeListener, DocumentListener {
  protected static final String START = "start-editing";
  protected static final String CANCEL = "cancel-editing";
  protected static final String RENAME = "rename-tab-title";
  protected final JTextField editor = new JTextField();
  protected final JTabbedPane tabbedPane;
  protected int editingIdx = -1;
  protected int len = -1;
  protected Dimension dim;
  protected Component tabComponent;
  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      editingIdx = tabbedPane.getSelectedIndex();
      tabComponent = tabbedPane.getTabComponentAt(editingIdx);
      tabbedPane.setTabComponentAt(editingIdx, editor);
      editor.setVisible(true);
      editor.setText(tabbedPane.getTitleAt(editingIdx));
      editor.selectAll();
      editor.requestFocusInWindow();
      len = editor.getText().length();
      dim = editor.getPreferredSize();
      editor.setMinimumSize(dim);
    }
  };
  protected final Action renameTabTitle = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      String title = editor.getText().trim();
      if (editingIdx >= 0 && !title.isEmpty()) {
        tabbedPane.setTitleAt(editingIdx, title);
      }
      ActionEvent a = new ActionEvent(tabbedPane, ActionEvent.ACTION_PERFORMED, CANCEL);
      cancelEditing.actionPerformed(a);
    }
  };
  protected final Action cancelEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      if (editingIdx >= 0) {
        tabbedPane.setTabComponentAt(editingIdx, tabComponent);
        editor.setVisible(false);
        editingIdx = -1;
        len = -1;
        // tabComponent = null;
        editor.setPreferredSize(null);
        tabbedPane.requestFocusInWindow();
      }
    }
  };

  protected TabTitleEditListener(JTabbedPane tabbedPane) {
    super();
    this.tabbedPane = tabbedPane;
    editor.setBorder(BorderFactory.createEmptyBorder());
    editor.addFocusListener(new FocusAdapter() {
      @Override public void focusLost(FocusEvent e) {
        ActionEvent a = new ActionEvent(tabbedPane, ActionEvent.ACTION_PERFORMED, RENAME);
        renameTabTitle.actionPerformed(a);
      }
    });
    editor.getDocument().addDocumentListener(this);
    // editor.addKeyListener(new KeyAdapter() {
    //   @Override public void keyPressed(KeyEvent e) {
    //     if (e.getKeyCode() == KeyEvent.VK_ENTER) {
    //       renameTabTitle();
    //     } else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
    //       cancelEditing();
    //     } else {
    //       editor.setPreferredSize(editor.getText().length() > len ? null : dim);
    //       tabbedPane.revalidate();
    //     }
    //   }
    // });

    KeyStroke enterKey = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    InputMap im = editor.getInputMap(JComponent.WHEN_FOCUSED);
    im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), CANCEL);
    im.put(enterKey, RENAME);

    ActionMap am = editor.getActionMap();
    am.put(CANCEL, cancelEditing);
    am.put(RENAME, renameTabTitle);

    tabbedPane.getInputMap(JComponent.WHEN_FOCUSED).put(enterKey, START);
    tabbedPane.getActionMap().put(START, startEditing);
  }

  @Override public void stateChanged(ChangeEvent e) {
    ActionEvent a = new ActionEvent(tabbedPane, ActionEvent.ACTION_PERFORMED, RENAME);
    renameTabTitle.actionPerformed(a);
  }

  @Override public void insertUpdate(DocumentEvent e) {
    updateTabSize();
  }

  @Override public void removeUpdate(DocumentEvent e) {
    updateTabSize();
  }

  @Override public void changedUpdate(DocumentEvent e) {
    /* not needed */
  }

  @Override public void mouseClicked(MouseEvent e) {
    Rectangle r = tabbedPane.getBoundsAt(tabbedPane.getSelectedIndex());
    boolean isDoubleClick = e.getClickCount() >= 2;
    if (isDoubleClick && r.contains(e.getPoint())) {
      ActionEvent a = new ActionEvent(tabbedPane, ActionEvent.ACTION_PERFORMED, START);
      startEditing.actionPerformed(a);
    } else {
      ActionEvent a = new ActionEvent(tabbedPane, ActionEvent.ACTION_PERFORMED, RENAME);
      renameTabTitle.actionPerformed(a);
    }
  }

  protected void updateTabSize() {
    editor.setPreferredSize(editor.getText().length() > len ? null : dim);
    tabbedPane.revalidate();
  }
}
