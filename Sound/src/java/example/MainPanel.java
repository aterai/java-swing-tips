package example;
// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@
import java.awt.*;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public final class MainPanel extends JPanel {
    private MainPanel() {
        super(new BorderLayout());

        JButton b1 = new JButton("play");
        b1.addActionListener(e -> loadAndPlayAudio("notice1.wav"));

        JButton b2 = new JButton("play");
        b2.addActionListener(e -> loadAndPlayAudio("notice2.wav"));

        Box box = Box.createVerticalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        box.add(makeTitledPanel("notice1.wav", b1));
        box.add(Box.createVerticalStrut(5));
        box.add(makeTitledPanel("notice2.wav", b2));

        add(box, BorderLayout.NORTH);
        setPreferredSize(new Dimension(320, 240));
    }
    protected void loadAndPlayAudio(String path) {
        try (AudioInputStream sound = AudioSystem.getAudioInputStream(getClass().getResource(path))) {
            AudioFormat format = sound.getFormat();
            DataLine.Info di = new DataLine.Info(Clip.class, format);
            Clip clip = (Clip) AudioSystem.getLine(di);
            clip.open(sound);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
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
