# Focused corrupt continuation and exact-unused-backup recovery

Accepted evidence: reports/native-corrupt-continuation-recovery-focused-evidence.json. Runtime 7094378e and its real new-process successor dec346f3 both report observer failures 0, Java exit 0 and normal all-dimension save. Stage5 revalidates 304 cumulative input hashes. Candidate 2ca083af remains unpromoted and NOT-INSTALLABLE; the adopted cumulative receipt remains unchanged.

## Four rejection cases

The first runtime boots a copied, coherently normally saved paid bow world with only the final HMAC byte of its copied continuation document flipped before startup. Its backup is the exact original authentic unused committed document. Original player file, whole item, wear state and active matching lineage remain unchanged. Original store authentication rejects the damaged MAC. The observer then tests a 16-byte truncated document, an independently authentic original repair-journal document placed under the continuation domain and an absent document. It never signs a new document or changes a lineage field. The wrong-domain input is authentic for its own original journal owner but invalid for the continuation owner.

Each case calls actual original PlayerDataStorage.load twice on the original server thread. All eight calls materialize the unchanged coherent original whole item but grant neither lease nor repair generation. Each rejection preserves the exact corrupt bytes or absence, every other signed authority .bin file, original player bytes and wear bytes. The target path is excluded only after canonical absolute normalization; it is checked independently by exact hash or absence.

## Explicit recovery, not automatic product repair

Only after proving every rejection leaves the predecessor active and matching does the test restore the exact previously unused authentic backup bytes. This is an explicit isolated test intervention, not automatic product backup recovery, forged authority, replay of a consumed backup, reset of retired state or reopened CLAIM. Actual original load then reserves/consumes the authentic ticket and issues a genuine non-null successor on the same epoch, generation 0 to 1. A second original load of that consumed player save grants no authority and leaves the first successor and consumed document unchanged. Actual original save commits a fresh continuation; the process stops and saves normally.

Runtime dec346f3 starts from an independently hash-verified copy of that normally saved recovery world. Actual original load obtains a genuine non-null successor on the same epoch, generation 1 to 2, with exact whole-item and wear coherence. Its consumed-file replay grants no authority and changes neither the first successor nor authenticated lineage/document. Its actual original post-restart save commits, then all dimensions save and the process exits normally. No runtime observer writes authority files in this second process.

## Preserved unsuccessful fixture

Earlier runtime c7c158b7 rejected corrupt inputs in product code and stopped/saved normally, but observer failures 1 prevent acceptance. The first observer compared relative Files.walk entries against an absolute excluded-document path; the deliberately changed document was erroneously included among all-other-authority invariants. The separate immutable canonical-path fixture corrects that comparison and retains the independent exact target-byte/absence invariant. No production check was removed and no product source/class was changed.

## Remaining scope

These checks do not claim a complete lifecycle audit, arbitrary backup recovery, authority-key corruption recovery, real client or negotiated authenticated multiplayer acceptance. Frozen repair/payment/signed Sessions, restarts, pending-save failure, active-CAS crash and JVM-definition proofs are reused without rerunning them. No formal instance is deployed or modified.
