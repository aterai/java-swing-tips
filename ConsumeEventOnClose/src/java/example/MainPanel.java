package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JPopupMenu popup  = new JPopupMenu();
        popup.add("000");
        popup.add("11111");
        popup.add("2222222");
        popup.add("33333333333333");

        MenuToggleButton b = new MenuToggleButton("Popup");
        b.setPopupMenu(popup);

        setComponentPopupMenu(popup);

        JButton beep = new JButton("Beep");
        beep.addActionListener(e -> {
            Toolkit.getDefaultToolkit().beep();
            System.out.println("Beep button clicked");
        });

        JComboBox<String> combo1 = new JComboBox<>(new String[] {"00000", "111", "2"});
        JComboBox<String> combo2 = new JComboBox<>(new String[] {"00000", "111", "2"});
        combo2.setEditable(true);
        JTextField textField = new JTextField(16);

        String key = "PopupMenu.consumeEventOnClose";
        JCheckBox check = new JCheckBox(key, UIManager.getBoolean(key)) {
            @Override public void updateUI() {
                super.updateUI();
                boolean f = UIManager.getLookAndFeelDefaults().getBoolean(key);
                setSelected(f);
                UIManager.put(key, f);
            }
        };
        check.addActionListener(e -> {
            JCheckBox c = (JCheckBox) e.getSource();
            UIManager.put(key, c.isSelected());
        });

        JToolBar toolbar = new JToolBar("toolbar");
        toolbar.add(b);
        toolbar.add(Box.createGlue());

        JPanel p = new JPanel();
        p.add(beep);
        p.add(combo1);
        p.add(combo2);
        p.add(textField);

        add(toolbar, BorderLayout.NORTH);
        add(p);
        add(check, BorderLayout.SOUTH);
        setPreferredSize(new Dimension(320, 240));
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

class MenuArrowIcon implements Icon {
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.setPaint(Color.BLACK);
        g2.drawLine(2, 3, 6, 3);
        g2.drawLine(3, 4, 5, 4);
        g2.drawLine(4, 5, 4, 5);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return 9;
    }
    @Override public int getIconHeight() {
        return 9;
    }
}

class MenuToggleButton extends JToggleButton {
    private static final Icon ARROW_ICON = new MenuArrowIcon();
    protected JPopupMenu pop;

    protected MenuToggleButton() {
        this("", null);
    }
    protected MenuToggleButton(Icon icon) {
        this("", icon);
    }
    protected MenuToggleButton(String text) {
        this(text, null);
    }
    protected MenuToggleButton(String text, Icon icon) {
        super();
        Action action = new AbstractAction(text) {
            @Override public void actionPerformed(ActionEvent e) {
                Component b = (Component) e.getSource();
                if (Objects.nonNull(pop)) {
                    pop.show(b, 0, b.getHeight());
                }
            }
        };
        action.putValue(Action.SMALL_ICON, icon);
        setAction(action);
        setFocusable(false);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4 + ARROW_ICON.getIconWidth()));
    }
    public void setPopupMenu(JPopupMenu pop) {
        this.pop = pop;
        pop.addPopupMenuListener(new PopupMenuListener() {
            @Override public void popupMenuCanceled(PopupMenuEvent e) { /* not needed */ }
            @Override public void popupMenuWillBecomeVisible(PopupMenuEvent e) { /* not needed */ }
            @Override public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                setSelected(false);
            }
        });
    }
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Dimension dim = getSize();
        Insets ins = getInsets();
        int x = dim.width - ins.right;
        int y = ins.top + (dim.height - ins.top - ins.bottom - ARROW_ICON.getIconHeight()) / 2;
        ARROW_ICON.paintIcon(this, g, x, y);
    }
}
