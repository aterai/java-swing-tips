package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import java.util.Objects;
import java.util.stream.Stream;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.plaf.basic.*;
import javax.swing.text.DefaultEditorKit;

public final class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    private final JMenuBar menuBar = new JMenuBar();
    private final JMenuBar dummyBar = new JMenuBar();

    public MainPanel() {
        super(new BorderLayout());

        JButton button = new JButton(new ModalInternalFrameAction3("Show"));
        button.setMnemonic(KeyEvent.VK_S);
        JInternalFrame internal = new JInternalFrame("Button");
        internal.getContentPane().add(button);
        internal.setSize(100, 100);
        internal.setLocation(20, 20);
        internal.setVisible(true);
        desktop.add(internal);

        dummyBar.add(new JMenu("Frame"));
        add(dummyBar, BorderLayout.NORTH);
        dummyBar.setVisible(false);

        JMenu menu = new JMenu("Frame");
        menu.setMnemonic(KeyEvent.VK_F);
        menuBar.add(menu);

        JMenuItem menuItem = new JMenuItem(new AbstractAction("New Frame") {
            private int openFrameCount;
            @Override public void actionPerformed(ActionEvent e) {
                JInternalFrame iframe = new JInternalFrame("title", true, true, true, true);
                iframe.setSize(130, 100);
                iframe.setLocation(30 * openFrameCount, 30 * openFrameCount);
                getDesktop().add(iframe);
                iframe.setVisible(true);
                openFrameCount++;
            }
        });
        menuItem.setMnemonic(KeyEvent.VK_N);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.ALT_DOWN_MASK));
        menu.add(menuItem);

        menu.addSeparator();

        menuItem = menu.add(new ModalInternalFrameAction1("InternalMessageDialog(Nomal)"));
        menuItem.setMnemonic(KeyEvent.VK_1);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.ALT_DOWN_MASK));

        menuItem = menu.add(new ModalInternalFrameAction2("InternalMessageDialog"));
        menuItem.setMnemonic(KeyEvent.VK_2);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.ALT_DOWN_MASK));

        menuItem = menu.add(new ModalInternalFrameAction3("InternalMessageDialog(Print)"));
        menuItem.setMnemonic(KeyEvent.VK_3);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.ALT_DOWN_MASK));

