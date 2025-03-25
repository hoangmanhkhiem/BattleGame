from flask import Flask, request, jsonify, render_template
from flask_cors import CORS
from exce import play

app = Flask(__name__)
CORS(app)

@app.route('/ai-battleship', methods=['POST'])
def index():
    placeShip = request.json['placeShip']
    getListShipNotSunk = request.json['getListShipNotSunk']
    coordinate = play(placeShip, getListShipNotSunk)
    print(coordinate)
    return jsonify(coordinate)

if __name__ == '__main__':
    app.run(debug=True)
