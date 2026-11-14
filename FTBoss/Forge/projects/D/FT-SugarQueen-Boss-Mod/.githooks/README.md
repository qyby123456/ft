# Git hooks

Enable the hooks once after cloning:

    git config core.hooksPath .githooks

- `pre-commit` runs `node tools/check_docs_policy.cjs --staged`. It rejects any commit
  that touches `docs/` unless `FTBOSS_DOCS_WRITER=1` is set for the docs-writer role.
  New docs pages must also stage `docs/README.md` in the same commit.
- One-time exception: the repository baseline import commits the pre-policy legacy docs
  with `FTBOSS_DOCS_BASELINE=1 FTBOSS_DOCS_WRITER=1`. Do not use the baseline variable
  for any later commit.
- Use `node tools/check_docs_policy.cjs` to run the structural checks without a commit.
- `git commit --no-verify` bypasses the hook. That is a process violation, not a permission.
- Path-level write denial must come from project-level agent configuration and plugins;
  this hook is the last commit-time gate for the documentation library.
