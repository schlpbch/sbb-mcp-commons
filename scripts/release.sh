#!/bin/bash
# Release script for sbb-mcp-commons
# Builds, tests, and installs to local Maven repository

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}SBB MCP Commons Release Script${NC}"
echo -e "${GREEN}========================================${NC}"

# Get current version from pom.xml
CURRENT_VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
echo -e "${YELLOW}Current version: ${CURRENT_VERSION}${NC}"

# Check for uncommitted changes
if [[ -n $(git status -s) ]]; then
    echo -e "${RED}Error: You have uncommitted changes. Please commit or stash them first.${NC}"
    git status -s
    exit 1
fi

# Run tests
echo -e "${YELLOW}Running tests...${NC}"
mvn clean test

# Build and install to local Maven repository
echo -e "${YELLOW}Building and installing to local Maven repository...${NC}"
mvn clean install

# Check if downstream projects exist
DOWNSTREAM_PROJECTS=(
    "../swiss-mobility-mcp"
    "../journey-service-mcp"
)

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Downstream Project Verification${NC}"
echo -e "${GREEN}========================================${NC}"

for project in "${DOWNSTREAM_PROJECTS[@]}"; do
    if [[ -d "$project" ]]; then
        echo -e "${YELLOW}Updating $project...${NC}"
        (cd "$project" && mvn clean package -DskipTests)
        if [[ $? -eq 0 ]]; then
            echo -e "${GREEN}✓ $project builds successfully${NC}"
        else
            echo -e "${RED}✗ $project failed to build${NC}"
            exit 1
        fi
    else
        echo -e "${YELLOW}⚠ $project not found (skipping)${NC}"
    fi
done

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Release Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo -e "Version ${CURRENT_VERSION} has been:"
echo -e "  ✓ Tested"
echo -e "  ✓ Installed to local Maven repository (~/.m2/repository)"
echo -e "  ✓ Verified with downstream projects"
echo ""
echo -e "${YELLOW}Next steps:${NC}"
echo -e "  1. Tag the release: git tag v${CURRENT_VERSION}"
echo -e "  2. Push the tag: git push origin v${CURRENT_VERSION}"
echo -e "  3. Create a GitHub release"