//         JButton b = new JButton(new AbstractAction("dummy button") {
//             @Override public void actionPerformed(ActionEvent e) {
//                 Toolkit.getDefaultToolkit().beep();
//             }
//         });
        JButton b = new JButton(new DefaultEditorKit.BeepAction());
        b.setMnemonic(KeyEvent.VK_B);
        add(b, BorderLayout.SOUTH);
        add(desktop);
        EventQueue.invokeLater(() -> getRootPane().setJMenuBar(menuBar));
        setPreferredSize(new Dimension(320, 240));
    }

    protected JDesktopPane getDesktop() {
        return desktop;
    }

    // menuItem = new JMenuItem(new ModalInternalFrameAction1("InternalMessageDialog(Nomal)"));
    // menuItem.setMnemonic(KeyEvent.VK_1);
    class ModalInternalFrameAction1 extends AbstractAction {
        protected ModalInternalFrameAction1(String label) {
            super(label);
        }
        @Override public void actionPerformed(ActionEvent e) {
            setJMenuEnabled(false);
            JOptionPane.showInternalMessageDialog(getDesktop(), "information", "modal1", JOptionPane.INFORMATION_MESSAGE);
            setJMenuEnabled(true);
        }
    }

    // menuItem = new JMenuItem(new ModalInternalFrameAction2("InternalMessageDialog"));
    // menuItem.setMnemonic(KeyEvent.VK_2);
    class ModalInternalFrameAction2 extends AbstractAction {
        private final JComponent glass = new MyGlassPane();
        protected ModalInternalFrameAction2(String label) {
            super(label);
            glass.setOpaque(false);
            glass.setVisible(false);
            getDesktop().add(glass, JLayeredPane.MODAL_LAYER);
        }
        @Override public void actionPerformed(ActionEvent e) {
            setJMenuEnabled(false);
            Window w = SwingUtilities.getWindowAncestor(getDesktop());
            Rectangle screen = w.getGraphicsConfiguration().getBounds();
            glass.setSize(screen.width, screen.height);
            glass.setVisible(true);
            JOptionPane.showInternalMessageDialog(getDesktop(), "information", "modal2", JOptionPane.INFORMATION_MESSAGE);
            glass.setVisible(false);
            setJMenuEnabled(true);
        }
    }

    // menuItem = new JMenuItem(new ModalInternalFrameAction3("Modal"));
    // menuItem.setMnemonic(KeyEvent.VK_3);
    // Creating Modal Internal Frames -- Approach 1 and Approach 2
    // http://java.sun.com/developer/JDCTechTips/2001/tt1220.html
    class ModalInternalFrameAction3 extends AbstractAction {
        protected final JComponent glass = new PrintGlassPane();
        protected ModalInternalFrameAction3(String label) {
            super(label);
            glass.setVisible(false);
        }
        @Override public void actionPerformed(ActionEvent e) {
            JOptionPane optionPane = new JOptionPane();
            // TEST: UIManager.put("InternalFrame.titleButtonToolTipsOn", Boolean.FALSE);
            JInternalFrame modal = optionPane.createInternalFrame(getDesktop(), "modal3");
            // TEST: UIManager.put("InternalFrame.titleButtonToolTipsOn", Boolean.TRUE);
//*
            optionPane.setMessage("Hello, World");
            optionPane.setMessageType(JOptionPane.INFORMATION_MESSAGE);
            removeSystemMenuListener(modal);
/*/
            // GlassPane + JComboBox Test:
            String[] items = {"Banana", "Apple", "Pear", "Grape", "Kiwi"};
            // JComboBox<String> combo = new JComboBox<>(items);
            JComboBox combo = new JComboBox(items);
            combo.setEditable(true);
            try {
                Field field;
                if (System.getProperty("java.version").startsWith("1.6.0")) {
                    Class clazz = Class.forName("javax.swing.PopupFactory");
                    field = clazz.getDeclaredField("forceHeavyWeightPopupKey");
                } else { // 1.7.0, 1.8.0
                    Class clazz = Class.forName("javax.swing.ClientPropertyKey");
                    field = clazz.getDeclaredField("PopupFactory_FORCE_HEAVYWEIGHT_POPUP");
                }
                field.setAccessible(true);
                modal.putClientProperty(field.get(null), Boolean.TRUE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            optionPane.setMessage(combo);
            optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
//*/
            modal.addInternalFrameListener(new InternalFrameAdapter() {
                @Override public void internalFrameClosed(InternalFrameEvent e) {
                    glass.removeAll();
                    glass.setVisible(false);
                }
            });
            glass.add(modal);
            modal.pack();
//             Rectangle screen = desktop.getBounds();
//             modal.setLocation(screen.x + screen.width  / 2 - modal.getSize().width  / 2,
//                               screen.y + screen.height / 2 - modal.getSize().height / 2);
            getRootPane().setGlassPane(glass);
            glass.setVisible(true);
            modal.setVisible(true);
        }
    }

    protected void setJMenuEnabled(boolean flag) {
        JMenuBar bar = getRootPane().getJMenuBar();
        bar.setVisible(flag);
        dummyBar.setVisible(!flag);
    }

    protected static void removeSystemMenuListener(JInternalFrame modal) {
        BasicInternalFrameUI ui = (BasicInternalFrameUI) modal.getUI();
        JComponent titleBar = (JComponent) ui.getNorthPane();
        Stream.of(titleBar.getComponents())
            .filter(c -> JLabel.class.isInstance(c) || "InternalFrameTitlePane.menuButton".equals(c.getName()))
            .forEach(MainPanel::removeComponentMouseListener);
//         for (Component c: titleBar.getComponents()) {
//             if (c instanceof JLabel || "InternalFrameTitlePane.menuButton".equals(c.getName())) {
//                 removeComponentMouseListener(c)
//             }
//         }
    }

    protected static void removeComponentMouseListener(Component c) {
        for (MouseListener ml: c.getMouseListeners()) {
            ((JComponent) c).removeMouseListener(ml);
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

class MyGlassPane extends JDesktopPane {
    private static final TexturePaint TEXTURE = TextureFactory.createCheckerTexture(6);
//     protected MyGlassPane() {
//         super((LayoutManager) null);
//     }
    @Override public void updateUI() {
        // setFocusTraversalPolicy(null);
        super.updateUI();
        setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
            @Override public boolean accept(Component c) {
                return false;
            }
        });
    }
    @Override protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

class PrintGlassPane extends JDesktopPane {
    private static final TexturePaint TEXTURE = TextureFactory.createCheckerTexture(4);
//     protected PrintGlassPane() {
//         super((LayoutManager) null);
//     }
    @Override public void setVisible(boolean isVisible) {
        boolean oldVisible = isVisible();
        super.setVisible(isVisible);
        JRootPane rootPane = getRootPane();
        if (Objects.nonNull(rootPane) && isVisible() != oldVisible) {
            rootPane.getLayeredPane().setVisible(!isVisible);
        }
    }
    @Override protected void paintComponent(Graphics g) {
        JRootPane rootPane = getRootPane();
        if (Objects.nonNull(rootPane)) {
            // http://weblogs.java.net/blog/alexfromsun/archive/2008/01/disabling_swing.html
            // it is important to call print() instead of paint() here
            // because print() doesn't affect the frame's double buffer
            rootPane.getLayeredPane().print(g);
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setPaint(TEXTURE);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }
}

final class TextureFactory {
    private static final Color DEFAULT_COLOR = new Color(100, 100, 100, 100);
    private TextureFactory() { /* Singleton */ }
    public static TexturePaint createCheckerTexture(int cs, Color color) {
        int size = cs * cs;
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setPaint(color);
        g2.fillRect(0, 0, size, size);
        for (int i = 0; i * cs < size; i++) {
            for (int j = 0; j * cs < size; j++) {
                if ((i + j) % 2 == 0) {
                    g2.fillRect(i * cs, j * cs, cs, cs);
                }
            }
        }
        g2.dispose();
        return new TexturePaint(img, new Rectangle(size, size));
    }
    public static TexturePaint createCheckerTexture(int cs) {
        return createCheckerTexture(cs, DEFAULT_COLOR);
    }
}
