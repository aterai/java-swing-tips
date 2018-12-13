package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());

    JTabbedPane tabbedPane = new EditableTabbedPane();
    tabbedPane.addTab("Title", new JLabel("Tab1"));
    tabbedPane.addTab("aaa", new JLabel("Tab2"));
    tabbedPane.addTab("000", new JLabel("Tab3"));
    tabbedPane.setComponentPopupMenu(new TabbedPanePopupMenu());

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
    } catch (ClassNotFoundException | InstantiationException
         | IllegalAccessException | UnsupportedLookAndFeelException ex) {
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

class ComparableTab { // implements Comparable<ComparableTab> {
  private final String title;
  private final Component comp;

  protected ComparableTab(String title, Component comp) {
    this.title = title;
    this.comp = comp;
  }

  public String getTitle() {
    return title;
  }

  public Component getComponent() {
    return comp;
  }
  // @Override public int compareTo(ComparableTab o) {
  //   return title.compareToIgnoreCase(o.title);
  // }
  // // http://jqno.nl/equalsverifier/errormessages/subclass-equals-is-not-final/
  // @Override public final boolean equals(Object o) {
  //   if (o == this) {
  //     return true;
  //   }
  //   if (o instanceof ComparableTab) {
  //     ComparableTab other = (ComparableTab) o;
  //     return Objects.equals(title, other.title) && Objects.equals(comp, other.comp);
  //   }
  //   return false;
  // }
  // @Override public final int hashCode() {
  //   return Objects.hash(title, comp);
  // }
}

class EditableTabbedPane extends JTabbedPane {
  protected final Container glassPane = new EditorGlassPane();
  protected final JTextField editor = new JTextField();
  protected final Action startEditing = new AbstractAction() {
    @Override public void actionPerformed(ActionEvent e) {
      getRootPane().setGlassPane(glassPane);
      Rectangle rect = getBoundsAt(getSelectedIndex());
      Point p = SwingUtilities.convertPoint(EditableTabbedPane.this, rect.getLocation(), glassPane);
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

  protected EditableTabbedPane() {
    super();
    editor.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
    editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "rename-tab");
    editor.getActionMap().put("rename-tab", new AbstractAction() {
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
    });
    editor.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel-editing");
    editor.getActionMap().put("cancel-editing", cancelEditing);

    addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        boolean isDoubleClick = e.getClickCount() >= 2;
        if (isDoubleClick) {
          startEditing.actionPerformed(null);
        }
      }
    });
    getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "start-editing");
    getActionMap().put("start-editing", startEditing);
  }

  protected JTextField getEditor() {
    return editor;
  }

  private class EditorGlassPane extends JComponent {
    protected EditorGlassPane() {
      super();
      setOpaque(false);
      setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
        @Override public boolean accept(Component c) {
          return Objects.equals(c, getEditor());
        }
      });
      addMouseListener(new MouseAdapter() {
        @Override public void mouseClicked(MouseEvent e) {
          JTextField tabEditor = getEditor();
          Optional.ofNullable(tabEditor.getActionMap().get("rename-tab"))
            .filter(a -> !tabEditor.getBounds().contains(e.getPoint()))
            .ifPresent(a -> a.actionPerformed(null));
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

class TabbedPanePopupMenu extends JPopupMenu {
  protected transient int count;
  private final JMenuItem sortTabs;
  private final JMenuItem closePage;
  private final JMenuItem closeAll;
  private final JMenuItem closeAllButActive;

  protected TabbedPanePopupMenu() {
    super();
    add("New tab").addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.addTab("Title: " + count, new JLabel("Tab: " + count));
      tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
      count++;
    });
    sortTabs = add("Sort");
    sortTabs.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      List<ComparableTab> list = IntStream.range(0, tabbedPane.getTabCount())
          .mapToObj(i -> new ComparableTab(tabbedPane.getTitleAt(i), tabbedPane.getComponentAt(i)))
          .sorted(Comparator.comparing(ComparableTab::getTitle)).collect(Collectors.toList());
      tabbedPane.removeAll();
      list.forEach(c -> tabbedPane.addTab(c.getTitle(), c.getComponent()));
    });
    addSeparator();
    closePage = add("Close");
    closePage.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.remove(tabbedPane.getSelectedIndex());
    });
    addSeparator();
    closeAll = add("Close all");
    closeAll.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      tabbedPane.removeAll();
    });
    closeAllButActive = add("Close all bat active");
    closeAllButActive.addActionListener(e -> {
      JTabbedPane tabbedPane = (JTabbedPane) getInvoker();
      int tabidx = tabbedPane.getSelectedIndex();
      String title = tabbedPane.getTitleAt(tabidx);
      Component cmp = tabbedPane.getComponentAt(tabidx);
      tabbedPane.removeAll();
      tabbedPane.addTab(title, cmp);
    });
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTabbedPane) {
      JTabbedPane tabbedPane = (JTabbedPane) c;
      // JDK 1.3: tabindex = tabbedPane.getUI().tabForCoordinate(tabbedPane, x, y);
      sortTabs.setEnabled(tabbedPane.getTabCount() > 1);
      closePage.setEnabled(tabbedPane.indexAtLocation(x, y) >= 0);
      closeAll.setEnabled(tabbedPane.getTabCount() > 0);
      closeAllButActive.setEnabled(tabbedPane.getTabCount() > 0);
      super.show(c, x, y);
    }
  }
}
