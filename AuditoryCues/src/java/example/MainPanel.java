package example;
//-*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.io.*;
import javax.sound.sampled.*;
import javax.swing.*;

// Swing Changes and New Features
// https://docs.oracle.com/javase/6/docs/technotes/guides/swing/SwingChanges.html#Miscellaneous
// Magic with Merlin: Swinging audio
// https://www.ibm.com/developerworks/java/library/j-mer0730/
public final class MainPanel extends JPanel {
    private static final String[] AUDITORY_CUES = {
        "OptionPane.errorSound", "OptionPane.informationSound",
        "OptionPane.questionSound", "OptionPane.warningSound"
    };

    private MainPanel() {
        super(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));

        JButton button1 = new JButton("showMessageDialog1");
        button1.addActionListener(e -> JOptionPane.showMessageDialog(panel, "showMessageDialog1"));

        JButton button2 = new JButton("showMessageDialog2");
        button2.addActionListener(e -> {
            UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.noAuditoryCues"));
            loadAndPlayAudio("notice2.wav");
            JOptionPane.showMessageDialog(panel, "showMessageDialog2");
            UIManager.put("AuditoryCues.playList", AUDITORY_CUES);
        });

        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(makeTitledPanel("Look&Feel Default", button1));
        panel.add(makeTitledPanel("notice2.wav", button2));

        JMenuBar mb = new JMenuBar();
        mb.add(LookAndFeelUtil.createLookAndFeelMenu());
        add(mb, BorderLayout.NORTH);
        add(panel);
        setPreferredSize(new Dimension(320, 240));
    }
    private static Component makeTitledPanel(String title, Component c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private void loadAndPlayAudio(String audioResource) {
        try (AudioInputStream soundStream = AudioSystem.getAudioInputStream(MainPanel.class.getResource(audioResource))) {
            DataLine.Info info = new DataLine.Info(Clip.class, soundStream.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(soundStream);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {
            ex.printStackTrace();
        }
    }
// import java.security.*;
//     private byte[] loadAudioData(String soundFile) {
//         if (soundFile == null) {
//           return null;
//         }
//         byte[] buffer = (byte[]) AccessController.doPrivileged(new PrivilegedAction<?>() {
//             @Override public Object run() {
//                 try {
//                     InputStream resource = getClass().getResourceAsStream(soundFile);
//                     if (resource == null) {
//                         return null;
//                     }
//                     BufferedInputStream in = new BufferedInputStream(resource);
//                     ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
//                     byte[] buffer = new byte[1024];
//                     int n;
//                     while ((n = in.read(buffer)) > 0) {
//                         out.write(buffer, 0, n);
//                     }
//                     in.close();
//                     out.flush();
//                     buffer = out.toByteArray();
//                     return buffer;
//                 } catch (IOException ex) {
//                     ex.printStackTrace();
//                     return null;
//                 }
//             }
//         });
//         if (buffer == null) {
//             System.err.println(getClass().getName() + "/" + soundFile + " not found.");
//             return null;
//         }
//         if (buffer.length == 0) {
//             System.err.println("warning: " + soundFile + " is zero-length");
//             return null;
//         }
//         return buffer;
//     }

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
        } catch (ClassNotFoundException | InstantiationException
               | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        // UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.allAuditoryCues"));
        // UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.defaultCueList"));
        // UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.noAuditoryCues"));
        UIManager.put("AuditoryCues.playList", AUDITORY_CUES);
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

// @see https://java.net/projects/swingset3/sources/svn/content/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java
final class LookAndFeelUtil {
    private static String lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
    private LookAndFeelUtil() { /* Singleton */ }
    public static JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        ButtonGroup lafRadioGroup = new ButtonGroup();
        for (UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName(), lafRadioGroup));
        }
        return menu;
    }
    private static JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName, ButtonGroup lafRadioGroup) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem(lafName, lafClassName.equals(lookAndFeel));
        lafItem.setActionCommand(lafClassName);
        lafItem.setHideActionText(true);
        lafItem.addActionListener(e -> {
            ButtonModel m = lafRadioGroup.getSelection();
            try {
                setLookAndFeel(m.getActionCommand());
            } catch (ClassNotFoundException | InstantiationException
                   | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                ex.printStackTrace();
            }
        });
        lafRadioGroup.add(lafItem);
        return lafItem;
    }
    private static void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = LookAndFeelUtil.lookAndFeel;
        if (!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            LookAndFeelUtil.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            // firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private static void updateLookAndFeel() {
        for (Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
}
