package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.Optional;
import java.util.stream.Stream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        DefaultListModel<String> model = new DefaultListModel<>();
        Stream.of("aa", "bbbbbbbbbbbbb", "ccc", "dddddddddddddddd", "eeeeeee").forEach(model::addElement);

        add(new JScrollPane(new LinkCellList<>(model)));
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

class LinkCellList<E> extends JList<E> {
    private int prevIndex = -1;
    protected LinkCellList(ListModel<E> model) {
        super(model);
    }
    @Override public void updateUI() {
        setForeground(null);
        setBackground(null);
        setSelectionForeground(null);
        setSelectionBackground(null);
        super.updateUI();
        setFixedCellHeight(32);
        setCellRenderer(new LinkCellRenderer<>());
        // TEST: putClientProperty("List.isFileList", Boolean.TRUE);
    }
    @Override protected void processMouseMotionEvent(MouseEvent e) {
        Point pt = e.getPoint();
        int i = locationToIndex(pt);
        E s = getModel().getElementAt(i);
        Component c = getCellRenderer().getListCellRendererComponent(this, s, i, false, false);
        Rectangle r = getCellBounds(i, i);
        c.setBounds(r);
        if (prevIndex != i) {
            c.doLayout();
        }
        prevIndex = i;
        pt.translate(-r.x, -r.y);
        setCursor(Optional.ofNullable(SwingUtilities.getDeepestComponentAt(c, pt.x, pt.y))
            .map(Component::getCursor)
            .orElse(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)));
    }
}

class LinkCellRenderer<E> implements ListCellRenderer<E> {
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    private final JCheckBox check = new JCheckBox("check") {
        @Override public void updateUI() {
            super.updateUI();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            setOpaque(false);
        }
    };
    private final JButton button = new JButton("button") {
        @Override public void updateUI() {
            super.updateUI();
            setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        }
    };
    private final JLabel label = new JLabel() {
        @Override public void updateUI() {
            super.updateUI();
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    };
    @Override public Component getListCellRendererComponent(JList<? extends E> list, E value, int index, boolean isSelected, boolean cellHasFocus) {
        panel.removeAll();
        panel.add(label);
        panel.add(check);
        panel.add(button);
        panel.setOpaque(true);
        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }
        label.setText("<html><a href='#'>" + value);
        return panel;
    }
}
