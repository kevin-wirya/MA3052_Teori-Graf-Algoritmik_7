import urllib.request
import csv
import json
import io

print("Downloading airports data...")
airports_url = "https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat"
airports_response = urllib.request.urlopen(airports_url)
airports_data = airports_response.read().decode('utf-8')

print("Downloading routes data...")
routes_url = "https://raw.githubusercontent.com/jpatokal/openflights/master/data/routes.dat"
routes_response = urllib.request.urlopen(routes_url)
routes_data = routes_response.read().decode('utf-8')

# Parse airports
# Format: Airport ID, Name, City, Country, IATA, ICAO, Latitude, Longitude, Altitude, Timezone, DST, Tz database timezone, Type, Source
indonesian_airports = {}
reader = csv.reader(io.StringIO(airports_data))
for row in reader:
    if len(row) >= 8:
        country = row[3]
        if country == "Indonesia":
            iata = row[4]
            if iata and iata != "\\N":
                indonesian_airports[iata] = {
                    "id": row[0],
                    "name": row[1],
                    "city": row[2],
                    "iata": iata,
                    "lat": float(row[6]),
                    "lon": float(row[7])
                }

print(f"Found {len(indonesian_airports)} Indonesian airports.")

# Parse routes
# Format: Airline, Airline ID, Source airport, Source airport ID, Destination airport, Destination airport ID, Codeshare, Stops, Equipment
domestic_routes = []
reader = csv.reader(io.StringIO(routes_data))
for row in reader:
    if len(row) >= 6:
        src = row[2]
        dest = row[4]
        if src in indonesian_airports and dest in indonesian_airports:
            domestic_routes.append({
                "src": src,
                "dest": dest,
                "airline": row[0]
            })

print(f"Found {len(domestic_routes)} domestic routes between Indonesian airports.")

# Structure the graph output
output_data = {
    "airports": list(indonesian_airports.values()),
    "routes": domestic_routes
}

output_path = "indonesian_flights.json"
with open(output_path, "w", encoding="utf-8") as f:
    json.dump(output_data, f, indent=2, ensure_ascii=False)

print(f"Saved flight data to {output_path}")
