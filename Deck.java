/* Davi Klitz
 * dkk2131
 * This file implements deck mechanics. 
 */

import java.util.Random;

public class Deck {
	
	public int AMOUNTCARDS = 52;
	private Card[] sorted = new Card[AMOUNTCARDS]; // change back to private
	public Card[] shuffled = new Card[AMOUNTCARDS]; // change back to private 
	private int top = 0; // the index of the top of the deck

	// add more instance variables if needed
	
	public Deck(){
	
		int index = 0;
		int [] cardNumbers = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
		int [] suitNumbers = {1, 2, 3, 4};

		for (int b: suitNumbers) {
			for (int a: cardNumbers) {
				sorted[index] = new Card(a, b);
				index++;
			}
		}

        shuffle();
		
	}

	// I used the Fisher-Yates shuffle methode (I learnt if from leetcode)

	public void shuffle(){
		Random rand = new Random();
		int [] temp  = new int[52];  

		for (int i = 0; i < 52; i++) {
			temp[i] = i;
		}

		for (int i = 51; i > 0; i--) {
			int j = rand.nextInt(i+1);

            int swap = temp[i];
            temp[i] = temp[j];
            temp[j] = swap;

		}

        for (int i  = 0; i <52; i ++) {
            shuffled[i] = sorted[temp[i]];
		}
	}

	public Card deal(){
		if (top < 52) {
			return shuffled[top++];
		}
		else {
			return null;
		}
	}
}
