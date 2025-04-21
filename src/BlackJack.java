import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class BlackJack {
    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("AJQK".contains(value)) { // Ace, Jack, Queen, King
                if (value == "A") {
                    return 11;
                }
                else {
                    return 10;
                }
            }
            return Integer.parseInt(value); // 2-10
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "./cards/" + toString() + ".png";
        }
    }

    ArrayList<Card> deck;
    Random random = new Random(); // shuffle the deck

    //dealer
    Card hiddenCard;
    ArrayList<Card> dealerHand;
    int dealerSum;
    int dealerAceCount;
    int dealerBankroll = 1500; // dealer's bankroll
    int dealerBet = 0; // dealer's bet

    //player
    ArrayList<Card> playerHand;
    int playerSum;
    int playerAceCount;
    int playerBankroll = 1000; // player's bankroll
    int playerBet = 0; // player's bet

    int pot = 0; // total bet
    String message = ""; // message to display

    // window
    int boardWidth = 600;
    int boardHeight = boardWidth;

    //card size
    int cardWidth = 110; //ratio should be 1:1.4
    int cardHeight = 154;

    JFrame frame = new JFrame("Black Jack");
    JPanel gamePanel = new JPanel() {
        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);

            try {
                //draw hidden card
                Image hiddenCardImage = new ImageIcon(getClass().getResource("./cards/BACK.png")).getImage();
                if (!stayButton.isEnabled()) {
                    hiddenCardImage = new ImageIcon(getClass().getResource(hiddenCard.getImagePath())).getImage();
                }
                g.drawImage(hiddenCardImage, 20, 20, cardWidth, cardHeight, null);

                //draw dealer's hand
                for (int i = 0; i < dealerHand.size(); i++) {
                    Card card = dealerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, cardWidth + 25 + (cardWidth + 5) * i, 20, cardWidth, cardHeight, null);
                }

                //draw player's hand
                for (int i = 0; i < playerHand.size(); i++) {
                    Card card = playerHand.get(i);
                    Image cardImage = new ImageIcon(getClass().getResource(card.getImagePath())).getImage();
                    g.drawImage(cardImage, 20 + (cardWidth + 5) * i, 320, cardWidth, cardHeight, null);
                }

                //draw win condition text
                if (!stayButton.isEnabled()) {
                    dealerSum = reduceDealerAce();
                    playerSum = reducePlayerAce();
                    System.out.println("STAY");
                    System.out.println(dealerSum);
                    System.out.println(playerSum);

                    if (playerSum > 21) {
                        message = "You lose!";
                        dealerBankroll += pot; // dealer wins the bet
                    } else if (dealerSum > 21) {
                        message = "You win!";
                        playerBankroll += pot; // player wins the bet
                    } else if (playerSum > dealerSum) {
                        message = "You win!";
                        playerBankroll += pot; // player wins the bet
                    } else if (playerSum < dealerSum) {
                        message = "You lose!";
                        dealerBankroll += pot; // dealer wins the bet
                    } else {
                        message = "Draw!";
                        playerBankroll += pot / 2; // player gets back half the bet
                        dealerBankroll += pot / 2; // dealer gets back half the bet
                        dealerBankroll += pot % 2; // dealer gets back the extra dollar if the value is odd
                    }
                    pot = 0; // reset the pot
                    dealerBet = 0; // reset the dealer's bet
                    playerBet = 0; // reset the player's bet

                    //determine if either player or dealer is out of money
                    if (dealerBankroll <= 0) {
                        message = "Dealer out of funds! You win!";
                    } else if (playerBankroll <= 0) {
                        message = "No more funds! Dealer wins!";
                    }

                    g.setFont(new Font("Arial", Font.PLAIN, 30));
                    g.setColor(Color.white);
                    g.drawString(message, 200, 250);

                    nextHandButton.setEnabled(true);
                }
                //draw player and dealer bankroll
                //dealer's bankroll
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.setColor(Color.white);
                g.drawString("Dealer: $" + String.valueOf(dealerBankroll), 5, 200);

                //player's bankroll
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.setColor(Color.white);
                g.drawString("Player: $" + String.valueOf(playerBankroll), 5, 300);

                //draw the total bet
                g.setFont(new Font("Arial", Font.PLAIN, 20));
                g.setColor(Color.white);
                g.drawString("Bet: $" + String.valueOf(pot), 5, 250);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    JPanel buttonPanel = new JPanel();
    JButton hitButton = new JButton("Hit");
    JButton stayButton = new JButton("Stay");
    JButton nextHandButton = new JButton("Next Hand");
    JLabel playerBetLabel = new JLabel("Bet: $");
    JSpinner playerBetSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10)); // betting spinner

    BlackJack() {
        startGame();

        frame.setVisible(true);
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);

        gamePanel.setLayout(new BorderLayout());
        gamePanel.setBackground(new Color(53, 101, 77));
        frame.add(gamePanel);

        buttonPanel.add(playerBetLabel);
        playerBetSpinner.setFocusable(false);
        buttonPanel.add(playerBetSpinner);
        hitButton.setFocusable(false);
        buttonPanel.add(hitButton);
        stayButton.setFocusable(false);
        buttonPanel.add(stayButton);
        nextHandButton.setFocusable(false);
        nextHandButton.setEnabled(false);
        buttonPanel.add(nextHandButton);
        frame.add(buttonPanel, BorderLayout.SOUTH);

        hitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Card card = deck.remove(deck.size() - 1); // remove the last card from the deck
                playerSum += card.getValue();
                playerAceCount += card.isAce() ? 1 : 0;
                playerHand.add(card);
                if (reducePlayerAce() > 21) {
                    hitButton.setEnabled(false);
                }

                gamePanel.repaint();
            }
        });

        stayButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // disable buttons as it's the dealer's turn
                hitButton.setEnabled(false);
                stayButton.setEnabled(false);
                
                // dealer's turn
                while (dealerSum < 17) {
                    Card card = deck.remove(deck.size() - 1); // remove the last card from the deck
                    dealerSum += card.getValue();
                    dealerAceCount += card.isAce() ? 1 : 0;
                    dealerHand.add(card);
                }
                gamePanel.repaint();
            }
        });

        nextHandButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                nextHandButton.setEnabled(false);
                hitButton.setEnabled(true);
                stayButton.setEnabled(true);
                playerHand.clear();
                dealerHand.clear();
                if (dealerBankroll <= 0) {
                    dealerBankroll = 1500; // reset the dealer's bankroll
                }
                if (playerBankroll <= 0) {
                    playerBankroll = 1000; // reset the player's bankroll
                }
                startGame();
                gamePanel.repaint();
            }
        });

        gamePanel.repaint();
    }

    public void startGame() {
        //deck
        buildDeck();
        shuffleDeck();

        message = ""; // reset the message

        //betting
        playerBet = (int) playerBetSpinner.getValue(); // get the player's bet from the spinner
        if (playerBet > playerBankroll) {
            playerBet = playerBankroll; // set the bet to the player's bankroll if it's greater
        } else if (playerBet < 10) {
            playerBet = 10; // set the bet to the minimum if it's less than 10
        }
        playerBetSpinner.setValue(playerBet); // update the spinner value
        System.out.println("PLAYER BET: " + playerBet);
        dealerBet = playerBet; // dealer's bet is the same as player's bet
        dealerBet = modifyValue(dealerBet); // modify the dealer's bet randomly
        if (dealerBet > dealerBankroll) {
            dealerBet = dealerBankroll; // set the bet to the dealer's bankroll if it's greater
        } else if (dealerBet < 10) {
            dealerBet = 10; // set the bet to the minimum if it's less than 10
        }
        System.out.println("DEALER BET: " + dealerBet);

        dealerBankroll -= dealerBet; // deduct the bet from the dealer's bankroll
        playerBankroll -= playerBet; // deduct the bet from the player's bankroll
        pot = dealerBet + playerBet; // total bet

        //dealer
        dealerHand = new ArrayList<Card>();
        dealerSum = 0;
        dealerAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1); // remove the last card from the deck
        dealerSum += hiddenCard.getValue();
        dealerAceCount += hiddenCard.isAce() ? 1 : 0;
        
        Card card = deck.remove(deck.size() - 1); // remove the last card from the deck
        dealerSum += card.getValue();
        dealerAceCount += card.isAce() ? 1 : 0;
        dealerHand.add(card);

        System.out.println("DEALER HAND");
        System.out.println(hiddenCard);
        System.out.println(dealerHand);
        System.out.println(dealerSum);
        System.out.println(dealerAceCount);

        //player
        playerHand = new ArrayList<Card>();
        playerSum = 0;
        playerAceCount = 0;

        for (int i = 0; i < 2; i++) {
            card = deck.remove(deck.size() - 1); // remove the last card from the deck
            playerSum += card.getValue();
            playerAceCount += card.isAce() ? 1 : 0;
            playerHand.add(card);
        }

        System.out.println("PLAYER HAND");
        System.out.println(playerHand);
        System.out.println(playerSum);
        System.out.println(playerAceCount);
    }

    public void buildDeck() {
        deck = new ArrayList<Card>();
        String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A"};
        String[] types = {"H", "D", "C", "S"};

        for (String value : values) {
            for (String type : types) {
                deck.add(new Card(value, type));
            }
        }

        System.out.println("BUILD DECK");
        System.out.println(deck);
    }

    public void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card currentCard = deck.get(i);
            Card randomCard = deck.get(j);
            deck.set(i, randomCard);
            deck.set(j, currentCard);
        }

        System.out.println("AFTER SHUFFLE");
        System.out.println(deck);
    }

    public int reducePlayerAce() {
        while (playerSum > 21 && playerAceCount > 0) {
            playerSum -= 10; // reduce the value of Ace from 11 to 1
            playerAceCount--;
        }
        return playerSum;
    }

    public int reduceDealerAce() {
        while (dealerSum > 21 && dealerAceCount > 0) {
            dealerSum -= 10; // reduce the value of Ace from 11 to 1
            dealerAceCount--;
        }
        return dealerSum;
    }

    public int modifyValue(int value) {
        Random random = new Random();
        int multiplier = random.nextInt(5) + 1; // Random number between 1 and 5
        int change = multiplier * 5; // Multiple of 5
        boolean add = random.nextBoolean(); // Randomly decide to add or subtract

        return add ? value + change : value - change;
    }
}

/*
 * To do:
 * - add the ability for the player to split pairs
 */