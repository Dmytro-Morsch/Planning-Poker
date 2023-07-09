package dev.planningpoker.controller;

import dev.planningpoker.repository.PokerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class PokerController {

    @Autowired
    private PokerRepository pokerRepository;

    @PostMapping("/api/room")
    private Map<String, Long> createRoom(@RequestBody String playerName) {
        Long roomId = pokerRepository.createRoom();
        Long playerId = pokerRepository.addPlayer(playerName, roomId);
        return Map.of("roomId", roomId, "playerId", playerId);
    }

    @PostMapping("/api/room/{roomId}/player")
    private ResponseEntity<?> addPlayerToRoom(@PathVariable Long roomId,
                                              @RequestBody String playerName) {
        Boolean shown = pokerRepository.areCardsShown(roomId);
        if (shown == null) {
            return new ResponseEntity<>("Room not found!", HttpStatus.NOT_FOUND);
        }
        Long playerId = pokerRepository.addPlayer(playerName, roomId);
        var votes = getVotes(roomId, shown);
        return ResponseEntity.ok(Map.of("playerId", playerId, "votes", votes));
    }

    @PostMapping("/api/vote/{playerId}")
    private ResponseEntity<?> vote(@PathVariable Long playerId,
                                   @RequestBody String value) {
        if (!pokerRepository.vote(playerId, Integer.parseInt(value))) {
            return new ResponseEntity<>("Player not found!", HttpStatus.NOT_FOUND);
        }

        Long roomId = pokerRepository.getRoomIdByPlayerId(playerId);
        Boolean shown = pokerRepository.areCardsShown(roomId);
        if (shown == null) {
            return new ResponseEntity<>("Room not found!", HttpStatus.NOT_FOUND);
        }

        var votes = getVotes(roomId, shown);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/api/room/{roomId}")
    private ResponseEntity<?> getVotes(@PathVariable Long roomId) {
        Boolean shown = pokerRepository.areCardsShown(roomId);
        if (shown == null) {
            return new ResponseEntity<>("Room not found!", HttpStatus.NOT_FOUND);
        }

        var votes = getVotes(roomId, shown);
        return ResponseEntity.ok(votes);
    }

    @GetMapping("/api/room/{roomId}/players")
    private ResponseEntity<?> getPlayers(@PathVariable Long roomId) {
        if (!pokerRepository.roomExists(roomId)) {
            return new ResponseEntity<>("Room not found!", HttpStatus.NOT_FOUND);
        }
        List<Long> playersId = pokerRepository.getPlayersId(roomId);
        return ResponseEntity.ok(playersId);
    }

    @PostMapping("/api/room/{roomId}/clear")
    private ResponseEntity<?> clearVotes(@PathVariable Long roomId) {
        if (!pokerRepository.roomExists(roomId)) {
            return new ResponseEntity<>("Room not found!", HttpStatus.NOT_FOUND);
        }
        pokerRepository.hideVotes(roomId);
        pokerRepository.clearVotes(roomId);
        var votes = getVotes(roomId, false);
        return ResponseEntity.ok(votes);
    }

    @PostMapping("/api/room/{roomId}/show")
    private ResponseEntity<?> showVotes(@PathVariable Long roomId) {
        if (!pokerRepository.roomExists(roomId)) {
            return new ResponseEntity<>("Room not found!", HttpStatus.NOT_FOUND);
        }
        pokerRepository.showVotes(roomId);
        var votes = getVotes(roomId, true);
        return ResponseEntity.ok(votes);
    }

    private Map<String, Integer> getVotes(Long roomId, boolean shown) {
        Map<String, Integer> votes = pokerRepository.getVotes(roomId);
        if (!shown) {
            for (String player : votes.keySet()) {
                if (votes.get(player) != null) {
                    votes.put(player, -1);
                }
            }
        }
        return votes;
    }
}
