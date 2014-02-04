package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private final JPanel p1 = new JPanel();
    private final JPanel p2 = new JPanel();
    private final Action defaultFileChooserAction = new AbstractAction("Default") {
        @Override public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int retvalue = fileChooser.showOpenDialog(p1);
            System.out.println(retvalue);
        }
    };
    private final Action fixedSizeFileChooserAction = new AbstractAction("Resizable(false)") {
        @Override public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser() {
                @Override protected JDialog createDialog(Component parent) throws HeadlessException {
                     JDialog dialog = super.createDialog(parent);
                     dialog.setResizable(false);
                     return dialog;
                 }
            };
            int retvalue = fileChooser.showOpenDialog(p1);
            System.out.println(retvalue);
        }
    };
    private final Action minimumSizeFileChooserAction = new AbstractAction("MinimumSize(640,480)(JDK 6)") {
        @Override public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser() {
                @Override protected JDialog createDialog(Component parent) throws HeadlessException {
                     JDialog dialog = super.createDialog(parent);
                     dialog.setMinimumSize(new Dimension(640,480));
                     return dialog;
                 }
            };
            int retvalue = fileChooser.showOpenDialog(p2);
            System.out.println(retvalue);
        }
    };
    private final Action customSizeFileChooserAction = new AbstractAction("MinimumSize(640,480)") {
        @Override public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser() {
                @Override protected JDialog createDialog(Component parent) throws HeadlessException {
                     final JDialog dialog = super.createDialog(parent);
                     dialog.addComponentListener(new ComponentAdapter() {
                         @Override public void componentResized(ComponentEvent e) {
                             int mw = 640;
                             int mh = 480;
                             int fw = dialog.getSize().width;
                             int fh = dialog.getSize().height;
                             dialog.setSize(mw>fw?mw:fw, mh>fh?mh:fh);
                         }
                     });
                     return dialog;
                 }
            };
            int retvalue = fileChooser.showOpenDialog(p2);
            System.out.println(retvalue);
        }
    };

    public MainPanel() {
        super(new GridLayout(2,1));
        p1.setBorder(BorderFactory.createTitledBorder("JFileChooser setResizable"));
        p1.add(new JButton(defaultFileChooserAction));
        p1.add(new JButton(fixedSizeFileChooserAction));

        p2.setBorder(BorderFactory.createTitledBorder("JFileChooser setMinimumSize"));
        p2.add(new JButton(minimumSizeFileChooserAction));
        p2.add(new JButton(customSizeFileChooserAction));
        add(p1);
        add(p2);
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
        }catch(ClassNotFoundException | InstantiationException |
               IllegalAccessException | UnsupportedLookAndFeelException ex) {
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
