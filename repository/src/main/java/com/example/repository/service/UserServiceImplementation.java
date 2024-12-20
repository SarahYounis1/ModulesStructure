package com.example.repository.service;
import com.example.exception.UserAlreadyExistException;
import com.example.repository.TaskRepository;
import com.example.repository.TokenRepository;
import com.example.repository.UserRepository;
import com.example.repository.entity.Tokens;
import com.example.repository.entity.User;
import com.example.security.JWTSecurity.JwtUtil;
import com.example.security.UserDetailsServiceImpl;
import com.example.security.models.AuthenticationRequest;
import com.example.security.models.AuthenticationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;


@Service
public class UserServiceImplementation {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtTokenUtil;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final AuthenticationManager authenticationManager;


    @Autowired
    public UserServiceImplementation(UserDetailsServiceImpl userDetailsService, JwtUtil jwtTokenUtil, TaskRepository taskRepository,
                                     UserRepository theUserRepository, TokenRepository tokenRepository,
                                     AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.taskRepository = taskRepository;
        this.userRepository = theUserRepository;
        this.tokenRepository = tokenRepository;
        this.authenticationManager = authenticationManager;
    }


    //sec version
    public User createNewUser(User newUser)  {
        if(userRepository.findByUsername(newUser.getUsername()).isEmpty()){
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            newUser.setPassword( "{bcrypt}" + encoder.encode(newUser.getPassword()));
            userRepository.save(newUser);
            return newUser;
        }
        else {

            throw new UserAlreadyExistException();
        }
    }

    //ses version
    @Transactional
    public User getUserInfo(){

        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public AuthenticationResponse createAuthenticationToken(AuthenticationRequest authenticationRequest) {
        try {

            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken
                    (authenticationRequest.getUsername(), authenticationRequest.getPassword()));
        } catch (BadCredentialsException e) {

            throw new BadCredentialsException("Incorrect username or password", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtTokenUtil.generateToken(userDetails);
       Tokens token = new Tokens();
        User user=(User)userDetails;
        token.setUser(user);
        token.setJwtToken(jwt);
        tokenRepository.save(token);
        user.addToken(token);
        userRepository.save(user);
        return new AuthenticationResponse(jwt);
    }

    @Transactional
    public User editUser(User editUser) {
        User requestingUser= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        requestingUser.setPassword( "{bcrypt}" + encoder.encode(editUser.getPassword()));
        requestingUser.setName(editUser.getName());
        requestingUser.setEmail(editUser.getEmail());
        requestingUser.setAge(editUser.getAge());
        userRepository.save(requestingUser);
        return requestingUser;
    }

    @Transactional
    public void deleteUser() {
        User requestingUser= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        taskRepository.deleteAllByUser_Id(requestingUser.getId());
        tokenRepository.deleteAllByUserId(requestingUser.getId());
        userRepository.deleteById(requestingUser.getId());
    }

    public void logOut(HttpServletRequest request) {
        final String authorizationHeader = request.getHeader("Authorization");
        String jwt = authorizationHeader.substring(7); //get the jwt and delete by it
        tokenRepository.deleteById(jwt);
    }

    public void logOutAll() {
        //delete all  jwt for this user
        User requestingUser= (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        tokenRepository.deleteAllByUserId(requestingUser.getId());

    }
}