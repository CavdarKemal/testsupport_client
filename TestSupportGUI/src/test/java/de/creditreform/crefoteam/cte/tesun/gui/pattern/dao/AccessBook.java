package de.creditreform.crefoteam.cte.tesun.gui.pattern.dao;

import de.creditreform.crefoteam.cte.tesun.gui.pattern.dao.dao.BookDao;
import de.creditreform.crefoteam.cte.tesun.gui.pattern.dao.daoimpl.BookDaoImpl;
import de.creditreform.crefoteam.cte.tesun.gui.pattern.dao.model.Books;

public class AccessBook {

   public static void main(String[] args) {

      BookDao bookDao = new BookDaoImpl();

      for (Books book : bookDao.getAllBooks()) {
         System.out.println("Book ISBN : " + book.getIsbn());
      }

      //update student
      Books book = bookDao.getAllBooks().get(1);
      book.setBookName("Algorithms");
      bookDao.saveBook(book);
   }
}
