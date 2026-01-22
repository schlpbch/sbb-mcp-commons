#!/bin/bash
# Development setup script for SBB MCP ecosystem
# Clones all repositories and sets up local development environment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

WORKSPACE_DIR="${1:-$HOME/code}"
GITHUB_USER="${2:-schlpbch}"

echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}SBB MCP Ecosystem Development Setup${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Workspace: ${YELLOW}${WORKSPACE_DIR}${NC}"
echo -e "GitHub User: ${YELLOW}${GITHUB_USER}${NC}"
echo ""

# Create workspace directory if it doesn't exist
mkdir -p "$WORKSPACE_DIR"
cd "$WORKSPACE_DIR"

# Repository configuration
declare -A REPOS=(
    ["sbb-mcp-commons"]="Core library with MCP protocol implementation, security, and utilities"
    ["swiss-mobility-mcp"]="Swiss Mobility ticketing and booking service"
    ["journey-service-mcp"]="SBB journey planning and real-time information service"
)

echo -e "${GREEN}Cloning/updating repositories...${NC}"
echo ""

for repo in "${!REPOS[@]}"; do
    echo -e "${YELLOW}${repo}${NC}: ${REPOS[$repo]}"

    if [[ -d "$repo" ]]; then
        echo -e "  ℹ Already exists, pulling latest changes..."
        (cd "$repo" && git pull)
    else
        echo -e "  ⬇ Cloning..."
        git clone "git@github.com:${GITHUB_USER}/${repo}.git"
    fi
    echo ""
done

echo -e "${GREEN}Building sbb-mcp-commons...${NC}"
(cd sbb-mcp-commons && mvn clean install -DskipTests)

echo -e "${GREEN}Verifying downstream projects...${NC}"
for project in swiss-mobility-mcp journey-service-mcp; do
    if [[ -d "$project" ]]; then
        echo -e "${YELLOW}Building ${project}...${NC}"
        (cd "$project" && mvn clean package -DskipTests)
    fi
done

echo ""
echo -e "${BLUE}========================================${NC}"
echo -e "${BLUE}Development Environment Ready!${NC}"
echo -e "${BLUE}========================================${NC}"
echo ""
echo -e "Repository locations:"
for repo in "${!REPOS[@]}"; do
    echo -e "  ${GREEN}${repo}${NC}: ${WORKSPACE_DIR}/${repo}"
done
echo ""
echo -e "${YELLOW}Development workflow:${NC}"
echo -e "  1. Make changes to sbb-mcp-commons"
echo -e "  2. Run: cd sbb-mcp-commons && ./scripts/release.sh"
echo -e "  3. Downstream projects automatically pick up the new version"
echo ""
echo -e "${YELLOW}Useful commands:${NC}"
echo -e "  Update all repos:     ${WORKSPACE_DIR}/sbb-mcp-commons/scripts/update-all.sh"
echo -e "  Build all projects:   ${WORKSPACE_DIR}/sbb-mcp-commons/scripts/build-all.sh"
echo -e "  Run tests:            ${WORKSPACE_DIR}/sbb-mcp-commons/scripts/test-all.sh"
