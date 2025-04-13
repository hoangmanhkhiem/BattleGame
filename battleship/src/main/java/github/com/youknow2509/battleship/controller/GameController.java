package github.com.youknow2509.battleship.controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import github.com.youknow2509.battleship.consts.Consts;
import github.com.youknow2509.battleship.model.Board;
import github.com.youknow2509.battleship.model.Cell;
import github.com.youknow2509.battleship.model.ship.Ship;
import github.com.youknow2509.battleship.model.ship.ShipType;
import github.com.youknow2509.battleship.utils.image.ImageViewUtils;
import github.com.youknow2509.battleship.utils.okhttp.HttpClient;
import github.com.youknow2509.battleship.utils.utils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.list;

public class GameController {
    @FXML
    private VBox root;
    @FXML
    private GridPane playerGrid, botGrid;
    @FXML
    private TextField playerTitle, botTitle;

    @FXML
    private ImageView battleship_player, aircraft_carrier_player, cruiser_player, submarine_player, destroyer_player;
    @FXML
    private ImageView battleship_bot, aircraft_carrier_bot, cruiser_bot, submarine_bot, destroyer_bot;
    @FXML
    private TextField imv_turn;
    private final Gson gson = new Gson();

    private final Board playerBoard, botBoard;
    private int playerTurns = 0; // 0 = player, 1 = bot

    private final Map<ShipType, ImageView> playerShips = new HashMap<>(), botShips = new HashMap<>();

    public GameController(Board playerBoard, Board botBoard) {
        this.playerBoard = playerBoard;
        this.botBoard = botBoard;
        utils.printResult(playerBoard);
    }

    @FXML
    public void initialize() {
        botGrid.setDisable(true);
        setupShipMappings();
        renderGrid(botGrid, botBoard);
        setupPlayerClickEvents();
    }

    // Gọi API /fire
    public void fire(int x, int y) {
        HttpClient.fire(x, y, new HttpClient.HttpResponseCallback() {
            @Override
            public void onSuccess(String response) {
                Platform.runLater(() -> handleFireResponse(response));
            }

            @Override
            public void onFailure(IOException e) {
                System.out.println("Error firing at (" + x + ", " + y + "): " + e.getMessage());
            }
        });
    }

    // Xử lý phản hồi từ API /fire
    private void handleFireResponse(String response) {
        Map<String, Object> result = gson.fromJson(response, Map.class);
        boolean hit = (boolean) result.get("hit");
        System.out.println("Fire result: " + (hit ? "Hit!" : "Miss!"));
    }

    // Gọi API /ai-move
    public void aiMove() {
        HttpClient.aiMove(new HttpClient.HttpResponseCallback() {
            @Override
            public void onSuccess(String response) {
                Platform.runLater(() -> handleAiMoveResponse(response));
            }

            @Override
            public void onFailure(IOException e) {
                System.out.println("Error during AI move: " + e.getMessage());
            }
        });
    }

    // Xử lý phản hồi từ API /ai-move
    private void handleAiMoveResponse(String response) {
        Type type = new TypeToken<Map<String, Object>>() {}.getType();
        Map<String, Object> result = gson.fromJson(response, type);

        int x = ((Double) result.get("x")).intValue();
        int y = ((Double) result.get("y")).intValue();
        boolean hit = (boolean) result.get("hit");

        System.out.println("AI move at (" + x + ", " + y + "): " + (hit ? "Hit!" : "Miss!"));
    }

    // Gọi API /board
    public void getBoard() {
        HttpClient.getBoard(new HttpClient.HttpResponseCallback() {
            @Override
            public void onSuccess(String response) {
                Platform.runLater(() -> handleGetBoardResponse(response));
            }

            @Override
            public void onFailure(IOException e) {
                System.out.println("Error fetching board: " + e.getMessage());
            }
        });
    }

    // Xử lý phản hồi từ API /board
    private void handleGetBoardResponse(String response) {
        System.out.println("Board data: " + response);
    }

    // Gọi API /reset
    public void resetGame() {
        HttpClient.reset(new HttpClient.HttpResponseCallback() {
            @Override
            public void onSuccess(String response) {
                Platform.runLater(() -> System.out.println("Game reset successful!"));
            }

            @Override
            public void onFailure(IOException e) {
                System.out.println("Error resetting game: " + e.getMessage());
            }
        });
    }

