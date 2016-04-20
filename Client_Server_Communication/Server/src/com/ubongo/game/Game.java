// Copyright (c) 2016 Albert Hambardzumyan
// All rights reserved.
// This software is released under the BSD license.
package com.ubongo.game;

/**
 * @author Albert Hambardzumyan
 */
import com.ubongo.player.Player;

import java.io.PrintWriter;
import java.util.ArrayList;

public class Game {
    private ArrayList<Player> players;
    private int difficulty;
    private int board;
    private boolean status;


    public Game() {
        this.players = new ArrayList<Player>();
        this.difficulty = 0; // by default easy difficult
        this.status = false;
    }

    public ArrayList<Player> getPlayers() {
        return players;
    }

    public int getBoard() {
        return board;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean getStatus() {
        return status;
    }

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public void setBoard(int board) {
        this.board = board;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


    public void addPlayer(String name, boolean ownerStatus, PrintWriter out) {

        int count=0;//Uses this to make sure that players can't have the same name
        for(Player p:players){
            if(p.getName().length()>=name.length() && p.getName().substring(0, name.length()).equals(name)) {
                count++;

            }
        }
        if(count==0) {
            players.add(new Player(name, ownerStatus, out));

        }
        else{
            players.add(new Player(name+""+count, ownerStatus, out));

        }

    }
}