package com.technews.user;

import java.util.List;
import java.util.Optional;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.technews.post.Post;
import com.technews.vote.VoteRepository;

@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    UserRepository repository;

    @Autowired
    VoteRepository voteRepository;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        List<User> userList = repository.findAll();
        for (User user : userList) {
            List<Post> postList = user.getPosts();
            for (Post post : postList) {
                post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));
            }
        }
        return userList;
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable Integer id) throws Exception {
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            User existingUser = optionalUser.get();
            List<Post> postList = existingUser.getPosts();
            for (Post post : postList) {
                post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));
            }
            return existingUser;
        } else {
            // Handle the case when the user with the given id is not found
            throw new Exception("User not found with id: " + id);
        }
    }

    @PostMapping("/users")
    public User addUser(@RequestBody User user) {
        // Encrypt password
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        repository.save(user);
        return user;
    }

    @PutMapping("/users/{id}")
    public User updateUser(@PathVariable int id, @RequestBody User user) throws Exception {
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            User updatedUser = optionalUser.get();
            updatedUser.setName(user.getName());
            updatedUser.setEmail(user.getEmail());
            updatedUser.setPassword(user.getPassword());

            repository.save(updatedUser);
            return updatedUser;
        } else {
            throw new Exception("User not found with id: " + id);
        }
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable int id) {
        repository.deleteById(id);
    }

}