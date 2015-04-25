package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.plaf.basic.*;

public final class MainPanel extends JPanel {
    private static final String DISABLED_ARE_NAVIGABLE = "MenuItem.disabledAreNavigable";
    private final JCheckBox disabledAreNavigableCheck = new JCheckBox(new AbstractAction(DISABLED_ARE_NAVIGABLE) {
        @Override public void actionPerformed(ActionEvent e) {
            Boolean b = ((JCheckBox) e.getSource()).isSelected();
            UIManager.put(DISABLED_ARE_NAVIGABLE, b);
        }
    });
    private MainPanel() {
        super();
        Boolean b = UIManager.getBoolean(DISABLED_ARE_NAVIGABLE);
        System.out.println(DISABLED_ARE_NAVIGABLE + ": " + b);
        disabledAreNavigableCheck.setSelected(b);
        add(disabledAreNavigableCheck);

//         EventQueue.invokeLater(new Runnable() {
//             @Override public void run() {
//                 ActionListener al = new ActionListener() {
//                     @Override public void actionPerformed(final ActionEvent e) {
//                         EventQueue.invokeLater(new Runnable() {
//                             @Override public void run() {
//                                 Object o = e.getSource();
//                                 if (o instanceof JRadioButtonMenuItem) {
//                                     JRadioButtonMenuItem rbmi = (JRadioButtonMenuItem) o;
//                                     if (rbmi.isSelected()) {
//                                         Boolean b = UIManager.getBoolean(DISABLED_ARE_NAVIGABLE);
//                                         System.out.println(rbmi.getText() + ": " + b);
//                                         disabledAreNavigableCheck.setSelected(b);
//                                     }
//                                 }
//                             }
//                         });
//                     }
//                 };
//                 List<JRadioButtonMenuItem> list = new ArrayList<>();
//                 ManuBarUtil.searchAllMenuElements(getRootPane().getJMenuBar(), list);
//                 for (JRadioButtonMenuItem mi: list) {
//                     mi.addActionListener(al);
//                 }
//             }
//         });

        JPopupMenu popup = new JPopupMenu();
        ManuBarUtil.initMenu(popup);
        setComponentPopupMenu(popup);
        setPreferredSize(new Dimension(320, 240));
    }
    @Override public void updateUI() {
        super.updateUI();
        if (Objects.nonNull(disabledAreNavigableCheck)) {
            Boolean b = UIManager.getBoolean(DISABLED_ARE_NAVIGABLE);
            disabledAreNavigableCheck.setSelected(b);
        }
    }
    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
//         try {
//             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//         } catch (ClassNotFoundException | InstantiationException
//                | IllegalAccessException | UnsupportedLookAndFeelException ex) {
//             ex.printStackTrace();
//         }
        JMenuBar menuBar = ManuBarUtil.createMenuBar();

//         Stream.of(menuBar)
//           .flatMap(new Function<MenuElement, Stream<MenuElement>>() {
//               @Override public Stream<MenuElement> apply(MenuElement me) {
//                   return Stream.concat(Stream.of(me), Stream.of(me.getSubElements()).flatMap(e -> apply(e)));
//               }
//           })
//           .filter(mi -> mi instanceof JRadioButtonMenuItem)
//           .forEach(mi -> System.out.println("----\n" + mi.getClass()));

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

class ExitAction extends AbstractAction {
    public ExitAction() {
        super("Exit");
    }
    @Override public void actionPerformed(ActionEvent e) {
        JComponent c = (JComponent) e.getSource();
        Window window = null;
        Container parent = c.getParent();
        if (parent instanceof JPopupMenu) {
            JPopupMenu popup = (JPopupMenu) parent;
            JComponent invoker = (JComponent) popup.getInvoker();
            window = SwingUtilities.getWindowAncestor(invoker);
        } else if (parent instanceof JToolBar) {
            JToolBar toolbar = (JToolBar) parent;
            if (((BasicToolBarUI) toolbar.getUI()).isFloating()) {
                window = SwingUtilities.getWindowAncestor(toolbar).getOwner();
            } else {
                window = SwingUtilities.getWindowAncestor(toolbar);
            }
        } else {
            Component invoker = c.getParent();
            window = SwingUtilities.getWindowAncestor(invoker);
        }
        if (Objects.nonNull(window)) {
            //window.dispose();
            window.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
        }
    }
}

final class ManuBarUtil {
    private ManuBarUtil() { /* Singleton */ }
    public static JMenuBar createMenuBar() {
        JMenuBar mb = new JMenuBar();
        JMenu m = new JMenu("File");
        initMenu(m);
        mb.add(m);
        m = createMenu("Edit");
        mb.add(m);
        m = LookAndFeelUtil.createLookAndFeelMenu();
        mb.add(m);
        mb.add(Box.createGlue());
        m = new JMenu("Help");
        m.add("About");
        mb.add(m);
        return mb;
    }
    private static JMenu createMenu(String key) {
        JMenu menu = new JMenu(key);
        for (String k: Arrays.asList("Cut", "Copy", "Paste", "Delete")) {
            JMenuItem m = new JMenuItem(k);
            m.setEnabled(false);
            menu.add(m);
        }
        return menu;
    }
    public static void initMenu(JComponent p) {
        JMenuItem item = new JMenuItem("Open(disabled)");
        item.setEnabled(false);
        p.add(item);
        item = new JMenuItem("Save(disabled)");
        item.setEnabled(false);
        p.add(item);
        p.add(new JSeparator());
        p.add(new JMenuItem(new ExitAction()));
    }
    public static void searchAllMenuElements(MenuElement me, List<JRadioButtonMenuItem> list) {
        if (me instanceof JRadioButtonMenuItem) {
            list.add((JRadioButtonMenuItem) me);
        }
        MenuElement[] sub = me.getSubElements();
        if (sub.length != 0) {
            for (MenuElement e: sub) {
                searchAllMenuElements(e, list);
            }
        }
    }
}

//http://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lookAndFeelRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lookAndFeelRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, final ButtonGroup lookAndFeelRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try {
                    setLookAndFeel(m.getActionCommand());
                } catch (ClassNotFoundException | InstantiationException
                       | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            //firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
