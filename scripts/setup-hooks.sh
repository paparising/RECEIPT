#!/bin/bash
# Setup script to install pre-commit hooks

set -e

HOOK_DIR=".git/hooks"
PROJECT_ROOT=$(git rev-parse --show-toplevel)
cd "$PROJECT_ROOT"

echo "Setting up pre-commit hooks..."

# Make hook executable
chmod +x "$HOOK_DIR/pre-commit"

# Make wrapper executable
if [ -f "scripts/git-commit" ]; then
	chmod +x "scripts/git-commit"
	echo "✓ Wrapper scripts/git-commit installed"
fi

# Configure local git alias so `git commit` uses the wrapper in this repo
if git rev-parse --git-dir > /dev/null 2>&1; then
  git config --local alias.commit '!f() { scripts/git-commit "$@"; }; f'
  echo "✓ Git alias 'commit' configured to use scripts/git-commit (local only)"
fi
echo "✓ Pre-commit hook installed successfully"
echo ""
echo "The hook will now run before each commit and check:"
echo "  • Code formatting and style"
echo "  • Project build status"
echo "  • Test suite execution"
echo "  • Common code issues (debug statements, etc.)"
echo ""
echo "To bypass the hook (not recommended), use:"
echo "  git commit --no-verify"
