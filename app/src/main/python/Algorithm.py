import os
import pandas as pd
import networkx as nx
import geopy.distance
import heapq
import pickle
import sqlite3
from shapely.geometry import Point

current_dir = os.path.dirname(os.path.abspath(__file__))

# Load data into pandas DataFrames
db_file = os.path.join(current_dir,'public_transport.db')
graph_file_path = os.path.join(current_dir, 'graph.pkl')

# Connect to the SQLite database
conn = sqlite3.connect(db_file)

# Read tables from the database
edges_df = pd.read_sql_query("SELECT * FROM edges_table", conn)
nodes_df = pd.read_sql_query("SELECT * FROM nodes_table", conn)
trips_df = pd.read_sql_query("SELECT * FROM trips_table", conn)

# Convert nodes_df to a GeoDataFrame {for find_closest_node method}
geometry = [Point(lon, lat) for lon, lat in zip(nodes_df['longitude'], nodes_df['latitude'])]

#open the graph file
with open(graph_file_path, 'rb') as file:
    G = pickle.load(file)

# Finds the closest node (bus stop) to a given coordinates (location)
def find_closest_node(latitude, longitude):
    distances = [(node, geopy.distance.distance((latitude, longitude), (G.nodes[node]['latitude'], G.nodes[node]['longitude'])).km) for node in G.nodes]
    closest_node, closest_distance = min(distances, key=lambda x: x[1])
    return closest_node, closest_distance * 1000

# Returns the stop info of a stop_id
def get_stop_info_for_stop(stop_id):
    row = nodes_df[nodes_df['stop_id'] == stop_id]
    if not row.empty:
        stop_info = (row['latitude'].values[0], row['longitude'].values[0], row['stop_name'].values[0])
    return stop_info

# Heuristic function using the Haversine formula to calculate the straight-line distance
def haversine_heuristic(node1, node2):
    coords_1 = (G.nodes[node1]['latitude'], G.nodes[node1]['longitude'])
    coords_2 = (G.nodes[node2]['latitude'], G.nodes[node2]['longitude'])
    return geopy.distance.distance(coords_1, coords_2).meters

# A* algorithm with bus transfer handling
def astar_path_with_transfers(start, goal):
    frontier = []
    heapq.heappush(frontier, (0, start, [], 0, None, 0))
    explored = {(start, None): 0}
    while frontier:
        _, current_node, path, current_cost, current_trip_id, transfers = heapq.heappop(frontier)
        if haversine_heuristic(current_node, goal) < 500:
            return path + [(current_node,
                            G.nodes[current_node]['stop_name'],
                            G.nodes[current_node]['latitude'],
                            G.nodes[current_node]['longitude'])]
        for neighbor, edge_attr in G[current_node].items():
            transfer_penalty = 0
            new_transfers = transfers
            if current_trip_id is not None and edge_attr['trip_id'] != current_trip_id:
                transfer_penalty = 300
                new_transfers += 1
            new_cost = current_cost + edge_attr['weight'] + transfer_penalty
            if (neighbor, edge_attr['trip_id']) not in explored or new_cost < explored[(neighbor, edge_attr['trip_id'])]:
                explored[(neighbor, edge_attr['trip_id'])] = new_cost
                priority = new_cost + haversine_heuristic(neighbor, goal) + (100000 * new_transfers)
                new_path = path + [(current_node,
                                    G.nodes[current_node]['stop_name'],
                                    G.nodes[current_node]['latitude'],
                                    G.nodes[current_node]['longitude'])]
                heapq.heappush(frontier, (priority, neighbor, new_path, new_cost, edge_attr['trip_id'], new_transfers))
    return None

# Finds the buses that pass the output path points of the path finding algorithm
def find_routes(path):
    enhanced_path = []
    current_stop_index = 0
    while current_stop_index < len(path):
        current_stop_id, _, _, _ = path[current_stop_index]
        max_covered_stops = 0
        best_trip = None
        for _, trip in trips_df.iterrows():
            trip_stops = trip['stops'].split(',')
            try:
                start_index = trip_stops.index(current_stop_id)
            except ValueError:
                continue
            covered_stops = 1
            for i, stop_id in enumerate(trip_stops[start_index + 1:]):
                if (current_stop_index + i + 1) >= len(path) or stop_id != path[current_stop_index + i + 1][0]:
                    break
                covered_stops += 1
            if covered_stops > max_covered_stops:
                max_covered_stops = covered_stops
                best_trip = trip['route_id'], trip['route_short_name']
        if best_trip:
            for i in range(current_stop_index, current_stop_index + max_covered_stops):
                enhanced_path.append((*path[i], *best_trip))
            current_stop_index += max_covered_stops
        else:
            current_stop_index += 1
    return enhanced_path

def calculate_total_time(path):
    total_time = 0
    for i in range(len(path) - 1):
        edge_data = G.get_edge_data(path[i][0], path[i+1][0])
        total_time += edge_data['weight']
    return total_time

def find_closest_nodes(latitude, longitude, num_stops=3):
    distances = [(node, geopy.distance.distance((latitude, longitude), (G.nodes[node]['latitude'], G.nodes[node]['longitude'])).km) for node in G.nodes]
    closest_nodes = heapq.nsmallest(num_stops, distances, key=lambda x: x[1])
    return [(node, distance * 1000) for node, distance in closest_nodes]

# Function that handles all other functions, takes as input the coordinates of the start point and Returns the following:
def main(start_lat, start_lon, end_lat, end_lon):
    start_stops = find_closest_nodes(start_lat, start_lon, num_stops=3)
    end_stop, end_distance = find_closest_node(end_lat, end_lon)
    if end_distance > 2000:
        return None
    end_stop_info = get_stop_info_for_stop(end_stop)

    best_path = None
    best_total_time = float('inf')

    for start_stop, start_distance in start_stops:
        if start_distance > 2000:
            continue
        start_stop_info = get_stop_info_for_stop(start_stop)
        path = astar_path_with_transfers(start_stop, end_stop)
        if path is None:
            continue

        routes = find_routes(path)
        total_time = calculate_total_time(routes)

        if total_time < best_total_time:
            best_path = routes
            best_total_time = total_time

    if best_path is None:
        return None

    return best_path, start_stop_info, end_stop_info