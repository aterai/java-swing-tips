// -*- mode:java; encoding:utf-8 -*-
// vim:set fileencoding=utf-8:
// @homepage@

package example;

import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.swing.*;

public final class MainPanel extends JPanel {
  private static final byte END_OF_TRACK = 0x2F;

  private MainPanel() {
    super(new BorderLayout(5, 5));
    JButton start = makeButton("start");
    JButton pause = makeButton("pause");
    pause.setEnabled(false);
    JButton reset = makeButton("reset");

    URL url = getClass().getResource("Mozart_toruko_k.mid");
    SwingWorker<Void, Long> worker = new SwingWorker<Void, Long>() {
      protected long tickpos;
      @Override public Void doInBackground() {
        try (Sequencer sequencer = MidiSystem.getSequencer()) {
          sequencer.open();
          sequencer.setSequence(MidiSystem.getSequence(url));
          sequencer.addMetaEventListener(e -> {
            if (e.getType() == END_OF_TRACK) {
              publish(0L);
            }
          });

          EventQueue.invokeLater(() -> {
            start.addActionListener(e -> {
              sequencer.setTickPosition(tickpos);
              sequencer.start();
              initButtons(false);
            });

            pause.addActionListener(e -> {
              publish(sequencer.getTickPosition());
              sequencer.stop();
              initButtons(true);
            });

            reset.addActionListener(e -> {
              sequencer.stop();
              tickpos = 0;
              initButtons(true);
            });
          });

          try {
            while (sequencer.isOpen()) {
              if (sequencer.isRunning()) {
                publish(sequencer.getTickPosition());
              }
              Thread.sleep(1000);
            }
          } catch (InterruptedException ex) {
            System.out.println(ex.getMessage());
          }
        } catch (InvalidMidiDataException | MidiUnavailableException | IOException ex) {
          ex.printStackTrace();
          publish(0L);
        } finally {
          System.out.println("try-with-resources: AutoCloseable");
        }
        return null;
      }

      @Override protected void process(List<Long> chunks) {
        for (Long tp: chunks) {
          tickpos = tp;
          if (tickpos == 0) {
            initButtons(true);
          }
        }
      }

      // @Override public void done() {
      //   tickpos = 0;
      //   initButtons(true);
      // }

      protected void initButtons(boolean flg) {
        start.setEnabled(flg);
        pause.setEnabled(!flg);
      }
    };
    worker.execute();

    addHierarchyListener(e -> {
      if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !e.getComponent().isDisplayable()) {
        System.out.println("DISPLAYABILITY_CHANGED");
        worker.cancel(true);
      }
    });

    Box box = Box.createHorizontalBox();
    box.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));
    box.add(Box.createHorizontalGlue());
    box.add(start);
    box.add(pause);
    box.add(reset);

    add(makeTitle(getBackground()));
    add(box, BorderLayout.SOUTH);
    setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
    setPreferredSize(new Dimension(320, 240));
  }

  private static Component makeTitle(Color bgc) {
    JTextArea label = new JTextArea("Wolfgang Amadeus Mozart\nPiano Sonata No. 11 in A major, K 331\n(Turkish Rondo)");
    label.setBorder(BorderFactory.createTitledBorder("MIDI"));
    label.setEditable(false);
    label.setBackground(bgc);
    return label;
  }

  private static JButton makeButton(String title) {
    JButton b = new JButton(title);
    b.setFocusable(false);
    return b;
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
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
      ex.printStackTrace();
      Toolkit.getDefaultToolkit().beep();
    }
    JFrame frame = new JFrame("@title@");
    // frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.getContentPane().add(new MainPanel());
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
