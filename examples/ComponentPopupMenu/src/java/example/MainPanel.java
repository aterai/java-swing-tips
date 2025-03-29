// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea textArea = new JTextArea("ComponentPopupMenu Test\n11111111\n222222\n333");
    textArea.setComponentPopupMenu(new TextComponentPopupMenu());
    add(new JScrollPane(textArea));
    setPreferredSize(new Dimension(320, 240));
  }

  // // Another way:
  // private static JPopupMenu makePopupMenu() {
  //   JPopupMenu popup = new JPopupMenu();
  //   Action cutAction = new DefaultEditorKit.CutAction();
  //   Action copyAction = new DefaultEditorKit.CopyAction();
  //   Action pasteAction = new DefaultEditorKit.PasteAction();
  //   Action deleteAction = new AbstractAction("delete") {
  //     @Override public void actionPerformed(ActionEvent e) {
  //       ((JTextComponent) getInvoker()).replaceSelection(null);
  //     }
  //   };
  //   Action selectAllAction = new AbstractAction("select all") {
  //     @Override public void actionPerformed(ActionEvent e) {
  //       JPopupMenu p = (JPopupMenu) e.getSource();
  //       ((JTextComponent) p.getInvoker()).selectAll();
  //     }
  //   };
  //   popup.add(cutAction);
  //   popup.add(copyAction);
  //   popup.add(pasteAction);
  //   popup.addSeparator();
  //   popup.add(deleteAction);
  //   popup.addSeparator();
  //   popup.add(selectAllAction);
  //   popup.addPopupMenuListener(new PopupMenuListener() {
  //     @Override public void popupMenuCanceled(PopupMenuEvent e) {}
  //     @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
  //     @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
  //       JPopupMenu p = (JPopupMenu) e.getSource();
  //       JTextComponent tc = (JTextComponent) p.getInvoker();
  //       boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
  //       cutAction.setEnabled(hasSelectedText);
  //       copyAction.setEnabled(hasSelectedText);
  //       deleteAction.setEnabled(hasSelectedText);
  //     }
  //   });
  //   return popup;
  // }

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

final class TextComponentPopupMenu extends JPopupMenu {
  private final Action cutAction = new DefaultEditorKit.CutAction();
  private final Action copyAction = new DefaultEditorKit.CopyAction();
  private final JMenuItem deleteItem;

  /* default */ TextComponentPopupMenu() {
    super();
    add(cutAction);
    add(copyAction);
    add(new DefaultEditorKit.PasteAction());
    addSeparator();
    deleteItem = add("delete");
    deleteItem.addActionListener(e -> getTextComponent().replaceSelection(null));
    addSeparator();
    add(DefaultEditorKit.selectAllAction).addActionListener(e -> getTextComponent().selectAll());
    // add(DefaultEditorKit.selectAllAction).addActionListener(e -> {
    //   JTextComponent tc = (JTextComponent) getInvoker();
    //   tc.getActionMap().get(DefaultEditorKit.selectAllAction).actionPerformed(e);
    // });
  }

  private JTextComponent getTextComponent() {
    return (JTextComponent) getInvoker();
  }

  @Override public void show(Component c, int x, int y) {
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      cutAction.setEnabled(hasSelectedText);
      copyAction.setEnabled(hasSelectedText);
      deleteItem.setEnabled(hasSelectedText);
      super.show(c, x, y);
    }
  }
}
