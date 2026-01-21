# Simple Cassandra User Logging System
## Run Cassandra Cluster
### Run a single-node Cassandra container:
```sh
docker run --name cassandra-node -p 9042:9042 -d cassandra:5.0
```
Wait ~25 seconds for boot.
Now Cassandra is running locally on port .
Open Cassandra CQL Shell (cqlsh)
```sh
docker exec -it cassandra-node cqlsh
```

## Create Keyspace (Database)
Run inside cqlsh:
```sql
CREATE KEYSPACE user_logging WITH REPLICATION = { 'class': 'SimpleStrategy', 'replication_factor': 1 };
```
Switch into it:
```sql
USE user_logging;
```

## Create Tables
### Table 1: Users
```sql
CREATE TABLE users ( user_id text PRIMARY KEY, firstname text,lastname text,company_name text,phone text,city text);
```
### Table 2: Logins by User
```sql
CREATE TABLE logins_by_user (user_id text,login_ts timestamp,ip_address text,device text,status text,PRIMARY KEY ((user_id), login_ts) ) WITH CLUSTERING ORDER BY (login_ts DESC);
```
### Table 3: Users by City
```sql
CREATE TABLE users_by_city (city text,user_id text,firstname text,lastname text,company_name text,phone text,PRIMARY KEY ((city), user_id));
```

## Insert Sample Users
```sql
INSERT INTO users(user_id, firstname, lastname, company_name, phone, city)
VALUES('U001', 'Mukesh', 'Ch', 'EA', '9876543210', 'Hyderabad');

INSERT INTO users_by_city(city, user_id, firstname, lastname, company_name, phone)
VALUES('Hyderabad', 'U001', 'Raj', 'Ch', 'EA', '9876543210');
Add another:
INSERT INTO users(user_id, firstname, lastname, company_name, phone, city)
VALUES('U002', 'Daivik', 'B', 'Qualcomm', '9988776655', 'Hyderabad');

INSERT INTO users_by_city(city, user_id, firstname, lastname, company_name, phone)
VALUES('Hyderabad', 'U002', 'Daivik', 'B', 'Qualcomm', '9988776655');
```
## Insert Login Events
### Simulate user login:
```sql
INSERT INTO logins_by_user(user_id, login_ts, ip_address, device, status)
VALUES('U001', toTimestamp(now()), '10.0.0.8', 'Chrome', 'SUCCESS');

INSERT INTO logins_by_user(user_id, login_ts, ip_address, device, status)
VALUES('U001', toTimestamp(now()), '10.0.0.8', 'Mobile', 'SUCCESS');

INSERT INTO logins_by_user(user_id, login_ts, ip_address, device, status)
VALUES('U002', toTimestamp(now()), '10.0.0.5', 'Firefox', 'FAILED');
```

## Query the Data

### A. Fetch user profile
```sql
SELECT * FROM users WHERE user_id='U001';
```
### B. Fetch last 5 logins
```sql
SELECT * FROM logins_by_user WHERE user_id='U001' LIMIT 5;
```
### C. Fetch users by city
```sql
SELECT * FROM users_by_city WHERE city='Hyderabad';

```