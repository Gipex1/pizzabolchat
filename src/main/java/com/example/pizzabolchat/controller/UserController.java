package com.example.pizzabolchat.controller;

import com.example.pizzabolchat.dto.LogUser;
import com.example.pizzabolchat.dto.RegUser;
import com.example.pizzabolchat.model.User;
import com.example.pizzabolchat.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public Map<String, Long> register(@RequestBody RegUser req) {
        User u = userService.register(req);
        return Map.of("userId", u.getId());
    }

    @PostMapping("/login")
    public LogUser login(@RequestBody RegUser req){
        String token = userService.login(req.getEmail(), req.getPassword());
        return new LogUser(token, req.getEmail());
    }

    // GET: Получить всех юзеров
    @GetMapping
    public java.util.List<User> getAll() {
        return userService.getAllUsers();
    }

    // GET: Получить одного юзера по ID
    @GetMapping("/{id}")
    public User getOne(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // PUT: Обновить юзера
    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @RequestParam String email) {
        return userService.updateUser(id, email);
    }

    // DELETE: Удалить юзера
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return "Пользователь с ID " + id + " удален";
    }
}
