# Agent Documentation Constraints (English)

Agent-facing, English-language operational constraints for documentation work in this
repository. They bind the Lead agent, the dedicated docs-writer subagent, and every
implementation agent or subagent.

Repository-facing documentation policy (canonical for the documentation library, maintained
by the documentation maintainers together with the pages themselves):

- `docs/README.md` — navigation entry point and directory map.
- `docs/AGENTS.md` — write constraints for the `docs/` directory.
- `docs/WRITING_RULES.md` — writing and maintenance rules.

This file does not replace those pages. It adds the agent-level role, permission and
workflow rules that project-level agent configuration and harness plugins must enforce. If
this file and the repository-facing policy disagree about documentation content, the
repository-facing policy wins. The hard permission rules in section 2 always bind agents.

Related documents: [docs-writer profile](docs-writer.md),
[implementation-agent profile](implementation-agent.md), repository-root `AGENTS.md`,
`CODE_STYLE.md`.

## 1. Architecture over implementation

Documentation makes the current architecture legible: which systems exist, where their
boundaries run, who owns which state, how data moves, what must remain true, and where to
start reading code. It is written for a maintainer who did not build the feature.

Pages describe: system purpose and non-goals; module boundaries; persisted and runtime
state ownership; data flow between server, client, resources and integrations; lifecycle and
state machines; invariants and refusal paths; stable contracts such as registries, saved
data keys and network messages; extension points; short operational runbooks.

Pages must not contain: `file:line` references or line counts; code pastes or
method-by-method walkthroughs; per-constant, per-field or per-method inventories; build
output, logs, hashes or candidate identifiers; development history or "what changed"
narrative.

Documentation routes readers into the code; it does not restate it. Code, resources and
configuration are ground truth. If a page and the code disagree, the page is wrong and is
corrected or removed; the disagreement is reported to the Lead, never preserved as a
"known inaccuracy" note.

Numeric values are allowed only when a reader must know them to reason about the
architecture (a persisted format version, a hard protocol limit, a registry size that changes
a design). State such a value once, name the constant that owns it, and stop. Balance and
drop tables belong to the report content sources under `docs/`, not to architecture pages.

## 2. Ownership and hard permissions

- `docs/**` has exactly one writer: the dedicated docs-writer agent, dispatched by the Lead
  after approval. That role is defined in `.agents/docs-writer.md`.
- Implementation agents and subagents must not create, edit, move, rename or delete anything
  under `docs/**`. There is no exception for typo fixes, formatting, navigation entries,
  "small syncs", or corrections that are obviously right.
- Implementation agents submit proposals only, at `_work/docs-proposals/<topic>.md` based on
  `_work/docs-proposals/_TEMPLATE.md`. The implementation-agent profile is
  `.agents/implementation-agent.md`.
- The Lead triages each proposal against the code and decides what enters the library.
  Approval is per scope and per page, not a standing permission to write.
- The docs-writer may refuse, reshape or narrow a scope that violates this file and must say
  why in its report.
- Documentation work is its own task. It does not bundle source edits, builds, deployments,
  asset generation, or Word export.
- Proposals are working artifacts under `_work/`. They are never linked from `docs/` and do
  not become documentation by being approved.
- One writer at a time on `docs/`. If a docs-writer detects edits it did not make, it stops
  and reports before writing; last-writer-wins is a defect, not a workflow.
- A docs write performed by a non-docs-writer is a process bug even when the content is
  correct. Revert it and redo the change through the docs-writer role; do not approve the
  result retroactively.

## 3. No change logs

- Pages describe the present state. History belongs to version control. This working copy is
  currently not a Git repository, so the single change-record artifact is
  `docs/CHANGELOG.md`; that is an interim arrangement, not a licence for change logs inside
  the library.
- `docs/CHANGELOG.md` is a change-record artifact outside the architecture library. A
  documentation task does not extend it unless the Lead explicitly asks for that.
- Replacing a description means deleting the old one. No "previously", "now also",
  "as of <date>", version notes, strikethrough comparisons, or before/after tables.
- Forbidden inside the library: dated filenames, `CHANGE-*` files, handover, session, thread
  and status files, `*_FINAL`, `*_V2`, task or candidate identifiers, hash suffixes, and
  sections named "Update", "Changelog", "History" or "Migration notes".
- A page that only makes sense next to its predecessor is a task record, not documentation.

## 4. Language

- This file, the agent profiles, documentation proposals and agent delivery reports are
  English.
- Documentation content language follows `docs/WRITING_RULES.md` (currently Chinese). An
  English-language migration of the documentation library requires an explicit Lead
  decision; until then the docs-writer writes the repository's documentation language and
  must not mix languages within a page.
- Quoted in-game display names, UI strings and localized identifiers may keep their original
  language anywhere.

## 5. Roles and dispatch

