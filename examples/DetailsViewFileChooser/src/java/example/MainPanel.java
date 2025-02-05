// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Optional;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JTextArea log = new JTextArea();
    JPanel p = new JPanel();
    p.setBorder(BorderFactory.createTitledBorder("JFileChooser"));

    JButton listView = new JButton("List View(Default)");
    listView.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      int retValue = chooser.showOpenDialog(p);
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    JButton detailsView = new JButton("Details View");
    detailsView.addActionListener(e -> {
      JFileChooser chooser = new JFileChooser();
      // sun.swing.FilePane filePane = findChildComponent(chooser, sun.swing.FilePane.class);
      // filePane.setViewType(sun.swing.FilePane.VIEWTYPE_DETAILS);

      // if (searchAndClick(chooser, UIManager.getIcon("FileChooser.detailsViewIcon"))) {
      //   Dimension d = chooser.getPreferredSize();
      //   chooser.setPreferredSize(new Dimension(d.width + 20, d.height)); // XXX
      // }

      // java - How can I start the JFileChooser in the Details view? - Stack Overflow
      // https://stackoverflow.com/questions/16292502/how-can-i-start-the-jfilechooser-in-the-details-view
      // for (Object key : chooser.getActionMap().allKeys()) {
      //   System.out.println(key);
      // }

      String cmd = "viewTypeDetails";
      Optional.ofNullable(chooser.getActionMap().get(cmd))
          .ifPresent(a -> a.actionPerformed(new ActionEvent(e.getSource(), e.getID(), cmd)));

      int retValue = chooser.showOpenDialog(p);
      if (retValue == JFileChooser.APPROVE_OPTION) {
        log.setText(chooser.getSelectedFile().getAbsolutePath());
      }
    });

    p.add(listView);
    p.add(detailsView);
    add(p, BorderLayout.NORTH);
    add(new JScrollPane(log));
    setPreferredSize(new Dimension(320, 240));
  }

  // public static boolean searchAndClick(Container parent, Icon icon) {
  //   for (Component c : parent.getComponents()) {
  //     if (c instanceof JToggleButton && ((JToggleButton) c).getIcon() == icon) {
  //       ((AbstractButton) c).doClick();
  //       return true;
  //     } else {
  //       if (searchAndClick((Container) c, icon)) {
  //         return true;
  //       }
  //     }
  //   }
  //   return false;
  // }
  //
  // private static AbstractButton getDetailsViewButton(Container parent, Icon icon) {
  //   AbstractButton b = null;
  //   for (Component c : parent.getComponents()) {
  //     if (c instanceof JToggleButton && ((JToggleButton) c).getIcon() == icon) {
  //       b = (AbstractButton) c;
  //       break;
  //     } else {
  //       if (Objects.nonNull(b = getDetailsViewButton((Container) c, icon))) {
  //         break;
  //       }
  //     }
  //   }
  //   return b;
  // }
  //
  // private Component findChildComponent(Container container, Class<?> cls) {
  //   int n = container.getComponentCount();
  //   for (int i = 0; i < n; i++) {
  //     Component comp = container.getComponent(i);
  //     if (cls.isInstance(comp)) {
  //       return comp;
  //     } else if (comp instanceof Container) {
  //       Component c = findChildComponent((Container) comp, cls);
  //       if (Objects.nonNull(c)) {
  //         return c;
  //       }
  //     }
  //   }
  //   return null;
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
