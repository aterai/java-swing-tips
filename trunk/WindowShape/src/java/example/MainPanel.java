package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private final JTextField textField = new JTextField("\u2605", 20);
    private final FontRenderContext frc = new FontRenderContext(null,true,true);
    private final Font font = new Font("serif", Font.PLAIN, 300);
    private final JLabel label = new JLabel("", SwingConstants.CENTER);
    private final JToggleButton button = new JToggleButton(new AbstractAction("show") {
        private JFrame frame = null;
        @Override public void actionPerformed(ActionEvent e) {
            AbstractButton button = (AbstractButton)e.getSource();
            Window parent = SwingUtilities.getWindowAncestor(button);
            if(frame==null) {
                frame = new JFrame();
                frame.setUndecorated(true);
                frame.setAlwaysOnTop(true);
                frame.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
                frame.getContentPane().add(label);
                frame.getContentPane().setBackground(Color.GREEN);
                frame.pack();
            }
            if(button.isSelected()) {
                String str = textField.getText().trim();
                //label.setText(str);
                TextLayout tl = new TextLayout(str, font, frc);
                Rectangle2D b = tl.getBounds();
                Shape shape = tl.getOutline(AffineTransform.getTranslateInstance(-b.getX(),-b.getY()));

//                 int w = 300;
//                 int h = 300;
//                 GeneralPath p = new GeneralPath();
//                 p.moveTo(- w / 4.0f, - h / 12.0f);
//                 p.lineTo(+ w / 4.0f, - h / 12.0f);
//                 p.lineTo(- w / 6.0f, + h / 4.0f);
//                 p.lineTo(+     0.0f, - h / 4.0f);
//                 p.lineTo(+ w / 6.0f, + h / 4.0f);
//                 p.closePath();
//                 AffineTransform at = AffineTransform.getTranslateInstance(w/4, h/4);
//                 shape = at.createTransformedShape(p);

                frame.setBounds(shape.getBounds());
                //frame.setSize(shape.getBounds().width, shape.getBounds().height);
                com.sun.awt.AWTUtilities.setWindowShape(frame, shape);
                //frame.setShape(shape); // 1.7.0
                frame.setLocationRelativeTo(parent);
                frame.setVisible(true);
            }else{
                frame.setVisible(false);
            }
        }
    });

    public MainPanel() {
        super();
        add(textField);
        add(button);
        DragWindowListener dwl = new DragWindowListener();
        label.addMouseListener(dwl);
        label.addMouseMotionListener(dwl);
        setPreferredSize(new Dimension(320, 240));
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
class DragWindowListener extends MouseAdapter {
    private MouseEvent start;
    private Window window;
    @Override public void mousePressed(MouseEvent me) {
        start = me;
    }
    @Override public void mouseDragged(MouseEvent me) {
        if(window==null) {
            window = SwingUtilities.windowForComponent(me.getComponent());
        }
        Point eventLocationOnScreen = me.getLocationOnScreen();
        window.setLocation(eventLocationOnScreen.x - start.getX(),
                           eventLocationOnScreen.y - start.getY());
    }
}
