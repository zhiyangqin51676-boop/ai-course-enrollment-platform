# data/

Mock corpus and evaluation fixtures used across lessons 4–10.

| File | Used by | What it is |
|---|---|---|
| `handbook.md` | Lesson 5 / 6 / 7 | The U+ University student handbook. Indexed by Chroma. Course codes, instructors, schedules are kept in sync with `uplusstudent/src/main/resources/db/migration/V4__Insert_sample_data.sql` so the RAG answer matches what the backend will return. |
| `courses-catalog.md` | Lesson 5 / 6 / 7 | Detailed catalogue entry for each of the five seed courses. Indexed alongside the handbook. |
| `faq.md` | Lesson 5 / 6 / 7 | Real-sounding student Q&As. Improves retrieval recall by covering the phrasing students actually use. |
| `prompts/lesson-04-test-cases.json` | Lesson 4 | 10 utterances + expected structured outputs. Used to compare prompt versions A/B/C. |
| `golden/rag-eval.json` | Lesson 7 (acceptance) / Lesson 10 (eval) | 8 questions with reference answers. 2 are intentionally not in the handbook to test the "I don't know" fallback. |

## Why these specific course codes

The five courses (CS101, CS201, MATH101, MATH201, ENG101) are the ones seeded in the backend's Flyway migrations. By aligning the RAG corpus to them, lesson 7's RAG answers and lesson 9's Agent backend calls describe the **same** courses. Students see the two halves of the bootcamp fit together.

## Built indexes

`data/chroma_db/` will be created when students run `scripts/build_index.py` in lesson 5. It is gitignored — always rebuildable from the markdown sources.
