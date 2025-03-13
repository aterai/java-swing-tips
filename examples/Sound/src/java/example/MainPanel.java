// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private MainPanel() {
    super(new BorderLayout());
    JButton b1 = new JButton("play");
    b1.addActionListener(e -> loadAndPlayAudio("example/notice1.wav"));

    JButton b2 = new JButton("play");
    b2.addActionListener(e -> loadAndPlayAudio("example/notice2.wav"));

    Box box = Box.createVerticalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    box.add(makeTitledPanel("notice1.wav", b1));
    box.add(Box.createVerticalStrut(5));
    box.add(makeTitledPanel("notice2.wav", b2));

    add(box, BorderLayout.NORTH);
    setPreferredSize(new Dimension(320, 240));
  }

  public void loadAndPlayAudio(String path) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(path);
    if (url == null) {
      return;
    }
    try (AudioInputStream wav = AudioSystem.getAudioInputStream(url);
         Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, wav.getFormat()))) {
      EventQueue eventQueue = Toolkit.getDefaultToolkit().getSystemEventQueue();
      SecondaryLoop loop = eventQueue.createSecondaryLoop();
      clip.addLineListener(e -> {
        LineEvent.Type t = e.getType();
        if (Objects.equals(t, LineEvent.Type.STOP) || Objects.equals(t, LineEvent.Type.CLOSE)) {
          loop.exit();
        }
      });
      clip.open(wav);
      clip.start();
      loop.enter();
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
      // ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public static void main(String[] args) {
    EventQueue.invokeLater(MainPanel::createAndShowGui);
  }

  private static void createAndShowGui() {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (UnsupportedLookAndFeelException ignored) {
      Toolkit.getDefaultToolkit().beep();
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
      ex.printStackTrace();
      return;
    }
    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
