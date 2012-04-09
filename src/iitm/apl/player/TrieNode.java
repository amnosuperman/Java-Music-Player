package iitm.apl.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.ListIterator;

public class TrieNode {

	public Song song = null;
	public char ch;
	public ArrayList<TrieNode> lst = new ArrayList<TrieNode>();
	public TrieNode nextNode = null; 
	
	public TrieNode(){
		
		for(int i=0;i<4;i++){
			lst.add(null);
		}
		
	}

	public TrieNode search(char c){
		
		if(c>='A'&&c<='Z')
			c = (char) (c + 32);
		
		TrieNode temp = lst.get(c%4);
		
		while(temp!=null){

			if(temp.ch>c)
				break;
			else if(temp.ch == c)
				return temp;

			temp = temp.nextNode;
		}
		
		return null;
	}

	public TrieNode insert(char c, Song song){
		
		if(c>='A'&&c<='Z')
			c = (char) (c + 32);
		
		TrieNode temp = lst.get(c%4);
		TrieNode prev = temp;

		if(temp == null){
			temp = new TrieNode();
			temp.ch = c;
			temp.song = song;
			lst.set(c%4,temp);
			return temp;
		}
		
		if(temp.ch == c)
			return temp;
		
		else if(temp.ch>c)
		{
			TrieNode newNode = new TrieNode();
			newNode.ch = c;
			newNode.song = song;
			newNode.nextNode = temp;
			lst.set(c%4, newNode);
			return newNode;

		}
		else
		{
		temp = temp.nextNode;
		
		while(temp!=null){
			if(temp.ch < c)
			{
				prev = temp;
				temp = temp.nextNode;
			}
			else if(temp.ch == c)
				return temp;
			else
				break;
		}
		
		TrieNode newNode = new TrieNode();
		newNode.ch = c;
		newNode.song = song;
		prev.nextNode = newNode;
		newNode.nextNode = temp;
		
		return newNode;
		}
	}
}
