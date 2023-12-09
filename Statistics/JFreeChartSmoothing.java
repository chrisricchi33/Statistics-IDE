import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.apache.commons.math4.legacy.exception.MathIllegalArgumentException;
import org.apache.commons.math4.legacy.stat.descriptive.moment.Mean;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class JFreeChartSmoothing extends JFrame {

    private JLabel statusLabel;
    private JButton loadCSVButton;
    private JButton smoothDataButton;
    private JButton saltDataButton;
    private JButton revertButton;
    private JButton exportCSVButton;
    private ChartPanel chartPanel;

    private File loadedFile;
    private double[] originalXData;
    private double[] originalYData;
    private double[] xData;
    private double[] yData;

    public JFreeChartSmoothing() {
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
        chartPanel = createChartPanel();
        add(chartPanel, BorderLayout.CENTER);

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

    private ChartPanel createChartPanel() {
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Data Plot",
                "X-Axis",
                "Y-Axis",
                null
        );

        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        plot.setRenderer(renderer);

        NumberAxis xAxis = new NumberAxis("X-Axis");
        NumberAxis yAxis = new NumberAxis("Y-Axis");
        plot.setDomainAxis(xAxis);
        plot.setRangeAxis(yAxis);

        TickUnits units = new TickUnits();
        units.add(new NumberTickUnit(1.0));
        units.add(new NumberTickUnit(5.0));
        units.add(new NumberTickUnit(10.0));
        xAxis.setStandardTickUnits(units);

        return new ChartPanel(chart);
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

                originalXData = new double[0];
                originalYData = new double[0];
                xData = new double[0];
                yData = new double[0];

                while (scanner.hasNextLine()) {
                    String dataLine = scanner.nextLine();
                    String[] dataTokens = dataLine.split(",");

                    if (dataTokens.length != 2) {
                        showErrorMessage("CSV File contains improperly formatted data");
                        return;
                    }

                    try {
                        double x = Double.parseDouble(dataTokens[0].trim());
                        double y = Double.parseDouble(dataTokens[1].trim());

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
    
    private double[] resizeArray(double[] arr, int newSize) {
        double[] newArray = new double[newSize];
        System.arraycopy(arr, 0, newArray, 0, Math.min(arr.length, newSize));
        return newArray;
    }

    private void saltData() {
        // Salt each point with a random value within a reasonable range
        Random rand = new Random();
        double saltRange = 1.0; // You can adjust this range as needed

        if (originalXData != null && originalYData != null) {
            for (int i = 0; i < xData.length; i++) {
                double saltX = rand.nextDouble() * 2 * saltRange - saltRange;
                double saltY = rand.nextDouble() * 2 * saltRange - saltRange;

                xData[i] += saltX;
                yData[i] += saltY;
            }

            plotData();
        } else {
            showErrorMessage("No data detected. Please load a CSV file.");
        }
    }

    private void smoothData() {
        if (originalXData != null && originalYData != null) {
            int windowSize = 3;

            try {
                for (int i = 1; i < xData.length - 1; i++) {
                    double[] xSubset = Arrays.copyOfRange(xData, Math.max(0, i - windowSize / 2), Math.min(xData.length, i + windowSize / 2 + 1));
                    double[] ySubset = Arrays.copyOfRange(yData, Math.max(0, i - windowSize / 2), Math.min(yData.length, i + windowSize / 2 + 1));

                    // Calculate the mean value for the subset
                    Mean meanFunction = new Mean();
                    xData[i] = meanFunction.evaluate(xSubset);
                    yData[i] = meanFunction.evaluate(ySubset);
                }

                plotData();
            } catch (MathIllegalArgumentException e) {
                e.printStackTrace();
                showErrorMessage("Error smoothing data.");
            }
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
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "Data", "X-Axis", "Y-Axis",
                    createDataset(), // Call a method to create the dataset
                    org.jfree.chart.plot.PlotOrientation.VERTICAL,
                    true, true, false
            );

            XYPlot plot = chart.getXYPlot();
            chartPanel.setChart(chart);

            // Enable the "Revert to Original" button after loading data
            revertButton.setEnabled(true);
        } else {
            showErrorMessage("No data to plot. Please load a CSV file.");
        }
    }

    private XYSeriesCollection createDataset() {
        XYSeries series = new XYSeries("Data");
        for (int i = 0; i < xData.length; i++) {
            series.add(xData[i], yData[i]);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series);

        return dataset;
    }
    
    private void showErrorMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
            	JFreeChartSmoothing gui = new JFreeChartSmoothing();
                gui.setVisible(true);
            }
        });
    }
}
