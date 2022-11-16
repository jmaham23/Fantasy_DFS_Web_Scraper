package com.jmaham.fantasy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PlayerController {
    @Autowired
    PlayerService playerService;
    @GetMapping(path = "players")
    List<Player> getPlayers(){
        return playerService.findAll();
    }
}
