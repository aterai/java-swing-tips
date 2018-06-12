package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("SpringLayout", makeUI1());
        tabbedPane.addTab("Custom BorderLayout", makeUI2());
        add(tabbedPane);
        setPreferredSize(new Dimension(320, 240));
    }
    private static <E> void move(JList<E> from, JList<E> to) {
        ListSelectionModel sm = from.getSelectionModel();
        int[] selectedIndices = from.getSelectedIndices();
        DefaultListModel<E> fromModel = (DefaultListModel<E>) from.getModel();
        DefaultListModel<E> toModel = (DefaultListModel<E>) to.getModel();
        List<E> unselectedValues = new ArrayList<>();
        for (int i = 0; i < fromModel.getSize(); i++) {
            if (!sm.isSelectedIndex(i)) {
                unselectedValues.add(fromModel.getElementAt(i));
            }
        }
        if (selectedIndices.length > 0) {
            for (int i: selectedIndices) {
                toModel.addElement(fromModel.get(i));
            }
            fromModel.clear();
            // unselectedValues.forEach(fromModel::addElement);
            DefaultListModel<E> model = new DefaultListModel<>();
            unselectedValues.forEach(model::addElement);
            from.setModel(model);
        }
    }
    private static <E> JList<E> makeList(ListModel<E> model) {
        JList<E> list = new JList<>(model);
        JPopupMenu popup = new JPopupMenu();
        popup.add("reverse").addActionListener(e -> {
            ListSelectionModel sm = list.getSelectionModel();
            for (int i = 0; i < list.getModel().getSize(); i++) {
                if (sm.isSelectedIndex(i)) {
                    sm.removeSelectionInterval(i, i);
                } else {
                    sm.addSelectionInterval(i, i);
                }
            }
        });
        list.setComponentPopupMenu(popup);
        return list;
    }
    private static JButton makeButton(String title) {
        JButton button = new JButton(title);
        button.setFocusable(false);
        button.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return button;
    }
    private static Component makeCenterBox(JButton... buttons) {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        box.add(Box.createVerticalGlue());
        for (JButton b: buttons) {
            box.add(b);
            box.add(Box.createVerticalStrut(20));
        }
        box.add(Box.createVerticalGlue());
        return box;
    }
    private Component makeUI1() {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("loooooooooooooooooooooooooooooooooooong");
        IntStream.range(0, 5000).mapToObj(Objects::toString).forEach(model::addElement);
        JList<String> leftList = makeList(model);
        JScrollPane lsp = new JScrollPane(leftList);
        lsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JList<String> rightList = makeList(new DefaultListModel<>());
        JScrollPane rsp = new JScrollPane(rightList);
        rsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton button1 = makeButton(">");
        button1.addActionListener(e -> move(leftList, rightList));

        JButton button2 = makeButton("<");
        button2.addActionListener(e -> move(rightList, leftList));

        Component box = makeCenterBox(button1, button2);

        SpringLayout layout = new SpringLayout();
        JPanel panel = new JPanel(layout);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        SpringLayout.Constraints centerConstraints = layout.getConstraints(box);
        centerConstraints.setWidth(Spring.constant(box.getPreferredSize().width));

        SpringLayout.Constraints leftConstraints = layout.getConstraints(lsp);
        SpringLayout.Constraints rightConstraints = layout.getConstraints(rsp);

        Spring width = Spring.max(leftConstraints.getWidth(), rightConstraints.getWidth());
        leftConstraints.setWidth(width);
        rightConstraints.setWidth(width);

        panel.add(lsp, leftConstraints);
        panel.add(box, centerConstraints);
        panel.add(rsp, rightConstraints);

        // TEST1: Spring height = Spring.max(leftConstraints.getHeight(), rightConstraints.getHeight());
        Spring height = layout.getConstraint(SpringLayout.HEIGHT, panel);
        leftConstraints.setHeight(height);
        rightConstraints.setHeight(height);
        centerConstraints.setHeight(height);

        centerConstraints.setConstraint(SpringLayout.WEST, leftConstraints.getConstraint(SpringLayout.EAST));
        rightConstraints.setConstraint(SpringLayout.WEST, centerConstraints.getConstraint(SpringLayout.EAST));

        layout.putConstraint(SpringLayout.EAST, panel, 0, SpringLayout.EAST, rsp);
        // TEST1: layout.putConstraint(SpringLayout.SOUTH, panel, 0, SpringLayout.SOUTH, rsp);

        return panel;
    }
    private Component makeUI2() {
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("loooooooooooooooooooooooooooooooooooong");
        IntStream.range(0, 5000).mapToObj(Objects::toString).forEach(model::addElement);
        JList<String> leftList = makeList(model);
        JScrollPane lsp = new JScrollPane(leftList);
        lsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JList<String> rightList = makeList(new DefaultListModel<>());
        JScrollPane rsp = new JScrollPane(rightList);
        rsp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        JButton button1 = makeButton(">");
        button1.addActionListener(e -> move(leftList, rightList));

        JButton button2 = makeButton("<");
        button2.addActionListener(e -> move(rightList, leftList));

        Component box = makeCenterBox(button1, button2);

        JPanel panel = new JPanel(new BorderLayout(0, 0) {
            @Override public void layoutContainer(Container target) {
                synchronized (target.getTreeLock()) {
                    Insets insets = target.getInsets();
                    int top = insets.top;
                    int bottom = target.getHeight() - insets.bottom;
                    int left = insets.left;
                    int right = target.getWidth() - insets.right;
                    int hgap = getHgap();
                    int wc = right - left;
                    int we = wc / 2;
                    int ww = wc - we;
                    Component c = getLayoutComponent(CENTER);
                    if (c != null) {
                        Dimension d = c.getPreferredSize();
                        wc -= d.width + hgap + hgap;
                        we = wc / 2;
                        ww = wc - we;
                        c.setBounds(left + hgap + ww, top, wc, bottom - top);
                    }
                    c = getLayoutComponent(EAST);
                    if (c != null) {
                        c.setBounds(right - we, top, we, bottom - top);
                    }
                    c = getLayoutComponent(WEST);
                    if (c != null) {
                        c.setBounds(left, top, ww, bottom - top);
                    }
                }
            }
        });
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(lsp, BorderLayout.WEST);
        panel.add(box, BorderLayout.CENTER);
        panel.add(rsp, BorderLayout.EAST);
        return panel;
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
