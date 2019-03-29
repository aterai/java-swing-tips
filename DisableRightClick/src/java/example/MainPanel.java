// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import com.sun.java.swing.plaf.windows.WindowsComboBoxUI;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Objects;
import java.util.stream.IntStream;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    // Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
    //   @Override public void eventDispatched(AWTEvent event) {
    //     if (event instanceof MouseWheelEvent) {
    //       Object source = event.getSource();
    //       if (source instanceof JScrollPane) {
    //         System.out.println("JScrollPane");
    //         return;
    //       }
    //       ((MouseWheelEvent) event).consume();
    //     }
    //   }
    // }, AWTEvent.MOUSE_WHEEL_EVENT_MASK);

    JComboBox<String> combo1 = makeComboBox(5);
    if (combo1.getUI() instanceof WindowsComboBoxUI) {
      combo1.setUI(new WindowsComboBoxUI() {
        @Override protected ComboPopup createPopup() {
          return new BasicComboPopup2(comboBox);
        }
      });
    } else {
      combo1.setUI(new BasicComboBoxUI() {
        @Override protected ComboPopup createPopup() {
          return new BasicComboPopup2(comboBox);
        }
      });
    }

    JComboBox<String> combo2 = makeComboBox(20);
    if (combo2.getUI() instanceof WindowsComboBoxUI) {
      combo2.setUI(new WindowsComboBoxUI() {
        @Override protected ComboPopup createPopup() {
          return new BasicComboPopup3(comboBox);
        }
      });
    } else {
      combo2.setUI(new BasicComboBoxUI() {
        @Override protected ComboPopup createPopup() {
          return new BasicComboPopup3(comboBox);
        }
      });
    }

    Box box = Box.createVerticalBox();
    box.add(makeTitledPanel("default:", makeComboBox(5)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("default:", makeComboBox(20)));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("disable right click in drop-down list:", combo1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("disable right click and scroll in drop-down list:", combo2));
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  private static JComboBox<String> makeComboBox(int size) {
    DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();
    IntStream.range(0, size).forEach(i -> model.addElement("No." + i));
    return new JComboBox<>(model);
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

class BasicComboPopup2 extends BasicComboPopup {
  private transient MouseListener handler2;

  @Override public void uninstallingUI() {
    super.uninstallingUI();
    handler2 = null;
  }

  protected BasicComboPopup2(JComboBox<?> combo) {
    super(combo);
  }

  @Override protected MouseListener createListMouseListener() {
    if (Objects.isNull(handler2)) {
      handler2 = new Handler2();
    }
    return handler2;
  }

  private class Handler2 extends MouseAdapter {
    @Override public void mouseReleased(MouseEvent e) {
      if (!Objects.equals(e.getSource(), list)) {
        return;
      }
      if (list.getModel().getSize() > 0) {
        // <ins>
        if (!SwingUtilities.isLeftMouseButton(e) || !comboBox.isEnabled()) {
          return;
        }
        // </ins>
        // JList mouse listener
        if (comboBox.getSelectedIndex() == list.getSelectedIndex()) {
          comboBox.getEditor().setItem(list.getSelectedValue());
        }
        comboBox.setSelectedIndex(list.getSelectedIndex());
      }
      comboBox.setPopupVisible(false);
      // workaround for cancelling an edited item (bug 4530953)
      if (comboBox.isEditable() && Objects.nonNull(comboBox.getEditor())) {
        comboBox.configureEditor(comboBox.getEditor(), comboBox.getSelectedItem());
      }
    }
  }
}

class BasicComboPopup3 extends BasicComboPopup {
  protected BasicComboPopup3(JComboBox<?> combo) {
    super(combo);
  }

  // // Fixed 8u60: [JDK-8033069] mouse wheel scroll closes combobox popup - Java Bug System
  // // https://bugs.openjdk.java.net/browse/JDK-8033069
  // @Override protected JScrollPane createScroller() {
  //   JScrollPane sp = new JScrollPane(list) {
  //     @Override protected void processEvent(AWTEvent e) {
  //       if (e instanceof MouseWheelEvent) {
  //         JScrollBar toScroll = getVerticalScrollBar();
  //         if (Objects.isNull(toScroll) || !toScroll.isVisible()) {
  //           ((MouseWheelEvent) e).consume();
  //           return;
  //         }
  //       }
  //       super.processEvent(e);
  //     }
  //   };
  //   sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
  //   sp.setHorizontalScrollBar(null);
  //   return sp;
  // }

  @SuppressWarnings("unchecked")
  @Override protected JList<?> createList() {
    return new JList<Object>(comboBox.getModel()) {
      @Override protected void processMouseEvent(MouseEvent e) {
        if (SwingUtilities.isRightMouseButton(e)) {
          return;
        }
        MouseEvent ev = e;
        if (e.isControlDown()) {
          // Fix for 4234053. Filter out the Control Key from the list.
          // ie., don't allow CTRL key deselection.
          ev = new MouseEvent(
            e.getComponent(), e.getID(), e.getWhen(),
            // e.getModifiers() ^ InputEvent.CTRL_MASK,
            // Java 10: e.getModifiersEx() ^ Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx(),
            e.getModifiersEx() ^ InputEvent.CTRL_DOWN_MASK,
            e.getX(), e.getY(),
            e.getXOnScreen(), e.getYOnScreen(),
            e.getClickCount(),
            e.isPopupTrigger(),
            MouseEvent.NOBUTTON);
        }
        super.processMouseEvent(ev);
      }
    };
  }
}
