package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panel = new JPanel(cardLayout);
    private final JDesktopPane desktopPane = new JDesktopPane();
    private final JTabbedPane tabbedPane = new JTabbedPane();
    private int openFrameCount;
    private int row;
    private int col;
    private final Action swapAction = new AbstractAction("JDesktopPane <-> JTabbedPane") {
        @Override public void actionPerformed(ActionEvent e) {
            if (((AbstractButton) e.getSource()).isSelected()) {
                //tabbedPane.removeAll();
                Arrays.stream(desktopPane.getAllFrames())
                  .sorted(Comparator.comparing(JInternalFrame::getTitle))
                  .forEach(f -> tabbedPane.addTab(f.getTitle(), f.getFrameIcon(), f.getContentPane()));

                JInternalFrame sf = desktopPane.getSelectedFrame();
                if (Objects.nonNull(sf)) {
                    tabbedPane.setSelectedIndex(tabbedPane.indexOfTab(sf.getTitle()));
                }
                cardLayout.show(panel, tabbedPane.getClass().getName());
            } else {
                Arrays.stream(desktopPane.getAllFrames())
                  .forEach(f -> f.setContentPane((Container) tabbedPane.getComponentAt(tabbedPane.indexOfTab(f.getTitle()))));
                cardLayout.show(panel, desktopPane.getClass().getName());
            }
        }
    };
    private final Action addAction = new AbstractAction("add") {
        @Override public void actionPerformed(ActionEvent e) {
            JInternalFrame f = createInternalFrame();
            desktopPane.add(f);
            Icon icon = f.getFrameIcon();
            String title = f.getTitle();
            JComponent c = new JScrollPane(new JTextArea(title));
            if (desktopPane.isShowing()) {
                f.add(c);
            } else {
                tabbedPane.addTab(title, icon, c);
            }
        }
    };

    public MainPanel() {
        super(new BorderLayout());
        panel.add(desktopPane, desktopPane.getClass().getName());
        panel.add(tabbedPane, tabbedPane.getClass().getName());

        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.setFloatable(false);
        toolbar.add(new JButton(addAction));
        toolbar.add(Box.createGlue());
        toolbar.add(new JToggleButton(swapAction));

        add(panel);
        add(toolbar, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private JInternalFrame createInternalFrame() {
        JInternalFrame f = new JInternalFrame(String.format("Document #%s", ++openFrameCount), true, true, true, true);
        row += 1;
        f.setSize(240, 120);
        f.setLocation(20 * row + 20 * col, 20 * row);
        f.setVisible(true);
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                Rectangle drect = desktopPane.getBounds();
                drect.setLocation(0, 0);
                if (!drect.contains(f.getBounds())) {
                    row = 0;
                    col += 1;
                }
            }
        });
        return f;
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }

    public static void createAndShowGUI() {
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
