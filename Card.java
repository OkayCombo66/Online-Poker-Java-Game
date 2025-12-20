/* Davi Klitz
 * dkk2131
 * This file implements card mechanics. 
 */

public class Card implements Comparable<Card>{
	
	private int suit; // use integers 1-4 to encode the suit
	private int rank; // use integers 1-13 to encode the rank
	
	public Card(int r, int s){
		rank = r;
		suit = s; 
		
	}
	
	public int compareTo(Card c){
		if (this.getRank() == c.getRank()) {
			return 0;
		}

		else if (this.getRank() > c.getRank()) { 
			return 1;
		}

		else {
			return -1;
		}

	}
	
    public String toString() {
        if (rank >= 11 || rank == 1) {
            return getIntString(rank) + " of " + getSuitString(suit) + "    ";
        } else {
            return rank + " of " + getSuitString(suit) + "    ";
        }
    }
	
	public int getRank(){
		return rank;
	}

	public int getSuit() {
		return suit;
	}


	public String getSuitString(int suit) {
		if (suit == 1) {
			return "Spades";
		}
		else if (suit == 2) {
			return "Diamonds";
		}
		else if (suit == 3) {
			return "Clubs";
		}
		else {
			return "Hearts";
		}
	}

	public String getIntString(int rank) {
		if (rank == 11) {
			return "Jack";
		}

		else if(rank == 12) {
			return "Queen";
		}
		else if(rank==13) {
			return "King";
		}
		else {
			return "Ace";
		}		
	}
}
