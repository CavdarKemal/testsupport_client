package de.creditreform.crefoteam.cte.tesun.gui.swingworker;

import java.io.File;
import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class DirectoryTreeModel extends DefaultTreeModel
{
  public DirectoryTreeModel( String path )
  {
    super( new DefaultMutableTreeNode( path ), false );
  }

  public void addFileNode( File theFile )
  {
    DefaultMutableTreeNode parentNode = findParentNode( theFile );
    if( parentNode == null )
    {
      parentNode = (DefaultMutableTreeNode)super.getRoot();
    }
    FileNode fileNode = new FileNode( theFile );
    parentNode.add( fileNode );
    nodeStructureChanged( fileNode.getParent() );
  }

  public DefaultMutableTreeNode findParentNode( File theFile )
  {
    // System.out.println("File: " + theFile.getPath() + "...");
    DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)super.getRoot();
    String parentPath = theFile.getParent();
    Enumeration childsEnumeration = rootNode.breadthFirstEnumeration();
    while( childsEnumeration.hasMoreElements() )
    {
      Object nextElement = childsEnumeration.nextElement();
      if( nextElement instanceof FileNode )
      {
        FileNode currentNode = (FileNode)nextElement;
        String currentPath = ( (File)currentNode.getUserObject() ).getPath();
        if( parentPath.equals( currentPath ) )
        {
          // System.out.println("\t--> " + currentPath );
          return currentNode;
        }
      }
    }
    // System.out.println("\t--> ROOT");
    return null;
  }

  class FileNode extends DefaultMutableTreeNode
  {

    public FileNode( File theFile )
    {
      super( theFile );
    }

    @Override public String toString()
    {
      File theFile = (File)getUserObject();
      return ( theFile.isDirectory() ? ( "[ " + theFile.getName() + " ]" + "{" + getChildCount() +"}" ) : theFile.getName() );
    }
  }
}
