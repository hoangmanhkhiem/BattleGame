import random


class Battleship:

    def __init__(self):
        self.SHIP_MAP = [[0] * 10 for _ in range(10)]  # Thay numpy array bằng danh sách 2 chiều
        self.SHIP_INFO = {"Carrier": 5, "Battleship": 4, "Destroyer": 3, "Submarine": 3, "Patrol Boat": 2}
        self.SHIP_COORDINATE_DICT = dict()
        self.COORDINATE_SHIP_DICT = dict()
        self.SUNK_SHIP_COORDINATES = []
        self.SHOT_MAP = [[0] * 10 for _ in range(10)]  # Thay numpy array bằng danh sách 2 chiều
        self.PROB_MAP = [[0] * 10 for _ in range(10)]  # Thay numpy array bằng danh sách 2 chiều

        # ai variables
        self.hunt = True
        self.targets = []

    def place_ships(self):
        for ship, ship_size in self.SHIP_INFO.items():
            # select random start point for ship that isn't on top of another ship
            while True:
                start_row = random.choice(range(10))
                start_col = random.choice(range(10))

                # randomly choose an axis to hold constant
                const_axis = random.choice(["row", "col"])

                # randomly choose a direction
                direction = random.choice(["up", "down"])

                # select endpoint
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

                elif const_axis == "col":
                    if direction == "up" and start_row - ship_size >= 0:
                        end_row = start_row - ship_size
                        start_row, end_row = end_row, start_row
                        end_col = start_col + 1
                    elif direction == "down" and start_row + ship_size <= 9:
                        end_row = start_row + ship_size
                        end_col = start_col + 1
                    else:
                        continue

                # check that all spaces that we want to insert into are clear
                if all(self.SHIP_MAP[i][j] == 0 for i in range(start_row, end_row) for j in range(start_col, end_col)):
                    for i in range(start_row, end_row):
                        for j in range(start_col, end_col):
                            self.SHIP_MAP[i][j] = 1
                    # create a quickly-searchable dictionary of coordinates mapped to ships
                    if const_axis == "row":
                        coord_list = list(zip([start_row] * ship_size, [col for col in range(start_col, end_col)]))
                        self.SHIP_COORDINATE_DICT[ship] = coord_list
                        for coord in coord_list:
                            self.COORDINATE_SHIP_DICT[coord] = ship
                    elif const_axis == "col":
                        coord_list = list(zip([row for row in range(start_row, end_row)], [start_col] * ship_size))
                        self.SHIP_COORDINATE_DICT[ship] = coord_list
                        for coord in coord_list:
                            self.COORDINATE_SHIP_DICT[coord] = ship

                else:
                    continue
                break

    def set_ship_map(self, matrix):
        self.SHIP_MAP = matrix

    def get_ships(self):
        return self.SHIP_INFO

    def gen_prob_map(self):
        prob_map = [[0] * 10 for _ in range(10)]  # Dùng danh sách 2 chiều thay vì numpy array
        for ship_name in set(self.COORDINATE_SHIP_DICT.values()):
            ship_size = self.SHIP_INFO[ship_name]
            use_size = ship_size - 1
            # check where a ship will fit on the board
            for row in range(10):
                for col in range(10):
                    if self.SHOT_MAP[row][col] != 1:
                        # get potential ship endpoints
                        endpoints = []
                        # add 1 to all endpoints to compensate for python indexing
                        if row - use_size >= 0:
                            endpoints.append(((row - use_size, col), (row + 1, col + 1)))
                        if row + use_size <= 9:
                            endpoints.append(((row, col), (row + use_size + 1, col + 1)))
                        if col - use_size >= 0:
                            endpoints.append(((row, col - use_size), (row + 1, col + 1)))
                        if col + use_size <= 9:
                            endpoints.append(((row, col), (row + 1, col + use_size + 1)))

                        for (start_row, start_col), (end_row, end_col) in endpoints:
                            if all(self.SHOT_MAP[i][j] == 0 for i in range(start_row, end_row) for j in range(start_col, end_col)):
                                for i in range(start_row, end_row):
                                    for j in range(start_col, end_col):
                                        prob_map[i][j] += 1

                    # increase probability of attacking squares near successful hits
                    if self.SHOT_MAP[row][col] == 1 and \
                            self.SHIP_MAP[row][col] == 1 and \
                            (row, col) not in self.SUNK_SHIP_COORDINATES:  # un-weight hits on sunk ships

                        if (row + 1 <= 9) and (self.SHOT_MAP[row + 1][col] == 0):
                            if (row - 1 >= 0) and \
                                    (row - 1, col) not in self.SUNK_SHIP_COORDINATES and \
                                    (self.SHOT_MAP[row - 1][col] == self.SHIP_MAP[row - 1][col] == 1):
                                prob_map[row + 1][col] += 15
                            else:
                                prob_map[row + 1][col] += 10

                        if (row - 1 >= 0) and (self.SHOT_MAP[row - 1][col] == 0):
                            if (row + 1 <= 9) and \
                                    (row + 1, col) not in self.SUNK_SHIP_COORDINATES and \
                                    (self.SHOT_MAP[row + 1][col] == self.SHIP_MAP[row + 1][col] == 1):
                                prob_map[row - 1][col] += 15
                            else:
                                prob_map[row - 1][col] += 10

                        if (col + 1 <= 9) and (self.SHOT_MAP[row][col + 1] == 0):
                            if (col - 1 >= 0) and \
                                    (row, col - 1) not in self.SUNK_SHIP_COORDINATES and \
                                    (self.SHOT_MAP[row][col - 1] == self.SHIP_MAP[row][col - 1] == 1):
                                prob_map[row][col + 1] += 15
                            else:
                                prob_map[row][col + 1] += 10

                        if (col - 1 >= 0) and (self.SHOT_MAP[row][col - 1] == 0):
                            if (col + 1 <= 9) and \
                                    (row, col + 1) not in self.SUNK_SHIP_COORDINATES and \
                                    (self.SHOT_MAP[row][col + 1] == self.SHIP_MAP[row][col + 1] == 1):
                                prob_map[row][col - 1] += 15
                            else:
                                prob_map[row][col - 1] += 10

                    # decrease probability for misses to zero
                    elif self.SHOT_MAP[row][col] == 1 and self.SHIP_MAP[row][col] != 1:
                        prob_map[row][col] = 0

        self.PROB_MAP = prob_map

    def guess_random(self, length=None):
        while True:
            guess_row, guess_col = random.choice(range(10)), random.choice(range(10))
            if length:
                if (guess_row + guess_col) % length != 0:
                    continue
            if self.SHOT_MAP[guess_row][guess_col] == 0:
                break

        return guess_row, guess_col

    def shoot(self, guess_row, guess_col):
        self.SHOT_MAP[guess_row][guess_col] = 1

        if self.SHIP_MAP[guess_row][guess_col] == 1:
            ship = self.COORDINATE_SHIP_DICT.pop((guess_row, guess_col))
            if ship not in self.COORDINATE_SHIP_DICT.values():
                self.SUNK_SHIP_COORDINATES.extend(self.SHIP_COORDINATE_DICT[ship])
                self.SHIP_COORDINATE_DICT.pop(ship)

        return guess_row, guess_col

    def hunt_target(self, length=None):
        if not self.targets:
            guess_row, guess_col = self.guess_random(length)
        else:
            guess_row, guess_col = self.targets.pop()

        if self.SHIP_MAP[guess_row][guess_col] == 1:
            potential_targets = [(guess_row + 1, guess_col), (guess_row, guess_col + 1),
                                 (guess_row - 1, guess_col), (guess_row, guess_col - 1)]

            # Add the valid adjacent squares to the target list if they are valid and not already targeted
            for target_row, target_col in potential_targets:
                if (0 <= target_row <= 9) and (0 <= target_col <= 9) and \
                        self.SHOT_MAP[target_row][target_col] == 0 and \
                        (target_row, target_col) not in self.targets:
                    self.targets.append((target_row, target_col))

        return guess_row, guess_col

    def play(self, placeShip, getListShipNotSunk, flag):
        set_ship_map(placeShip)
        getListShipNotSunk = convert_arr_to_list(getListShipNotSunk)
        self.SHIP_INFO = getListShipNotSunk

        if flag == 0:
            # First random guess when starting the game
            guess_row, guess_col = self.guess_random()
            shot_coordinates = self.shoot(guess_row, guess_col)
        else:
            # Hunt mode: after hitting a ship, continue to attack adjacent squares
            guess_row, guess_col = self.hunt_target()  # This method will find adjacent targets and continue attacking
            shot_coordinates = self.shoot(guess_row, guess_col)

        return shot_coordinates


def convert_arr_to_list(arr):
        ship_names = ["Carrier", "Battleship", "Destroyer", "Submarine", "Patrol Boat"]
        _list = {"Carrier": -1, "Battleship": -1, "Destroyer": -1, "Submarine": -1, "Patrol Boat": -1}
        for i in range(len(arr)):
            _list[ship_names[i]] = arr[i]
        return _list

def set_ship_map(matrix):
    Battleship().set_ship_map(matrix)

def play(placeShip, getListShipNotSunk,flag):
    game = Battleship()  # Khởi tạo đối tượng game
    shot_coordinates = game.play(placeShip, getListShipNotSunk,flag)  # Gọi phương thức play và nhận tọa độ bắn
    return shot_coordinates  # Trả về tọa độ bắn
