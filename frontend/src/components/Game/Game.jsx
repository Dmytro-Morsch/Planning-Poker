import {useEffect, useState} from "react";
import {useParams} from "react-router-dom";
import {useGame} from "../../context/Game.context.jsx";

import {Deck, JoinGame} from "../";
import api from "../../api.js";

import './Game.css';
import VoteTable from "../VoteTable/VoteTable";
import useInterval from "../../useInterval.js";

function Game() {
    const [votes, setVotes] = useState([]);
    const [selectedCard, setSelectedCard] = useState();
    const [error, setError] = useState();
    const [userExist, setUserExist] = useState(false);
    const {player, setPlayer} = useGame();
    const {gameId} = useParams();

    function handleError(e) {
        setError(e.message);
    }

    function vote(playerId, vote) {
        api.vote(playerId, vote).then(setVotes).catch(handleError);
    }

    function resetGame() {
        api.resetGame(gameId).then(setVotes).catch(handleError);
    }

    function showVotes() {
        api.showVotes(gameId).then(setVotes).catch(handleError);
    }

    function handleJoinGame(playerName) {
        api.addPlayerToGame(gameId, playerName).then(result => {
            const playerId = result.playerId;
            setVotes(result.votes);
            const player = {playerId, playerName};
            localStorage.setItem("player", JSON.stringify(player));
            setPlayer(player);
            setUserExist(true);
        }).catch(handleError);
    }

    function handleDeletePlayer(playerId) {
        if (confirm("Sure?")) {
            api.deletePlayer(playerId).then(setVotes).catch(handleError);
        }
    }


    useEffect(() => {
        const oldGameId = localStorage.getItem("gameId");
        if (oldGameId !== gameId || !player) {
            localStorage.removeItem("player");
            setUserExist(false);
        } else {
            setUserExist(true);
        }
        localStorage.setItem("gameId", gameId);
    }, [gameId]);

    useInterval(() => {
        api.getVotes(gameId).then(setVotes).catch(handleError);
    }, 2000);

    return (
        <>
            {error && (
                <div className="error-block">{error}</div>
            )}
            {userExist && (
                <>
                    <Deck selectedCard={selectedCard} onSelect={(value) => {
                        if (value === selectedCard) value = null;
                        setSelectedCard(value);
                        vote(player.playerId, value);
                    }}/>
                    <div className="estimate-block">
                        <div className="buttons">
                            <button className="button reset" onClick={resetGame}>Reset</button>
                            <button className="button show" onClick={showVotes}>Show</button>
                        </div>
                        <VoteTable votes={votes} onDeletePlayer={handleDeletePlayer}/>
                    </div>
                </>
            )}

            {!userExist && (
                <JoinGame votes={votes} onJoin={handleJoinGame}/>
            )}
        </>
    );
}

export default Game;