    // handle click menu button - handle popup menu
    public void handleClickMenu(MouseEvent mouseEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(Consts.XML_RESOURCE_GAME_MENU));
            Parent root = loader.load();

            // Get the controller instance from the FXMLLoader
            GameMenuController controller = loader.getController();

            // Get the current stage from the event source
            Stage currentStage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            // Create a new stage and set it to the controller
            Stage newStage = new Stage();
            controller.initialize(currentStage);

            // Center the stage on the screen
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            newStage.setX((screenBounds.getWidth() - newStage.getWidth()) / 2);
            newStage.setY((screenBounds.getHeight() - newStage.getHeight()) / 2);

            newStage.setTitle("Menu");
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // check winner
    private void checkWinner(int playerTurns) {
        switch (playerTurns) {
            case 0:
                if (playerBoard.isAllShipsSunk()) {
                    handleWinner(0);
                }
                break;
            case 1:
                if (botBoard.isAllShipsSunk()) {
                    handleWinner(1);
                }
                break;
            default:
                break;
        }
    }

    // Handle the winner of the game
    private void handleWinner(int player) {
        // Disable both grids when the game ends
        playerGrid.setDisable(true);
        botGrid.setDisable(true);
        botTitle.setOpacity(0.0);

        // Set the winner and loser titles
        if (player == 0) {
            playerTitle.setText("You Win!");
            playerTitle.setStyle("-fx-background-color: #00ff00");
            botGrid.setOpacity(0.2);
        } else {
            playerTitle.setText("You Lose!");
            playerTitle.setStyle("-fx-background-color: #ff0000");
            playerGrid.setOpacity(0.2);
        }

        // Add animation for the title change
        utils.animateTitle(playerTitle);
    }

    // Set image ship for player and bot
    private void setupShipMappings() {
        playerShips.put(ShipType.BATTLESHIP, battleship_player);
        playerShips.put(ShipType.CARRIER, aircraft_carrier_player);
        playerShips.put(ShipType.CRUISER, cruiser_player);
        playerShips.put(ShipType.SUBMARINE, submarine_player);
        playerShips.put(ShipType.DESTROYER, destroyer_player);

        botShips.put(ShipType.BATTLESHIP, battleship_bot);
        botShips.put(ShipType.CARRIER, aircraft_carrier_bot);
        botShips.put(ShipType.CRUISER, cruiser_bot);
        botShips.put(ShipType.SUBMARINE, submarine_bot);
        botShips.put(ShipType.DESTROYER, destroyer_bot);
    }

    // listen player click event in grid
    private void setupPlayerClickEvents() {
        playerGrid.getChildren().forEach(node -> node.setOnMouseClicked(event -> {
            if (playerTurns == 1) return;

            int row = GridPane.getRowIndex(node);
            int col = GridPane.getColumnIndex(node);
            handlePlayerClick(row, col);
            node.setDisable(true);
        }));
    }

    // handle player click event
    private void handlePlayerClick(int row, int col) {
        Cell cell = playerBoard.getCell(row, col);
        if (cell.isHasShip()) {
            handleShipHit(playerGrid, cell, 0);
        } else {
            handleMiss(playerGrid, cell);
            botTurn();
        }
    }

