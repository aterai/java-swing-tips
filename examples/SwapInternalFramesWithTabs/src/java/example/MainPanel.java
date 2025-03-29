// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private final CardLayout cardLayout = new CardLayout();
  private final JPanel panel = new JPanel(cardLayout);
  private final JDesktopPane desktopPane = new JDesktopPane();
  private final JTabbedPane tabbedPane = new JTabbedPane();
  private int openFrameCount;
  private int row;
  private int col;

  private MainPanel() {
    super(new BorderLayout());
    panel.add(desktopPane, desktopPane.getClass().getName());
    panel.add(tabbedPane, tabbedPane.getClass().getName());

    JToggleButton swapButton = new JToggleButton("JDesktopPane <-> JTabbedPane");
    swapButton.addActionListener(e -> {
      if (((AbstractButton) e.getSource()).isSelected()) {
        // tabbedPane.removeAll();
        Stream.of(desktopPane.getAllFrames())
            .sorted(Comparator.comparing(JInternalFrame::getTitle))
            .forEach(f -> tabbedPane.addTab(f.getTitle(), f.getFrameIcon(), f.getContentPane()));
        Optional.ofNullable(desktopPane.getSelectedFrame())
            .ifPresent(f -> tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(f.getTitle())));
        cardLayout.show(panel, tabbedPane.getClass().getName());
      } else {
        Stream.of(desktopPane.getAllFrames()).forEach(f -> {
          int i = tabbedPane.indexOfTab(f.getTitle());
          f.setContentPane((Container) tabbedPane.getComponentAt(i));
        });
        cardLayout.show(panel, desktopPane.getClass().getName());
      }
    });

    JButton addButton = new JButton("add");
    addButton.addActionListener(e -> {
      JInternalFrame f = createInternalFrame();
      desktopPane.add(f);
      Icon icon = f.getFrameIcon();
      String title = f.getTitle();
      Component c = new JScrollPane(new JTextArea(title));
      if (desktopPane.isShowing()) {
        f.add(c);
      } else {
        tabbedPane.addTab(title, icon, c);
      }
    });

    JToolBar toolBar = new JToolBar();
    toolBar.setFloatable(false);
    toolBar.add(addButton);
    toolBar.add(Box.createGlue());
    toolBar.add(swapButton);

    add(panel);
    add(toolBar, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  private JInternalFrame createInternalFrame() {
    String title = String.format("Document #%s", ++openFrameCount);
    JInternalFrame f = new JInternalFrame(title, true, true, true, true);
    row += 1;
    f.setSize(240, 120);
    f.setLocation(20 * row + 20 * col, 20 * row);
    EventQueue.invokeLater(() -> {
      f.setVisible(true);
      Rectangle rect = desktopPane.getBounds();
      rect.setLocation(0, 0);
      if (!rect.contains(f.getBounds())) {
        row = 0;
        col += 1;
      }
    });
    return f;
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
