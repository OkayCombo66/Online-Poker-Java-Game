import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/**
 * Casino-style Swing GUI for your Video Poker game.
 * Uses existing Card, Deck, Player, and Game.checkHand() logic.
 */
public class VideoPokerGUI extends JFrame {

    private Player player;
    private Deck deck;

    private JToggleButton[] cardButtons;   // the 5 card buttons
    private JLabel[] holdLabels;           // "HOLD" labels under cards

    private JButton dealButton;
    private JButton drawButton;
    private JButton maxBetButton;
    private JButton resetButton;

    private JTextField betField;
    private JLabel bankrollLabel;
    private JLabel messageLabel;
    private JLabel titleLabel;

    private int currentBet = 0;
    private boolean handDealt = false;

    // For deal animation
    private Timer dealTimer;
    private int dealIndex = 0;

    // constants
    private static final int MAX_BET = 5;
    private static final int CARD_WIDTH = 90;
    private static final int CARD_HEIGHT = 135;

    public VideoPokerGUI() {
        super("Video Poker - Casino Edition");

        // try nice look and feel
        try {
            UIManager.setLookAndFeel(
                UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ignored) {}

        deck = new Deck();
        player = new Player(50.0, deck);

        initComponents();
        layoutComponents();
        attachListeners();

        updateBankrollLabel();
        updateCardDisplayEmpty();

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null); // center
        setVisible(true);
    }

