package com.jmaham.fantasy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PlayerService {
    @Autowired
    PlayerRepository playerRepository;
    List<Player> findAll(){
        List<PlayerEntity> playerEntities= new ArrayList<>();
        List<Player> players = new ArrayList<>();
        playerEntities.addAll(playerRepository.findAll());
        for(PlayerEntity p : playerEntities){
            players.add(new Player(p.getName(), p.getMedian(), p.getHigh(), p.getLow(), p.getTeam(), p.getOpponent(),
                    p.getLine(), p.getSalary(), p.getImpliedOwnership(), p.getProjectedOwnership(), p.getPosition(), p.getLeverageScore()));
        }
        return players;
    }
}
