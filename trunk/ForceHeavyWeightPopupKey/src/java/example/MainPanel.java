package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.lang.reflect.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JComponent glass = new MyGlassPane();
    private final JLabel label1 = makeLabel("11111111111111111111");
    private final JLabel label2 = makeLabel("22222222222222222222");
    public MainPanel(final JFrame frame) {
        super();
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

        try{
            Field field;
            if(System.getProperty("java.version").startsWith("1.6.0")) {
                // https://forums.oracle.com/thread/1357949 ComboBox scroll and selected/highlight on glasspane
                Class clazz = Class.forName("javax.swing.PopupFactory");
                field = clazz.getDeclaredField("forceHeavyWeightPopupKey");
            }else{ //JDK 1.7.0, 1.8.0
                Class clazz = Class.forName("javax.swing.ClientPropertyKey");
                field = clazz.getDeclaredField("PopupFactory_FORCE_HEAVYWEIGHT_POPUP");
            }
            field.setAccessible(true);
            label2.putClientProperty(field.get(null), Boolean.TRUE);
        }catch(Exception ex) {
            ex.printStackTrace();
        }
        glass.add(label1, BorderLayout.WEST);
        glass.add(label2, BorderLayout.EAST);
        glass.add(Box.createVerticalStrut(60), BorderLayout.SOUTH);
        frame.setGlassPane(glass);
        frame.getGlassPane().setVisible(true);

//         add(new JButton(new AbstractAction("show GlassPane") {
//             @Override public void actionPerformed(ActionEvent e) {
//                 final JButton button = (JButton)e.getSource();
//                 button.setEnabled(false);
//                 frame.getGlassPane().setVisible(true);
//                 new SwingWorker() {
//                     @Override public Object doInBackground() {
//                         try{
//                             Thread.sleep(8000);
//                         }catch(Exception ex) {
//                             ex.printStackTrace();
//                         }
//                         return "Done";
//                     }
//                     @Override public void done() {
//                         frame.getGlassPane().setVisible(false);
//                         button.setEnabled(true);
//                     }
//                 }.execute();
//             }
//         }));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JLabel makeLabel(String title) {
        JLabel label = new JLabel(title);
        label.setOpaque(true);
        label.setBackground(Color.ORANGE);
        label.setToolTipText("1234567890");
        return label;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGUI();
            }
        });
    }
    public static void createAndShowGUI() {
        try{
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel(frame));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
class MyGlassPane extends JPanel {
    private static final Color bgc = new Color(200,100,100,100);
    public MyGlassPane() {
        super(new BorderLayout());
        setOpaque(false);
    }
    @Override public void paintComponent(Graphics g) {
        g.setColor(bgc);
        g.fillRect(0,0,getWidth(),getHeight());
        super.paintComponent(g);
    }
}
