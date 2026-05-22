# ADR-001: Engine Selection — BlackBox Fork

## Status
Accepted

## Context
We need a virtualization engine to run multiple isolated instances of Android apps. Three approaches were evaluated:

| Approach | Pros | Cons |
|----------|------|------|
| Fork BlackBox (Apache 2.0) | Proven architecture, fast MVP | Last updated Nov 2022, no Android 13+ |
| Build from scratch | Full control | 12-18 months minimum |
| Hybrid (Fork + Rewrite) | Best of both worlds | Requires deep understanding |

## Decision
Fork BlackBox as the base engine, apply NewBlackbox's Android 13+ patches, then progressively modernize with Kotlin/C++ rewrites.

## Strategy
1. Fork `BlackBoxing/BlackBox` (Apache 2.0)
2. Study and apply `ALEX5402/NewBlackbox` patches for Android 13-15 compatibility
3. Build Flutter UI layer on top
4. Incrementally rewrite engine components in modern Kotlin/C++

## Consequences
- Faster time to MVP (proven core engine)
- Technical debt from legacy Java codebase
- Need to maintain compatibility across Android 10-15
- Must ensure Apache 2.0 compliance (no VirtualApp proprietary code leakage)
