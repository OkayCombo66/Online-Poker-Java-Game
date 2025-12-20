/* Davi Klitz
 * dkk2131
 * This file implements the poker game mechanics. 
 */


import java.util.ArrayList;
import java.util.Scanner;

public class Game {
	private Scanner s;
	private Deck cards;
	private Player p;
	private double numTokens;
	private int numExchange;
	private double numCards;
    private boolean testMode = false;
	
	public Game(){
		System.out.println("Welcome to Video Poker!");
		s = new Scanner(System.in);
        cards = new Deck();
		p = new Player(50.0, cards);  

        for (int i = 0; i < 5; i++) {
            p.getHandArrayList().add(cards.deal());
        }  
	}


    public Game(String[] testHand) {
        testMode = true;
        System.out.println("Welcome to Video Poker!");
        s = new Scanner(System.in);
        cards = new Deck();     


        p = new Player(50.0, cards);
        p.getHandArrayList().clear();

        for (String cardString : testHand) {
            p.getHandArrayList().add(parseCard(cardString));
        }
    }		
	
	public void play(){
		numTokens = 0;
		numCards = 0;

		while(p.getBankroll() > 0) {
			

				
			double oldBankroll = p.getBankroll();
            System.out.println("YOUR TOKENS: " + p.getBankroll());
            System.out.println("Would you like to play a round? (y/n): ");
            String playGame = s.next();
            if (playGame.equals("y")) {
                cards = new Deck();
                while(true) {
                    System.out.println("How many tokens to bet this hand? (1 to 5): ");
                    numTokens = s.nextDouble();
                    if (numTokens < p.getBankroll() && numTokens >= 1 && numTokens <= 5) {
						p.bets(numTokens);
                        break;
                    }
                System.out.println("Number of tokens must be between 0 and 5.");
                }

                
            
                System.out.println("The hand is:    " + p.getHand());

                while(true) {
                    System.out.println("How many cards (0-5) would you like to exchange? ");
                    numCards = s.nextInt();
                    if (numCards >= 0 && numCards <= 5) {
                        break;
                    }

                System.out.println("Number must be between 0 and 5.");
                }
                
				ArrayList<Integer> chosen = new ArrayList<>();

				for (int i = 1; i <= numCards; i++) {
					int choice;

					while (true) {
						System.out.println("Which card (1-5) would you like to exchange? ");
						choice = s.nextInt();


						if (choice < 1 || choice > 5) {
							System.out.println("Invalid card number. Pick between 1 and 5.");
							continue;
						}


						if (chosen.contains(choice)) {
							System.out.println("You already chose that card! Pick a different one.");
							continue;
						}

						break; 
					}

					chosen.add(choice);
					p.addCard(choice - 1, cards.deal());
				}

				System.out.println("The hand is:    " + p.getHand());
				String combination = Game.checkHand(p.getHandArrayList());
				System.out.println("You got: " + combination);
                double winnings = getWinning(combination, numTokens);
                p.winnings(winnings);
				System.out.println("PAYOUT: " + winnings);



            }

            else {
                break;
            }

        }

		if (p.getBankroll() <= 0) {
            System.out.println("Sorry, you are out of tokens. :(");
        } else {
            System.out.println("Thank you for playing Video Poker!");
        }
	
	
	}
	
