package example;
//-*- mode:java; encoding:utf8n; coding:utf-8 -*-
// vim:set fileencoding=utf-8:
//@homepage@
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import javax.sound.midi.*;
import javax.sound.midi.spi.*;

class MainPanel extends JPanel {
    private long tickpos = 0;
    private final JButton start;
    private final JButton stop;
    private final JButton init;
    public MainPanel() {
        super(new BorderLayout());
        URL url = getClass().getResource("Mozart_toruko_k.mid");
        boolean flag = false;
        final Sequencer sequencer;
        try{
            Sequence s = MidiSystem.getSequence(url);
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
            sequencer.setSequence(s);
        }catch(Exception ex) {
            ex.printStackTrace();
            start = null;
            stop = null;
            init = null;
            add(new JLabel(ex.toString()));
            return;
        }
        sequencer.addMetaEventListener(new MetaEventListener() {
            public void meta(MetaMessage meta) {
                if(meta.getType() == 47) {
                    tickpos = 0;
                    start.setEnabled(true);
                    stop.setEnabled(false);
                }
            }
        });
        start = new JButton(new AbstractAction("start") {
            @Override public void actionPerformed(ActionEvent ae) {
                sequencer.setTickPosition(tickpos);
                sequencer.start();
                stop.setEnabled(true);
                start.setEnabled(false);
            }
        });
        stop = new JButton(new AbstractAction("stop") {
            @Override public void actionPerformed(ActionEvent ae) {
                tickpos = sequencer.getTickPosition();
                sequencer.stop();
                start.setEnabled(true);
                stop.setEnabled(false);
            }
        });
        init = new JButton(new AbstractAction("init") {
            @Override public void actionPerformed(ActionEvent ae) {
                sequencer.stop();
                tickpos = 0;
                start.setEnabled(true);
                stop.setEnabled(false);
            }
        });
        stop.setEnabled(false);

        JTextArea label = new JTextArea("Wolfgang Amadeus Mozart\nPiano Sonata No. 11 in A major, K 331\n(Turkish Rondo)");
        label.setEditable(false);
        label.setBackground(getBackground());

        Box box = Box.createHorizontalBox();
        box.setBorder(BorderFactory.createEmptyBorder(5,5,0,0));
        box.add(Box.createHorizontalGlue());
        box.add(start);
        box.add(stop);
        box.add(init);

        Box b = Box.createVerticalBox();
        b.setBorder(BorderFactory.createTitledBorder("MIDI"));
        b.add(label);
        b.add(new JSeparator());
        b.add(box);

        add(b);
        //setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        setPreferredSize(new Dimension(320, 160));
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
        }catch(Exception e) {
            e.printStackTrace();
        }
        JFrame frame = new JFrame("@title@");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.getContentPane().add(new MainPanel());
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
