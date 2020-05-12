package es.ulpgc.LectioBackend.controller;

import com.google.gson.Gson;
import es.ulpgc.LectioBackend.model.Book;
import es.ulpgc.LectioBackend.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class BookController {

    @Autowired
    private BookRepository bookRepository;

    @RequestMapping(path = "/books", method = {RequestMethod.POST})
    public ResponseEntity createBook(@RequestBody Book book) {
        try {
            return buildResponse(HttpStatus.CREATED, store(book));
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"There was a problem, couldn't create book\" }");
        }
    }

    @RequestMapping(path = "/books", method = {RequestMethod.GET})
    public ResponseEntity getAllBooks(@RequestParam(required = false) String offset, @RequestParam(required = false, defaultValue = "0") String limit) {
        try {
            List<Book> books;
            if (offset == null || limit.equals("0")) {
                books = new ArrayList<>(bookRepository.findAll());
                offset = "0";
            } else {
                books = new ArrayList<>(
                        bookRepository.findAll(Integer.valueOf(offset) * Integer.valueOf(limit), Integer.valueOf(limit)));
            }

            return (books.isEmpty()) ? buildResponse(HttpStatus.NO_CONTENT, null) : buildPaginatedResponse(HttpStatus.OK, convertToJson(Integer.valueOf(offset), Integer.valueOf(limit), books));
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"There was a problem, couldn't get books\" }");
        }
    }

    @RequestMapping(path = "/books/{bookId}", method = {RequestMethod.GET})
    public ResponseEntity getBookById(@PathVariable(value = "bookId") long id) {
        try {
            return getIdResponse(id);
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"Couldn't find book with id " + id + "\" }");
        }
    }

    private String convertToJson(int offset, int limit, List<Book> books) {
        Gson gson = new Gson();
        return "{\"numBooks\": " + bookRepository.count() + ", \"page\": " + offset + ", \"size\": " + limit + ", \"books\": " + gson.toJson(books) + "}";
    }

    private Book store(@RequestBody Book book) {
        return bookRepository
                .save(new Book(book.getTitle(), book.getAuthor(), book.getPublisher(), book.getPages(), book.getIsbn(),
                        book.getGenres(), book.getSynopsis()));
    }

    private HttpHeaders setHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json; charset=UTF-8");
        return headers;
    }

    private <T> ResponseEntity<T> buildResponse(HttpStatus _status, T _body) {
        return ResponseEntity.status(_status)
                .headers(setHeaders())
                .body(_body);
    }

    private ResponseEntity<String> buildPaginatedResponse(HttpStatus _status, String response) {
        return ResponseEntity.status(_status)
                .headers(setHeaders())
                .body(response);
    }

    private ResponseEntity getIdResponse(@PathVariable("userId") long _id) {
        Book _book = bookRepository.findById(_id).get();
        if(_book==null)
            return buildResponse(HttpStatus.NO_CONTENT, _book);
        return buildResponse(HttpStatus.OK, _book);
    }
}