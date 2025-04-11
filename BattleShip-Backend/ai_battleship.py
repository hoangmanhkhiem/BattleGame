import numpy as np
import random

class Battleship:

    def __init__(self):
        self.SHIP_MAP = np.zeros([10, 10])
        self.SHIP_INFO = {"Carrier": 5, "Battleship": 4, "Destroyer": 3, "Submarine": 3, "Patrol Boat": 2}
        self.SHIP_COORDINATE_DICT = dict()
        self.COORDINATE_SHIP_DICT = dict()
        self.SUNK_SHIP_COORDINATES = []
        self.SHOT_MAP = np.zeros([10, 10])
        self.targets = []
        self.SCORE = 0
        self.NUM_GUESSES = 0
        self.GAME_OVER = False
        self.place_ships()

    def reset_board(self):
        self.__init__()

    def place_ships(self):
        for ship, ship_size in self.SHIP_INFO.items():
            while True:
                start_row = random.randint(0, 9)
                start_col = random.randint(0, 9)
                const_axis = random.choice(["row", "col"])
                direction = random.choice(["up", "down"])

                if const_axis == "row":
                    if direction == "up" and start_col - ship_size >= 0:
                        end_row = start_row + 1
                        end_col = start_col - ship_size
                        start_col, end_col = end_col, start_col
                    elif direction == "down" and start_col + ship_size <= 9:
                        end_row = start_row + 1
                        end_col = start_col + ship_size
                    else:
                        continue
                else:
                    if direction == "up" and start_row - ship_size >= 0:
                        end_row = start_row - ship_size
                        start_row, end_row = end_row, start_row
                        end_col = start_col + 1
                    elif direction == "down" and start_row + ship_size <= 9:
                        end_row = start_row + ship_size
                        end_col = start_col + 1
                    else:
                        continue

                if np.all(self.SHIP_MAP[start_row:end_row, start_col:end_col] == 0):
                    self.SHIP_MAP[start_row:end_row, start_col:end_col] = 1
                    if const_axis == "row":
                        coords = list(zip([start_row]*ship_size, list(range(start_col, end_col))))
                    else:
                        coords = list(zip(list(range(start_row, end_row)), [start_col]*ship_size))
                    self.SHIP_COORDINATE_DICT[ship] = coords
                    for coord in coords:
                        self.COORDINATE_SHIP_DICT[coord] = ship
                    break

    def guess_random_diagonal(self):
        while True:
            row, col = random.randint(0, 9), random.randint(0, 9)
            if self.SHOT_MAP[row][col] == 0 and (row + col) % 2 == 0:
                return row, col

    def hunt_target(self):
        if self.targets:
            row, col = self.targets.pop()
            return row, col
        else:
            return self.guess_random_diagonal()

    def add_adjacent_targets(self, row, col):
        directions = [(0, 1), (1, 0), (-1, 0), (0, -1)]
        for dr, dc in directions:
            r, c = row + dr, col + dc
            if 0 <= r < 10 and 0 <= c < 10 and self.SHOT_MAP[r][c] == 0:
                if (r, c) not in self.targets:
                    self.targets.append((r, c))

    def shoot(self, row, col):
        self.SHOT_MAP[row][col] = 1
        self.NUM_GUESSES += 1

        if self.SHIP_MAP[row][col] == 1:
            self.SCORE += 1
            self.add_adjacent_targets(row, col)
            if (row, col) in self.COORDINATE_SHIP_DICT:
                ship = self.COORDINATE_SHIP_DICT.pop((row, col))
                if ship not in self.COORDINATE_SHIP_DICT.values():
                    self.SUNK_SHIP_COORDINATES.extend(self.SHIP_COORDINATE_DICT[ship])
                    self.SHIP_COORDINATE_DICT.pop(ship)
        if self.SCORE == sum(self.SHIP_INFO.values()):
            self.GAME_OVER = True
