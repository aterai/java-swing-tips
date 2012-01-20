package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.io.*;
// import java.security.*;
import javax.swing.*;
import javax.sound.sampled.*;

//Swing Changes and New Features>http://java.sun.com/javase/6/docs/technotes/guides/swing/SwingChanges.html#Miscellaneous
////                             http://java.sun.com/javase/ja/6/docs/ja/technotes/guides/swing/SwingChanges.html#Miscellaneous
//Magic with Merlin: Swinging audio>http://www.ibm.com/developerworks/java/library/j-mer0730/
////                                http://www.ibm.com/developerworks/jp/java/library/j-mer0730/
class MainPanel extends JPanel {
    private static final Object[] optionPaneAuditoryCues = {
        "OptionPane.errorSound", "OptionPane.informationSound",
        "OptionPane.questionSound", "OptionPane.warningSound"
      };
    private final JPanel panel = new JPanel(new GridLayout(2,1,5,5));
    public MainPanel() {
        super(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        panel.add(makePanel("Look&Feel Default", new JButton(new AbstractAction("showMessageDialog1") {
            @Override public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(panel, "showMessageDialog1");
            }
        })));
        panel.add(makePanel("notice2.wav", new JButton(new AbstractAction("showMessageDialog2") {
            @Override public void actionPerformed(ActionEvent e) {
                UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.noAuditoryCues"));
                loadAndPlayAudio("notice2.wav");
                JOptionPane.showMessageDialog(panel, "showMessageDialog2");
                UIManager.put("AuditoryCues.playList", optionPaneAuditoryCues);
            }
        })));
        JMenuBar mb = new JMenuBar();
        mb.add(createLookAndFeelMenu());
        add(mb, BorderLayout.NORTH);
        add(panel);
        setPreferredSize(new Dimension(320, 180));
    }
    private static JPanel makePanel(String title, JComponent c) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(title));
        p.add(c);
        return p;
    }
    private void loadAndPlayAudio(String audioResource) {
        try{
            AudioInputStream soundStream = AudioSystem.getAudioInputStream(
                getClass().getResource(audioResource));
            DataLine.Info info = new DataLine.Info(Clip.class, soundStream.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);
            clip.open(soundStream);
            clip.start();
        }catch(UnsupportedAudioFileException uafe) {
            uafe.printStackTrace();
        }catch(IOException ioe) {
            ioe.printStackTrace();
        }catch(LineUnavailableException lue) {
            lue.printStackTrace();
        }
    }
//     private byte[] loadAudioData(final String soundFile) {
//         if(soundFile==null) return null;
//         byte[] buffer = (byte[])AccessController.doPrivileged(new PrivilegedAction() {
//             public Object run() {
//                 try{
//                     InputStream resource = getClass().getResourceAsStream(soundFile);
//                     if(resource==null) return null;
//                     BufferedInputStream in = new BufferedInputStream(resource);
//                     ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
//                     byte[] buffer = new byte[1024];
//                     int n;
//                     while((n = in.read(buffer)) > 0) {
//                         out.write(buffer, 0, n);
//                     }
//                     in.close();
//                     out.flush();
//                     buffer = out.toByteArray();
//                     return buffer;
//                 }catch(IOException ioe) {
//                     ioe.printStackTrace();
//                     return null;
//                 }
//             }
//         });
//         if(buffer==null) {
//             System.err.println(getClass().getName() + "/" + soundFile + " not found.");
//             return null;
//         }
//         if(buffer.length == 0) {
//             System.err.println("warning: " + soundFile + " is zero-length");
//             return null;
//         }
//         return buffer;
//     }

    //<blockquote cite="https://swingset3.dev.java.net/svn/swingset3/trunk/SwingSet3/src/com/sun/swingset3/SwingSet3.java">
    private ButtonGroup lookAndFeelRadioGroup;
    private String lookAndFeel;
    protected JMenu createLookAndFeelMenu() {
        JMenu menu = new JMenu("LookAndFeel");
        lookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        lookAndFeelRadioGroup = new ButtonGroup();
        for(UIManager.LookAndFeelInfo lafInfo: UIManager.getInstalledLookAndFeels()) {
            menu.add(createLookAndFeelItem(lafInfo.getName(), lafInfo.getClassName()));
        }
        return menu;
    }
    protected JRadioButtonMenuItem createLookAndFeelItem(String lafName, String lafClassName) {
        JRadioButtonMenuItem lafItem = new JRadioButtonMenuItem();
        lafItem.setSelected(lafClassName.equals(lookAndFeel));
        lafItem.setHideActionText(true);
        lafItem.setAction(new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) {
                ButtonModel m = lookAndFeelRadioGroup.getSelection();
                try{
                    setLookAndFeel(m.getActionCommand());
                }catch(Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        lafItem.setText(lafName);
        lafItem.setActionCommand(lafClassName);
        lookAndFeelRadioGroup.add(lafItem);
        return lafItem;
    }
    public void setLookAndFeel(String lookAndFeel) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        String oldLookAndFeel = this.lookAndFeel;
        if(!oldLookAndFeel.equals(lookAndFeel)) {
            UIManager.setLookAndFeel(lookAndFeel);
            this.lookAndFeel = lookAndFeel;
            updateLookAndFeel();
            firePropertyChange("lookAndFeel", oldLookAndFeel, lookAndFeel);
        }
    }
    private void updateLookAndFeel() {
        for(Window window: Frame.getWindows()) {
            SwingUtilities.updateComponentTreeUI(window);
        }
    }
    //</blockquote>

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
        //UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.allAuditoryCues"));
        //UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.defaultCueList"));
        //UIManager.put("AuditoryCues.playList", UIManager.get("AuditoryCues.noAuditoryCues"));
        UIManager.put("AuditoryCues.playList", optionPaneAuditoryCues);
        //UIManager.put("OptionPane.informationSound", "/example/notice2.wav");
        //UIManager.put("OptionPane.informationSound", "sounds/OptionPaneError.wav");
        //System.out.println(UIManager.get("AuditoryCues.actionMap"));

        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
