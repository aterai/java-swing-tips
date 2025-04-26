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

// Auditory Feedback for Swing Components - Swing Changes and New Features
// https://docs.oracle.com/javase/8/docs/technotes/guides/swing/1.4/SwingChanges.html#bug4290988
// Magic with Merlin: Swinging audio
// https://www.ibm.com/developerworks/java/library/j-mer0730/
public final class MainPanel extends JPanel {
  private static final String AUDITORY_KEY = "AuditoryCues.playList";
  private static final String[] AUDITORY_CUES = {
      "OptionPane.errorSound", "OptionPane.informationSound",
      "OptionPane.questionSound", "OptionPane.warningSound"
  };

  private MainPanel() {
    super(new BorderLayout());
    JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));

    JButton button1 = new JButton("showMessageDialog1");
    button1.addActionListener(e -> {
      UIManager.put(AUDITORY_KEY, AUDITORY_CUES);
      JOptionPane.showMessageDialog(panel, "showMessageDialog1");
    });

    JButton button2 = new JButton("showMessageDialog2");
    button2.addActionListener(e -> {
      UIManager.put(AUDITORY_KEY, UIManager.get("AuditoryCues.noAuditoryCues"));
      showMessageDialogAndPlayAudio(panel, "showMessageDialog2", "example/notice2.wav");
    });

    panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    panel.add(makeTitledPanel("Look&Feel Default", button1));
    panel.add(makeTitledPanel("notice2.wav", button2));

    JMenuBar mb = new JMenuBar();
    mb.add(LookAndFeelUtils.createLookAndFeelMenu());
    EventQueue.invokeLater(() -> getRootPane().setJMenuBar(mb));

    add(panel);
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitledPanel(String title, Component c) {
    JPanel p = new JPanel(new BorderLayout());
    p.setBorder(BorderFactory.createTitledBorder(title));
    p.add(c);
    return p;
  }

  public void showMessageDialogAndPlayAudio(Component p, String msg, String audioResource) {
    URL url = Thread.currentThread().getContextClassLoader().getResource(audioResource);
    if (url == null) {
      UIManager.getLookAndFeel().provideErrorFeedback(p);
      JOptionPane.showMessageDialog(p, audioResource + " not found");
      return;
    }
    try (AudioInputStream ss = AudioSystem.getAudioInputStream(url);
         Clip clip = (Clip) AudioSystem.getLine(new DataLine.Info(Clip.class, ss.getFormat()))) {
      SecondaryLoop loop = p.getToolkit().getSystemEventQueue().createSecondaryLoop();
      clip.addLineListener(e -> {
        LineEvent.Type t = e.getType();
        // System.out.println(t);
        if (Objects.equals(t, LineEvent.Type.STOP) || Objects.equals(t, LineEvent.Type.CLOSE)) {
          loop.exit();
        }
      });
      clip.open(ss);
      clip.start();
      JOptionPane.showMessageDialog(p, msg);
      loop.enter();
    } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
      ex.printStackTrace();
      UIManager.getLookAndFeel().provideErrorFeedback(p);
    }
  }

  // import java.security.*;
  // private byte[] loadAudioData(String soundFile) {
  //   if (soundFile == null) {
  //     return null;
  //   }
  //   byte[] buffer = (byte[]) AccessController.doPrivileged(new PrivilegedAction<?>() {
  //     @Override public Object run() {
  //       try {
  //         InputStream resource = getClass().getResourceAsStream(soundFile);
  //         if (resource == null) {
  //           return null;
  //         }
  //         BufferedInputStream in = new BufferedInputStream(resource);
  //         ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
  //         byte[] buffer = new byte[1024];
  //         int n;
  //         while ((n = in.read(buffer)) > 0) {
  //           out.write(buffer, 0, n);
  //         }
  //         in.close();
  //         out.flush();
  //         buffer = out.toByteArray();
  //         return buffer;
  //       } catch (IOException ex) {
  //         return null;
  //       }
  //     }
  //   });
  //   if (buffer == null) {
  //     System.err.println(getClass().getName() + "/" + soundFile + " not found.");
  //     return null;
  //   }
  //   if (buffer.length == 0) {
  //     System.err.println("warning: " + soundFile + " is zero-length");
  //     return null;
  //   }
  //   return buffer;
  // }

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
    // UIManager.put(AUDITORY_KEY, UIManager.get("AuditoryCues.allAuditoryCues"));
    // UIManager.put(AUDITORY_KEY, UIManager.get("AuditoryCues.defaultCueList"));
    // UIManager.put(AUDITORY_KEY, UIManager.get("AuditoryCues.noAuditoryCues"));
    UIManager.put(AUDITORY_KEY, AUDITORY_CUES);
    // UIManager.put("OptionPane.informationSound", "/example/notice2.wav");
    // UIManager.put("OptionPane.informationSound", "sounds/OptionPaneError.wav");
    // System.out.println(UIManager.get("AuditoryCues.actionMap"));

    JFrame frame = new JFrame("@title@");
    frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}

// @see SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtils {
  private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();

  private LookAndFeelUtils() {
    /* Singleton */
  }

  public static JMenu createLookAndFeelMenu() {
    JMenu menu = new JMenu("LookAndFeel");
    ButtonGroup buttonGroup = new ButtonGroup();
    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
      AbstractButton b = makeButton(info);
      initLookAndFeelAction(info, b);
      menu.add(b);
      buttonGroup.add(b);
    }
    return menu;
  }

  private static AbstractButton makeButton(UIManager.LookAndFeelInfo info) {
    boolean selected = info.getClassName().equals(lookAndFeel);
    return new JRadioButtonMenuItem(info.getName(), selected);
  }

  public static void initLookAndFeelAction(UIManager.LookAndFeelInfo info, AbstractButton b) {
    String cmd = info.getClassName();
    b.setText(info.getName());
    b.setActionCommand(cmd);
    b.setHideActionText(true);
    b.addActionListener(e -> setLookAndFeel(cmd));
  }

  private static void setLookAndFeel(String newLookAndFeel) {
    String oldLookAndFeel = lookAndFeel;
    if (!oldLookAndFeel.equals(newLookAndFeel)) {
      try {
        UIManager.setLookAndFeel(newLookAndFeel);
        lookAndFeel = newLookAndFeel;
      } catch (UnsupportedLookAndFeelException ignored) {
        Toolkit.getDefaultToolkit().beep();
      } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
        ex.printStackTrace();
        return;
      }
      updateLookAndFeel();
      // firePropertyChange("lookAndFeel", oldLookAndFeel, newLookAndFeel);
    }
  }

  private static void updateLookAndFeel() {
    for (Window window : Window.getWindows()) {
      SwingUtilities.updateComponentTreeUI(window);
    }
  }
}
