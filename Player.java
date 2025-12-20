/* Davi Klitz
 * dkk2131
 * This file implements the player mechanics. 
 */

import java.util.ArrayList;
 
public class Player {
		
	private ArrayList<Card> hand; 
	private double bankroll;
    private double bet;
    private Deck deck;

		
    public Player(double initialBankroll, Deck deck) {
        hand = new ArrayList<Card>();
        bankroll = initialBankroll;
        this.deck = deck;
    }

	public void addCard(int index, Card card){
	    hand.set(index, card);
	}

	public void removeCard(Card c){
	    hand.remove(c);
        }
		
    public void bets(double amt){
        bankroll = bankroll - amt;
    }

    public void winnings(double odds){
        bankroll = bankroll + odds;
    }

    public double getBankroll(){
        return bankroll;
    }

    public ArrayList<Card> getHandArrayList() {
        return hand;
    }

    public String getHand() {
        String wholeHand = "";
        for (Card a: hand) {
            wholeHand = wholeHand + a + "\t";
        }

        return wholeHand;
    }

}