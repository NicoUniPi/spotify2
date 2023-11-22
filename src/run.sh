#!/bin/bash

# List of MongoDB configuration files
config_files=(
  #shard1
  "C:\Users\ACER\Desktop\MongoConfig\replicas\cfg_rs0\mongod_1.cfg"
  "C:\Users\ACER\Desktop\MongoConfig\replicas\cfg_rs0\mongod_2.cfg"
  "C:\Users\ACER\Desktop\MongoConfig\replicas\cfg_rs0\mongod_3.cfg"
  
  #shard2
  "C:\Users\ACER\Desktop\MongoConfig\replicas\cfg_rs1\mongod_1.cfg"
  "C:\Users\ACER\Desktop\MongoConfig\replicas\cfg_rs1\mongod_2.cfg"
  "C:\Users\ACER\Desktop\MongoConfig\replicas\cfg_rs1\mongod_3.cfg"

  #sharding
  "C:\Users\ACER\Desktop\MongoConfig\sharding\mongod_1.cfg"
  "C:\Users\ACER\Desktop\MongoConfig\sharding\mongod_2.cfg"
  "C:\Users\ACER\Desktop\MongoConfig\sharding\mongod_3.cfg"
)

# Loop through each configuration file and run mongod
for config_file in "${config_files[@]}"; do
  mongod -config "$config_file" &
done

# Wait for mongod instances to start
sleep 5

# Start mongos
mongos --configdb configReplSet/localhost:27023,localhost:27024,localhost:27025 --port 27016

# Wait for all background processes to finish
wait
