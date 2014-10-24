package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());
        JTextArea textArea = new JTextArea("\u2605 \u2606 \u293E \u293F \u2940 \u2941 \u21D0 \u21D2 \u21E6 \u21E8 \u21BA \u21BB \u21B6 \u21B7");
        JPopupMenu popup = makePopup();
        textArea.setComponentPopupMenu(popup);
        add(new JScrollPane(textArea));
        setPreferredSize(new Dimension(320, 240));
    }
    private static JPopupMenu makePopup() {
        JPopupMenu popup = new JPopupMenu();
        GridBagConstraints c = new GridBagConstraints();
        popup.setLayout(new GridBagLayout());
        c.gridheight = 1;

        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.CENTER;

        c.gridwidth = 1;
        c.gridy = 0;
        c.gridx = 0; popup.add(makeButton("\u21E6"), c);
        c.gridx = 1; popup.add(makeButton("\u21E8"), c);
        c.gridx = 2; popup.add(makeButton("\u21BB"), c);
        c.gridx = 3; popup.add(makeButton("\u2729"), c);

        c.gridwidth = 4;
        c.gridx = 0;
        c.insets = new Insets(2, 0, 2, 0);
        c.gridy = 1; popup.add(new JSeparator(), c);
        c.insets = new Insets(0, 0, 0, 0);
        c.gridy = 2; popup.add(new JMenuItem("aaaaaaaaaa"), c);
        c.gridy = 3; popup.add(new JPopupMenu.Separator(), c);
        c.gridy = 4; popup.add(new JMenuItem("bbbb"), c);
        c.gridy = 5; popup.add(new JMenuItem("ccccccccccccccccccccc"), c);
        c.gridy = 6; popup.add(new JMenuItem("dddddddddd"), c);

        return popup;
    }
    private static AbstractButton makeButton(String symbol) {
        final Icon icon = new SymbolIcon(symbol);
        JMenuItem b = new JMenuItem() {
            private final Dimension d = new Dimension(icon.getIconWidth(), icon.getIconHeight());
            @Override public Dimension getPreferredSize() {
                return d;
            }
            @Override public void paintComponent(Graphics g) {
                super.paintComponent(g);
                Dimension cd = getSize();
                Dimension pd = getPreferredSize();
                int offx = (int) (.5 + .5 * (cd.width  - pd.width));
                int offy = (int) (.5 + .5 * (cd.height - pd.height));
                icon.paintIcon(this, g, offx, offy);
            }
        };
        b.setOpaque(true);
        if ("\u21E8".equals(symbol)) { //Test
            b.setEnabled(false);
            b.setToolTipText("forward");
        }
        return b;
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
        } catch (ClassNotFoundException | InstantiationException |
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

class SymbolIcon implements Icon {
    private static final int ICON_SIZE = 32;
    private static Font font = new Font(Font.SANS_SERIF, Font.BOLD, ICON_SIZE);
    private static FontRenderContext frc = new FontRenderContext(null, true, true);
    private final Shape symbol;

    public SymbolIcon(String str) {
         symbol = new TextLayout(str, font, frc).getOutline(null);
    }
    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(x, y);
        g2.setPaint(c.isEnabled() ? Color.BLACK : Color.GRAY);
        Rectangle2D b = symbol.getBounds();
        Point2D p = new Point2D.Double(b.getX() + b.getWidth() / 2d, b.getY() + b.getHeight() / 2d);
        AffineTransform toCenterAT = AffineTransform.getTranslateInstance(getIconWidth() / 2d - p.getX(), getIconHeight() / 2d - p.getY());
        g2.fill(toCenterAT.createTransformedShape(symbol));
        g2.translate(-x, -y);
        g2.dispose();
    }
    @Override public int getIconWidth() {
        return ICON_SIZE;
    }
    @Override public int getIconHeight() {
        return ICON_SIZE;
    }
}
