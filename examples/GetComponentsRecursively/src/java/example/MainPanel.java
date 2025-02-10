// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JButton button1 = new JButton("Default");
    button1.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });
    JButton button2 = new JButton("Details View");
    button2.addActionListener(e -> {
      JFileChooser fc = new JFileChooser();
      initFileChooserViewType(fc);
      int retValue = fc.showOpenDialog(getRootPane());
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(fc.getSelectedFile().getAbsolutePath());
      }
    });
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));
    p.add(button1);
    p.add(button2);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  private static void initFileChooserViewType(JFileChooser fc) {
    // java - How can I start the JFileChooser in the Details view? - Stack Overflow
    // https://stackoverflow.com/questions/16292502/how-can-i-start-the-jfilechooser-in-the-details-view
    String cmd = "viewTypeDetails";
    Action detailsAction = fc.getActionMap().get(cmd);
    if (Objects.nonNull(detailsAction)) {
      detailsAction.actionPerformed(new ActionEvent(fc, ActionEvent.ACTION_PERFORMED, cmd));
    }

    // TEST1:
    // SwingUtils.searchAndResizeMode(fc);

    // TEST2:
    // Component c = SwingUtils.findChildComponent(fc, JTable.class);
    // if (c instanceof JTable) { ... }

    // TEST3:
    // SwingUtils.getComponentByClass(fc, JTable.class).ifPresent(t -> ...);

    // TEST4:
    SwingUtils.descendants(fc)
        .filter(JTable.class::isInstance).map(JTable.class::cast)
        .findFirst()
        .ifPresent(t -> t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN));

    // TEST5:
    // SwingUtils.descendantOrSelf(fc)
    //     .filter(JTable.class::isInstance).map(JTable.class::cast)
    //     .findFirst()
    //     .ifPresent(t -> t.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN));
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

final class SwingUtils {
  private SwingUtils() {
    /* Singleton */
  }

  // TEST1
  @SuppressWarnings("PMD.OnlyOneReturn")
  public static boolean searchAndResizeMode(Container parent) {
    for (Component c : parent.getComponents()) {
      if (c instanceof JTable) {
        ((JTable) c).setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        return true;
      } else if (c instanceof Container && searchAndResizeMode((Container) c)) {
        return true;
      }
    }
    return false;
  }

  // TEST2
  @SuppressWarnings("PMD.OnlyOneReturn")
  public static Component findChild(Container container, Class<? extends Component> clz) {
    int n = container.getComponentCount();
    for (int i = 0; i < n; i++) {
      Component comp = container.getComponent(i);
      if (clz.isInstance(comp)) {
        return comp;
      } else if (comp instanceof Container) {
        Component c = findChild((Container) comp, clz);
        if (Objects.nonNull(c)) {
          return c;
        }
      }
    }
    return null;
  }

  // TEST3
  @SuppressWarnings("PMD.OnlyOneReturn")
  public static <T> Optional<T> getComponentByClass(Container parent, Class<T> clz) {
    if (clz.isInstance(parent)) {
      return Optional.of(clz.cast(parent));
    }
    for (Component c : parent.getComponents()) {
      if (c instanceof Container) {
        Optional<T> op = getComponentByClass((Container) c, clz);
        if (op.isPresent()) {
          return op;
        }
      }
    }
    return Optional.empty();
  }

  // TEST4
  public static Stream<Component> descendants(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(Container.class::cast)
        .flatMap(c -> Stream.concat(Stream.of(c), descendants(c)));
  }

  // TEST5
  public static Stream<Component> descendantOrSelf(Container parent) {
    return Stream.of(parent.getComponents())
        .filter(Container.class::isInstance).map(c -> descendantOrSelf((Container) c))
        .reduce(Stream.of(parent), Stream::concat);
  }

  // TEST6
  // public static Stream<Component> descendantOrSelf2(Container parent) {
  //   return Stream.concat(Stream.of(parent), Stream.of(parent.getComponents())
  //       .filter(Container.class::isInstance).map(Container.class::cast)
  //       .flatMap(SwingUtils::descendantOrSelf2));
  // }

  // // import java.util.function.Function;
  // private static Optional<Component> findFileNameTextField(JFileChooser fc) {
  //   return Stream.of(fc.getComponents()).flatMap(new Function<Component, Stream<Component>>() {
  //     @Override public Stream<Component> apply(Component c) {
  //       if (c instanceof Container) {
  //         Component[] sub = ((Container) c).getComponents();
  //         return sub.length == 0 ? Stream.of(c) : Arrays.stream(sub).flatMap(cc -> apply(cc));
  //       } else {
  //         return Stream.of(c);
  //       }
  //     }
  //   }).filter(c -> c instanceof JTextField).findFirst();
  // }
}
