// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea("");
    JPopupMenu popup1 = makePopupMenu(log);
    JTextField textField1 = new JTextField("Default setComponentPopupMenu");
    textField1.setComponentPopupMenu(popup1);
    textField1.setName("textField1");
    // // TEST:
    // textField1.addMouseListener(new MouseAdapter() {
    //   @Override public void mousePressed(MouseEvent e) {
    //     e.getComponent().requestFocusInWindow();
    //   }
    // });

    JPopupMenu popup2 = new TextComponentPopupMenu();
    log.setComponentPopupMenu(popup2);
    JTextField textField2 = new JTextField("Override JPopupMenu#show(...)");
    textField2.setComponentPopupMenu(popup2);
    textField2.setName("textField2");

    String[] model3 = {"JPopupMenu does not open???", "aaa", "bbb"};
    JComboBox<String> combo3 = new JComboBox<>(model3);
    combo3.setEditable(true);
    // NOT work: combo3.setComponentPopupMenu(popup2);
    JTextField textField3 = (JTextField) combo3.getEditor().getEditorComponent();
    textField3.setComponentPopupMenu(popup2);
    textField3.setName("textField3");
    // TEST: textField3.putClientProperty("doNotCancelPopup", null);

    String[] model4 = {"addMouseListener", "ccc", "ddd"};
    JComboBox<String> combo4 = new JComboBox<>(model4);
    combo4.setEditable(true);
    JTextField textField4 = (JTextField) combo4.getEditor().getEditorComponent();
    textField4.setComponentPopupMenu(popup2);
    textField4.setName("textField4");
    textField4.addMouseListener(new MouseAdapter() {
      @Override public void mousePressed(MouseEvent e) {
        if (combo4.isPopupVisible()) {
          log.append("Close all JPopupMenu(excludes dropdown list of own JComboBox)\n");
          // https://ateraimemo.com/Swing/GetAllPopupMenus.html
          for (MenuElement m : MenuSelectionManager.defaultManager().getSelectedPath()) {
            if (m instanceof JPopupMenu) {
              ((JPopupMenu) m).setVisible(false);
            }
          }
        }
      }
    });
    // textField4.addFocusListener(new FocusAdapter() {
    //   @Override public void focusGained(FocusEvent e) {
    //     log.append("focusGained\n");
    //     for (MenuElement m : MenuSelectionManager.defaultManager().getSelectedPath()) {
    //       if (m instanceof JPopupMenu) {
    //         ((JPopupMenu) m).setVisible(false);
    //       }
    //     }
    //   }
    // });

    Box box = Box.createVerticalBox();
    Stream.of(textField1, textField2, combo3, combo4).forEach(c -> {
      box.add(c);
      box.add(Box.createVerticalStrut(5));
    });

    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static JPopupMenu makePopupMenu(JTextArea log) {
    Action cutAction = new DefaultEditorKit.CutAction();
    Action copyAction = new DefaultEditorKit.CopyAction();
    Action pasteAction = new DefaultEditorKit.PasteAction();

    JPopupMenu popup1 = new JPopupMenu();
    popup1.add(cutAction);
    popup1.add(copyAction);
    popup1.add(pasteAction);
    popup1.addPopupMenuListener(new PopupMenuListener() {
      @Override public void popupMenuCanceled(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
        /* not needed */
      }

      @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        JPopupMenu pop = (JPopupMenu) e.getSource();
        JTextComponent tc = (JTextComponent) pop.getInvoker();
        log.append(tc.getClass().getName() + ": " + tc.getName() + "\n");
        // TEST: tc.requestFocusInWindow(); tc.selectAll();
        boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
        cutAction.setEnabled(hasSelectedText);
        copyAction.setEnabled(hasSelectedText);
      }
    });
    return popup1;
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

final class TextComponentPopupMenu extends JPopupMenu {
  private final transient Action cutAction = new DefaultEditorKit.CutAction();
  private final transient Action copyAction = new DefaultEditorKit.CopyAction();
  // private final transient Action pasteAction = new DefaultEditorKit.PasteAction();

  /* default */ TextComponentPopupMenu() {
    super();
    add(cutAction);
    add(copyAction);
    add(new DefaultEditorKit.PasteAction());
  }

  @Override public void show(Component c, int x, int y) {
    // System.out.println(c.getClass().getName() + ": " + c.getName());
    if (c instanceof JTextComponent) {
      JTextComponent tc = (JTextComponent) c;
      tc.requestFocusInWindow();
      boolean hasSelectedText = Objects.nonNull(tc.getSelectedText());
      if (!tc.isFocusOwner() && !hasSelectedText) {
        tc.selectAll();
        hasSelectedText = true;
      }
      cutAction.setEnabled(hasSelectedText);
      copyAction.setEnabled(hasSelectedText);
      super.show(c, x, y);
    }
  }
}
