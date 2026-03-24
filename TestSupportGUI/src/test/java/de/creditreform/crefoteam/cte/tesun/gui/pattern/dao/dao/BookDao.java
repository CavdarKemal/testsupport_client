package de.creditreform.crefoteam.cte.tesun.gui.pattern.dao.dao;

import de.creditreform.crefoteam.cte.tesun.gui.pattern.dao.model.Books;

import java.util.List;

public interface BookDao {

    List<Books> getAllBooks();
    Books getBookByIsbn(int isbn);
    void saveBook(Books book);
    void deleteBook(Books book);
}