	public static String checkHand(ArrayList<Card> handToCheck){

		ArrayList<Card> handSorted = handSort(handToCheck);
        ArrayList<Integer> handRankTemp = new ArrayList<>();



        for (Card a: handSorted) {
            handRankTemp.add(a.getRank());
        }

		if (handRankTemp.contains(1)) {
			for (int i = 0; i < handRankTemp.size(); i++) {
				if (handRankTemp.get(i) == 1) {
					handRankTemp.set(i, 14);
				}
			}
				
		}
		
		
		ArrayList<Integer> handRankTempSorted = new ArrayList<>(handRankTemp);
		handSort(handRankTempSorted);

        

		


		int hearts = 0;
        int clubs = 0;
        int diamonds = 0;
        int spades = 0;

		int tempStraight = 0;

		boolean isFlush = false;
		boolean isStraight = true;
		boolean isRoyalFlush = false;

		int numberOfPairs = 0;

    	
		//count pairs

		for (int i = 0; i < handSorted.size() - 1; i++) {
			for (int j = i + 1; j < handSorted.size(); j++) {
				if (handSorted.get(i).compareTo(handSorted.get(j)) == 0) {
					numberOfPairs++;
				}
			}
		}
		
		//check flush

        for (Card c : handSorted) {
            if ((c.getSuitString(c.getSuit())).equals("Hearts")) {hearts++;}
            else if ((c.getSuitString(c.getSuit())).equals("Clubs")) {clubs++;}
            else if ((c.getSuitString(c.getSuit())).equals("Diamonds")) {diamonds++;}
            else {spades++;}                                        
        }

        if (hearts == 5 || clubs == 5 || diamonds == 5 || spades == 5) {isFlush = true;}



		// check special case where it's ace-low straight 
		boolean lowAceStraight =
			handRankTempSorted.get(0) == 2 &&
			handRankTempSorted.get(1) == 3 &&
			handRankTempSorted.get(2) == 4 &&
			handRankTempSorted.get(3) == 5 &&
			handRankTempSorted.get(4) == 14;

		// normal straight
		for (int i = 0; i < 4; i++) {
			if (handRankTempSorted.get(i+1) - handRankTempSorted.get(i) != 1) {
				isStraight = false;
				break;
			}
		}


		if (lowAceStraight) {
			isStraight = true;
		}


		//check royal flush
		if (isStraight && isFlush && handRankTempSorted.get(0) == 10 && handRankTempSorted.get(4) == 14) {isRoyalFlush = true;}

		//check all

		if (isRoyalFlush) return "Royal Flush";
		if (isStraight && isFlush) return "Straight Flush";
		if (numberOfPairs == 6) return "Four of a Kind";
		if (numberOfPairs == 4) return "Full House";
		if (isFlush) return "Flush";
		if (isStraight) return "Straight";
		if (numberOfPairs == 3) return "Three of a kind";
		if (numberOfPairs == 2) return "Two pair";
		if (numberOfPairs == 1) return "One pair";
		return "No Pair";
		
	}

	public double getWinning(String combination, double bet) {
		if (combination.equals("One pair")) { return bet * 1; }
		else if (combination.equals("Two pair")) { return bet * 2; }
		else if (combination.equals("Three of a kind")) { return bet * 3; }
		else if (combination.equals("Straight")) { return bet * 4; }
		else if (combination.equals("Flush")) { return bet * 5; }
		else if (combination.equals("Full House")) { return bet * 6; }
		else if (combination.equals("Four of a Kind")) { return bet * 25; }
		else if (combination.equals("Straight Flush")) { return bet * 50; }
		else if (combination.equals("Royal Flush")) { return bet * 250; }
		else { return 0; }
	}

    // make this method generic so can be used for cards and integers

	public static <T extends Comparable<T>> ArrayList<T> handSort(ArrayList<T> hand) {
		for (int i = 0; i < hand.size() - 1; i++) {
			for (int j = 0; j < hand.size() - 1 - i; j++) {
				if (hand.get(j).compareTo(hand.get(j + 1)) > 0) {
					T temp = hand.get(j);
					hand.set(j, hand.get(j + 1));
					hand.set(j + 1, temp);
				}
			}
		}
		return hand;
	}

    private Card parseCard(String s) {

        int rank;
        char suitChar = s.charAt(s.length() - 1);  
        String rankPart = s.substring(0, s.length() - 1);


        switch (rankPart) {
            case "A": rank = 1; break;
            case "J": rank = 11; break;
            case "Q": rank = 12; break;
            case "K": rank = 13; break;
            default:  rank = Integer.parseInt(rankPart);
        }

        int suit;
        switch (suitChar) {
            case 'S': suit = 1; break; 
            case 'D': suit = 2; break; 
            case 'C': suit = 3; break; 
            default:  suit = 4; break;
        }

        return new Card(rank, suit);
    }
}

