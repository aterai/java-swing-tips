package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.synth.*;
//import com.sun.java.swing.Painter; // 1.6.0

public final class MainPanel extends JPanel {
    private final JDesktopPane desktop = new JDesktopPane();
    public MainPanel() {
        super(new BorderLayout());
        JPanel p1 = new JPanel();
        p1.setOpaque(false);
        JPanel p2 = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                //super.paintComponent(g);
                g.setColor(new Color(100, 50, 50, 100));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        p2.setOpaque(false);

//         d.put("InternalFrame[Enabled].backgroundPainter", new Painter<JComponent>() {
//             @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
//                 g.setColor(new Color(100, 200, 100, 100));
//                 g.fillRoundRect(0, 0, w - 1, h - 1, 15, 15);
//             }
//         });
//         d.put("InternalFrame[Enabled+WindowFocused].backgroundPainter", new Painter() {
//             @Override public void paint(Graphics2D g, JComponent c, int w, int h) {
//                 g.setColor(new Color(100, 250, 120, 100));
//                 g.fillRoundRect(0, 0, w - 1, h - 1, 15, 15);
//             }
//         });
        createFrame(initPanel(p1), 0);
        createFrame(initPanel(p2), 1);
        add(desktop);
        setPreferredSize(new Dimension(320, 240));
    }
//     private final UIDefaults d = new UIDefaults();

    private static JPanel initPanel(JPanel p) {
        p.add(new JLabel("label"));
        p.add(new JButton("button"));
        return p;
    }

    protected JInternalFrame createFrame(JPanel panel, int idx) {
        JInternalFrame frame = new MyInternalFrame();
//         frame.putClientProperty("Nimbus.Overrides", d);
//         //frame.putClientProperty("Nimbus.Overrides.InheritDefaults", false);
        frame.setContentPane(panel);
        frame.getRootPane().setOpaque(false);
        frame.setOpaque(false);
        frame.setVisible(true);
        frame.setLocation(10 + 60 * idx, 10 + 40 * idx);
        desktop.add(frame);
        desktop.getDesktopManager().activateFrame(frame);
        return frame;
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
            //UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            for (UIManager.LookAndFeelInfo laf: UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(laf.getName())) {
                    UIManager.setLookAndFeel(laf.getClassName());
                    SynthLookAndFeel.setStyleFactory(
                        new MySynthStyleFactory(SynthLookAndFeel.getStyleFactory()));
                    break;
                }
            }
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

class MyInternalFrame extends JInternalFrame {
    protected MyInternalFrame() {
        super("title", true, true, true, true);
        setSize(160, 100);
    }
}

class MySynthStyleFactory extends SynthStyleFactory {
    private final SynthStyleFactory wrappedFactory;
    protected MySynthStyleFactory(SynthStyleFactory factory) {
        super();
        this.wrappedFactory = factory;
    }
    @Override public SynthStyle getStyle(JComponent c, Region id) {
        SynthStyle s = wrappedFactory.getStyle(c, id);
        //if (id == Region.INTERNAL_FRAME_TITLE_PANE || id == Region.INTERNAL_FRAME) {
        if (id == Region.INTERNAL_FRAME) {
            s = new TranslucentSynthSytle(s);
        }
        return s;
    }
}

class TranslucentSynthSytle extends SynthStyle {
    private final SynthStyle style;
    protected TranslucentSynthSytle(SynthStyle s) {
        super();
        style = s;
    }
    @Override public Object get(SynthContext context, Object key) {
        return style.get(context, key);
    }
    @Override public boolean getBoolean(SynthContext context, Object key, boolean defaultValue) {
        return style.getBoolean(context, key, defaultValue);
    }
    @Override public Color getColor(SynthContext context, ColorType type) {
        return style.getColor(context, type);
    }
    @Override public Font getFont(SynthContext context) {
        return style.getFont(context);
    }
    @Override public SynthGraphicsUtils getGraphicsUtils(SynthContext context) {
        return style.getGraphicsUtils(context);
    }
    @Override public Icon getIcon(SynthContext context, Object key) {
        return style.getIcon(context, key);
    }
    @Override public Insets getInsets(SynthContext context, Insets insets) {
        return style.getInsets(context, insets);
    }
    @Override public int getInt(SynthContext context, Object key, int defaultValue) {
        return style.getInt(context, key, defaultValue);
    }
    @Override public SynthPainter getPainter(final SynthContext context) {
        return new SynthPainter() {
            @Override public void paintInternalFrameBackground(SynthContext context, Graphics g,
                                                               int x, int y, int w, int h) {
                g.setColor(new Color(100, 200, 100, 100));
                g.fillRoundRect(x, y, w - 1, h - 1, 15, 15);
            }
        };
    }
    @Override public String getString(SynthContext context,
                            Object key, String defaultValue) {
        return style.getString(context, key, defaultValue);
    }
    @Override public void installDefaults(SynthContext context) {
        style.installDefaults(context);
    }
    @Override public void uninstallDefaults(SynthContext context) {
        style.uninstallDefaults(context);
    }
    @Override public boolean isOpaque(SynthContext context) {
        if (context.getRegion() == Region.INTERNAL_FRAME) {
            return false;
        } else {
            return style.isOpaque(context);
        }
    }
    @Override public Color getColorForState(SynthContext context, ColorType type) {
        return null; //Color.RED;
    }
    @Override public Font getFontForState(SynthContext context) {
        return null; //new Font(Font.MONOSPACED, Font.ITALIC, 24);
    }
}
