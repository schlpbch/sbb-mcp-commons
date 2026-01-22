# Post-MVP: Support git worktrees for parallel development

## Summary

Add support for git worktrees to enable developers to work on multiple branches simultaneously across the SBB MCP ecosystem (sbb-mcp-commons, swiss-mobility-mcp, journey-service-mcp).

## Background

Currently, our cross-repository development scripts assume a single working tree per repository. Git worktrees allow developers to check out multiple branches of the same repository in different directories, enabling:

- Parallel development on multiple features
- Easy comparison between branches
- Testing feature branches against different versions of dependencies
- Context switching without stashing changes

## Use Cases

1. **Feature Development**: Working on a feature in `swiss-mobility-mcp` while maintaining a stable version for daily work
2. **Dependency Testing**: Testing a new version of `sbb-mcp-commons` against multiple feature branches
3. **Bug Fixing**: Quickly switching to a bugfix branch while preserving work-in-progress features
4. **Code Review**: Checking out a PR branch for review while keeping main development branch active

## Proposed Changes

### 1. Update Development Scripts

Modify scripts in `sbb-mcp-commons/scripts/` to detect and support worktrees:

**`dev-setup.sh`**
- Detect if repository is using worktrees
- Support cloning into a worktree-friendly structure
- Add `--worktree` flag to enable worktree mode

**`release.sh`**
- Detect worktree paths
- Verify all downstream worktrees build successfully
- Report which worktrees pass/fail

**New: `worktree-setup.sh`**
```bash
# Create worktree structure:
# ~/code/sbb-mcp-commons/
#   ├── main/           (main branch)
#   ├── feature-a/      (feature branch)
#   └── bugfix-123/     (bugfix branch)
```

### 2. Documentation

Add `docs/WORKTREE_DEVELOPMENT.md` with:
- Git worktree primer
- Setup instructions for SBB MCP ecosystem
- Best practices
- Troubleshooting common issues

### 3. Example Workflow

```bash
# Setup worktrees
cd ~/code
./sbb-mcp-commons/scripts/worktree-setup.sh

# Create feature worktree
cd sbb-mcp-commons/main
git worktree add ../feature-compression feature/compression

# Work in the feature branch
cd ../feature-compression
mvn clean install

# Test with downstream projects
cd ~/code/swiss-mobility-mcp/main
git worktree add ../test-compression main
cd ../test-compression
mvn clean package  # Uses feature version from local .m2
```

## Benefits

- ✅ **Faster context switching** - No need to stash/commit work-in-progress
- ✅ **Parallel development** - Work on multiple features simultaneously
- ✅ **Better testing** - Test dependencies across multiple branches
- ✅ **Cleaner git history** - Fewer WIP commits
- ✅ **Easier code review** - Check out PRs without affecting main work

## Implementation Checklist

- [ ] Add worktree detection to existing scripts
- [ ] Create `worktree-setup.sh` script
- [ ] Add `--worktree` flag to `dev-setup.sh`
- [ ] Update `release.sh` to handle worktree paths
- [ ] Write `docs/WORKTREE_DEVELOPMENT.md`
- [ ] Add worktree examples to README
- [ ] Test with all three repositories
- [ ] Create video tutorial/demo

## References

- [Git Worktree Documentation](https://git-scm.com/docs/git-worktree)
- [Pro Git Book - Git Worktree](https://git-scm.com/book/en/v2/Git-Tools-Advanced-Merging)
- [Git Worktree Best Practices](https://morgan.cugerone.com/blog/how-to-use-git-worktree-and-in-a-clean-way/)

## Priority

**Post-MVP** - This is a developer experience enhancement that doesn't block core functionality. Should be implemented after:
- Core MCP protocol features are stable
- CI/CD pipelines are established
- Basic cross-repository workflow is documented

## Estimated Effort

- **Development**: 1-2 days
- **Testing**: 1 day
- **Documentation**: 1 day
- **Total**: 3-4 days

---

**Labels**: `enhancement`, `developer-experience`, `post-mvp`
**Milestone**: Post-MVP Enhancements
