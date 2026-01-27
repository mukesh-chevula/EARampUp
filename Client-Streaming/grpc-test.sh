#!/bin/bash

# gRPC Client Streaming Test Commands for UserService

echo "=== CREATE USER ==="
cat > /tmp/create.json <<'EOF'
{"field":"firstname","value":"Mukesh"}
{"field":"lastname","value":"Ch"}
{"field":"email","value":"mukesh.ch@ea.com"}
{"field":"phone","value":"1234567890"}
EOF
grpcurl -plaintext -d @ localhost:9090 UserService/CreateUser < /tmp/create.json
USERID=$(grpcurl -plaintext -d '{}' localhost:9090 UserService/GetAllUsers | jq -r '.users[0].id' 2>/dev/null || echo "manual-id")
echo "Created user ID: $USERID"
echo ""

echo "=== GET ALL USERS ==="
grpcurl -plaintext -d '{}' localhost:9090 UserService/GetAllUsers
echo ""

echo "=== GET USER BY ID ==="
# Replace with actual ID from previous response if needed
grpcurl -plaintext -d "{\"id\":\"$USERID\"}" localhost:9090 UserService/GetUser
echo ""

echo "=== UPDATE USER ==="
cat > /tmp/update.json <<'EOF'
{"field":"id","value":"USER_ID_PLACEHOLDER"}
{"field":"firstname","value":"Mukesh"}
{"field":"email","value":"mukesh.ch@ea.com"}
EOF
# Replace USER_ID_PLACEHOLDER with actual ID
sed -i '' "s/USER_ID_PLACEHOLDER/$USERID/g" /tmp/update.json
grpcurl -plaintext -d @ localhost:9090 UserService/UpdateUser < /tmp/update.json
echo ""

echo "=== DELETE USER ==="
cat > /tmp/delete.json <<'EOF'
{"field":"id","value":"USER_ID_PLACEHOLDER"}
EOF
sed -i '' "s/USER_ID_PLACEHOLDER/$USERID/g" /tmp/delete.json
grpcurl -plaintext -d @ localhost:9090 UserService/DeleteUser < /tmp/delete.json
echo ""

echo "=== GET ALL USERS (AFTER DELETE) ==="
grpcurl -plaintext -d '{}' localhost:9090 UserService/GetAllUsers
echo ""
