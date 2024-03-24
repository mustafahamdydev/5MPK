import os
import pandas as pd
import networkx as nx
import geopy.distance
import heapq
import pickle
from shapely.geometry import Point

current_dir = os.path.dirname(os.path.abspath(__file__))

# Load data into pandas DataFrames
nodes_table_file = os.path.join(current_dir,'nodes_table.csv')
nodes_df = pd.read_csv(nodes_table_file)
edges_table_file = os.path.join(current_dir,'edges_table.csv')
edges_df = pd.read_csv(edges_table_file)
trips_with_stops_file = os.path.join(current_dir,'trips_with_stops_updated.csv')
trips_df = pd.read_csv(trips_with_stops_file)
graph_file_path = os.path.join(current_dir, 'graph.pkl')

# Convert nodes_df to a GeoDataFrame {for find_closest_node method}
geometry = [Point(lon, lat) for lon, lat in zip(nodes_df['longitude'], nodes_df['latitude'])]

# Try to open the graph.pkl file if it exists and if it doesnt create it
try:
    with open(graph_file_path, 'rb') as file:
        G = pickle.load(file)
except FileNotFoundError:
    # Create a directed graph
    G = nx.DiGraph()

    # Add nodes to the graph
    for index, row in nodes_df.iterrows():
        G.add_node(row['stop_id'], stop_name=row['stop_name'],
                latitude=row['latitude'], longitude=row['longitude'])

    # Add edges to the graph
    for index, row in edges_df.iterrows():
        G.add_edge(row['start_stop_id'], row['end_stop_id'], route_id=row['route_id'],
                direction_id=row['direction_id'], trip_id=row['trip_id'],
                sequence=row['sequence'], weight=row['weight'])

    # Save the graph object
    with open('graph.pkl', 'wb') as file:
        pickle.dump(G, file)

# Finds the closest node (bus stop) to a given coordinates (location)
def find_closest_node(latitude, longitude):
    distances = [(node, (G.nodes[node]['latitude'], G.nodes[node]['longitude'])) for node in G.nodes]
    closest_node = min(distances, key=lambda x: geopy.distance.distance((latitude, longitude), x[1]).m)[0]
    return closest_node

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
    # Priority queue with (priority, node, path, cost, trip_id, transfers_count)
    frontier = []
    heapq.heappush(frontier, (0, start, [], 0, None, 0))

    # Explored nodes and their lowest cost and trip_id found
    explored = {(start, None): 0}

    while frontier:
        _, current_node, path, current_cost, current_trip_id, transfers = heapq.heappop(frontier)

        if current_node == goal:
            return path + [(current_node,
                            G.nodes[current_node]['stop_name'],
                            G.nodes[current_node]['latitude'],
                            G.nodes[current_node]['longitude'])]

        for neighbor, edge_attr in G[current_node].items():
            # Check for transfer
            transfer_penalty = 0
            new_transfers = transfers
            if current_trip_id is not None and edge_attr['trip_id'] != current_trip_id:
                transfer_penalty = 300  # Arbitrary transfer penalty
                new_transfers += 1

            new_cost = current_cost + edge_attr['weight'] + transfer_penalty
            # Only consider edges with the same direction_id if one has been set
            if (neighbor, edge_attr['trip_id']) not in explored or new_cost < explored[(neighbor, edge_attr['trip_id'])]:
                explored[(neighbor, edge_attr['trip_id'])] = new_cost
                priority = new_cost + haversine_heuristic(neighbor, goal) + (100000 * new_transfers)  # Penalize transfers
                new_path = path + [(current_node,
                                    G.nodes[current_node]['stop_name'],
                                    G.nodes[current_node]['latitude'],
                                    G.nodes[current_node]['longitude'])]
                heapq.heappush(frontier, (priority, neighbor, new_path, new_cost, edge_attr['trip_id'], new_transfers))
    return None  # Goal not reachable

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
            covered_stops = 1  # Start with 1 to account for the current stop
            for i, stop_id in enumerate(trip_stops[start_index + 1:]):
                if (current_stop_index + i + 1) >= len(path) or stop_id != path[current_stop_index + i + 1][0]:
                    break
                covered_stops += 1
            if covered_stops > max_covered_stops:
                max_covered_stops = covered_stops
                best_trip = trip['route_id'], trip['route_short_name']
        if best_trip:
            for i in range(current_stop_index, current_stop_index + max_covered_stops):
                enhanced_path.append((*path[i], *best_trip))  # Unpack the tuple
            current_stop_index += max_covered_stops
        else:
            current_stop_index += 1
    return enhanced_path

# Function that handles all other functions, takes as input the coordinates of the start and end points:
def main(start_lat,start_lon,end_lat,end_lon):
    start_stop = find_closest_node(start_lat, start_lon)
    end_stop = find_closest_node(end_lat, end_lon)
    start_stop_info = get_stop_info_for_stop(start_stop)
    end_stop_info = get_stop_info_for_stop(end_stop)
    path = astar_path_with_transfers(start_stop, end_stop)
    routes = find_routes(path)
    return(routes, start_stop_info, end_stop_info)