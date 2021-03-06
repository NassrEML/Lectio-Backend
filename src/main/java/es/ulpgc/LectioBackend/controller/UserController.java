package es.ulpgc.LectioBackend.controller;

import es.ulpgc.LectioBackend.model.UserList;
import es.ulpgc.LectioBackend.repository.UserListRepository;
import es.ulpgc.LectioBackend.repository.UserRepository;
import es.ulpgc.LectioBackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private UserListRepository listRepository;


    /**
     * URL: [GET] /api/users/{userId_or_email}
     *
     * Example 1: /api/users/33
     * Example 2: /api/users/jose@email.com
     *
     * @return Review
     */
    @RequestMapping(path = "/users/{userId}", method = {RequestMethod.GET})
    public ResponseEntity getUserByIdOrEmail(@PathVariable(value = "userId") String id) {
        try {
            return isNumeric(id) ? getIDResponse(id) : getEmailResponse(id);
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"Couldn't find user with id " + id + "\" }");
        }
    }


    /**
     * URL: [GET] /api/users/
     *
     * @return List
     */
    @RequestMapping(path = "/users", method = {RequestMethod.GET})
    public ResponseEntity getAllUsers() {
        try {
            List<User> users = new ArrayList<>(userRepository.findAll());
            return (users.isEmpty()) ? buildResponse(HttpStatus.NO_CONTENT, null) : buildResponse(HttpStatus.OK, users);
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"There was a problem, couldn't get users\" }");
        }
    }


    /**
     * body: {
     *     "firstName": String,
     *     "lastName": String,
     *     "email": String",
     *     "password": String,
     *     "role": Student or Librarian or Administrator,
     *     "photo": String (optional),
     *     "additional": String (optional)
     * }
     *
     * URL: [POST] /api/users/
     *
     * @return User
     */
    @RequestMapping(path = "/users", method = {RequestMethod.POST})
    public ResponseEntity createUser(@RequestBody User user) {
        try {
            user.setPassword(encodePassword(user.getPassword()));

            User _user = store(user);

            storeUserList(new UserList(_user.getUser_id(), "Pending", ""));
            storeUserList(new UserList(_user.getUser_id(), "Finished", ""));

            return buildResponse(HttpStatus.CREATED, _user);
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"There was a problem, couldn't create user\" }");
        }
    }


    /**
     * URL: [DELETE] /api/users/{userId}
     *
     * @return String
     */
    @RequestMapping(path = "/users/{userId}", method = {RequestMethod.DELETE})
    public ResponseEntity deleteUser(@PathVariable(value = "userId") long id) {
        try {
            userRepository.deleteById(id);
            return buildResponse(HttpStatus.OK, "{ \"message\": \"User deleted successfully\" }");
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"There was a problem, couldn't delete user with id " + id + "\" }");
        }
    }


    /**
     * body: {
     *     "firstName": String,
     *     "lastName": String,
     *     "email": String",
     *     "role": Student or Librarian or Administrator,
     *     "photo": String (optional),
     *     "additional": String (optional)
     * }
     *
     * URL: [PUT] /api/users/{userId}
     *
     * @return User
     */
    @RequestMapping(path = "/users/{userId}", method = {RequestMethod.PUT})
    public ResponseEntity updateUser(@PathVariable(value = "userId") long id, @RequestBody User user) {
        try {
            User _user = userRepository.findById(id).get();
            _user.updateAll(user);
            return buildResponse(HttpStatus.ACCEPTED, userRepository.save(_user));
        } catch (Exception e) {
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"There was a problem, couldn't update user\" }");
        }
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


    private User store(@RequestBody User user) {
        return userRepository
                .save(new User(user.getFirstName(), user.getLastName(), user.getEmail(), user.getPassword(), user.getPhoto(),
                        user.getRole(), user.getAdditional()));
    }


    private String encodePassword(String password) {
        if (password.length() > 2) {
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            password = passwordEncoder.encode(password);
        }
        return password;
    }


    public boolean isNumeric(String strNum) {
        if (strNum == null)
            return false;

        try {
            Long.parseLong(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }


    private ResponseEntity getEmailResponse(@PathVariable("userId") String email) {
        User _user = userRepository.findByEmail(email);
        if (_user.getFirstName().equals(""))
            return buildResponse(HttpStatus.CONFLICT, "{ \"message\": \"Couldn't find user with email " + email + "\" }");

        return buildResponse(HttpStatus.OK, _user);
    }


    private ResponseEntity getIDResponse(@PathVariable("userId") String id) {
        long _id = Long.parseLong(id);
        User _user = userRepository.findById(_id).get();
        return buildResponse(HttpStatus.OK, _user);
    }


    private void storeUserList(UserList userList) {
        listRepository
                .save(new UserList(userList.getUser_id(), userList.getList_name(), userList.getList_description()));
    }
}