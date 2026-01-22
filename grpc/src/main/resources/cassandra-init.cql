-- Create keyspace
CREATE KEYSPACE IF NOT EXISTS grpc_keyspace
WITH replication = {
    'class': 'SimpleStrategy',
    'replication_factor': 1
};

-- Create users table
CREATE TABLE IF NOT EXISTS grpc_keyspace.users (
    user_id text PRIMARY KEY,
    first_name text,
    last_name text,
    company text,
    age int,
    phone_number text
) WITH CLUSTERING ORDER BY (user_id ASC);
