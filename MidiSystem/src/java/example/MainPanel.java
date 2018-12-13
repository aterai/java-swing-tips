// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final byte END_OF_TRACK = 0x2F;
  private long tickpos;
  private final JButton start = new JButton("start");
  private final JButton stop = new JButton("stop");
  private final JButton init = new JButton("init");

  public MainPanel() {
    super(new BorderLayout(5, 5));
    Sequencer sequencer;
    URL url = getClass().getResource("Mozart_toruko_k.mid");
    try {
      Sequence s = MidiSystem.getSequence(url);
      sequencer = MidiSystem.getSequencer();
      sequencer.open();
      sequencer.setSequence(s);
    } catch (InvalidMidiDataException | MidiUnavailableException | IOException ex) {
      ex.printStackTrace();
      add(new JLabel(ex.toString()));
      start.setEnabled(false);
      stop.setEnabled(false);
      init.setEnabled(false);
      return;
    }
    sequencer.addMetaEventListener(e -> {
      if (e.getType() == END_OF_TRACK) {
        tickpos = 0;
        start.setEnabled(true);
        stop.setEnabled(false);
      }
    });
    start.addActionListener(e -> {
      sequencer.setTickPosition(tickpos);
      sequencer.start();
      stop.setEnabled(true);
      start.setEnabled(false);
    });
    stop.addActionListener(e -> {
      tickpos = sequencer.getTickPosition();
      sequencer.stop();
      start.setEnabled(true);
      stop.setEnabled(false);
    });
    init.addActionListener(e -> {
      sequencer.stop();
      tickpos = 0;
      start.setEnabled(true);
      stop.setEnabled(false);
    });
    stop.setEnabled(false);

    JTextArea label = new JTextArea("Wolfgang Amadeus Mozart\nPiano Sonata No. 11 in A major, K 331\n(Turkish Rondo)");
    label.setBorder(BorderFactory.createTitledBorder("MIDI"));
    label.setEditable(false);
    label.setBackground(getBackground());

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    box.add(Box.createHorizontalGlue());
    box.add(start);
    box.add(stop);
    box.add(init);

    add(label);
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
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
