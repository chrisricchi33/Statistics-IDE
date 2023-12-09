import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class DataSmoothing extends JFrame {

    private JLabel statusLabel;
    private JButton loadCSVButton;
    private JButton smoothDataButton;
    private JButton saltDataButton;
    private JButton revertButton;
    private JButton exportCSVButton;
    private PlotPanel plotPanel;

    private File loadedFile;
    private int[] originalXData;
    private int[] originalYData;
    private int[] xData;
    private int[] yData;

    public DataSmoothing() {
        setTitle("CSV Loader");
        setSize(1000, 800);
        setMaximumSize(new Dimension(1000, 800));
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Top Panel
        JPanel topPanel = new JPanel();
        statusLabel = new JLabel("No CSV data loaded. Please load CSV file.");
        statusLabel.setForeground(Color.RED);
        topPanel.add(statusLabel);
        add(topPanel, BorderLayout.NORTH);

        // Center Panel
        plotPanel = new PlotPanel();
        add(plotPanel, BorderLayout.CENTER);

        // Bottom Panel
        JPanel bottomPanel = new JPanel();
        loadCSVButton = new JButton("Load CSV");
        smoothDataButton = new JButton("Smooth Data");
        saltDataButton = new JButton("Salt Data");
        revertButton = new JButton("Revert to Original");
        exportCSVButton = new JButton("Export as CSV");

        loadCSVButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loadCSV();
                // Automatically plot data upon loading CSV
                plotData();
            }
        });

        smoothDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                smoothData();
            }
        });

        saltDataButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saltData();
            }
        });

        revertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                revertToOriginal();
            }
        });

        exportCSVButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportToCSV();
            }
        });

        bottomPanel.add(loadCSVButton);
        bottomPanel.add(smoothDataButton);
        bottomPanel.add(saltDataButton);
        bottomPanel.add(revertButton);
        bottomPanel.add(exportCSVButton);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void loadCSV() {
        JFileChooser fileChooser = new JFileChooser();
        int result = fileChooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            loadedFile = fileChooser.getSelectedFile();

            try {
                Scanner scanner = new Scanner(loadedFile);

                if (!scanner.hasNextLine()) {
                    showErrorMessage("CSV File is empty");
                    return;
                }

                // Read the header line
                String headerLine = scanner.nextLine();
                String[] headerTokens = headerLine.split(",");
                if (headerTokens.length != 2) {
                    showErrorMessage("CSV File contains improperly formatted header");
                    return;
                }

                String xAxisLabel = headerTokens[0].trim();
                String yAxisLabel = headerTokens[1].trim();

                originalXData = new int[0];
                originalYData = new int[0];
                xData = new int[0];
                yData = new int[0];

                while (scanner.hasNextLine()) {
                    String dataLine = scanner.nextLine();
                    String[] dataTokens = dataLine.split(",");

                    if (dataTokens.length != 2) {
                        showErrorMessage("CSV File contains improperly formatted data");
                        return;
                    }

                    try {
                        int x = Integer.parseInt(dataTokens[0].trim());
                        int y = Integer.parseInt(dataTokens[1].trim());

                        // Resize arrays to accommodate new data point
                        originalXData = resizeArray(originalXData, originalXData.length + 1);
                        originalYData = resizeArray(originalYData, originalYData.length + 1);

                        // Store original data point
                        originalXData[originalXData.length - 1] = x;
                        originalYData[originalYData.length - 1] = y;

                        // Initialize current data point with original values
                        xData = Arrays.copyOf(originalXData, originalXData.length);
                        yData = Arrays.copyOf(originalYData, originalYData.length);
                    } catch (NumberFormatException ex) {
                        showErrorMessage("CSV File contains improperly formatted data");
                        return;
                    }
                }

                statusLabel.setText(loadedFile.getName());
                statusLabel.setForeground(Color.GREEN);

            } catch (FileNotFoundException ex) {
                showErrorMessage("Error reading the file");
            }
        }
    }

    private void saltData() {
        // Salt each point with a random value within a reasonable range
        Random rand = new Random();
        int saltRange = 1; // You can adjust this range as needed
        
        if (originalXData != null && originalYData != null) {
        	for (int i = 0; i < xData.length; i++) {
                int saltX = rand.nextInt(2 * saltRange + 1) - saltRange;
                int saltY = rand.nextInt(2 * saltRange + 1) - saltRange;

                xData[i] += saltX;
                yData[i] += saltY;
            }

            plotData();
        } else {
            showErrorMessage("No data detected. Please load a CSV file.");
        }
    }

    private void smoothData() {
        // Implement a simple smoothing algorithm (e.g., moving average) based on your data characteristics
        // For simplicity, let's use a simple moving average with a window size of 3
    	
    	if (originalXData != null && originalYData != null) {
    		for (int i = 1; i < xData.length - 1; i++) {
                xData[i] = (xData[i - 1] + xData[i] + xData[i + 1]) / 3;
                yData[i] = (yData[i - 1] + yData[i] + yData[i + 1]) / 3;
            }
    		
    		plotData();
        } else {
        	showErrorMessage("No data detected. Please load a CSV file.");
        }
    }

    private void revertToOriginal() {
        // Restore original data
        if (originalXData != null && originalYData != null) {
            xData = Arrays.copyOf(originalXData, originalXData.length);
            yData = Arrays.copyOf(originalYData, originalYData.length);
            plotData();
        } else {
            showErrorMessage("No original data to revert to.");
        }
    }

    private void plotData() {
        if (xData != null && yData != null) {
            plotPanel.updateData("X-Axis", "Y-Axis", xData, yData);
            // Enable the "Revert to Original" button after loading data
            revertButton.setEnabled(true);
        } else {
            showErrorMessage("No data to plot. Please load a CSV file.");
        }
    }

    private void exportToCSV() {
        if (xData != null && yData != null) {
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showSaveDialog(this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File outputFile = fileChooser.getSelectedFile();
                try (PrintWriter writer = new PrintWriter(outputFile)) {
                    writer.println("X-Axis,Y-Axis");
                    for (int i = 0; i < xData.length; i++) {
                        writer.println(xData[i] + "," + yData[i]);
                    }
                } catch (FileNotFoundException e) {
                    showErrorMessage("Error exporting data to CSV");
                }
            }
        } else {
            showErrorMessage("No data to export. Please load a CSV file.");
        }
    }

    public class PlotPanel extends JPanel {
        private String xAxisLabel;
        private String yAxisLabel;
        private int[] xData;
        private int[] yData;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            drawCoordinateGrid(g);
            if (xData != null && yData != null) {
                plotData(g);
            }
        }

        private void drawCoordinateGrid(Graphics g) {
            int margin = 30;

            // Find the maximum values in xData and yData
            int xMax = getMaxValue(xData);
            int yMax = getMaxValue(yData);

            // Draw horizontal grid lines
            for (int i = 0; i <= 10; i++) {
                int y = margin + i * (getHeight() - 2 * margin) / 10;
                g.drawLine(margin, y, getWidth() - margin, y);
                // Display scale values on the side
                String label = String.valueOf((int) (yMax * (10 - i) / 10.0));
                g.drawString(label, margin - 25, y + 5);
            }

            // Draw vertical grid lines
            for (int i = 0; i <= 10; i++) {
                int x = margin + i * (getWidth() - 2 * margin) / 10;
                g.drawLine(x, getHeight() - margin, x, margin);
                // Display scale values on the bottom
                String label = String.valueOf((int) (xMax * i / 10.0));
                g.drawString(label, x - 10, getHeight() - margin + 20);
            }
        }

        private int getMaxValue(int[] array) {
            if (array == null || array.length == 0) {
                return 1;
            }
            int max = array[0];
            for (int value : array) {
                max = Math.max(max, value);
            }
            return Math.max(1, max);
        }

        private void plotData(Graphics g) {
            int margin = 30;

            // Find the maximum values in xData and yData
            int xMax = getMaxValue(xData);
            int yMax = getMaxValue(yData);

            // Plot data points
            for (int i = 0; i < xData.length; i++) {
                int x = margin + xData[i] * (getWidth() - 2 * margin) / xMax;
                int y = getHeight() - margin - yData[i] * (getHeight() - 2 * margin) / yMax;
                g.fillOval(x - 3, y - 3, 6, 6); // Plot points as circles

                // Display data values near the points
                g.drawString("(" + xData[i] + ", " + yData[i] + ")", x + 8, y - 8);
            }

            // Connect data points with lines
            g.setColor(Color.BLUE);
            for (int i = 1; i < xData.length; i++) {
                int x1 = margin + xData[i - 1] * (getWidth() - 2 * margin) / xMax;
                int y1 = getHeight() - margin - yData[i - 1] * (getHeight() - 2 * margin) / yMax;
                int x2 = margin + xData[i] * (getWidth() - 2 * margin) / xMax;
                int y2 = getHeight() - margin - yData[i] * (getHeight() - 2 * margin) / yMax;
                g.drawLine(x1, y1, x2, y2);
            }
            g.setColor(Color.BLACK);
        }

        public void updateData(String xAxisLabel, String yAxisLabel, int[] xData, int[] yData) {
            this.xAxisLabel = xAxisLabel;
            this.yAxisLabel = yAxisLabel;
            this.xData = xData;
            this.yData = yData;
            repaint();
        }
    }

    private int[] resizeArray(int[] arr, int newSize) {
        int[] newArray = new int[newSize];
        System.arraycopy(arr, 0, newArray, 0, Math.min(arr.length, newSize));
        return newArray;
    }

    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DataSmoothing gui = new DataSmoothing();
                gui.setVisible(true);
            }
        });
    }
}