    private void botTurn() {
        // Tạo một luồng riêng để xử lý logic của bot
        new Thread(() -> {
            try {
                // Tạm dừng để tạo hiệu ứng thời gian cho bot
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Gọi API /ai-move để lấy tọa độ mà bot sẽ tấn công
            HttpClient.aiMove(new HttpClient.HttpResponseCallback() {
                @Override
                public void onSuccess(String response) {
                    // Phân tích phản hồi từ server
                    Gson gson = new Gson();
                    Map<String, Object> result = gson.fromJson(response, new TypeToken<Map<String, Object>>() {}.getType());

                    int row = ((Double) result.get("x")).intValue();
                    int col = ((Double) result.get("y")).intValue();
                    boolean hit = (boolean) result.get("hit");

                    // Xử lý kết quả từ lượt tấn công của bot
                    Cell cell = botBoard.getCell(row, col);
                    Platform.runLater(() -> {
                        if (hit) {
                            handleShipHit(botGrid, cell, 1); // Xử lý khi trúng tàu
                            botTurn(); // Bot tiếp tục lượt nếu trúng tàu
                        } else {
                            handleMiss(botGrid, cell); // Xử lý khi không trúng tàu
                            playerTurns = 0; // Chuyển lượt cho người chơi
                            imv_turn.setText("YOUR TURN");
                        }
                    });
                }

                @Override
                public void onFailure(IOException e) {
                    System.out.println("Lỗi khi gọi API /ai-move: " + e.getMessage());
                    BotRandomMode(); // Chuyển sang chế độ random nếu không kết nối được với server
                }
            });
        }).start();
    }



    //Mode random if disconnect server
    private void BotRandomMode(){
        Cell cell;
        do {
            int row = (int) (Math.random() * botBoard.getRows());
            int col = (int) (Math.random() * botBoard.getColumns());
            cell = botBoard.getCell(row, col);
        } while (cell.isHit());

        Cell finalCell = cell;
        Platform.runLater(() -> {
            if (finalCell.isHasShip()) {
                handleShipHit(botGrid, finalCell, 1);
                botTurn();
            } else {
                handleMiss(botGrid, finalCell);
                playerTurns = 0;
            }
        });
    }

    // handle when ship hit for user or bot
    private void handleShipHit(GridPane grid, Cell cell, int player)     {
        StackPane pane = getStackPane(grid, cell.getPosition().getX(), cell.getPosition().getY());
        if (pane == null) return;

        new ImageViewUtils().setImageView(pane, Consts.PATH_IMAGE_DAME_HIT, Consts.SIZE_CELL, Consts.SIZE_CELL);
        cell.setHit(true);

        Ship ship = cell.getShipInCell();
        ship.setHitCount(ship.getHitCount() + 1);
        if (ship.isSunk()) {
            pane.getChildren().clear();
            // Show ship in grid
            ship.getCells().forEach(c -> c.getShipInCell().showShipInGridPane(grid));
            // opacity image hip in button grid
            setShipOpacity(ship.getShipType(), player);
            // check winner or loser
            checkWinner(player);
        }
    }

    // Change turn and handle view in grid when miss ship
    private void handleMiss(GridPane grid, Cell cell) {
        playerTurns = playerTurns == 0 ? 1 : 0;
        StackPane pane = getStackPane(grid, cell.getPosition().getX(), cell.getPosition().getY());
        if (pane != null) {
            new ImageViewUtils().setImageView(pane, Consts.PATH_IMAGE_DAME_MISS, Consts.SIZE_CELL, Consts.SIZE_CELL);
        }
        String turn = playerTurns == 0 ? "YOUR TURN" : "BOT TURN";
        imv_turn.setText(turn);
    }

    // Set opacity for ship when it's sunk
    private void setShipOpacity(ShipType type, int player) {
        ImageView imageView = player == 1 ? botShips.get(type) : playerShips.get(type);
        if (imageView != null) imageView.setOpacity(0.5);
    }

    // render grid with ship - for bot
    private void renderGrid(GridPane grid, Board board) {
        board.getCells().stream().filter(Cell::isHasShip).forEach(cell -> {
            StackPane pane = getStackPane(grid, cell.getPosition().getX(), cell.getPosition().getY());
            if ( pane != null)
                pane.getChildren().add(new Rectangle(Consts.SIZE_CELL, Consts.SIZE_CELL, Color.RED));
        });
    }

    // get stack pane in grid
    private StackPane getStackPane(GridPane grid, int row, int col) {
        return grid.getChildren().stream()
                .filter(node -> GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == col)
                .map(node -> (StackPane) node).findFirst().orElse(null);
    }

    // help show data send to ai
    public void showDataReq(int[][] placeShip, List<Integer> getListShipNotSunk) {
        System.out.println("Place ship: ");
        for (int i = 0; i < placeShip.length; i++) {
            for (int j = 0; j < placeShip[i].length; j++) {
                System.out.print(placeShip[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("List ship not sunk: " + getListShipNotSunk);
        for (int i = 0; i < getListShipNotSunk.size(); i++) {
            System.out.println("Ship " + getListShipNotSunk.get(i) + " not sunk");
        }
    }
}