- **Docs-writer** (`.agents/docs-writer.md`): the only writer of `docs/**`; read-only
  everywhere else. The Lead dispatches it with an explicit scope: pages to write, the reader
  outcome, anchors, acceptance criteria, and whether replacing an existing page is
  authorized.
- **Implementation agent** (`.agents/implementation-agent.md`): owns source/tooling work;
  never writes `docs/**`; declares `docs impact: none` or files a proposal.
- **Lead**: triages proposals, decides the documentation scope, dispatches the docs-writer,
  reviews the result against the code, and owns the delivery.
- Harness and plugins must enforce the write scopes, not just describe them.

## 6. Workflow

1. An implementation task ends with a documentation-impact declaration:
   `docs impact: none` or the path of a proposal under `_work/docs-proposals/`.
2. If impact exists, the implementation agent writes the proposal in architecture terms:
   affected area, classes/registries/contracts involved, what the documentation says today,
   why it is wrong, and the proposed change.
3. The Lead triages the proposal against the code: approve, adjust, defer, or reject. The
   Lead decides what enters the library.
4. The Lead dispatches the docs-writer with that approved scope.
5. The docs-writer reads the code read-only, writes or replaces the pages, updates
   `docs/README.md` navigation, and self-reviews against the repository-facing policy and
   this file.
6. The Lead reviews the resulting diff and either accepts it or returns concrete
   corrections.
7. The delivery report lists files written, navigation changes, verified facts, open
   questions, and what was not touched.

No documentation change ships without the Lead's review. No implementation task is complete
while its documentation impact is undeclared. Documentation changes reach the repository
only through the docs-writer role.

## 7. Legacy material and migration

- Documents that predate the repository-facing policy — dated pages, `CHANGE-*` files,
  handover/session/status notes, task reports — are legacy: not authoritative, no new
  content, and no translation, extension, reformatting or "tidying" without explicit Lead
  approval.
- Authority is expressed through `docs/README.md` navigation; legacy pages do not need to be
  edited to be demoted.
- The current canonical set is the one named by `docs/README.md` and maintained by the
  documentation maintainers (`docs/ARCHITECTURE.md`, `docs/architecture/`,
  `docs/REFERENCE_INDEX.md` as a navigation finding aid). It evolves through the repo-facing
  policy; do not freeze it in this file.
- `docs/archive/` holds retired documents and `docs/audit/` holds point-in-time audit
  reports. Both are evidence, never current architecture.
- `docs/all_boss_reference/` and `docs/boss_reference/` are content sources for generated
  boss reports, outside the architecture library; their provenance style is deliberately not
  subject to the architecture-page rules.
- Do not rename, move, delete or translate a legacy file without explicit Lead approval.
  Links, reports and other sessions may depend on it.

## 8. Enforcement

Prompt text alone does not stop a determined editor. The repository provides the following
enforcement layers:

- Machine-readable write scopes: `.agents/agents.json` declares `write` and `denyWrite`
  paths for the `docs-writer`, `implementation-agent` and `lead` roles. Project-level agent
  configuration and harness plugins must consume it (or an equivalent mapping) and deny
  `docs/**` writes by non-writer roles as a hard permission. Deny rules outrank task
  instructions.
- Commit-time gate: `.githooks/pre-commit` runs `node tools/check_docs_policy.cjs --staged`.
  It rejects any commit that stages changes under `docs/` unless `FTBOSS_DOCS_WRITER=1` is
  set for an approved docs-writer commit, and it rejects adding new docs pages without
  updating `docs/README.md`. Enable it once per clone with
  `git config core.hooksPath .githooks`. The one-time repository baseline import may use
  `FTBOSS_DOCS_BASELINE=1` together with the writer variable to commit the pre-policy legacy
  files; that exception must not be reused for later commits.
- Structural check: `node tools/check_docs_policy.cjs` verifies that the canonical pages
  exist, that every docs directory is described in `docs/README.md`, that relative links
  resolve, that canonical files have one H1 / no BOM / LF / final newline, and that
  architecture pages contain no `file:line` implementation references.
- `git commit --no-verify` can bypass the commit gate. That is a process violation, not a
  permission; the Lead must revert such a commit and redo the change through the
  docs-writer role.
- Automation catches the mechanical subset only. The Lead's review remains the authority on
  architecture, scope and truthfulness. A docs write performed by a non-docs-writer is a
  process bug even when the content is correct.

## 9. Definition of done

A documentation change is done when all of the following hold:

- The approved scope is fully covered and nothing outside it was edited.
- Every claim was verified against code, resources or configuration at write time.
- Pages describe architecture and contracts, with anchors instead of implementation detail.
- Language, encoding and formatting match the repository-facing policy
  (`docs/WRITING_RULES.md`, `docs/AGENTS.md`), and each file has one H1 and a stable name.
- `docs/README.md` navigation is updated and describes every directory.
- No change-log, dated, handover or session content was introduced, and no legacy file was
  modified.
- The delivery report lists written files, the navigation change, and every open question
  with the reason it could not be verified.
