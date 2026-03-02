# AGENTS

Last verified: 2026-03-01

## Startup Scan Policy
- Read real files from the current worktree before making any change.
- Cover docs, build/config, platform files, core runtime sources, and tests when present.
- Use evidence-based conclusions only; label missing facts as `UNKNOWN`.
- Never infer architecture from one file.
- No changes before original context is understood.

## Architecture Summary
- Platform: Android app module (`:app`) built with Gradle/AGP.
- Language: Java source in `app/src/main/java`.
- Main entrypoint: `com.monochrome.monochrome_player.MainActivity` launched from `AndroidManifest.xml`.
- UI: XML layouts (`activity_main.xml`, `full_player.xml`) with `NavigationRailView`, `RecyclerView`, bottom-sheet/full player surfaces.
- Domain models: `Song`, `Artist`, `Album`, `Playlist`.
- Adapters: `SongAdapter`, `GenericListAdapter`, `PlaylistAdapter`.
- Persistence/settings: `SettingsManager` via `SharedPreferences` (theme, sort mode, folders, playlists JSON).
- Playback: `MediaPlayer` + media notification/session wiring inside `MainActivity`.

## Minimal Change Policy
- Make the smallest safe change that solves the requested task.
- Preserve existing structure, naming, and behavior unless change scope requires otherwise.
- Avoid unrelated refactors and formatting churn.
- Validate with the narrowest relevant build/test command first.

## Unknowns Policy
- If required behavior/config cannot be proven from repository files, mark as `UNKNOWN`.
- Do not invent commands, modules, or runtime behavior.
- Escalate unresolved assumptions explicitly before broad changes.
