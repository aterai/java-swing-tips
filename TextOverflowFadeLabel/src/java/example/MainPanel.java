package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        String text = "012345678901234567890123456789012345678901234567890123456789";
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makeTitledPanel("defalut JLabel ellipsis", new JLabel(text)));
        box.add(Box.createVerticalStrut(15));
        box.add(makeTitledPanel("html JLabel fade out", new FadeOutLabel("<html>" + text)));
        box.add(Box.createVerticalStrut(15));
        box.add(makeTitledPanel("JTextField fade out", new FadingOutLabel(text)));
        box.add(Box.createVerticalGlue());

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }

    private static Component makeTitledPanel(String title, Component c) {
        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createTitledBorder(title));
        box.add(Box.createVerticalStrut(2));
        box.add(c);
        return box;
    }

    public static void main(String... args) {
        EventQueue.invokeLater(new Runnable() {
            @Override public void run() {
                createAndShowGui();
            }
        });
    }

    public static void createAndShowGui() {
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

class FadeOutLabel extends JLabel {
    private static final int LENGTH = 20;
    private static final float DIFF = .05f;

    protected FadeOutLabel(String text) {
        super(text);
    }

    @Override public void paintComponent(Graphics g) {
        Insets i = getInsets();
        int w = getWidth() - i.left - i.right;
        int h = getHeight() - i.top - i.bottom;

        Rectangle rect = new Rectangle(i.left, i.top, w - LENGTH, h);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setFont(g.getFont());
        g2.setClip(rect);
        g2.setComposite(AlphaComposite.SrcOver.derive(.99999f));
        super.paintComponent(g2);

        rect.width = 1;
        float alpha = 1f;
        for (int x = w - LENGTH; x < w; x++) {
            rect.x = x;
            alpha = Math.max(0f, alpha - DIFF);
            g2.setComposite(AlphaComposite.SrcOver.derive(alpha));
            g2.setClip(rect);
            super.paintComponent(g2);
        }
        g2.dispose();
    }
}

class FadingOutLabel extends JTextField {
    private static final int LENGTH = 20;
    private final Dimension dim = new Dimension();
    private transient BufferedImage img;

    protected FadingOutLabel(String text) {
        super(text);
    }

    @Override public void updateUI() {
        super.updateUI();
        setOpaque(false);
        setEditable(false);
        setFocusable(false);
        setEnabled(false);
        setBorder(BorderFactory.createEmptyBorder());
    }

    @Override public void paintComponent(Graphics g) {
        // super.paintComponent(g);
        int w = getWidth();
        int h = getHeight();
        if (img == null || dim.width != w || dim.height != h) {
            dim.setSize(w, h);
            img = updateImage(dim);
        }
        g.drawImage(img, 0, 0, this);
    }

    private BufferedImage updateImage(Dimension d) {
        img = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setFont(getFont());
        g2.setPaint(getForeground());
        FontRenderContext frc = g2.getFontRenderContext();
        TextLayout tl = new TextLayout(getText(), getFont(), frc);
        int baseline = getBaseline(d.width, d.height);
        tl.draw(g2, getInsets().left, baseline);
        g2.dispose();

        int spx = Math.max(0, d.width - LENGTH);
        for (int x = 0; x < LENGTH; x++) {
            double factor = 1d - x / (double) LENGTH;
            for (int y = 0; y < d.height; y++) {
                int argb = img.getRGB(spx + x, y);
                int rgb = argb & 0x00FFFFFF;
                int a = (argb >> 24) & 0xFF;
                img.setRGB(spx + x, y, ((int) (a * factor) << 24) | rgb);
            }
        }
        return img;
    }
}
