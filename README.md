# Monochrome-Player
Monochrome-Music Player

> Project started on march 3rd 2020

> first initialize the "master" repo and start the IDE setup/sync

> then checkout on another branch first before starting

App source code is Java-only (no Kotlin sources).

## Local AI Genre Models (Recommended Options)

This app now supports local model-based genre inference via files in:

- `app files dir` + `/models/genre_small.tflite`
- `app files dir` + `/models/genre_labels.txt`

Current app behavior:

- Uses local model if present.
- Auto-installs a bundled model from APK assets (`app/src/main/assets/models/`) into app files on first run.
- Maps raw model labels to app-level genres (e.g., Rock, Metal, Electronic).
- Falls back to metadata + heuristic classification if model files are missing.

Bundled model files expected:

- `app/src/main/assets/models/genre_small.tflite`
- `app/src/main/assets/models/genre_labels.txt`

Runtime model path used by the app:

- `<app-files>/models/genre_small.tflite`
- `<app-files>/models/genre_labels.txt`

On first app open, onboarding asks the user whether to run full-library AI genre analysis.

### Option A (Recommended First): YAMNet-based pipeline

- Model family: YAMNet (AudioSet event classifier)
- Why first: lightweight architecture (MobileNet-based), strong open baseline, easy to convert/use with TFLite, good for on-device iteration.
- Notes: YAMNet predicts audio events (521 classes), not direct music genres, so use label-to-genre mapping (already wired in app).

References:

- TensorFlow Models YAMNet: https://github.com/tensorflow/models/tree/master/research/audioset/yamnet
- TensorFlow Hub tutorial: https://www.tensorflow.org/hub/tutorials/yamnet

### Option B: VGGish embeddings + tiny genre head

- Model family: VGGish embeddings (embedding model), with your own small classifier head.
- Why: good control for a custom music-focused genre layer, smaller trainable component.
- Notes: many community ports exist; validate maintenance/compatibility before production.

Reference:

- Community port (archived): https://github.com/harritaylor/torchvggish

### Option C: OpenL3 embedding route

- Model family: OpenL3 embeddings + custom head.
- Why: strong semantic embeddings for experimentation and research.
- Notes: integration complexity is higher for direct Android on-device deployment than Option A.

Reference:

- OpenL3: https://github.com/marl/openl3

### What to build next (best path)

1. Start with Option A for shipping quality quickly.
2. Add user correction feedback (song -> corrected genre).
3. Train your own small classifier head on top of embeddings.
4. Ship periodic lightweight model updates.
