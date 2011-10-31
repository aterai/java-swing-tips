package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel{
    private final static String TEST = "1234567890\nabcdefghijklmn";
    private final JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
    private final Font font = new Font("monospaced", Font.PLAIN, 12);
    private final JTextPane editor1 = new JTextPane();
    private final JTextPane editor2 = new JTextPane() {
        float font_size = 0.0f;
        @Override public void doLayout() {
            Insets i = getInsets();
            float f = .08f * (getWidth() - i.left - i.right);
            if(Math.abs(font_size-f) > 1.0e-1) {
                setFont(font.deriveFont(f));
                font_size = f;
            }
            super.doLayout();
        }
    };
    public MainPanel() {
        super(new BorderLayout());
        editor1.setFont(font);
        editor1.setText("Default\n"+TEST);
        editor2.setFont(font);
        editor2.setText("doLayout+deriveFont\n"+TEST);

        sp.setTopComponent(editor1);
        sp.setBottomComponent(editor2);
        sp.setResizeWeight(0.5);
        add(sp);
        setPreferredSize(new Dimension(320, 240));
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                sp.setDividerLocation(0.5);
            }
        });
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
