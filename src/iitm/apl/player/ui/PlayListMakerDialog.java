package iitm.apl.player.ui;

import iitm.apl.player.Song;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class PlayListMakerDialog extends JDialog {
	private static final long serialVersionUID = -2891581224281215999L;
	private Vector<Song> songList;
	private PlaylistTableModel playlistModel;
	static int time;
	JTable playlistTable;
	JScrollPane playlistPane;

	public PlayListMakerDialog(JamPlayer parent) {
		super();
		songList = parent.getSongList();

		Container pane = getContentPane();
		pane.add(Box.createVerticalStrut(20), BorderLayout.NORTH);
		pane.add(Box.createVerticalStrut(20), BorderLayout.SOUTH);
		pane.add(Box.createHorizontalStrut(20), BorderLayout.EAST);
		pane.add(Box.createHorizontalStrut(20), BorderLayout.WEST);
		// Create the dialog window
		GroupLayout layout = new GroupLayout(getContentPane());
		getContentPane().setLayout(layout);

		// Create the slider
		JLabel label0 = new JLabel("Play List Length: ");
		int totalTime = getTotalLength(songList);
		time = totalTime;
		JSlider contentSlider = new JSlider(0, totalTime, totalTime);
		final JLabel timeLabel = new JLabel();
		timeLabel.setText(String.format("%d:%02d:%02d", (totalTime / 3600),
				(totalTime / 60) % 60, totalTime % 60));
		contentSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				JSlider contentSlider = (JSlider) arg0.getSource();
				time = contentSlider.getValue();
				timeLabel.setText(String.format("%d:%02d:%02d", (time / 3600),
						(time / 60) % 60, time % 60));
			}
		});
		// TODO: Connect to handler that will populate PlaylistTableModel
		// appropriately.
		JButton makeButton = new JButton("Make!");
		makeButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

				playlistModel.set(makePlaylist(songList));
				playlistTable = new JTable(playlistModel);
				playlistPane = new JScrollPane(playlistTable);
			}

		});
		playlistModel = new PlaylistTableModel();
		playlistModel.set(songList);
		playlistTable = new JTable(playlistModel);
		playlistPane = new JScrollPane(playlistTable);

		layout.setVerticalGroup(layout.createSequentialGroup().addGroup(
				layout.createParallelGroup(Alignment.LEADING).addComponent(
						label0).addComponent(contentSlider).addComponent(
						timeLabel).addComponent(makeButton)).addContainerGap()
				.addComponent(playlistPane));

		layout.setHorizontalGroup(layout.createParallelGroup().addGroup(
				layout.createSequentialGroup().addComponent(label0)
						.addComponent(contentSlider).addComponent(timeLabel)
						.addComponent(makeButton)).addComponent(playlistPane));
		this.pack();
	}

	public static int getTotalLength(Vector<Song> songs) {
		int time = 0;
		for (Song song : songs)
			time += song.getDuration();
		return time;
	}

	// Algorithm for creating play list
	public static Vector<Song> makePlaylist(Vector<Song> songs) {
		// using 0-1 knapsack algorithm; both 'weight' and 'value' are "time
		// duration of song"
		// 'time' is the specified time on slider
		int[][] A = new int[songs.size() + 1][time + 1];
		// i is the song ,j is time(seconds) , A[i][j] is optimal sum of
		// durations of songs upto i which is <=j
		// the required answer is A[songs.size][(specified)'time']

		for (int i = 1; i <= songs.size(); i++) {

			for (int j = 1; j <= time; j++) {
				if (songs.get(i - 1).getDuration() <= j) {
					int max = Math.max(A[i - 1][j], songs.get(i - 1).getDuration()
							+ A[i - 1][j - songs.get(i - 1).getDuration()]);
					A[i][j] = max;
				} else
					A[i][j] = A[i - 1][j];
			}
		}

		System.out.println(time
				+ "s is the given time and the new list plays for "
				+ A[songs.size()][time] + "s");
		
		//finding the songs which sum up to A[songs.size()][time] 
		int  j = time;
		Vector<Song> optimalList = new Vector<Song>();
		for(int i=songs.size();i>0;i--) {
			if (A[i][j] != A[i - 1][j]) {
				optimalList.add(songs.get(i - 1));
				j = j - songs.get(i - 1).getDuration();
			}
		}
		return optimalList;
	}
}