import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Random;

public class CasinoAgent extends JFrame {
    private JTextField txtMoney, txtTime, txtM1Bet, txtM2Bet;
    private JTextArea logArea;
    private JLabel lblBalance;
    private JPanel[] mPanels = new JPanel[3];
    private JLabel[] mStatuses = new JLabel[3];
    
    private double balance;
    private int timeLeft, currentMachine = 1;
    private final Random rand = new Random();
    
    // Probabilities  {0x, 1x, 5x, 100x}
    private final int[][] CHANCES = {{}, {50, 30, 15, 5}, {70, 10, 10, 10}};

    public CasinoAgent() {
        setTitle("Mini-Casino Dashboard");
        setSize(700, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(235, 235, 235)); 
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));

        // CONFIGURATION SECTION
        JPanel inputPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new CompoundBorder(
            new TitledBorder(new LineBorder(Color.GRAY), "AGENT CONFIGURATION"),
            new EmptyBorder(10, 10, 10, 10)));
        
        txtMoney = new JTextField(); txtTime = new JTextField();
        txtM1Bet = new JTextField(); txtM2Bet = new JTextField();
        
        inputPanel.add(new JLabel("Starting Capital:")); inputPanel.add(txtMoney);
        inputPanel.add(new JLabel("Machine 1 Stake (Php):")); inputPanel.add(txtM1Bet);
        inputPanel.add(new JLabel("Total Time (s):")); inputPanel.add(txtTime);
        inputPanel.add(new JLabel("Machine 2 Stake (Php):")); inputPanel.add(txtM2Bet);
        
        add(inputPanel, BorderLayout.NORTH);

        // SLOT MACHINE DISPLAY ---
        JPanel slotsContainer = new JPanel(new GridLayout(1, 2, 25, 0));
        slotsContainer.setOpaque(false);
        for(int i = 1; i <= 2; i++) {
            mPanels[i] = new JPanel(new BorderLayout());
            mPanels[i].setBackground(Color.WHITE);
           
            mPanels[i].setBorder(new CompoundBorder(
                new LineBorder(new Color(100, 100, 100), 2),
                new BevelBorder(BevelBorder.LOWERED)));
            
            JLabel title = new JLabel("SLOT MACHINE " + i, 0);
            title.setOpaque(true); 
            title.setBackground(new Color(45, 45, 45));
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 12));
            
            mStatuses[i] = new JLabel("IDLE", 0);
            mStatuses[i].setFont(new Font("Impact", Font.PLAIN, 40)); 
            
            mPanels[i].add(title, BorderLayout.NORTH);
            mPanels[i].add(mStatuses[i], BorderLayout.CENTER);
            mPanels[i].setPreferredSize(new Dimension(300, 250)); 
            slotsContainer.add(mPanels[i]);
        }
        add(slotsContainer, BorderLayout.CENTER);

        // DASHBOARD 
        JPanel footer = new JPanel(new BorderLayout(5, 5));
        footer.setOpaque(false);
        
        lblBalance = new JLabel("TOTAL BALANCE: PHP 0.00", 0);
        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblBalance.setForeground(new Color(0, 102, 204)); 
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        logArea.setBackground(new Color(250, 250, 250));

        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(0, 240)); 
        scroll.setBorder(new TitledBorder("AGENT STRATEGY NARRATIVE"));
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        JButton btnStart = new JButton("PLAY");
        btnStart.setPreferredSize(new Dimension(0, 50));
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 16));
        btnStart.setCursor(new Cursor(Cursor.HAND_CURSOR));

        footer.add(lblBalance, BorderLayout.NORTH);
        footer.add(scroll, BorderLayout.CENTER);
        footer.add(btnStart, BorderLayout.SOUTH);
        
        add(footer, BorderLayout.SOUTH);

        btnStart.addActionListener(e -> startAgent());
    }

    private void startAgent() {
        try {
            balance = Double.parseDouble(txtMoney.getText());
            timeLeft = Integer.parseInt(txtTime.getText());
            logArea.setText(""); 
            
            new Thread(() -> {
                while (timeLeft >= 10 && balance > 0) {
                    double bet = Double.parseDouble(currentMachine == 1 ? txtM1Bet.getText() : txtM2Bet.getText());
                    if (balance < bet) {
                        updateLog("SYSTEM: Insufficient funds for M" + currentMachine + "\n");
                        break;
                    }

                    try {
                        updateLog(String.format("[%ds] PLAY: M%d with Stake PHP %.2f\n", timeLeft, currentMachine, bet));
                        balance -= bet; 
                        timeLeft -= 10;
                        updateUI(currentMachine, "BETTING", Color.ORANGE);
                        
                        Thread.sleep(800);

                        double mult = getPayoff(CHANCES[currentMachine]);
                        double winnings = (bet * mult);
                        balance += winnings;

                        if (mult >= 1.0) {
                            updateUI(currentMachine, "WIN", new Color(40, 167, 69)); 
                            updateLog("   RESULT: Win detected. RULE: Stay.\n");
                        } else {
                            updateUI(currentMachine, "LOSE", new Color(220, 53, 69)); 
                            currentMachine = (currentMachine == 1) ? 2 : 1;
                            updateLog("   RESULT: Loss detected. RULE: Switch.\n");
                        }
                        
                        updateLog("   STATUS: Updated Balance: PHP " + balance + "\n\n");
                        Thread.sleep(1000);
                        
                        updateUI(1, "IDLE", Color.WHITE); updateUI(2, "IDLE", Color.WHITE);
                    } catch (Exception ex) { break; }
                }
                updateLog("--- AGENT TASK COMPLETE ---");
            }).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Enter valid numeric values.");
        }
    }

    private double getPayoff(int[] c) {
        int r = rand.nextInt(100);
        if (r < c[0]) return 0;
        if (r < c[0]+c[1]) return 1;
        if (r < c[0]+c[1]+c[2]) return 5;
        return 100;
    }

    private void updateLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateUI(int m, String s, Color c) {
        SwingUtilities.invokeLater(() -> {
            lblBalance.setText(String.format("TOTAL BALANCE: PHP %.2f", balance));
            if(m > 0) {
                mStatuses[m].setText(s);
                mPanels[m].setBackground(c);
            }
        });
    }

    public static void main(String[] args) { 
        new CasinoAgent().setVisible(true); 
    }
}