// Copyright (c) 2016 Albert Hambardzumyan
// All rights reserved.
// This software is released under the BSD license.
package com.ubongo.gameHolder;

/**
 * @author Albert Hambardzumyan
 */
import com.google.gson.Gson;
import com.ubongo.dataTransfer.ResponsePackage;
import com.ubongo.game.Game;
import com.ubongo.player.Player;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class GameHolder {
    private static GameHolder ourInstance = new GameHolder();
    private int uniqueId = 1001;
    private Gson gson;
    private HashMap<String, Game> games = new HashMap<String, Game>();


    public static GameHolder getInstance() {
        return ourInstance;
    }

    private GameHolder() {
        gson = new Gson();
    }

    /**
     *Create new game and generate new unused pin and return to the requester
     * @param name name of the player
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void startLobby(String name, PrintWriter out, int callId) {
        /**do not add into the vector
         just generate pin and return that
         */
        System.out.println("\nCalled method: 'Start Lobby'.  Arguments: Name " + name);
        String pin = generateUniqueId();
        Game game = new Game();
        games.put(pin, game);
        ResponsePackage responsePackage = new ResponsePackage(callId, 200, pin, null);
        String json = gson.toJson(responsePackage);
        out.println(json);
        out.flush();
        System.out.println("Done ...");
    }

    /**
     *Method called to generate a returnPackage containing the difficulty for a game
     * @param name name of the player
     * @param pin pin for the game
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void getDifficulty(String name, String pin, PrintWriter out, int callId){
        System.out.println("\nCalled method: 'getDifficulty'.  Arguments: Name " + name +"Pin " + pin);
        ResponsePackage responsePackage=new ResponsePackage(callId, 200,games.get(pin).getDifficulty()+"",null);
        String json =gson.toJson(responsePackage);
        out.println(json);
        out.flush();
        System.out.println("Done...");
    }

    /**
     *Method that adds a player to the playerlist for a specified game and returns the playerlist for the game
     * @param name name of the player
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void joinPlayer(String name, String pin, boolean ownerStatus, PrintWriter out, int callId) {
        /**keep the name in ArrayList
         notify everyone already in the game by returning ArrayList of names
         */
        System.out.println("\nCalled method: 'Join Player'. Arguments: Name " + name + ", Pin " + pin + ", Owner Status " + ownerStatus);
        Game game = games.get(pin);
        if (game == null) {
            gameNullHandler(callId, out);
        }
        else {
            gameExistLogger(game);

            System.out.println("\nAdding a new player. Name: " + name + ". Owner Status: " + ownerStatus);
            game.addPlayer(name, ownerStatus, out);

            ArrayList<String> playersName = generatePlayersName(game);
            notifyEveryone(callId, game, playersName.toString());

        }
        System.out.println("Done ...");
    }


    /**
     *Sends the winner of a game to all the players in the game and then ends the game
     * @param name name of the player
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void finishGame(String name, String pin, PrintWriter out, int callId) {
        /**
         * send the winner name to everyone
         * after the call check the if empty delete the game. // changes: after just delete the game
         */
        System.out.println("\nCalled method: 'Finish Game. Arguments: Name " + name + ", Pin " + pin);
        Game game = games.get(pin);
        if (game == null) {
            gameNullHandler(callId, out);
        }
        else if (!game.getStatus()){
            gameNOTStartedHandler(callId, out);
        }
        else {
            gameExistLogger(game);

            notifyEveryone(callId, game, name);

            System.out.println("Deleting the Game ...");
            games.remove(pin);
        }
        System.out.println("Done ...");
    }

    /**
     *Takes a player out of the game, deletes the game if the game is empty, notifies everyone that the player is out of the game
     * @param name name of the player
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void leaveGame(String name, String pin, PrintWriter out, int callId) {
        /**
         * take out this guy out of the Array List
         * after the call check the players vector, if empty delete the game
         * notify all - send list of players name
         */
        System.out.println("\nCalled method: 'Leave Game'. Arguments: Name " + name + ", Pin " + pin);
        Game game = games.get(pin);
        if (game == null) {
            gameNullHandler(callId, out);
        } else {
            gameExistLogger(game);

            // check player exist or not before delete. NO error but players are getting notification eith no difference
            System.out.println("\nDeleting the player .. " + name);
            for (Player player : game.getPlayers()) {
                if (player.getName().equals(name)) {
                    game.getPlayers().remove(player);
                    break;
                }
            }
            System.out.println("Checking and deleting the Game if there is NO player ...");
            if (game.getPlayers().size() == 0){
                games.remove(pin);
                System.out.println("Game is deleted ...");
            }
            else{
                System.out.println("Game still has players ...");
                ArrayList<String> playersName = generatePlayersName(game);
                notifyEveryone(callId, game, playersName.toString());
            }
        }
        System.out.println("Done ...");
    }

    /**
     *Starts a game and notifies all the players waiting for it to start by sending a board ID based on the difficulty for the game
     * @param name name of the player
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void startGame(String name, String pin, PrintWriter out, int callId) {
        /**
         * only owner. check it here
         * status set started
         * send all the players that started
         * generate random number within that difficulty range: board id
         */
        System.out.println("\nCalled method: 'Start Game'. Arguments: Name " + name + " Pin " + pin);
        Game game = games.get(pin);
        if (game == null) {
            gameNullHandler(callId, out);
        }
        else if (game.getStatus()){
            gameStartedHandler(callId, out, "Maybe you are faster then me :)");
        }
        else {
            gameExistLogger(game);

            System.out.println("\nChecking for player permissions ...");
            boolean flag = false;
            System.out.println("Looking for player in the system ...");
            for (Player player : game.getPlayers()) {
                if (player.getName().equals(name)) {
                    System.out.println("Found the player ...");
                    flag = true;
                    if (player.getOwnerStatus()) {
                        System.out.println("Player permissions allowed to start the game ...");

                        System.out.println("Starting the game ...");
                        game.setStatus(true);

                        //Send boardID to clients:
                        String boardID="";
                        Random rand=new Random();
                        int randInt = rand.nextInt(3);
                        if(game.getDifficulty()==0){
                            boardID="0" + randInt;//+easy[rand.nextInt(easy.length-1)];
                        }
                        else if(game.getDifficulty()==1){
                            boardID="1" + randInt; //+medium[rand.nextInt(medium.length-1)];
                        }
                        else if(game.getDifficulty()==2){
                            boardID="2" + randInt; //+hard[rand.nextInt(hard.length-1)];
                        }

                        notifyEveryone(callId, game, boardID);
                        //##########
                    } else {
                        noPermissionHandler(callId, out);
                    }
                    break;
                }
            }
            if (!flag) {
                noPlayerHandler(callId, out);
            }
        }
        System.out.println("Done ...");
    }

    /**
     *Removes a player from a game, notify all the players that the player left by sending the updated playerlist for the game
     * @param name name of the player
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void removePlayer(String name, String pin, PrintWriter out, int callId) {
        /**
         * this method can be called only before starting the game
         * take out this guy out of the vector
         * after the call check the players vector, if empty delete the game
         * if not empty then notify all the players send ArrayList<String>
         *     if owner removes, notify all the players (some key value), remove the game
         */
        System.out.println("\nCalled method: 'Remove Player'. Arguments: Name " + name + ", Pin " + pin);
        Game game = games.get(pin);
        if (game == null) {
            gameNullHandler(callId, out);
        }
        else {
            gameExistLogger(game);

            System.out.println("\nChecking the owner status ...  ");
            boolean flag = false;
            System.out.println("Looking for player in the system ...");
            for (Player player : game.getPlayers()) {
                if (player.getName().equals(name)) {
                    System.out.println("Found the player ...");
                    flag = true;
                    System.out.println("Checking player status ...");
                    if (player.getOwnerStatus()) {
                        System.out.println("Owner wants to leave the game before starting it ...");

                        System.out.println("Deleting the player .. " + name);
                        game.getPlayers().remove(player);

                        notifyEveryone(callId, game, "Owner left");

                        System.out.println("Deleting the Game ...");
                        games.remove(pin);
                    }
                    else {
                        System.out.println("Player wants to leave before game started is not the owner ...");
                        System.out.println("\nDeleting the player .. " + name);
                        game.getPlayers().remove(player);

                        // owner should be, but anyway check
                        System.out.println("Checking and deleting the Game if there is NO player ...");
                        if (game.getPlayers().size() == 0){
                            games.remove(pin);
                            System.out.println("Game is deleted ...");
                        }
                        else{
                            System.out.println("Game still has players ...");
                            ArrayList<String> playersName = generatePlayersName(game);
                            notifyEveryone(callId, game, playersName.toString());
                        }
                    }
                    break;
                }
            }
            if (!flag) {
                noPlayerHandler(callId, out);
            }
        }
        System.out.println("Done ...");
    }


    /**
     * Sets the difficulty for the game and notify all the playyers in the game by sending the uodated difficulty value.
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    public void setDifficulty(String difficult, String pin, PrintWriter out, int callId) {
        /** diff: int range 0-2. pin not sure.
         no need to check owner or not. limited from ui
         Set difficulty, and send all clients **/
        System.out.println("\nCalled method: 'Set Difficulty'. Arguments: Difficulty " + difficult + ", Pin " + pin);
        Game game = games.get(pin);
        if (game == null) {
            gameNullHandler(callId, out);
        }
        else if (game.getStatus()){
            gameStartedHandler(callId, out, "Cannot change the difficulty");
        }
        else {
            gameExistLogger(game);

            System.out.println("\nSetting the difficulty .. " + difficult);
            game.setDifficulty(Integer.parseInt(difficult));

            notifyEveryone(callId, game, difficult);
        }
        System.out.println("Done ...");
    }

    /**
     * Generates unique ID for a game
     * @return The unique pin
     */
    private String generateUniqueId() {
        //String uniqueID = UUID.randomUUID().toString();
        return Integer.toString(uniqueId++);
    }

    /**
     *Mathos handling the case if a game requested does not exist
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    private void gameNullHandler(int callId, PrintWriter out){
        System.out.println("Could NOT find such game. Existing games at this moment");
        for (Map.Entry m : games.entrySet()) {
            System.out.println(m.getKey() + " " + m.getValue().toString());
        }
        System.out.println("Sending back Response Status 404 : Could NOT find such game. Please check the PIN ...");
        ResponsePackage responsePackage = new ResponsePackage(callId, 404, null, "Could NOT find such game. Please check the PIN!");
        String json = gson.toJson(responsePackage);
        out.println(json);
        out.flush();
    }

    /**
     *Logs that the game was changed
     */
    private void gameExistLogger(Game game){
        System.out.print("Game exists. Current players are ...  ");
        for (Player player : game.getPlayers()) {
            System.out.print(player.getName() + ".. ");
        }
    }

    /**
     *Method handling the case if an already started game is requested to start again
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    private void gameStartedHandler(int callId, PrintWriter out, String subString){
        System.out.println("Game with specified PIN already started ...");
        System.out.println("Sending back Response Status 404 : Game with specified PIN already started. " + subString + " ...");
        ResponsePackage responsePackage = new ResponsePackage(callId, 404, null, "Game with specified PIN already started. " + subString + "!");
        String json = gson.toJson(responsePackage);
        out.println(json);
        out.flush();
    }

    /**
     *Method handling the case if someone try to end a game that has not been started yet
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    private void gameNOTStartedHandler(int callId, PrintWriter out){
        System.out.println("Game with specified PIN is not even started ...");
        System.out.println("Sending back Response Status 404 : Game with specified PIN is not even started ...");
        ResponsePackage responsePackage = new ResponsePackage(callId, 404, null, "Game with specified PIN is not even started");
        String json = gson.toJson(responsePackage);
        out.println(json);
        out.flush();
    }

    /**
     *Method for notififying everyone in a game about something
     * @param callId identificator for the type of request/response
     * @param game game that holds all the players that should be notified
     * @param responseContent The content to notify everyone about
     */
    private void notifyEveryone(int callId, Game game, String responseContent){
        System.out.println("Notifying everyone");
        ResponsePackage responsePackage = new ResponsePackage(callId, 200, responseContent, null);
        String json = gson.toJson(responsePackage);
        for (Player player : game.getPlayers()) {
            player.getOut().println(json);
            player.getOut().flush();
        }
    }

    /**
     *Generates a playerlist from a game
     * @param game The game whiich the player should be added to
     * @return playerlist with players
     */
    private ArrayList<String> generatePlayersName(Game game){
        System.out.println("Generating Players Name Response Content ...");
        ArrayList<String> playersName = new ArrayList<String>();
        for (Player player : game.getPlayers()) {
            playersName.add(player.getName());
        }
        return playersName;
    }

    /**
     *Method handling the case if a player was not found in a game
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    private void noPlayerHandler(int callId, PrintWriter out){
        System.out.println("Could not find such player in specified game ...");
        System.out.println("Sending back Response Status 404 : Could NOT find such player in specified game ...");
        ResponsePackage responsePackage = new ResponsePackage(callId, 404, null, "Could NOT find such player in specified game!");
        String json = gson.toJson(responsePackage);
        out.println(json);
    }

    /**
     *Mehod handling the case when a player tries to change settings that only an owner can change
     * @param out out-stream used to send the information over the socket to the requester of the information
     * @param callId identificator for the type of request/response
     */
    private void noPermissionHandler(int callId, PrintWriter out){
        System.out.println("Player does NOT have such permissions ...");
        System.out.println("Sending back Response Status 404 : Player does NOT have such permissions ...");
        ResponsePackage responsePackage = new ResponsePackage(callId, 404, null, "Player does NOT have such permissions!");
        String json = gson.toJson(responsePackage);
        out.println(json);
    }
}