package de.creditreform.crefoteam.cte.tesun.gui.utils;

import de.creditreform.crefoteam.cte.tesun.util.EnvironmentConfig;
import de.creditreform.crefoteam.cte.tesun.util.TimelineLogger;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import javax.swing.*;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;

public abstract class GUIFrame extends JFrame implements ActionListener {
    EnvironmentConfig environmentConfig;

    public GUIFrame(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;

        Dimension dimension = new Dimension(environmentConfig.getLastWindowWidth(), environmentConfig.getLastWindowHeight());
        setSize(dimension);
        setLocation(environmentConfig.getLastWindowXPos(), environmentConfig.getLastWindowYPos());

        JMenuBar theJMenuBar = createMenuBar();
        setJMenuBar(theJMenuBar);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent componentEvent) {
                shutDown();
            }
        });
    }

    public EnvironmentConfig getEnvironmentConfig() {
        return environmentConfig;
    }

    public void setEnvironmentConfig(EnvironmentConfig environmentConfig) {
        this.environmentConfig = environmentConfig;
    }

    public void setVersionsInfoInTitle(String versionsInfoInTitle) {
        setTitle(versionsInfoInTitle);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem) actionEvent.getSource();
        String actionCommand = menuItem.getActionCommand();
        doChangeLookAndFeel(actionCommand);
    }

    private void doChangeLookAndFeel(String lookAndFeel) {
        try {
            Dimension size = getSize();
            UIManager.setLookAndFeel(lookAndFeel);
            SwingUtilities.updateComponentTreeUI(this);
            pack();
            setSize(size);
            environmentConfig.setLastLookAndFeelClass(lookAndFeel);
        } catch (Exception ex) {
            new RuntimeException(ex);
        }
    }

    public static String getVersionFromPOM() {
        MavenXpp3Reader mavenXpp3Reader = new MavenXpp3Reader();
        String version = null;
        try {
            Model model = mavenXpp3Reader.read(new FileReader("pomx.xml"));
            Parent parent = null;
            version = model.getVersion();
            while (version == null) {
                parent = model.getParent();
                version = parent.getVersion();
            }
            return "POM:" + version;
        } catch (Exception ex) {
            return null;
        }
    }

    public JRadioButtonMenuItem createToLookAndFeelMenu(final UIManager.LookAndFeelInfo lookAndFeelInfo) {
        JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem(lookAndFeelInfo.getName());
        menuItem.setActionCommand(lookAndFeelInfo.getClassName());
        menuItem.addActionListener(this);
        return menuItem;
    }

    private JMenuBar createMenuBar() {
        JMenuBar jMenuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("Datei");
        JMenuItem exitMenuItem = new JMenuItem("Beenden");
        exitMenuItem.setMnemonic(KeyEvent.VK_B);
        exitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shutDown();
                System.exit(0);
            }
        });
        fileMenu.add(exitMenuItem);
        jMenuBar.add(fileMenu);

        final JMenu lookAndFeelsMenu = new JMenu("Look & Feels");
        jMenuBar.add(lookAndFeelsMenu);
        ButtonGroup buttonGroup = new ButtonGroup();
        UIManager.LookAndFeelInfo[] lookAndFeels = UIManager.getInstalledLookAndFeels();
        for (UIManager.LookAndFeelInfo lookAndFeelInfo : lookAndFeels) {
            final JRadioButtonMenuItem toLookAndFeelMenu = createToLookAndFeelMenu(lookAndFeelInfo);
            buttonGroup.add(toLookAndFeelMenu);
            lookAndFeelsMenu.add(toLookAndFeelMenu);
        }
        if (environmentConfig.getLastLookAndFeelClass() == null) {
            environmentConfig.setLastLookAndFeelClass(lookAndFeels[0].getClassName());
        }
        doChangeLookAndFeel(environmentConfig.getLastLookAndFeelClass());
        return jMenuBar;
    }

    private void shutDown() {
        Dimension size = getSize();
        Point location = getLocation();
        environmentConfig.setLastWindowXPos(location.x);
        environmentConfig.setLastWindowYPos(location.y);
        environmentConfig.setLastWindowWidth(size.width);
        environmentConfig.setLastWindowHeight(size.height);
        try {
            environmentConfig.updateEnvironmentConfig();
            TimelineLogger.close();

        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

}
