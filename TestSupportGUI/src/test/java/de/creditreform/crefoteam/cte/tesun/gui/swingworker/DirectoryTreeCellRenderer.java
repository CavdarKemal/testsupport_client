package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.io.File;

public class DirectoryTreeCellRenderer extends DefaultTreeCellRenderer
{

  private static final Icon closedDirectoryIcon = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_closed.png" ) );
  private static final Icon openedDirectoryIcon = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon xmlFileIcon         = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon jarFileIcon        = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon javaFileIcon        = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon classFileIcon        = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon txtFileIcon         = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon propsFileIcon         = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon iniFileIcon         = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon iconFileIcon         = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );
  private static final Icon defFileIcon         = new ImageIcon( DirectoryTreeCellRenderer.class.getResource( "/icons/folder_out.png" ) );

  @Override public Component getTreeCellRendererComponent( JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus )
  {
    super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    Object userObject = ( (DefaultMutableTreeNode)value ).getUserObject();
    if(!(userObject instanceof File) )
    {
      return this;
    }
    File theFile = (File)userObject;
    if( theFile.isDirectory() )
    {
      if( expanded )
      {
        setIcon( openedDirectoryIcon );
        setFont(getFont().deriveFont(Font.BOLD));
        setForeground(new Color(1, 222 ,75));
      }
      else
      {
        setIcon( closedDirectoryIcon );
        setFont(getFont().deriveFont(Font.PLAIN ));
        setForeground(new Color(93, 57 ,15));
      }
    }
    else
    {
      setFont(getFont().deriveFont(Font.PLAIN + Font.ITALIC));
      setForeground(new Color(193, 57 ,115));
      String tileName = theFile.getName();
      if( tileName.endsWith( ".xml" ) )
      {
        setIcon( xmlFileIcon );
      }
      else if( tileName.endsWith( ".java" ) )
      {
        setIcon( javaFileIcon );
      }
      else if( tileName.endsWith( ".jar" ) )
      {
        setIcon( jarFileIcon );
      }
      else if( tileName.endsWith( ".class" ) )
      {
        setIcon( classFileIcon );
      }
      else if( tileName.endsWith( ".txt" ) )
      {
        setIcon( txtFileIcon );
      }
      else if( tileName.endsWith( ".ini" ) )
      {
        setIcon( iniFileIcon );
      }
      else if( tileName.endsWith( ".properties" ) )
      {
        setIcon( propsFileIcon );
      }
      else if( tileName.endsWith( ".png" ) )
      {
        setIcon( iconFileIcon );
      }
      else
      {
        setIcon( defFileIcon );
      }
    }
    return this;
  }
}
