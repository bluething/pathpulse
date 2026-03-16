#!/bin/bash

# WebSocket test script wrapper for Go implementation
# Usage: ./test-websocket.sh [addr] [path] [count]

# Use defaults if not provided
ADDR=${1:-"localhost:8081"}
PATH_WS=${2:-"/ws/location"}
COUNT=${3:-10}

echo "Testing WebSocket connection to ws://${ADDR}${PATH_WS}"
echo "Calling Go WebSocket client..."

go run test-websocket.go -addr "$ADDR" -path "$PATH_WS" -count "$COUNT"

echo ""
echo "Test complete. Check storage-service CLI for results:"
echo "  > find-by-user --userId user-1"
echo "  > stats"
