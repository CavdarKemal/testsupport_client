package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import de.creditreform.crefoteam.cte.tesun.gui.utils.GUIStaticUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class WorkerResultsView extends WorkerResultsPanel
{
  private DirectorySwingWorker theWorker;

  public WorkerResultsView( String choosenWorkPath )
  {
    initListeners();
    initModel( choosenWorkPath );
    changeControlsState();
  }

  private void initModel( String choosenWorkPath )
  {
    getTextFieldWorkerPath().setText( choosenWorkPath );
    getTextFieldFileExtensions().setText( "*.*" );
  }

  private void initListeners()
  {
    getButtonClear().addActionListener( new ActionListener()
    {
      @Override public void actionPerformed( ActionEvent actionEvent )
      {
        doClear();
      }
    } );
    getTextFieldWorkerPath().addActionListener( new ActionListener()
    {
      @Override public void actionPerformed( ActionEvent actionEvent )
      {
        changeControlsState();
      }
    } );
    getButtonSelectWorkerPath().addActionListener( new ActionListener()
    {
      @Override public void actionPerformed( ActionEvent actionEvent )
      {
        doSelectWorkerPath();
      }
    } );
    getButtonStartStop().addActionListener( new ActionListener()
    {
      @Override public void actionPerformed( ActionEvent actionEvent )
      {
        doStartStop();
      }
    } );
  }

  private void changeControlsState()
  {
    String defFileName = getTextFieldWorkerPath().getText();
    boolean is = !defFileName.isEmpty() && new File( defFileName ).exists();
    getTextFieldWorkerPath().setForeground( is ? Color.BLACK : Color.RED );
    getButtonStartStop().setEnabled( is );
  }

  protected void doStartStop()
  {
    String actionCommand = getButtonStartStop().getText();
    if( actionCommand.contains( "start" ) )
    {
      GUIStaticUtils.setWaitCursor( this, true );
      getButtonSelectWorkerPath().setEnabled( false );
      getButtonStartStop().setText( "Suche abbrechen" );
      getTextAreaLogs().setText( "" );
      getProgressBarSearch().setVisible( true );
      getProgressBarSearch().setValue( 0 );
      getTabbedPaneDetails().setSelectedIndex( 0 );
      String sourcePath = getTextFieldWorkerPath().getText();
      final DirectoryTreeModel directoryTreeModel = new DirectoryTreeModel( sourcePath );
      getTreeResults().setModel( directoryTreeModel );
      getTreeResults().setCellRenderer( new DirectoryTreeCellRenderer() );
      // FileFilter fileFilter = new WildcardFileFilter( getTextFieldFileExtensions().getText() );
      FileFilter fileFilter = TrueFileFilter.INSTANCE;
      theWorker = new DirectorySwingWorker( sourcePath, fileFilter )
      {
        @Override protected void process( List<File> chunks )
        {
          for( File theFile : chunks )
          {
            directoryTreeModel.addFileNode( theFile );
          }
        }

        @Override protected void done()
        {
          GUIStaticUtils.setWaitCursor( WorkerResultsView.this, false );
          getButtonStartStop().setText( "Suche starten" );
          getProgressBarSearch().setVisible( false );
          getButtonSelectWorkerPath().setEnabled( true );
          try
          {
            Integer numFiles = get();
            if( isCancelled() )
            {
              System.out.println( "\nVorgang abgebrochen!" );
            }
          }
          catch( InterruptedException ex )
          {
          }
          catch( ExecutionException ex )
          {
          }
        }
      };
      theWorker.addPropertyChangeListener( new PropertyChangeListener()
      {
        public void propertyChange( PropertyChangeEvent evt )
        {
          String propertyName = evt.getPropertyName();
          if( "progress".equals( propertyName ) )
          {
            getProgressBarSearch().setValue( (Integer)evt.getNewValue() );
          }
          else if( "state".equals( propertyName ) )
          {
            getTextAreaLogs().append( evt.getNewValue().toString() + "\n" );
          }
        }
      } );
      theWorker.execute();
    }
    else if( theWorker != null )
    {
      theWorker.cancel( true );
    }
  }

  protected void doSelectWorkerPath()
  {
    String defWorkPath = getTextFieldWorkerPath().getText();
    if( defWorkPath.isEmpty() )
    {
      defWorkPath = System.getProperty( "user.dir" );
    }
    String choosenWorkPath = defWorkPath;
    {
      choosenWorkPath = GUIStaticUtils.chooseDirectory( this, defWorkPath, "Worker-Verzeichnis wählen" );
      if( !GUIStaticUtils.isEmpty( choosenWorkPath ) )
      {
        initModel( choosenWorkPath );
      }
      changeControlsState();
    }
  }

  private void doClear()
  {
  }

  public static void main( String[] cmdArgs )
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      @Override public void run()
      {
        try
        {
          JFrame mainFrame = new JFrame();
          WorkerResultsView workerResultsView = new WorkerResultsView( System.getProperty( "user.dir" ) );
          mainFrame.getContentPane().add( workerResultsView );
          mainFrame.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
          mainFrame.setSize( new Dimension( 1024, 800 ) );
          mainFrame.setLocationRelativeTo( null );
          mainFrame.setVisible( true );
        }
        catch( Exception ex )
        {
          ex.printStackTrace();
          System.exit( -1 );
        }
      }
    } );
  }

}
