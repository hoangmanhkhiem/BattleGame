from flask import Flask, request, jsonify
from battle_ship.game import BattleshipGame, EMPTY_TILE_ID
import battle_ship.ai as ai

app = Flask(__name__)
game = BattleshipGame()
game.set_random_board(1)
game.set_random_board(2)

@app.route("/fire", methods=["POST"])
def fire():
    data = request.json
    x = data["x"]
    y = data["y"]
    hit = game.fire_at_tile(opponent_id=2, x_position=x, y_position=y)
    return jsonify({"hit": hit})

@app.route("/ai-move", methods=["POST"])
def ai_move():
    board = game.get_view_board_opponent(opponent_id=1)
    ship_lengths = [
        len(ship["tiles"])
        for ship in game.player_1_pieces.values()
        if not ship["is_sank"]
    ]
    _, x, y = ai.find_best_move(board, ship_lengths)
    hit = game.fire_at_tile(opponent_id=1, x_position=x, y_position=y)
    return jsonify({"x": x, "y": y, "hit": hit})

@app.route("/board", methods=["GET"])
def get_board():
    return jsonify({
        "player_board": [[tile.__dict__ for tile in row] for row in game.player_1_board],
        "ai_board": [[tile.__dict__ for tile in row] for row in game.player_2_board]
    })

@app.route("/reset", methods=["POST"])
def reset():
    global game
    game = BattleshipGame()
    game.set_random_board(1)
    game.set_random_board(2)
    return jsonify({"message": "Game reset successful"})

if __name__ == "__main__":
    app.run(debug=True)
