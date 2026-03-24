package de.creditreform.crefoteam.cte.tesun.gui;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

public class FilePreviewer extends JFrame
{

  private JTree     tree;
  private JTextArea preview;
  private JLabel    status;

  public FilePreviewer( String directory )
  {
    tree = new JTree( new FileSelectorModel( directory ) );

    preview = new JTextArea();
    preview.setWrapStyleWord( true );
    preview.setLineWrap( true );
    preview.setEditable( false );

    status = new JLabel( directory );

    tree.addTreeSelectionListener( new TreeSelectionListener()
    {

      public void valueChanged( TreeSelectionEvent e )
      {
        FileNode selectedNode = (FileNode)tree.getLastSelectedPathComponent();
        status.setText( selectedNode.getAbsolutePath() );
        if( selectedNode.isFile() )
        {
          preview.setText( null );
          try
          {
            BufferedReader br = new BufferedReader( new FileReader( selectedNode.getAbsolutePath() ) );
            String line = "";
            while( ( line = br.readLine() ) != null )
            {
              preview.append( line );
              preview.append( System.getProperty( "line.separator" ) );
            }
          }
          catch (Exception ex)
          {
            new RuntimeException(ex);
          }
        }
      }
    } );

    getContentPane().add( BorderLayout.WEST, new JScrollPane( tree ) );
    getContentPane().add( BorderLayout.SOUTH, status );
    getContentPane().add( new JScrollPane( preview ) );

    setSize( 800, 600 );
    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    setTitle( "Quick'n'Dirty File Preview" );
    setVisible( true );
  }

  public static void main( String [] args )
  {
    new FilePreviewer( "C:\\" );
  }
}
