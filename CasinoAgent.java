import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.Random;

public class CasinoAgent extends JFrame {
    private JTextField txtMoney, txtTime;
    private JTextField[] m1Probs = new JTextField[4]; // 0x, 1x, 5x, 100x
    private JTextField[] m2Probs = new JTextField[4];
    private JTextArea logArea;
    private JLabel lblBalance;
    private JPanel[] mPanels = new JPanel[3];
    private JLabel[] mStatuses = new JLabel[3];
    
    private double balance;
    private int timeLeft, currentMachine = 1;
    private final Random rand = new Random();
    private final double SPIN_COST = 1.0;

    public CasinoAgent() {
        setTitle("Mini Casino - AI Reflex Agent");
        setSize(850, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        ((JPanel)getContentPane()).setBorder(new EmptyBorder(10, 10, 10, 10));

        // CONFIG & PROBABILITIES ---
        JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        
        // Session Config
        JPanel pnlSession = createTitledPanel("SESSION");
        pnlSession.setLayout(new GridLayout(2, 2, 5, 5));
        txtMoney = new JTextField(""); txtTime = new JTextField("");
        pnlSession.add(new JLabel("Starting Capital:")); pnlSession.add(txtMoney);
        pnlSession.add(new JLabel("Time (s):")); pnlSession.add(txtTime);

        // M1 Probs
        JPanel pnlM1 = createTitledPanel("Machine 1 Payoff Chances");
        pnlM1.setLayout(new GridLayout(2, 4, 2, 2));
        String[] headers = {"0x", "1x", "5x", "100x"};
        for(String h : headers) pnlM1.add(new JLabel(h, 0));
        for(int i=0; i<4; i++) { m1Probs[i] = new JTextField("25"); pnlM1.add(m1Probs[i]); }

        // M2 Probs
        JPanel pnlM2 = createTitledPanel("Machine 2 Payoff Chances ");
        pnlM2.setLayout(new GridLayout(2, 4, 2, 2));
        for(String h : headers) pnlM2.add(new JLabel(h, 0));
        for(int i=0; i<4; i++) { m2Probs[i] = new JTextField("25"); pnlM2.add(m2Probs[i]); }

        topPanel.add(pnlSession); topPanel.add(pnlM1); topPanel.add(pnlM2);
        add(topPanel, BorderLayout.NORTH);

        // VISUAL SLOTS 
        JPanel slotsContainer = new JPanel(new GridLayout(1, 2, 20, 0));
        for(int i = 1; i <= 2; i++) {
            mPanels[i] = new JPanel(new BorderLayout());
            mPanels[i].setBackground(Color.WHITE);
            mPanels[i].setBorder(new LineBorder(Color.BLACK, 2));
            JLabel title = new JLabel("SLOT MACHINE " + i, 0);
            title.setOpaque(true); title.setBackground(Color.DARK_GRAY); title.setForeground(Color.WHITE);
            
            mStatuses[i] = new JLabel("IDLE", 0);
            mStatuses[i].setFont(new Font("Impact", Font.PLAIN, 35));
            
            mPanels[i].add(title, BorderLayout.NORTH);
            mPanels[i].add(mStatuses[i], BorderLayout.CENTER);
            mPanels[i].setPreferredSize(new Dimension(300, 200));
            slotsContainer.add(mPanels[i]);
        }
        add(slotsContainer, BorderLayout.CENTER);

        // LOG & BUTTON 
        JPanel footer = new JPanel(new BorderLayout(5, 5));
        lblBalance = new JLabel("BALANCE: PHP 0.00", 0);
        lblBalance.setFont(new Font("SansSerif", Font.BOLD, 22));
        
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(logArea);
        
        JButton btnStart = new JButton("PLAY");
        btnStart.setPreferredSize(new Dimension(0, 50));
        btnStart.setFont(new Font("SansSerif", Font.BOLD, 16));

        footer.add(lblBalance, BorderLayout.NORTH); 
        footer.add(scroll, BorderLayout.CENTER); 
        footer.add(btnStart, BorderLayout.SOUTH);
        add(footer, BorderLayout.SOUTH);

        btnStart.addActionListener(e -> startAgent());
    }

    private JPanel createTitledPanel(String title) {
        JPanel p = new JPanel();
        p.setBorder(new TitledBorder(new LineBorder(Color.GRAY), title));
        return p;
    }

    private void startAgent() {
        try {
            balance = Double.parseDouble(txtMoney.getText());
            timeLeft = Integer.parseInt(txtTime.getText());
            
            // Parse Probabilities from text fields
            int[] m1Values = new int[4];
            int[] m2Values = new int[4];
            for(int i=0; i<4; i++) {
                m1Values[i] = Integer.parseInt(m1Probs[i].getText());
                m2Values[i] = Integer.parseInt(m2Probs[i].getText());
            }

            new Thread(() -> {
                while (timeLeft >= 10 && balance >= SPIN_COST) {
                    try {
                        updateLog(String.format("[%ds] Action: Play M%d\n", timeLeft, currentMachine));
                        balance -= SPIN_COST; 
                        timeLeft -= 10;
                        updateUI(currentMachine, "SPINNING...", Color.ORANGE);
                        Thread.sleep(700);

                        int[] currentChances = (currentMachine == 1) ? m1Values : m2Values;
                        
                        // Get specific multiplier hit
                        double mult = getPayoff(currentChances);
                        double winnings = (SPIN_COST * mult);
                        balance += winnings;

                        // Format multiplier for display
                        String resultTag = (mult == 0) ? "0x" : (int)mult + "x";

                        if (mult >= 1.0) {
                            // Win or Break-even logic (Stay)
                            updateUI(currentMachine, "WIN: " + resultTag, Color.GREEN);
                            updateLog("   Result: SUCCESS (" + resultTag + "). Strategy: STAY.\n");
                        } else {
                            // Loss logic (Switch)
                            updateUI(currentMachine, "LOSE: 0x", Color.RED);
                            updateLog("   Result: LOSS (0x). Strategy: SWITCH.\n");
                            currentMachine = (currentMachine == 1) ? 2 : 1;
                        }
                        
                        updateLog("   New Balance: PHP " + balance + "\n\n");
                        Thread.sleep(1000);
                        
                        
                        updateUI(1, "IDLE", Color.WHITE); 
                        updateUI(2, "IDLE", Color.WHITE);
                    } catch (Exception ex) { break; }
                }
                updateLog("--- AGENT SESSION FINISHED ---");
            }).start();
        } catch (Exception e) { 
            JOptionPane.showMessageDialog(this, "Check inputs! Ensure Capital, Time, and Probs are numbers."); 
        }
    }

    private double getPayoff(int[] c) {
        int r = rand.nextInt(100);
        if (r < c[0]) return 0;       // 0x
        if (r < c[0] + c[1]) return 1; // 1x
        if (r < c[0] + c[1] + c[2]) return 5; // 5x
        return 100; // 100x
    }

    private void updateLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateUI(int m, String s, Color c) {
        SwingUtilities.invokeLater(() -> {
            lblBalance.setText(String.format("BALANCE: PHP %.2f", balance));
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