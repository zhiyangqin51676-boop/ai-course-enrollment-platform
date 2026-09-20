# Runbook · How to get your Groq API key

> This is what you need before Lesson 4. **Every student needs their
> own key** — do not share, do not commit.

## Why Groq?

- **Free tier:** 14,400 requests / day. You cannot exhaust this during the bootcamp.
- **Fast:** answers stream back in ~1 second (LPU inference chip).
- **OpenAI-compatible:** if you ever switch to another provider later, only `.env` changes.

## Prerequisites

- A Gmail (or any email) account
- 3 minutes

## Steps

### 1 · Sign up

1. Open https://console.groq.com/
2. Click **Sign Up** (top right)
3. Choose **Continue with Google** for the fastest path, or use email + password
4. Verify your email if prompted (check spam if it doesn't arrive in 2 minutes)

### 2 · Create an API key

1. After sign-in, look at the left sidebar → click **API Keys**
2. Click **Create API Key**
3. Give it a name so future-you remembers what it's for:
   - Suggested: `uplus-bootcamp-2026`
4. **Copy the key immediately** — it starts with `gsk_` and is shown **only once**
5. Paste it into a scratch note temporarily (Notes.app, Sublime, whatever). You'll move it into `.env` next.

> **Lost the key?** Fine — go back, **delete that key**, create a new one. Never leave orphan keys — they're a security hole.

### 3 · Put the key in `.env`

From your `ustudent-ai` repo root:

```bash
# First time only: create your local .env from the example
cp .env.example .env
```

Open `.env` in your editor and edit:

```
LLM_API_KEY=gsk_paste_your_key_here
LLM_BASE_URL=https://api.groq.com/openai/v1
LLM_MODEL=llama-3.3-70b-versatile
```

**No quotes around the key** — pydantic reads them literally and your requests will 401.

### 4 · Confirm `.env` is NOT committed

```bash
git status
```

`.env` should **not** appear. If it does, something's wrong with `.gitignore`. Fix:

```bash
git rm --cached .env    # only if it was already tracked
```

The `.gitignore` in this repo already lists `.env`. Do not remove that line.

### 5 · Verify the key works

Start the service from the repo root:

```bash
uvicorn app.main:app --reload --port 8000
```

You should NOT see:
```
LLMNotConfigured: LLM_API_KEY is not set
```

Then open http://localhost:8000/docs → **POST /health** — should return `{"status":"ok"}`.

For a real LLM check (Lesson 4 onwards, once `/ask` is wired):

```bash
curl -X POST http://localhost:8000/ask \
    -H 'Content-Type: application/json' \
    -d '{"question": "Say hi in five words."}'
```

You should get a JSON response with an `answer` field.

## Troubleshooting

| Symptom | Fix |
|---|---|
| `LLMNotConfigured: LLM_API_KEY is not set` | `.env` missing, or you started `uvicorn` from wrong directory. Start from repo root. |
| `401 Unauthorized` | Key is wrong. Common causes: pasted with quotes, extra whitespace, or you copied only part of it. Regenerate a new key on Groq dashboard. |
| `429 Too Many Requests` | Free tier is 30 requests / minute. Slow down, or wait 30 seconds. The retry in `app/llm.py` handles this automatically. |
| No verification email received | Check spam. Try Gmail if you used a school/company address. |
| Signup blocked in your region | Ask your instructor for a fallback. Options: OpenRouter (requires small credit card top-up), or borrow shared temporary access for the class. |

## Security ground rules

1. **Never commit `.env`.** `git status` should not show it, ever.
2. **Never paste your key** in Slack, Discord, screenshots, or chat with the instructor. If someone needs to see how it fails, share the **error message**, not the key.
3. **If you leaked a key** (posted in chat, screenshot, pushed to git): go to Groq dashboard immediately → delete the key → create a new one. Don't wait.
4. **One key per student.** Do not share with classmates — free-tier quota is per key.