    private void initComponents() {
        // table background
        getContentPane().setBackground(new Color(0, 90, 0)); // dark green

        cardButtons = new JToggleButton[5];
        holdLabels = new JLabel[5];

        for (int i = 0; i < 5; i++) {
            cardButtons[i] = new JToggleButton();
            cardButtons[i].setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
            cardButtons[i].setFocusPainted(false);
            cardButtons[i].setBorder(new LineBorder(Color.DARK_GRAY, 2));
            cardButtons[i].setContentAreaFilled(false);
            cardButtons[i].setOpaque(false);
            cardButtons[i].setEnabled(false); // enabled after first deal

            holdLabels[i] = new JLabel(" ", SwingConstants.CENTER);
            holdLabels[i].setForeground(Color.YELLOW);
            holdLabels[i].setFont(new Font("SansSerif", Font.BOLD, 14));
        }

        dealButton = new JButton("DEAL");
        drawButton = new JButton("DRAW");
        maxBetButton = new JButton("MAX BET");
        resetButton = new JButton("RESET");

        drawButton.setEnabled(false);

        dealButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        drawButton.setFont(new Font("SansSerif", Font.BOLD, 16));
        maxBetButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        resetButton.setFont(new Font("SansSerif", Font.PLAIN, 12));

        dealButton.setBackground(new Color(255, 170, 0));
        drawButton.setBackground(new Color(0, 150, 255));
        maxBetButton.setBackground(new Color(200, 0, 0));
        resetButton.setBackground(new Color(80, 80, 80));

        dealButton.setForeground(Color.BLACK);
        drawButton.setForeground(Color.WHITE);
        maxBetButton.setForeground(Color.WHITE);
        resetButton.setForeground(Color.WHITE);

        betField = new JTextField("1", 4);

        bankrollLabel = new JLabel();
        bankrollLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        bankrollLabel.setForeground(Color.WHITE);

        messageLabel = new JLabel("Welcome to Video Poker!", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        messageLabel.setForeground(Color.YELLOW);

        titleLabel = new JLabel("VIDEO POKER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 28));
        titleLabel.setForeground(new Color(255, 215, 0)); // gold
    }

    private void layoutComponents() {
        setLayout(new BorderLayout(10, 10));

        // TOP PANEL (title + bankroll + bet) 
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);

        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);

        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        infoPanel.setOpaque(false);
        infoPanel.add(new JLabel("<html><font color='white'>Credits:</font></html>"));
        infoPanel.add(bankrollLabel);
        infoPanel.add(new JLabel("<html><font color='white'>Bet (1–5):</font></html>"));
        infoPanel.add(betField);
        infoPanel.add(maxBetButton);
        infoPanel.add(resetButton);

        topPanel.add(titlePanel, BorderLayout.NORTH);
        topPanel.add(infoPanel, BorderLayout.SOUTH);
        add(topPanel, BorderLayout.NORTH);

        //CENTER PANEL (cards) 
        JPanel centerPanel = new JPanel();
        centerPanel.setOpaque(false);
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

        JPanel cardsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        cardsRow.setOpaque(false);

        JPanel holdsRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        holdsRow.setOpaque(false);

        for (int i = 0; i < 5; i++) {
            cardsRow.add(cardButtons[i]);
            holdsRow.add(holdLabels[i]);
        }

        centerPanel.add(cardsRow);
        centerPanel.add(holdsRow);

        add(centerPanel, BorderLayout.CENTER);

        // BOTTOM PANEL (buttons + message)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.setLayout(new BorderLayout());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(dealButton);
        buttonsPanel.add(drawButton);

        bottomPanel.add(buttonsPanel, BorderLayout.NORTH);
        bottomPanel.add(messageLabel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void attachListeners() {
        dealButton.addActionListener(e -> startNewHand());
        drawButton.addActionListener(e -> drawCards());
        maxBetButton.addActionListener(e -> setMaxBet());
        resetButton.addActionListener(e -> resetGame());

        // toggle “HOLD” labels and borders
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            cardButtons[i].addItemListener(ev -> {
                if (cardButtons[idx].isSelected()) {
                    cardButtons[idx].setBorder(new LineBorder(Color.YELLOW, 3));
                    holdLabels[idx].setText("HOLD");
                } else {
                    cardButtons[idx].setBorder(new LineBorder(Color.DARK_GRAY, 2));
                    holdLabels[idx].setText(" ");
                }
            });
        }
    }

    private void setMaxBet() {
        int max = (int) Math.min(MAX_BET, (int) player.getBankroll());
        if (max <= 0) {
            showMessage("Not enough credits to bet.", Color.PINK);
            return;
        }
        betField.setText(Integer.toString(max));
    }

    private void resetGame() {
        player = new Player(50.0, new Deck());
        deck = new Deck();
        handDealt = false;
        currentBet = 0;
        for (JToggleButton b : cardButtons) {
            b.setEnabled(false);
            b.setSelected(false);
            b.setBorder(new LineBorder(Color.DARK_GRAY, 2));
        }
        for (JLabel h : holdLabels) {
            h.setText(" ");
        }
        betField.setText("1");
        betField.setEnabled(true);
        dealButton.setEnabled(true);
        drawButton.setEnabled(false);
        updateCardDisplayEmpty();
        updateBankrollLabel();
        showMessage("Game reset. Good luck!", Color.YELLOW);
    }

    private void startNewHand() {
        if (handDealt) return;

        // parse bet
        int bet;
        try {
            bet = Integer.parseInt(betField.getText().trim());
        } catch (NumberFormatException ex) {
            showMessage("Bet must be an integer between 1 and 5.", Color.PINK);
            return;
        }

        if (bet < 1 || bet > MAX_BET) {
            showMessage("Bet must be between 1 and 5.", Color.PINK);
            return;
        }

        if (bet > player.getBankroll()) {
            showMessage("You don't have enough credits for that bet.", Color.PINK);
            return;
        }

        currentBet = bet;

        // new deck like your text version
        deck = new Deck();

        // deal 5 cards into player's hand
        ArrayList<Card> hand = player.getHandArrayList();
        hand.clear();
        for (int i = 0; i < 5; i++) {
            hand.add(deck.deal());
        }

        // pay bet
        player.bets(currentBet);
        updateBankrollLabel();

        // set up UI state
        handDealt = true;
        dealButton.setEnabled(false);
        drawButton.setEnabled(false); // enabled after animation
        betField.setEnabled(false);
        for (JToggleButton b : cardButtons) {
            b.setEnabled(false);
            b.setSelected(false);
            b.setBorder(new LineBorder(Color.DARK_GRAY, 2));
        }
        for (JLabel h : holdLabels) {
            h.setText(" ");
        }

        updateCardDisplayBacks();
        showMessage("Dealing cards...", Color.CYAN);
        playSound("sounds/deal.wav");

        // animate cards appearing one by one
        dealIndex = 0;
        if (dealTimer != null && dealTimer.isRunning()) {
            dealTimer.stop();
        }
        dealTimer = new Timer(180, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (dealIndex < 5) {
                    showCardAtIndex(dealIndex);
                    dealIndex++;
                } else {
                    ((Timer) e.getSource()).stop();
                    // enable holds & draw after animation
                    for (JToggleButton b : cardButtons) {
                        b.setEnabled(true);
                    }
                    drawButton.setEnabled(true);
                    showMessage("Select cards to HOLD, then click DRAW.", Color.YELLOW);
                }
            }
        });
        dealTimer.start();
    }

    private void drawCards() {
        if (!handDealt) return;

        ArrayList<Card> hand = player.getHandArrayList();
        if (hand.size() != 5) return;

        // replace unheld cards
        for (int i = 0; i < 5; i++) {
            if (!cardButtons[i].isSelected()) {
                hand.set(i, deck.deal());
            }
        }

        // ⭐ IMPORTANT: show the final hand BEFORE anything else
        updateCardDisplay();
        revalidate();
        repaint();

        // evaluate hand using your existing Game logic
        String combination = Game.checkHand(hand);
        double winnings = computeWinnings(combination, currentBet);
        player.winnings(winnings);
        updateBankrollLabel();

        Color msgColor = (winnings > 0) ? Color.GREEN.brighter() : Color.LIGHT_GRAY;
        showMessage("You got: " + combination + " | Payout: " + winnings, msgColor);
        if (winnings > 0) {
            playSound("sounds/chips.wav");
        }

        // disable for next round
        handDealt = false;
        drawButton.setEnabled(false);
        dealButton.setEnabled(true);
        betField.setEnabled(true);

        // AFTER display is updated, clear hold states
        
        for (JToggleButton b : cardButtons) {
            b.setSelected(false);
            b.setBorder(new LineBorder(Color.DARK_GRAY, 2));
            b.setEnabled(true); // ⭐ KEEP ENABLED TO PREVENT WHITE MASK
        }
        
        for (JLabel h : holdLabels) {
            h.setText(" ");
        }

        // check game over
        if (player.getBankroll() <= 0) {
            showMessage("GAME OVER! You ran out of credits.", Color.PINK);
            dealButton.setEnabled(false);
            drawButton.setEnabled(false);
            betField.setEnabled(false);
        }
    }

        private void updateCardDisplay() {
            ArrayList<Card> hand = player.getHandArrayList();
            for (int i = 0; i < 5; i++) {
                if (i < hand.size() && hand.get(i) != null) {
                    cardButtons[i].setIcon(loadCardIcon(hand.get(i)));
                    cardButtons[i].setText("");
                } else {
                    cardButtons[i].setIcon(null);
                    cardButtons[i].setText("Card " + (i + 1));
                }
            }
        }

    private void showCardAtIndex(int i) {
        ArrayList<Card> hand = player.getHandArrayList();
        if (i < hand.size() && hand.get(i) != null) {
            cardButtons[i].setIcon(loadCardIcon(hand.get(i)));
            cardButtons[i].setText("");
        }
    }

    private void updateCardDisplayEmpty() {
        for (int i = 0; i < 5; i++) {
            cardButtons[i].setIcon(null);
            cardButtons[i].setText("Card " + (i + 1));
        }
    }

    private void updateCardDisplayBacks() {
        // optional: show back image before reveal; if no back image, leave blank
        ImageIcon back = loadCardBackIcon();
        for (int i = 0; i < 5; i++) {
            if (back != null) {
                cardButtons[i].setIcon(back);
                cardButtons[i].setText("");
            } else {
                cardButtons[i].setIcon(null);
                cardButtons[i].setText("Dealing...");
            }
        }
    }

    private void updateBankrollLabel() {
        bankrollLabel.setText(String.format("%.1f", player.getBankroll()));
    }

    private void showMessage(String msg, Color color) {
        messageLabel.setText(msg);
        messageLabel.setForeground(color);
    }

    // Same payout logic as Game.getWinning()
    private double computeWinnings(String combination, double bet) {
        if (combination.equals("One pair"))               return bet * 1;
        else if (combination.equals("Two pair"))          return bet * 2;
        else if (combination.equals("Three of a kind"))   return bet * 3;
        else if (combination.equals("Straight"))          return bet * 4;
        else if (combination.equals("Flush"))             return bet * 5;
        else if (combination.equals("Full House"))        return bet * 6;
        else if (combination.equals("Four of a Kind"))    return bet * 25;
        else if (combination.equals("Straight Flush"))    return bet * 50;
        else if (combination.equals("Royal Flush"))       return bet * 250;
        else return 0;
    }

    // IMAGE LOADING 

    private ImageIcon loadCardIcon(Card c) {
        String path = getImagePathForCard(c);
        ImageIcon icon = new ImageIcon(path);
        if (icon.getIconWidth() <= 0) {
            // fallback: text only if file not found
            return null;
        }
        Image scaled = icon.getImage().getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private ImageIcon loadCardBackIcon() {
        File backFile = new File("cards/BACK.png");
        if (!backFile.exists()) {
            return null;
        }
        ImageIcon icon = new ImageIcon(backFile.getPath());
        Image scaled = icon.getImage().getScaledInstance(CARD_WIDTH, CARD_HEIGHT, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private String getImagePathForCard(Card c) {
        String rankStr;
        int r = c.getRank();
        switch (r) {
            case 1:  rankStr = "A"; break;
            case 11: rankStr = "J"; break;
            case 12: rankStr = "Q"; break;
            case 13: rankStr = "K"; break;
            default: rankStr = Integer.toString(r); break;
        }

        String suitStr;
        int s = c.getSuit();
        // your mapping: 1 Spades, 2 Diamonds, 3 Clubs, 4 Hearts
        switch (s) {
            case 1:  suitStr = "S"; break;
            case 2:  suitStr = "D"; break;
            case 3:  suitStr = "C"; break;
            default: suitStr = "H"; break;
        }

        return "cards/" + rankStr + suitStr + ".png";
    }

    // SOUND LOADING (optional) 

    private void playSound(String path) {
        File f = new File(path);
        if (!f.exists()) return; // silently skip if no sound
        try {
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(f);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // ignore sound errors for this assignment
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new VideoPokerGUI());
    }
}
