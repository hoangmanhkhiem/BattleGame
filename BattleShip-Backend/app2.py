from flask import Flask, request, jsonify
from flask_cors import CORS
from ai_battleship import Battleship

app = Flask(__name__)
CORS(app)

game = Battleship()

@app.route('/init-map', methods=['POST'])
def init_map():
    game.reset_board()
    return jsonify({"message": "Map initialized"})

@app.route('/hit', methods=['POST'])
def mark_hit():
    data = request.json
    row, col = data['row'], data['col']
    game.SHIP_MAP[row][col] = 1
    game.SHOT_MAP[row][col] = 1
    game.add_adjacent_targets(row, col)
    return jsonify({"message": f"HIT at ({row},{col})"})

@app.route('/miss', methods=['POST'])
def mark_miss():
    data = request.json
    row, col = data['row'], data['col']
    game.SHOT_MAP[row][col] = 1
    return jsonify({"message": f"MISS at ({row},{col})"})

@app.route('/next-shot', methods=['GET'])
def next_shot():
    row, col = game.hunt_target()
    game.shoot(row, col)
    return jsonify({"row": int(row), "col": int(col)})

if __name__ == '__main__':
    app.run(debug=True)
