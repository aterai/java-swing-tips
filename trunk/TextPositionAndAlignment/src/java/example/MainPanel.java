package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import javax.swing.*;

public class MainPanel extends JPanel {
    private static enum Vertical {
        TOP, CENTER, BOTTOM;
    }
    private static enum Horizontal {
        LEFT, CENTER, RIGHT, LEADING, TRAILING;
    }
    private final JComboBox<Vertical> verticalAlignmentChoices        = new JComboBox<>(Vertical.values());
    private final JComboBox<Vertical> verticalTextPositionChoices     = new JComboBox<>(Vertical.values());
    private final JComboBox<Horizontal> horizontalAlignmentChoices    = new JComboBox<>(Horizontal.values());
    private final JComboBox<Horizontal> horizontalTextPositionChoices = new JComboBox<>(Horizontal.values());
    private final JLabel label   = new JLabel("Test Test", new StarburstIcon(), SwingConstants.CENTER);

    public MainPanel() {
        super(new BorderLayout());
        label.setOpaque(true);
        label.setBackground(Color.WHITE);

        //default
        verticalAlignmentChoices.setSelectedItem(Vertical.CENTER);
        verticalTextPositionChoices.setSelectedItem(Vertical.CENTER);
        horizontalAlignmentChoices.setSelectedItem(Horizontal.CENTER);
        horizontalTextPositionChoices.setSelectedItem(Horizontal.TRAILING);

        ItemListener il = new ItemListener() {
            @Override public void itemStateChanged(ItemEvent e) {
                if(e.getStateChange()==ItemEvent.SELECTED) {
                    initTitleBorder();
                }
            }
        };
        verticalAlignmentChoices.addItemListener(il);
        verticalTextPositionChoices.addItemListener(il);
        horizontalAlignmentChoices.addItemListener(il);
        horizontalTextPositionChoices.addItemListener(il);

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBorder(BorderFactory.createTitledBorder("JLabel Test"));
        p1.add(label);

        JPanel p2 = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridheight = 1;
        c.gridx   = 0;
        c.insets  = new Insets(5, 5, 5, 0);
        c.anchor  = GridBagConstraints.WEST;
        c.gridy   = 0; p2.add(new JLabel("setVerticalAlignment:"), c);
        c.gridy   = 1; p2.add(new JLabel("setVerticalTextPosition:"), c);
        c.gridy   = 2; p2.add(new JLabel("setHorizontalAlignment:"), c);
        c.gridy   = 3; p2.add(new JLabel("setHorizontalTextPosition:"), c);
        c.gridx   = 1;
        c.weightx = 1.0;
        c.fill    = GridBagConstraints.HORIZONTAL;
        c.gridy   = 0; p2.add(verticalAlignmentChoices, c);
        c.gridy   = 1; p2.add(verticalTextPositionChoices, c);
        c.gridy   = 2; p2.add(horizontalAlignmentChoices, c);
        c.gridy   = 3; p2.add(horizontalTextPositionChoices, c);

        add(p1);
        add(p2, BorderLayout.NORTH);
        setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 240));
    }
    private void initTitleBorder() {
        switch((Vertical)verticalAlignmentChoices.getSelectedItem()) {
          case TOP:    label.setVerticalAlignment(SwingConstants.TOP);    break;
          case CENTER: label.setVerticalAlignment(SwingConstants.CENTER); break;
          case BOTTOM: label.setVerticalAlignment(SwingConstants.BOTTOM); break;
        }
        switch((Vertical)verticalTextPositionChoices.getSelectedItem()) {
          case TOP:    label.setVerticalTextPosition(SwingConstants.TOP);    break;
          case CENTER: label.setVerticalTextPosition(SwingConstants.CENTER); break;
          case BOTTOM: label.setVerticalTextPosition(SwingConstants.BOTTOM); break;
        }
        switch((Horizontal)horizontalAlignmentChoices.getSelectedItem()) {
          case LEFT:     label.setHorizontalAlignment(SwingConstants.LEFT);     break;
          case CENTER:   label.setHorizontalAlignment(SwingConstants.CENTER);   break;
          case RIGHT:    label.setHorizontalAlignment(SwingConstants.RIGHT);    break;
          case LEADING:  label.setHorizontalAlignment(SwingConstants.LEADING);  break;
          case TRAILING: label.setHorizontalAlignment(SwingConstants.TRAILING); break;
        }
        switch((Horizontal)horizontalTextPositionChoices.getSelectedItem()) {
          case LEFT:     label.setHorizontalTextPosition(SwingConstants.LEFT);     break;
          case CENTER:   label.setHorizontalTextPosition(SwingConstants.CENTER);   break;
          case RIGHT:    label.setHorizontalTextPosition(SwingConstants.RIGHT);    break;
          case LEADING:  label.setHorizontalTextPosition(SwingConstants.LEADING);  break;
          case TRAILING: label.setHorizontalTextPosition(SwingConstants.TRAILING); break;
        }
        label.repaint();
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
class StarburstIcon implements Icon {
    private static final int R2 = 24;
    private static final int R1 = 20;
    private static final int VC = 18;
    private final AffineTransform at;
    private final Shape star;
    public StarburstIcon() {
        double agl = 0.0;
        double add = 2*Math.PI/(VC*2);
        Path2D.Double p = new Path2D.Double();
        p.moveTo(R2*1, R2*0);
        for(int i=0;i<VC*2-1;i++) {
            agl+=add;
            if(i%2==0) {
                p.lineTo(R1*Math.cos(agl), R1*Math.sin(agl));
            }else{
                p.lineTo(R2*Math.cos(agl), R2*Math.sin(agl));
            }
        }
        p.closePath();
        at = AffineTransform.getRotateInstance(-Math.PI/2,R2,0);
        star = new Path2D.Double(p, at);
    }
    @Override public int getIconWidth() {
        return 2*R2;
    }
    @Override public int getIconHeight() {
        return 2*R2;
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.translate(x, y);
        g2d.setPaint(Color.YELLOW);
        g2d.fill(star);
        g2d.setPaint(Color.BLACK);
        g2d.draw(star);
        g2d.translate(-x, -y);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}
