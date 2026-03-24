import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.text.DateFormat;
import java.util.Date;

/**
 * YourKit Java Profiler demo application
 */
public class DemoApp extends JComponent {
  public DemoApp() {
  }

  /**
   * 0..1
   */
  private double clippedRatio;
  private double clipIncrement = 0.005;

  private int rotationAngle;
  private final FontRenderContext frc = new FontRenderContext(null, true, false);
  private final Font font = UIManager.getFont("Label.font").deriveFont(Font.BOLD, 32);

  void drawDemo(final Graphics2D g) {
    final Dimension d = getSize();
    final int w = d.width;
    final int h = d.height;

    final TextLayout text = new TextLayout( DateFormat.getTimeInstance().format(new Date()), font, frc );

    final int size = Math.min(d.width, d.height);

    final Shape textShape = text.getOutline( AffineTransform.getScaleInstance( (size - 40) / text.getBounds().getWidth(),
                                                                               (d.height / 3) / text.getBounds().getHeight()) );

    g.setColor(Color.WHITE);
    g.fillRect(0, 0, w, h);
    g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

    final int clipY = (int)(h * clippedRatio);
    final Rectangle rect = new Rectangle(0, clipY, w - 1, h - clipY);

    final double textWidth = textShape.getBounds().getWidth();
    final double textHeight = textShape.getBounds().getHeight();

    final AffineTransform t = new AffineTransform();
    t.rotate(Math.toRadians(rotationAngle), w / 2, h / 2);
    t.translate(w / 2 - textWidth / 2, textHeight + (h - textHeight) / 2);

    final GeneralPath path = new GeneralPath();

    path.append(new Ellipse2D.Double(10, 10, 20, 20), false);
    path.append(new Ellipse2D.Double(d.width - 30, 10, 20, 20), false);
    path.append(new Ellipse2D.Double(10, d.height - 30, 20, 20), false);
    path.append(new Ellipse2D.Double(d.width - 30, d.height - 30, 20, 20), false);

    path.append(t.createTransformedShape(textShape), false);

    g.clip(rect);
    g.clip(path);

    g.setColor(Color.GREEN);
    g.fill(rect);

    g.setClip(new Rectangle(0, 0, w, h));

    g.setColor(Color.LIGHT_GRAY);
    g.draw(rect);
    g.setColor(Color.BLACK);
    g.draw(path);
  }

  @Override
  public void paint(final Graphics g) {
    drawDemo((Graphics2D)g);
  }

  void startDemo() {
    final Timer timer = new Timer( 20, new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent e) {
          clippedRatio += clipIncrement;
          if (clippedRatio >= 1) {
            clippedRatio = 1;
            clipIncrement = -clipIncrement;
          }
          else if (clippedRatio <= 0) {
            clippedRatio = 0;
            clipIncrement = -clipIncrement;
          }
          rotationAngle = (rotationAngle + 1) % 360;
          repaint();
        }
      }
    );
    timer.setRepeats(true);
    timer.start();
  }

  public static void main(final String[] argv) {
    final DemoApp demo = new DemoApp();
    final JFrame f = new JFrame("YourKit Demo App");
    f.addWindowListener( new WindowAdapter() {
        @Override
        public void windowClosing(final WindowEvent e) {
          System.exit(0);
        }
      } );
    f.add(demo);
    f.setSize(new Dimension(500, 500));
    f.setVisible(true);

    demo.startDemo();
  }
}
