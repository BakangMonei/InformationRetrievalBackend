package com.moneibakang.informationretrievalbackend.model;

/*
 * @Author: Monei Bakang
 * @Date: 21 March 2025
 * @Time: 01:52 hours
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import java.util.Random;

public class FractalTree extends JPanel {
    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final Random random = new Random(42);

    private Color backgroundColor = new Color(15, 15, 35);
    private Color[] gradientColors = {
            new Color(65, 105, 225),  // Royal Blue
            new Color(138, 43, 226),  // Blue Violet
            new Color(199, 21, 133),  // Medium Violet Red
            new Color(255, 105, 180)  // Hot Pink
    };

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Enable antialiasing for smoother lines
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Background
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw the fractal tree
        drawTree(g2d, WIDTH / 2, HEIGHT, -90, 11, 150);
    }

    private void drawTree(Graphics2D g, int x1, int y1, double angle, int depth, double length) {
        if (depth == 0) return;

        // Calculate branch end coordinates
        int x2 = x1 + (int) (Math.cos(Math.toRadians(angle)) * length);
        int y2 = y1 + (int) (Math.sin(Math.toRadians(angle)) * length);

        // Color based on depth
        float colorIndex = (float) depth / 11.0f;
        Color branchColor = getGradientColor(colorIndex);
        g.setColor(branchColor);

        // Set stroke width based on depth
        float strokeWidth = Math.max(1, depth * 0.8f);
        g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Draw the branch
        g.drawLine(x1, y1, x2, y2);

        // Add some randomness to angles and length
        double angleVariation = 15 + random.nextDouble() * 5;
        double lengthFactor = 0.75 - random.nextDouble() * 0.1;

        // Recursive calls for branches
        drawTree(g, x2, y2, angle - angleVariation, depth - 1, length * lengthFactor);
        drawTree(g, x2, y2, angle + angleVariation, depth - 1, length * lengthFactor);
    }

    private Color getGradientColor(float position) {
        // Map position to an index in the color array
        float indexFloat = position * (gradientColors.length - 1);
        int index = (int) indexFloat;
        float fraction = indexFloat - index;

        // Handle edge cases
        if (index >= gradientColors.length - 1) return gradientColors[gradientColors.length - 1];
        if (index < 0) return gradientColors[0];

        // Interpolate between colors
        Color color1 = gradientColors[index];
        Color color2 = gradientColors[index + 1];

        int r = (int) (color1.getRed() + fraction * (color2.getRed() - color1.getRed()));
        int g = (int) (color1.getGreen() + fraction * (color2.getGreen() - color1.getGreen()));
        int b = (int) (color1.getBlue() + fraction * (color2.getBlue() - color1.getBlue()));

        return new Color(r, g, b);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Fractal Tree");
        FractalTree panel = new FractalTree();
        panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.add(panel);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        // Optional: Save as image
        // saveAsImage(panel);
    }

    private static void saveAsImage(FractalTree panel) {
        BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        panel.paint(g2d);
        g2d.dispose();

        // Code to save image would go here
        // ImageIO.write(image, "png", new File("fractal_tree.png"));
    }
}