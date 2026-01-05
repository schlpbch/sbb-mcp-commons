#!/bin/bash

# Interactive MCP Test Script
# Usage: ./mcp-interactive-test.sh [SERVER_URL]
# Default SERVER_URL is http://localhost:8080/mcp/

SERVER_URL="${1:-http://localhost:8080/mcp/}"
SESSION_ID=""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${BLUE}╔════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║   🚂 MCP Interactive Test Client      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════╝${NC}"
echo -e "Server: ${SERVER_URL}"
echo ""

function initialize() {
    echo -e "${YELLOW}→ Initializing connection...${NC}"
    
    RESPONSE=$(curl -s -X POST "$SERVER_URL" \
        -H "Content-Type: application/json" \
        -d '{"jsonrpc":"2.0","id":1,"method":"initialize","params":{"protocolVersion":"2024-11-05","clientInfo":{"name":"Interactive CLI","version":"1.0.0"},"capabilities":{}}}' \
        -D -)
    
    SESSION_ID=$(echo "$RESPONSE" | grep -i "Mcp-Session-Id:" | cut -d' ' -f2 | tr -d '\r')
    
    if [ -n "$SESSION_ID" ]; then
        echo -e "${GREEN}✓ Connected!${NC}"
        echo -e "${GREEN}Session ID: $SESSION_ID${NC}"
        echo ""
        echo "$RESPONSE" | tail -1 | jq .
    else
        echo -e "${RED}✗ Failed to connect${NC}"
        echo "$RESPONSE"
    fi
    echo ""
}

function list_tools() {
    if [ -z "$SESSION_ID" ]; then
        echo -e "${RED}✗ Please initialize first (option 1)${NC}"
        return
    fi
    
    echo -e "${YELLOW}→ Listing tools...${NC}"
    
    RESPONSE=$(curl -s -X POST "$SERVER_URL" \
        -H "Content-Type: application/json" \
        -H "Mcp-Session-Id: $SESSION_ID" \
        -d '{"jsonrpc":"2.0","id":2,"method":"tools/list","params":{}}')
    
    TOOL_COUNT=$(echo "$RESPONSE" | jq -r '.result.tools | length')
    
    echo -e "${GREEN}✓ Found $TOOL_COUNT tools:${NC}"
    echo "$RESPONSE" | jq -r '.result.tools[].name' | nl
    echo ""
}

function list_prompts() {
    if [ -z "$SESSION_ID" ]; then
        echo -e "${RED}✗ Please initialize first (option 1)${NC}"
        return
    fi
    
    echo -e "${YELLOW}→ Listing prompts...${NC}"
    
    RESPONSE=$(curl -s -X POST "$SERVER_URL" \
        -H "Content-Type: application/json" \
        -H "Mcp-Session-Id: $SESSION_ID" \
        -d '{"jsonrpc":"2.0","id":3,"method":"prompts/list","params":{}}')
    
    PROMPT_COUNT=$(echo "$RESPONSE" | jq -r '.result.prompts | length')
    
    echo -e "${GREEN}✓ Found $PROMPT_COUNT prompt(s):${NC}"
    echo "$RESPONSE" | jq -r '.result.prompts[].name' | nl
    echo ""
}

function list_resources() {
    if [ -z "$SESSION_ID" ]; then
        echo -e "${RED}✗ Please initialize first (option 1)${NC}"
        return
    fi
    
    echo -e "${YELLOW}→ Listing resources...${NC}"
    
    RESPONSE=$(curl -s -X POST "$SERVER_URL" \
        -H "Content-Type: application/json" \
        -H "Mcp-Session-Id: $SESSION_ID" \
        -d '{"jsonrpc":"2.0","id":4,"method":"resources/list","params":{}}')
    
    RESOURCE_COUNT=$(echo "$RESPONSE" | jq -r '.result.resources | length')
    
    echo -e "${GREEN}✓ Found $RESOURCE_COUNT resource(s):${NC}"
    echo "$RESPONSE" | jq -r '.result.resources[].name' | nl
    echo ""
}

function show_all() {
    list_tools
    list_prompts
    list_resources
}

# Main menu loop
while true; do
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo "1) Initialize connection"
    echo "2) List tools"
    echo "3) List prompts"
    echo "4) List resources"
    echo "5) Show all"
    echo "6) Exit"
    echo -e "${BLUE}═══════════════════════════════════════${NC}"
    echo -n "Choose an option: "
    read -r choice
    echo ""
    
    case $choice in
        1) initialize ;;
        2) list_tools ;;
        3) list_prompts ;;
        4) list_resources ;;
        5) show_all ;;
        6) echo "Goodbye!"; exit 0 ;;
        *) echo -e "${RED}Invalid option${NC}"; echo "" ;;
    esac
done
