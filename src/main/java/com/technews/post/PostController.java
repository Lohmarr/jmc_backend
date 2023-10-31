package com.technews.post;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.technews.user.User;
import com.technews.user.UserRepository;
import com.technews.vote.Vote;
import com.technews.vote.VoteRepository;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
public class PostController {
    @Autowired
    PostRepository repository;

    @Autowired
    VoteRepository voteRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping("/posts")
    public List<Post> getAllPosts() {
        List<Post> postList = repository.findAll();
        for (Post post : postList) {
            post.setVoteCount(voteRepository.countVotesByPostId(post.getId()));
        }

        return postList;
    }

    @GetMapping("/posts/{id}")
    public Post getPost(@PathVariable Integer id) throws Exception {
        Optional<Post> optionalPost = repository.findById(id);
        if (optionalPost.isPresent()) {
            Post existingPost = optionalPost.get();
            existingPost.setVoteCount(voteRepository.countVotesByPostId(existingPost.getId()));

            return existingPost;
        } else {
            throw new Exception("Post not found with id: " + id);
        }
    }

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Post addPost(@RequestBody Post post) {
        repository.save(post);
        return post;
    }

    @PutMapping("/posts/{id}")
    public Post updatePost(@PathVariable Integer id, @RequestBody Post post) throws Exception {
        Optional<Post> optionalPost = repository.findById(id);
        if (optionalPost.isPresent()) {
            Post updatedPost = optionalPost.get();
            updatedPost.setTitle(post.getTitle());
            updatedPost.setPostUrl(post.getPostUrl());
            repository.save(updatedPost);

            return updatedPost;
        } else {
            throw new Exception("Post not found with id " + id);
        }
    }

    @PutMapping("/posts/upvote")
    public String addVote(@RequestBody Vote vote, HttpServletRequest request) throws Exception {
        String value = "";

        if (request.getSession(false) != null) {
            User sessionUser = (User) request.getSession().getAttribute("SESSION_USER");
            vote.setUserId(sessionUser.getId());
            voteRepository.save(vote);

            Optional<Post> optionalPost = repository.findById(vote.getPostId());
            if (optionalPost.isPresent()) {
                Post updatedPost = optionalPost.get();
                updatedPost.setVoteCount(voteRepository.countVotesByPostId(vote.getPostId()));
                repository.save(updatedPost);

                value = "";
            } else {
                throw new Exception("Could not successfully add vote");
            }
        } else {
            value = "login";
        }
        return value;
    }

    @DeleteMapping("/posts/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePost(@PathVariable Integer id) {
        repository.deleteById(id);
    }
}