package iitm.apl.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

public class TrieOps {

	private TrieNode head = new TrieNode();
	
	public void addSongs(Vector<Song> songlist)
	{
		int i;
		for(i=0;i<songlist.size();i++)
		{
			Song song = songlist.elementAt(i);
			String name = song.getTitle();
			//insert(name,song);
			String[] words = name.split(" ");
			String temp = "";
			int j = 0;
			for(j=words.length-1; j>=0;j--)
			{
				temp = words[j] + " " + temp;
				insert(temp,song);
			}
		}
	}
	
	public void insert(String name, Song song)
	{
		TrieNode ptr = head;
		for(int i=0;i<name.length();i++)
			ptr = ptr.insert(name.charAt(i), null);
		ptr.song = song;
		
	}
	
	public TrieNode search(String name)
	{
		TrieNode ptr = head;
		for(int i=0;i<name.length();i++)
		{
			ptr = ptr.search(name.charAt(i));
			if(ptr==null)
				break;
		}
		return ptr;
	}
	
	public Vector<Song> getsearchSongs(String name)
	{
		TrieNode trie;
		trie = search(name);
		HashSet<Song> set = new HashSet<Song>();
		Vector<Song> list = new Vector<Song>();
		getSongsUnder(trie,list,set);
		return list;
	}
	
	public Vector<Song> getSongsUnder(TrieNode trie, Vector<Song> songs, HashSet<Song> set)
	{
		if(trie == null)
			return songs;
		else if(trie.song!=null)
		{
			if(!set.contains(trie.song))
			{
				songs.add(trie.song);
				set.add(trie.song);
			}
		}
		
		ArrayList<TrieNode> lst = trie.lst;
		for(int i=0;i<4;i++)
		{
			TrieNode ptr = lst.get(i);
			while(ptr!=null)
			{
				getSongsUnder(ptr,songs,set);
				ptr = ptr.nextNode;
			}
		}
		
		return songs;
	}
}
