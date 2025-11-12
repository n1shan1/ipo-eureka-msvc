#!/bin/bash

# Git Pre-Push Verification Script for IPO Microservices
# This script helps verify your repository is clean before pushing to GitHub

echo "🔍 IPO Microservices - Git Pre-Push Verification"
echo "================================================"
echo ""

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Track issues
ISSUES=0

# 1. Check if git is initialized
echo "1️⃣  Checking Git initialization..."
if [ -d .git ]; then
    echo -e "${GREEN}✓${NC} Git repository initialized"
else
    echo -e "${RED}✗${NC} Not a git repository. Run: git init"
    exit 1
fi
echo ""

# 2. Check for large files
echo "2️⃣  Checking for large files (>5MB)..."
LARGE_FILES=$(find . -type f -size +5M -not -path "*/\.*" -not -path "*/target/*" -not -path "*/node_modules/*" 2>/dev/null)
if [ -z "$LARGE_FILES" ]; then
    echo -e "${GREEN}✓${NC} No large files found"
else
    echo -e "${YELLOW}⚠${NC}  Large files detected:"
    echo "$LARGE_FILES"
    echo "   Consider using Git LFS or excluding these files"
    ISSUES=$((ISSUES + 1))
fi
echo ""

# 3. Check for sensitive patterns
echo "3️⃣  Checking for sensitive information..."
SENSITIVE=$(git diff --cached 2>/dev/null | grep -i -E "password.*=.*[^postgres]|api[_-]?key|secret[_-]?key|private[_-]?key|token.*=|aws[_-]?access" || true)
if [ -z "$SENSITIVE" ]; then
    echo -e "${GREEN}✓${NC} No obvious sensitive data patterns found"
else
    echo -e "${RED}✗${NC} Potential sensitive data detected:"
    echo "$SENSITIVE"
    echo "   Review and remove before pushing!"
    ISSUES=$((ISSUES + 1))
fi
echo ""

# 4. Check .gitignore exists
echo "4️⃣  Checking .gitignore files..."
GITIGNORE_COUNT=0
if [ -f .gitignore ]; then
    GITIGNORE_COUNT=$((GITIGNORE_COUNT + 1))
fi
for dir in api-gateway service-registry ipo-application-service ipo-payment-service ipo-allotment-service ipo-notification-service common-dto; do
    if [ -f "$dir/.gitignore" ]; then
        GITIGNORE_COUNT=$((GITIGNORE_COUNT + 1))
    fi
done
echo -e "${GREEN}✓${NC} Found $GITIGNORE_COUNT .gitignore files (expected: 8)"
echo ""

# 5. Check if target directories are ignored
echo "5️⃣  Checking if build artifacts are ignored..."
TARGET_IN_GIT=$(git status --short 2>/dev/null | grep -c "target/" || true)
if [ "$TARGET_IN_GIT" -eq 0 ]; then
    echo -e "${GREEN}✓${NC} Build artifacts (target/) are properly ignored"
else
    echo -e "${RED}✗${NC} target/ directories found in git status"
    echo "   This shouldn't happen with proper .gitignore"
    ISSUES=$((ISSUES + 1))
fi
echo ""

# 6. Check if IDE files are ignored
echo "6️⃣  Checking if IDE files are ignored..."
IDE_IN_GIT=$(git status --short 2>/dev/null | grep -E "\.idea/|\.iml|\.vscode/" || true)
if [ -z "$IDE_IN_GIT" ]; then
    echo -e "${GREEN}✓${NC} IDE files are properly ignored"
else
    echo -e "${YELLOW}⚠${NC}  IDE files found in git status:"
    echo "$IDE_IN_GIT"
    echo "   Consider adding to .gitignore"
    ISSUES=$((ISSUES + 1))
fi
echo ""

# 7. Check for .DS_Store files (macOS)
echo "7️⃣  Checking for OS-specific files..."
DS_STORE=$(find . -name ".DS_Store" -not -path "*/\.*" 2>/dev/null)
if [ -z "$DS_STORE" ]; then
    echo -e "${GREEN}✓${NC} No .DS_Store files found"
else
    echo -e "${YELLOW}⚠${NC}  .DS_Store files found (should be gitignored):"
    echo "$DS_STORE"
fi
echo ""

# 8. Check if environment files are committed
echo "8️⃣  Checking for environment files..."
ENV_FILES=$(git status --short 2>/dev/null | grep -E "\.env$|application-local\." || true)
if [ -z "$ENV_FILES" ]; then
    echo -e "${GREEN}✓${NC} No environment files in git"
else
    echo -e "${RED}✗${NC} Environment files should not be committed:"
    echo "$ENV_FILES"
    ISSUES=$((ISSUES + 1))
fi
echo ""

# 9. Check staged files count
echo "9️⃣  Checking staged files..."
STAGED_COUNT=$(git diff --cached --name-only 2>/dev/null | wc -l | tr -d ' ')
if [ "$STAGED_COUNT" -eq 0 ]; then
    echo -e "${YELLOW}⚠${NC}  No files staged for commit"
    echo "   Run: git add ."
else
    echo -e "${GREEN}✓${NC} $STAGED_COUNT files staged for commit"
    echo ""
    echo "   Staged file types:"
    git diff --cached --name-only 2>/dev/null | sed 's/.*\./  - ./' | sort | uniq -c
fi
echo ""

# 10. Check branch name
echo "🔟  Checking branch..."
BRANCH=$(git branch --show-current 2>/dev/null)
if [ -z "$BRANCH" ]; then
    echo -e "${YELLOW}⚠${NC}  No branch found (detached HEAD?)"
else
    echo -e "${GREEN}✓${NC} Current branch: $BRANCH"
fi
echo ""

# Summary
echo "================================================"
echo "📊 Summary"
echo "================================================"
if [ $ISSUES -eq 0 ]; then
    echo -e "${GREEN}✓ All checks passed!${NC}"
    echo ""
    echo "Ready to push! Run:"
    echo "  git commit -m 'Your commit message'"
    echo "  git push -u origin main"
else
    echo -e "${RED}✗ Found $ISSUES issue(s)${NC}"
    echo ""
    echo "Please fix the issues above before pushing."
    exit 1
fi
echo ""

# Optional: Show what will be committed
read -p "Show detailed list of staged files? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo ""
    echo "📄 Staged files:"
    git diff --cached --name-status | head -50
fi
