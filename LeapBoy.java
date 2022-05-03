/*
 * Made by Kelly Hamilton
 * Last Modifide on 14/02/2018
 * Game name "Leap Boy"
 * Version 1.0
 * The game is a 2D platformer
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Label;
import javafx.stage.Stage;

//import javafx.scene.*;

public class LeapBoy extends Application {

    // Keyboard input
    private HashMap<KeyCode, Boolean> keys = new HashMap<KeyCode, Boolean>();
    
    // List of the platforms
    private ArrayList<Node> platforms = new ArrayList<Node>();
    // List of the coins
    private ArrayList<Node> coinList = new ArrayList<Node>();
    // List of the enemies
    private ArrayList<Node> enemyList = new ArrayList<Node>();
    // List of the enemy boss
    private ArrayList<Node> enemyBList = new ArrayList<Node>();
    // List of the warps
    private ArrayList<Node> warps = new ArrayList<Node>();
    // List of the jump pannels
    private ArrayList<Node> jumpPannels = new ArrayList<Node>();
    // List of the walls
    private ArrayList<Node> walls = new ArrayList<Node>();
    
    // Main root
    private Pane appRoot = new Pane();
    // Players,coins,ect
    private Pane gameRoot = new Pane();
    private Pane uiRoot = new Pane();
    
    private Node player;
    private Point2D playerVelocity = new Point2D(0, 0);
    private boolean canJump = true;
    
    private int levelWidth;
    
    private int coinWallet = 0;
    private boolean running = true;
    int levelNumber = 1;
    int bossHealth = 1;
    int lives = 5;
    int coinsCollected = 0;
    
    private Label coinCur = new Label("\n coin wallet: "+ coinWallet);
    private Label livesCur = new Label("\n \n lives: "+ lives);
    private Label totCoinColl = new Label(" total coins collected: "+ coinsCollected);
    
    
    private void initContent(){
        
        Rectangle bg = new Rectangle(1280, 720);
        bg.setFill(Color.GREY);
        
        levelWidth = Level.LEVEL1[0].length() * 60;
        
        buildL1();
        player = createEntity(0, 500, 40, 40, Color.BLUE);
        
        player.translateXProperty().addListener((obs, old, newValue) -> {
            int offset = newValue.intValue();
            
            if(offset > 640 && offset < levelWidth - 640){
                gameRoot.setLayoutX(-(offset - 640));
            }
        });
        
        appRoot.getChildren().addAll(bg, gameRoot, uiRoot);
        
    }
    
    private void update(){

        if(isPressed(KeyCode.UP) && player.getTranslateY() >= 5){
            jumpPlayer();
            jumpUpY();
        }
        if(isPressed(KeyCode.LEFT) && player.getTranslateX() >= 5){
            movePlayerX(-5);
            enemyDamage(-5);
            enemyBDamage(-5);
            jumpUpX();
        }
        if(isPressed(KeyCode.RIGHT) && player.getTranslateX() +40 <= levelWidth -5){
            movePlayerX(5);
            enemyDamage(5);
            enemyBDamage(5);
            jumpUpX();
        }
        if(playerVelocity.getY() < 10){
            playerVelocity = playerVelocity.add(0, 1);
        }
        movePlayerY((int)playerVelocity.getY());
        
    }
    
    private void coinUpdate(){
        
        for(Node coins : coinList){
            if(player.getBoundsInParent().intersects(coins.getBoundsInParent())){
                coins.getProperties().put("alive", false);
            }
        }
        for(Iterator<Node> it = coinList.iterator(); it.hasNext();){
            Node coins = it.next();
            if(!(Boolean)coins.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(coins);
                coinWallet++;
                coinsCollected++;
                if(coinsCollected % 10 == 0){
                    lives++;
                }
            }
        }
    }
    
    private void enemyDamage(int value){
        
        for(Node enemy : enemyList){
            if(player.getBoundsInParent().intersects(enemy.getBoundsInParent())){
                if(value > 0){
                    player.setTranslateX(player.getTranslateX()-25);
                }
                else{
                    player.setTranslateX(player.getTranslateX()+25);
                }
                if(coinWallet>0){
                    coinWallet-=3;
                    if(coinWallet<0){
                        coinWallet = 0;
                    }
                }
                else{
                    coinWallet = -1;
                    deathUpdate();
                    return;
                }
            }
        }
    }
    private void enemyBDamage(int value){
        
        for(Node enemyB : enemyBList){
            if(player.getBoundsInParent().intersects(enemyB.getBoundsInParent())){
                if(value > 0){
                    player.setTranslateX(player.getTranslateX()-40);
                }
                if(coinWallet>0){
                    coinWallet-=3;
                    if(coinWallet<0){
                        coinWallet = 0;
                    }
                }
                else{
                    coinWallet = -1;
                    deathUpdate();
                }
            }
        }
    }
    
    private void enemyKill(){
        
        for(Node enemy : enemyList){
            if(player.getBoundsInParent().intersects(enemy.getBoundsInParent())){
                enemy.getProperties().put("alive", false);
            }
        }
        for(Iterator<Node> it = enemyList.iterator(); it.hasNext();){
            Node enemy = it.next();
            if(!(Boolean)enemy.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(enemy);
                
                playerVelocity = playerVelocity.add(0, -20);
                canJump = false;
            }
        }
    }
    
    private void enemyBKill(){
        
        for(Node enemyB : enemyBList){
            if(player.getBoundsInParent().intersects(enemyB.getBoundsInParent())){
                playerVelocity = playerVelocity.add(0, -5);
                canJump = false;
                for(int i=0; i<200; i++){
                    player.setTranslateX(player.getTranslateX()-1);
                }
                bossHealth--;
                if(bossHealth == 0){
                    for(Node enemyB2 : enemyBList){
                        enemyB2.getProperties().put("alive", false);
                    }
                    for(Iterator<Node> it = enemyBList.iterator(); it.hasNext();){
                        Node enemyB2 = it.next();
                        if(!(Boolean)enemyB2.getProperties().get("alive")){
                            it.remove();
                            gameRoot.getChildren().remove(enemyB2);
                        }
                    }
                    for(Iterator<Node> it = enemyList.iterator(); it.hasNext();){
                        Node enemy = it.next();
                        if((Boolean)enemy.getProperties().get("alive")){
                            it.remove();
                            gameRoot.getChildren().remove(enemy);
                        }
                    }
                    return;
                }
            }
        }
    }
    
    private void jumpUpY(){
        
        for(Node jumpPan : jumpPannels){
            if(player.getBoundsInParent().intersects(jumpPan.getBoundsInParent())){
                jumpPan.getProperties().put("alive", false);
                
            }
        }
        for(Iterator<Node> it = jumpPannels.iterator(); it.hasNext();){
            Node jumpPan = it.next();
            if(!(Boolean)jumpPan.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(jumpPan);
                
                playerVelocity = playerVelocity.add(0, -10);
                canJump = false;
                
            }
        }
    }
    
    private void jumpUpX(){
        
        for(Node jumpPan : jumpPannels){
            if(player.getBoundsInParent().intersects(jumpPan.getBoundsInParent())){
                jumpPan.getProperties().put("alive", false);
            }
        }
        for(Iterator<Node> it = jumpPannels.iterator(); it.hasNext();){
            Node jumpPan = it.next();
            if(!(Boolean)jumpPan.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(jumpPan);
                
                playerVelocity = playerVelocity.add(0, -40);
                canJump = false;
                
            }
        }
    }
    
    private void nextLevel(){
        
        for(Node warp : warps){
            if(player.getBoundsInParent().intersects(warp.getBoundsInParent())){
                warp.getProperties().put("alive", false);
                
                for(Iterator<Node> it = warps.iterator(); it.hasNext();){
                    Node warpy = it.next();
                    if(!(Boolean)warp.getProperties().get("alive")){
                        it.remove();
                        gameRoot.getChildren().remove(warpy);
                    }
                }
                
                for(Node enemy : enemyList){
                    enemy.getProperties().put("alive", false);
                }
                for(Iterator<Node> it = enemyList.iterator(); it.hasNext();){
                    Node enemy = it.next();
                    if(!(Boolean)enemy.getProperties().get("alive")){
                        it.remove();
                        gameRoot.getChildren().remove(enemy);
                    }
                }
                
                for(Node coins : coinList){
                    coins.getProperties().put("alive", false);
                }
                for(Iterator<Node> it = coinList.iterator(); it.hasNext();){
                    Node coins = it.next();
                    if(!(Boolean)coins.getProperties().get("alive")){
                        it.remove();
                        gameRoot.getChildren().remove(coins);
                        
                    }
                }
                
                for(Node platform : platforms){
                    platform.getProperties().put("alive", false);
                }
                for(Iterator<Node> it = platforms.iterator(); it.hasNext();){
                    Node platform = it.next();
                    if(!(Boolean)platform.getProperties().get("alive")){
                        it.remove();
                        gameRoot.getChildren().remove(platform);
                        
                    }
                }
                
                for(Node enemyB : enemyBList){
                    enemyB.getProperties().put("alive", false);
                }
                for(Iterator<Node> it = enemyBList.iterator(); it.hasNext();){
                    Node enemyB = it.next();
                    if(!(Boolean)enemyB.getProperties().get("alive")){
                        it.remove();
                        gameRoot.getChildren().remove(enemyB);
                        
                    }
                }
                levelNumber++;
                reload();
                return;
            }
        }
        
    }
    
    private void deathUpdate(){
        if(player.getTranslateY() > 800){
            lives--;
            reload();
        }
        if(coinWallet<0){
            lives--;
            reload();
        }
    }
    
    private void reload(){
        
        if(lives<0){
            levelNumber = 1;
            lives = 5;
            coinsCollected = 0;
        }
        
        player.setTranslateX(0);
        player.setTranslateY(500);
        gameRoot.setLayoutX(0);
        
        coinWallet = 0;
        
        for(Node enemy : enemyList){
            enemy.getProperties().put("alive", false);
        }
        for(Iterator<Node> it = enemyList.iterator(); it.hasNext();){
            Node enemy = it.next();
            if(!(Boolean)enemy.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(enemy);
            }
        }
        for(Node coins : coinList){
            coins.getProperties().put("alive", false);
        }
       for(Iterator<Node> it = coinList.iterator(); it.hasNext();){
            Node coins = it.next();
            if(!(Boolean)coins.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(coins);
            }
        }
        
        for(Node jumpPan : jumpPannels){
            jumpPan.getProperties().put("alive", false);
        }
        for(Iterator<Node> it = jumpPannels.iterator(); it.hasNext();){
            Node jumpPan = it.next();
            if(!(Boolean)jumpPan.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(jumpPan);
            }
        }
        
        for(Node enemyB : enemyBList){
            enemyB.getProperties().put("alive", false);
        }
        for(Iterator<Node> it = enemyBList.iterator(); it.hasNext();){
            Node enemyB = it.next();
            if(!(Boolean)enemyB.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(enemyB);
            }
        }
        
        for(Node platform : platforms){
            platform.getProperties().put("alive", false);
        }
        for(Iterator<Node> it = platforms.iterator(); it.hasNext();){
            Node platform = it.next();
            if(!(Boolean)platform.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(platform);
            }
        }
        
        for(Node wall : walls){
            wall.getProperties().put("alive", false);
        }
        for(Iterator<Node> it = walls.iterator(); it.hasNext();){
            Node wall = it.next();
            if(!(Boolean)wall.getProperties().get("alive")){
                it.remove();
                gameRoot.getChildren().remove(wall);
            }
        }
        if(levelNumber == 1){
            buildL1();
        }
        else if(levelNumber == 2){
            buildL2();
        }
        else if(levelNumber == 3){
            buildL3();
        }
        else if(levelNumber ==4){
            buildL4();
        }
        else if(levelNumber ==5){
            buildL5();
        }
        else if(levelNumber ==6){
            buildL6();
        }
    }
    
    private void buildL1(){
        
        levelWidth = Level.LEVEL1[0].length() * 60;
        
        for(int i=0; i<Level.LEVEL1.length; i++){
            String line = Level.LEVEL1[i];
            for(int j=0; j<line.length(); j++){
                switch(line.charAt(j)){
                    case '0':
                        break;
                        
                    case '1':
                        Node platform = createEntity(j*60, i*60, 60, 60, Color.BROWN);
                        platforms.add(platform);
                        break;
                        
                    case '2':
                        Node enemy = createEntity(j*60+20, i*60+20, 40, 40, Color.RED);
                        enemyList.add(enemy);
                        break;
                        
                    case '3':
                        Node coins = createEntity(j*60, i*60, 40, 40, Color.GOLD);
                        coinList.add(coins);
                        break;
                        
                    case '4':
                        Node warp = createEntity(j*60, i*60, 50, 50, Color.GREEN);
                        warps.add(warp);
                        break;
                        
                    case '5':
                        Node jumpPan = createEntity(j*60, i*60, 50, 50, Color.DARKGREY);
                        jumpPannels.add(jumpPan);
                        break;
                        
                    case '6':
                        Node enemyB = createEntity(j*60, i*60-60, 120, 120, Color.RED);
                        enemyBList.add(enemyB);
                        break;
                    
                }
            }
        }
    }

    private void buildL2(){

        levelWidth = Level.LEVEL2[0].length() * 60;
        
        for(int i=0; i<Level.LEVEL2.length; i++){
            String line = Level.LEVEL2[i];
            for(int j=0; j<line.length(); j++){
                switch(line.charAt(j)){
                    case '0':
                    break;

                    case '1':
                    Node platform = createEntity(j*60, i*60, 60, 60, Color.BROWN);
                    platforms.add(platform);
                    break;

                    case '2':
                    Node enemy = createEntity(j*60+20, i*60+20, 40, 40, Color.RED);
                    enemyList.add(enemy);
                    break;

                    case '3':
                    Node coins = createEntity(j*60, i*60, 40, 40, Color.GOLD);
                    coinList.add(coins);
                    break;

                    case '4':
                    Node warp = createEntity(j*60, i*60, 50, 50, Color.GREEN);
                    warps.add(warp);
                    break;
                }
            }
        }
    }
    
    private void buildL3(){
        
        levelWidth = Level.LEVEL3[0].length() * 60;
        bossHealth = 3;
        
        for(int i=0; i<Level.LEVEL3.length; i++){
            String line = Level.LEVEL3[i];
            for(int j=0; j<line.length(); j++){
                switch(line.charAt(j)){
                    case '0':
                        break;
                        
                    case '1':
                        Node platform = createEntity(j*60, i*60, 60, 60, Color.BROWN);
                        platforms.add(platform);
                        break;
                        
                    case '2':
                        Node enemy = createEntity(j*60+20, i*60+20, 40, 40, Color.RED);
                        enemyList.add(enemy);
                        break;
                        
                    case '3':
                        Node coins = createEntity(j*60, i*60, 40, 40, Color.GOLD);
                        coinList.add(coins);
                        break;
                        
                    case '4':
                        Node warp = createEntity(j*60, i*60, 50, 50, Color.GREEN);
                        warps.add(warp);
                        break;
                        
                    case '5':
                        Node jumpPan = createEntity(j*60, i*60, 50, 50, Color.DARKGREY);
                        jumpPannels.add(jumpPan);
                        break;
                        
                    case '6':
                        Node enemyB = createEntity(j*60, i*60-60, 120, 120, Color.RED);
                        enemyBList.add(enemyB);
                        break;
                }
            }
        }
    }
    
    private void buildL4(){
        
        levelWidth = Level.LEVEL4[0].length() * 60;
        bossHealth = 3;
        
        for(int i=0; i<Level.LEVEL4.length; i++){
            String line = Level.LEVEL4[i];
            for(int j=0; j<line.length(); j++){
                switch(line.charAt(j)){
                    case '0':
                        break;
                        
                    case '1':
                        Node platform = createEntity(j*60, i*60, 60, 60, Color.BROWN);
                        platforms.add(platform);
                        break;
                        
                    case '2':
                        Node enemy = createEntity(j*60+20, i*60+20, 40, 40, Color.RED);
                        enemyList.add(enemy);
                        break;
                        
                    case '3':
                        Node coins = createEntity(j*60, i*60, 40, 40, Color.GOLD);
                        coinList.add(coins);
                        break;
                        
                    case '4':
                        Node warp = createEntity(j*60, i*60, 50, 50, Color.GREEN);
                        warps.add(warp);
                        break;
                        
                    case '5':
                        Node jumpPan = createEntity(j*60, i*60, 50, 50, Color.DARKGREY);
                        jumpPannels.add(jumpPan);
                        break;
                        
                    case '6':
                        Node enemyB = createEntity(j*60, i*60-60, 120, 120, Color.RED);
                        enemyBList.add(enemyB);
                        break;
                }
            }
        }
    }

    private void buildL5(){
        
        levelWidth = Level.LEVEL5[0].length() * 60;
        bossHealth = 3;
        
        for(int i=0; i<Level.LEVEL5.length; i++){
            String line = Level.LEVEL5[i];
            for(int j=0; j<line.length(); j++){
                switch(line.charAt(j)){
                    case '0':
                        break;
                        
                    case '1':
                        Node platform = createEntity(j*60, i*60, 60, 60, Color.BROWN);
                        platforms.add(platform);
                        break;
                        
                    case '2':
                        Node enemy = createEntity(j*60+20, i*60+20, 40, 40, Color.RED);
                        enemyList.add(enemy);
                        break;
                        
                    case '3':
                        Node coins = createEntity(j*60, i*60, 40, 40, Color.GOLD);
                        coinList.add(coins);
                        break;
                        
                    case '4':
                        Node warp = createEntity(j*60, i*60, 50, 50, Color.GREEN);
                        warps.add(warp);
                        break;
                        
                    case '5':
                        Node jumpPan = createEntity(j*60, i*60, 50, 50, Color.DARKGREY);
                        jumpPannels.add(jumpPan);
                        break;
                        
                    case '6':
                        Node enemyB = createEntity(j*60, i*60-60, 120, 120, Color.RED);
                        enemyBList.add(enemyB);
                        break;
                }
            }
        }
    }
    
    private void buildL6(){
        // no level 6
        return;
    }
    
    
    private void movePlayerX(int value){
        
        boolean movingRight = value > 0;
        
        for(int i=0; i < Math.abs(value); i++){
            for(Node platform : platforms){
                if(player.getBoundsInParent().intersects(platform.getBoundsInParent())){
                    if(movingRight){
                        if(player.getTranslateX() +40 == platform.getTranslateX()){
                            return;
                        }
                    }
                    else{
                        if(player.getTranslateX() == platform.getTranslateX()+60){
                            return;
                        }
                    }
                }
            }
            player.setTranslateX(player.getTranslateX() + (movingRight ? 1 : -1));
        }
        
    }
    
    private void movePlayerY(int value){
        
        boolean movingDown = value > 0;
        
        for(int i=0; i < Math.abs(value); i++){
            for(Node platform : platforms){
                if(player.getBoundsInParent().intersects(platform.getBoundsInParent())){
                    if(movingDown){
                        if(player.getTranslateY() +40 == platform.getTranslateY()){
                            player.setTranslateY(player.getTranslateY()-1);
                            canJump = true;
                            return;
                        }
                    }
                    else{
                        if(player.getTranslateY() == platform.getTranslateY()+60){
                            return;
                        }
                    }
                }
            }
            player.setTranslateY(player.getTranslateY() + (movingDown ? 1 : -1));
        }
    }
    
    private void jumpPlayer(){
        
        if(canJump){
            playerVelocity = playerVelocity.add(0, -30);
            canJump = false;
        }
    }
    
    private Node createEntity(int x, int y, int w, int h, Color color){
        
        Rectangle entity = new Rectangle(w, h);
        entity.setTranslateX(x);
        entity.setTranslateY(y);
        entity.setFill(color);
        entity.getProperties().put("alive", true);
        
        gameRoot.getChildren().add(entity);
        
        return entity;
    }
    
    private Boolean isPressed(KeyCode key){
        
        return keys.getOrDefault(key, false);
    }
    
    public void start(Stage primaryStage) throws Exception{
        initContent();
        
        Scene scene  = new Scene(appRoot);
        scene.setOnKeyPressed(event -> keys.put(event.getCode(), true));
        scene.setOnKeyReleased(event -> keys.put(event.getCode(), false));
        primaryStage.setTitle("Leap Boy");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        AnimationTimer timer = new AnimationTimer(){
            public void handle(long now){
                update();
                coinUpdate();
                enemyKill();
                enemyBKill();
                deathUpdate();
                jumpUpY();
                nextLevel();
                livesCur.setText("\n\n lives: "+ lives);
                coinCur.setText("\n coin wallet: "+ coinWallet);
                totCoinColl.setText(" total coins collected: "+ coinsCollected);
            }
        };
        timer.start();
        appRoot.getChildren().add(livesCur);
        appRoot.getChildren().add(coinCur);
        appRoot.getChildren().add(totCoinColl);
    }
    
    public static void main(String[] args){
        launch(args);
    }

}

