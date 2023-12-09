import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class TradeEvaluator extends JFrame {
    private JTextPane resultTextPane;
    private JLabel reportLabel;
    private List<StockData> stockDataList;
    private double initialBalance;
    private int buyCount;
    private int sellCount;
    private int doNothingCount;
    private double startingBalance;
    private double netProfits;
    private JComboBox<String> heuristicDropdown;
    private JTextField initialBalanceTextField;
    private int shares;  // Added variable to track the number of shares
    private double balance;  // Added variable to track the balance
    private List<StockData> historicalData;  // Added list to store historical data

    public TradeEvaluator() {
        setTitle("Trade Evaluator");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        createMainPanel();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JButton loadCSVButton = new JButton("Load CSV");
        loadCSVButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCSV();
            }
        });

        String[] heuristicOptions = {"Moving Average Crossover", "Relative Strength Index", "Buy Below Average"};
        heuristicDropdown = new JComboBox<>(heuristicOptions);

        JLabel initialBalanceLabel = new JLabel("Enter Initial Balance (USD):");
        initialBalanceTextField = new JTextField(10);

        JButton beginEvaluationButton = new JButton("Begin Evaluation");

        beginEvaluationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                beginEvaluation();
            }
        });

        resultTextPane = new JTextPane();
        resultTextPane.setEditable(false);
        resultTextPane.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        reportLabel = new JLabel();
        reportLabel.setHorizontalAlignment(SwingConstants.CENTER);
        reportLabel.setVerticalAlignment(SwingConstants.TOP);
        reportLabel.setForeground(Color.BLACK);

        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        inputPanel.add(loadCSVButton);
        inputPanel.add(initialBalanceLabel);
        inputPanel.add(initialBalanceTextField);
        inputPanel.add(heuristicDropdown);
        inputPanel.add(beginEvaluationButton);

        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.add(new JLabel("Report"), BorderLayout.NORTH);
        reportPanel.add(reportLabel, BorderLayout.CENTER);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(resultTextPane), BorderLayout.CENTER);
        mainPanel.add(reportPanel, BorderLayout.SOUTH);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        add(mainPanel);
    }

    private void loadCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);

        if (returnValue == JFileChooser.APPROVE_OPTION) {
            try {
                stockDataList = new ArrayList<>();
                BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()));
                String line;

                reader.readLine();

                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split(",");
                    StockData stockData = new StockData(parts[0], Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]), Double.parseDouble(parts[3]),
                            Double.parseDouble(parts[4]), Double.parseDouble(parts[5]),
                            Long.parseLong(parts[6]));

                    stockDataList.add(stockData);
                }

                reader.close();
                showMessage("CSV loaded successfully!");
            } catch (IOException | NumberFormatException e) {
                showMessage("Error loading CSV: " + e.getMessage());
            }
        }
    }

    private void beginEvaluation() {
        if (stockDataList == null || stockDataList.isEmpty()) {
            showMessage("No CSV data loaded. Please load a CSV file first.");
            return;
        }

        resetCounts();

        String initialBalanceString = initialBalanceTextField.getText();
        try {
            initialBalance = Double.parseDouble(initialBalanceString);
            startingBalance = initialBalance;
        } catch (NumberFormatException e) {
            showMessage("Invalid initial balance. Please enter a valid number.");
            return;
        }

        balance = initialBalance;  // Initialize balance
        shares = 0;  // Initialize shares

        historicalData = new ArrayList<>();  // Initialize historical data list

        StyledDocument styledDocument = resultTextPane.getStyledDocument();
        Style defaultStyle = styledDocument.addStyle("DefaultStyle", null);

        try {
            styledDocument.remove(0, styledDocument.getLength());

            Style headerStyle = styledDocument.addStyle("HeaderStyle", null);
            StyleConstants.setBold(headerStyle, true);

            Style greenStyle = styledDocument.addStyle("GreenStyle", defaultStyle);
            StyleConstants.setForeground(greenStyle, Color.GREEN);

            Style redStyle = styledDocument.addStyle("RedStyle", defaultStyle);
            StyleConstants.setForeground(redStyle, Color.RED);

            StyleConstants.setAlignment(defaultStyle, StyleConstants.ALIGN_LEFT);

            styledDocument.insertString(styledDocument.getLength(), String.format("%-12s%-10s%-8s%-12s%n",
                    "Date", "Action", "Shares", "Balance"), headerStyle);

            for (StockData data : stockDataList) {
                historicalData.add(data);

                if (historicalData.size() > 20) {
                    historicalData.remove(0);
                }

                int selectedIndex = heuristicDropdown.getSelectedIndex();

                int decision = numberOfShares(data, historicalData, selectedIndex);

                if (decision > 0) {
                    shares += decision;
                    balance -= decision * data.getClose();
                    buyCount++;
                } else if (decision < 0) {
                    shares += decision;
                    balance -= decision * data.getClose();
                    sellCount++;
                } else {
                    doNothingCount++;
                }

                netProfits = balance - startingBalance;
                displayReport();
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private int numberOfShares(StockData currentData, List<StockData> historicalData, int selectedIndex) {
        double rsi = calculateRelativeStrengthIndex(historicalData, 14);
        double movingAverage = calculateMovingAverage(historicalData, 10);

        // Placeholder logic: Buy if RSI is below 30 and the current price is below the moving average
        if (rsi < 30 && currentData.getClose() < movingAverage) {
            return 100; // Buy 100 shares
        } else if (rsi > 70) {
            return -50; // Sell 50 shares
        }

        return 0; // Do nothing
    }


    private void resetCounts() {
        buyCount = 0;
        sellCount = 0;
        doNothingCount = 0;
    }

    private void displayReport() {
        double avgProfitPerTrade = calculateAvgProfitPerTrade();

        String reportText = "<html><b>Summary Report</b><br>";
        reportText += "Buys: " + buyCount + "<br>";
        reportText += "Sells: " + sellCount + "<br>";
        reportText += "Do Nothings: " + doNothingCount + "<br>";
        reportText += "Starting Balance: $" + formatCurrency(startingBalance) + "<br>";
        reportText += "Net Profits: $";

        if (netProfits >= 0)
            reportText += "<font color='green'>" + formatCurrency(netProfits) + "</font><br>";
        else
            reportText += "<font color='red'>" + formatCurrency(netProfits) + "</font><br>";

        reportText += "Avg Profit per Trade: $" + formatCurrency(avgProfitPerTrade) + "<br>";
        reportText += "</html>";

        reportLabel.setText(reportText);
    }

    private double calculateAvgProfitPerTrade() {
        int totalTrades = buyCount + sellCount;
        if (totalTrades == 0) {
            return 0.0;
        }
        return netProfits / totalTrades;
    }
    
 // Method to calculate the moving average based on a window size
    private double calculateMovingAverage(List<StockData> data, int windowSize) {
        double sum = 0.0;

        for (int i = data.size() - windowSize; i < data.size(); i++) {
            sum += data.get(i).getClose();
        }

        return sum / windowSize;
    }
    
 // Method to calculate the relative strength index
 // Method to calculate the relative strength index
    private double calculateRelativeStrengthIndex(List<StockData> data, int windowSize) {
        double averageGain = 0.0;
        double averageLoss = 0.0;

        for (int i = 1; i < windowSize; i++) {
            double priceDiff = data.get(i).getClose() - data.get(i - 1).getClose();

            if (priceDiff > 0) {
                averageGain += priceDiff;
            } else {
                averageLoss -= priceDiff;
            }
        }

        averageGain /= windowSize;
        averageLoss /= windowSize;

        if (averageLoss == 0.0) {
            return 100.0;
        }

        double relativeStrength = averageGain / averageLoss;
        return 100.0 - (100.0 / (1.0 + relativeStrength));
    }



    private String formatCurrency(double value) {
        DecimalFormat currencyFormat = new DecimalFormat("#,###.00");
        return currencyFormat.format(value);
    }

    private void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                        | UnsupportedLookAndFeelException e) {
                    e.printStackTrace();
                }
                new TradeEvaluator();
            }
        });
    }
}

class StockData {
    private String date;
    private double open;
    private double high;
    private double low;
    private double close;
    private double adjClose;
    private long volume;

    public StockData(String date, double open, double high, double low, double close, double adjClose, long volume) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.adjClose = adjClose;
        this.volume = volume;
    }

    public String getDate() {
        return date;
    }

    public double getOpen() {
        return open;
    }

    public double getHigh() {
        return high;
    }

    public double getLow() {
        return low;
    }

    public double getClose() {
        return close;
    }

    public double getAdjClose() {
        return adjClose;
    }

    public long getVolume() {
        return volume;
    }
}
