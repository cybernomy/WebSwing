package org.webswing.demo.dnd;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.QuadCurve2D;
import java.io.IOException;

public class CubicCurveMouse extends JFrame {
  DrawingCanvas canvas;

  JLabel label = new JLabel("Mouse Location (x, y):  "); 
  JLabel coords = new JLabel("");

  public CubicCurveMouse() {
	  super();
    Container container = getContentPane();
    Image icon= null;
    try {
      icon = ImageIO.read(this.getClass().getResource("paintbrush.gif"));
      Cursor c = Toolkit.getDefaultToolkit().createCustomCursor(icon, new Point(5, 15), "brush");
      container.setCursor(c);
    } catch (IOException e) {
      e.printStackTrace();
    }

    JPanel panel = new JPanel();
    setBackground(Color.white);
    panel.setLayout(new GridLayout(1, 2));
    
    panel.add(label);
    panel.add(label);

    panel.add(coords);

    container.add(panel, BorderLayout.SOUTH);

    canvas = new DrawingCanvas();
    container.add(canvas);

    setSize(300,300);
    setVisible(true);
  }

  public static void main(String arg[]) {
    new CubicCurveMouse();
  }

  class DrawingCanvas extends JPanel {

    float x1, y1, xc1cur, yc1cur, xc1new, yc1new, xc2cur, yc2cur, xc2new,
        yc2new, x4cur, y4cur, x4new, y4new;

    int pressNo = 0;

    int dragFlag1 = -1;

    int dragFlag2 = -1;

    boolean clearFlag = false;

    float dashes[] = { 5f, 5f };

    BasicStroke stroke;

    public DrawingCanvas() {
      setBackground(Color.white);
      addMouseListener(new MyMouseListener());
      addMouseMotionListener(new MyMouseListener());
      setSize(400, 400);
      stroke = new BasicStroke(3f, BasicStroke.CAP_BUTT,
          BasicStroke.JOIN_BEVEL, 10f, dashes, 0f);
    }

    public void update(Graphics g) {
      paint(g);
    }

    public void paint(Graphics g) {
      Graphics2D g2D = (Graphics2D) g;

      if (pressNo == 1) {
        g2D.setXORMode(getBackground());
        g2D.setColor(Color.black);
        g2D.setStroke(stroke);

        // Erase the currently existing line
        g2D.draw(new Line2D.Float(x1, y1, x4cur, y4cur));
        // Draw the new line
        g2D.draw(new Line2D.Float(x1, y1, x4new, y4new));

        // Update the currently existing coordinate values
        x4cur = x4new;
        y4cur = y4new;
      }else if (pressNo == 2) {
        g2D.setXORMode(getBackground());
        g2D.setColor(Color.black);
        g2D.setStroke(stroke);

        if (dragFlag1 != -1) {
          g2D.draw(new QuadCurve2D.Float(x1, y1, xc1cur, yc1cur,
              x4new, y4new));
        }
        dragFlag1++; // Reset the drag-flag

        g2D.draw(new QuadCurve2D.Float(x1, y1, xc1new, yc1new, x4new,
            y4new));

        xc1cur = xc1new;
        yc1cur = yc1new;
      }else if (pressNo == 3) {
        g2D.setXORMode(getBackground());
        g2D.setColor(Color.black);

        if (dragFlag2 != -1) {
          g2D.draw(new CubicCurve2D.Float(x1, y1, xc1new, yc1new,
              xc2cur, yc2cur, x4new, y4new));
        }
        dragFlag2++; // Reset the drag flag
        g2D.draw(new CubicCurve2D.Float(x1, y1, xc1new, yc1new, xc2new,
            yc2new, x4new, y4new));
        xc2cur = xc2new;
        yc2cur = yc2new;
      }
      if (clearFlag) {
        g2D.setXORMode(getBackground());
        g2D.setColor(Color.black);
        g2D.setStroke(stroke);

        g2D.draw(new Line2D.Float(x1, y1, x4new, y4new));
        g2D.draw(new QuadCurve2D.Float(x1, y1, xc1new, yc1new, x4new,
            y4new));
        clearFlag = false;
      }
    }

    class MyMouseListener extends MouseAdapter implements MouseMotionListener {
      public void mousePressed(MouseEvent e) {
        if (pressNo == 0) { 
          pressNo++;
          x1 = x4cur = e.getX();
          y1 = y4cur = e.getY();
        } else if (pressNo == 1) {
          pressNo++;
          xc1cur = e.getX();
          yc1cur = e.getY();
        } else if (pressNo == 2) {
          pressNo++;
          xc2cur = e.getX();
          yc2cur = e.getY();
        }
      }
      public void mouseReleased(MouseEvent e) {
        if (pressNo == 1) {
          x4new = e.getX();
          y4new = e.getY();
          canvas.repaint();
        } else if (pressNo == 2) {
          xc1new = e.getX();
          yc1new = e.getY();
          canvas.repaint();
        } else if (pressNo == 3) {
          xc2new = e.getX();
          yc2new = e.getY();
          canvas.repaint();
          pressNo = 0;
          dragFlag1 = -1;
          dragFlag2 = -1;
          clearFlag = true;
        }
      }
      public void mouseDragged(MouseEvent e) {
        if (pressNo == 1) {
          x4new = e.getX();
          y4new = e.getY();
          String string = "(" + Integer.toString(e.getX()) + ", "
              + Integer.toString(e.getY()) + ")";
          coords.setText(string);
          canvas.repaint();
        } else if (pressNo == 2) {
          xc1new = e.getX();
          yc1new = e.getY();

          String string = "(" + Integer.toString(e.getX()) + ", "
              + Integer.toString(e.getY()) + ")";
          coords.setText(string);
          canvas.repaint();
        } else if (pressNo == 3) {
          xc2new = e.getX();
          yc2new = e.getY();
          String string = "(" + Integer.toString(e.getX()) + ", "
              + Integer.toString(e.getY()) + ")";
          coords.setText(string);
          canvas.repaint();
        }
      }

      public void mouseMoved(MouseEvent e) {
        String string = "(" + Integer.toString(e.getX()) + ", "
            + Integer.toString(e.getY()) + ")";
        coords.setText(string);
      }
    }
  }
}
