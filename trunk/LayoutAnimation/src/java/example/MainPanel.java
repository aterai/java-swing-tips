package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    public MainPanel() {
        super(new BorderLayout());
        add(makePanel());
        setPreferredSize(new Dimension(320, 240));
    }
    private final JTree tree       = new JTree();
    private final JTextField field = new JTextField("", 10);
    private final JButton button   = new JButton("Find Next(dummy)");
    private final JButton showHideButton = new JButton();

    private Timer animator = null;
    private boolean isHidden = true;
    private final JPanel controls = new JPanel(new BorderLayout(5, 5) {
        private int controlsHeight = 0;
        private int controlsPreferredHeight = 0;
        @Override public Dimension preferredLayoutSize(Container target) {
            //synchronized (target.getTreeLock()) {
            Dimension ps = super.preferredLayoutSize(target);
            controlsPreferredHeight = ps.height;
            if(animator!=null) {
                if(isHidden) {
                    if(controls.getHeight()<controlsPreferredHeight) { controlsHeight += 5; }
                }else{
                    if(controls.getHeight()>0) { controlsHeight -= 5; }
                }
                if(controlsHeight<=0) {
                    controlsHeight = 0;
                    animator.stop();
                }else if(controlsHeight>=controlsPreferredHeight) {
                    controlsHeight = controlsPreferredHeight;
                    animator.stop();
                }
            }
            ps.height = controlsHeight;
            return ps;
        }
    });
    private Action makeShowHideAction() {
        return new AbstractAction("Show/Hide Search Box") {
            @Override public void actionPerformed(ActionEvent e) {
                if(animator!=null && animator.isRunning()) { return; }
                isHidden = controls.getHeight()==0;
                animator = new Timer(5, new ActionListener() {
                    @Override public void actionPerformed(ActionEvent e) {
                        controls.revalidate();
                    }
                });
                animator.start();
            }
        };
    }

    public JPanel makePanel() {
        button.setFocusable(false);
        controls.setBorder(BorderFactory.createTitledBorder("Search down"));
        controls.add(new JLabel("Find what:"), BorderLayout.WEST);
        controls.add(field);
        controls.add(button, BorderLayout.EAST);
        Action act = makeShowHideAction();
        showHideButton.setAction(act);
        showHideButton.setFocusable(false);
        JPanel p = new JPanel(new BorderLayout());
        InputMap imap = p.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        imap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK), "open-searchbox");
        p.getActionMap().put("open-searchbox", act);
        p.add(controls, BorderLayout.NORTH);
        p.add(new JScrollPane(tree));
        p.add(showHideButton, BorderLayout.SOUTH);
        return p;
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
        //frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
