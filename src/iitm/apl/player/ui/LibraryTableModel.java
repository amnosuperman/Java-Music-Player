package iitm.apl.player.ui;

import iitm.apl.player.Song;

import java.util.Vector;
import javax.swing.table.AbstractTableModel;

/**
 * Table model for a library
 * 
 */
public class LibraryTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 8230354699902953693L;

	// TODO: Change to your implementation of Trie/BK-Tree
	private Vector<Song> songListing;

	LibraryTableModel() {
		songListing = new Vector<Song>();
	}

	public void add(Song song) {
		songListing.add(song);
		fireTableDataChanged();
	}

	public void add(Vector<Song> songs) {
		songListing.addAll(songs);
		if(songs.size()!=0)
			System.out.println("list generated");
		fireTableDataChanged();
	}

	public void removeAll() {
		songListing.removeAllElements();
		System.out.println("removed");
	}
	
	public void filter(String searchString) {
		// TODO: Connect the searchText keyPressed handler to update the filter
		// here.
	}

	// Gets the song at the currently visible index
	public Song get(int idx) {
		return songListing.get(idx);
	}

	@Override
	public int getColumnCount() {
		// Title, Album, Artist, Duration.
		return 4;
	}

	@Override
	public int getRowCount() {
		// TODO: Changes if you've filtered the list
		return songListing.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		// TODO: Get the appropriate row
		Song song = songListing.get(row);

		switch (col) {
		case 0: // Title
			return song.getTitle();
		case 1: // Album
			return song.getAlbum();
		case 2: // Artist
			return song.getArtist();
		case 3: // Duration
			int duration = song.getDuration();
			int mins = duration / 60;
			int secs = duration % 60;
			return String.format("%d:%2d", mins, secs);
		default:
			return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0: // Title
			return "Title";
		case 1: // Album
			return "Album";
		case 2: // Artist
			return "Artist";
		case 3: // Duration
			return "Duration";
		default:
			return super.getColumnName(column);
		}
	}

}
