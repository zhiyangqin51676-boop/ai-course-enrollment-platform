import React, { useEffect, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext';

// Simple chat UI over the ustudent-ai /agent-chat endpoint.
// nginx forwards /ai/agent-chat -> ustudent-ai:8000/agent-chat
const AI_CHAT_URL = '/ai/agent-chat';

// One thread_id per browser session. Refresh page = new conversation.
// This matches how the agent's MemorySaver keys memory per thread_id.
function newThreadId(userId) {
  const rand = Math.random().toString(36).slice(2, 8);
  return `user-${userId || 'anon'}-${Date.now()}-${rand}`;
}

const SUGGESTIONS = [
  'How many credits do I need to graduate?',
  'What is CS101 about?',
  'Can I take CS101 and MATH101 together?',
  'Sign me up for CS101.',
  // NOTE: CS201 asks for CS101 as a prerequisite, so "sign me up for CS201"
  // fails for a fresh student. Use CS101 as the demo path — the AI Chat
  // still gets to show query -> memory ("it") -> enrol -> real backend write.
];

const AIChatPage = () => {
  const { user } = useAuth();
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      text:
        "Hi! I'm the ustudent AI assistant. I can look up courses, answer handbook / policy questions, and enrol you in a class. Try one of the suggestions below or ask me anything.",
      toolCalls: [],
    },
  ]);
  const [input, setInput] = useState('');
  const [sending, setSending] = useState(false);
  const [threadId] = useState(() => newThreadId(user?.id));
  const scrollRef = useRef(null);

  useEffect(() => {
    // Scroll to bottom on new message.
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: 'smooth' });
  }, [messages, sending]);

  const send = async (raw) => {
    const text = (raw ?? input).trim();
    if (!text || sending) return;
    setInput('');

    // Inject the student_id hint so the agent's `enrol` tool knows who to sign up.
    const enriched =
      user?.id != null
        ? `${text}\n\n(context: my student_id is ${user.id})`
        : text;

    setMessages((m) => [...m, { role: 'user', text, toolCalls: [] }]);
    setSending(true);

    try {
      const res = await fetch(AI_CHAT_URL, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ message: enriched, thread_id: threadId }),
      });

      // The server SHOULD always return JSON (see app/main.py exception
      // handlers). But if an unhandled exception path slips through, uvicorn
      // returns plain text "Internal Server Error" — trying to res.json()
      // would throw "Unexpected token 'I'..." and crash the UI. Read raw
      // text first, then try to parse.
      const raw = await res.text();
      let data = null;
      try { data = raw ? JSON.parse(raw) : null; } catch { /* keep data=null */ }

      if (!res.ok) {
        const isConfig = data?.code === 'llm_not_configured';
        const text = isConfig
          ? `⚙️ The AI service isn't configured yet.\n\n${data.detail}\n\nYou can still explore the /rag-ask fallback path and the rest of the app while you wait for Lesson 3.`
          : `Sorry, the agent returned an error (${res.status}). ${data?.detail || raw?.slice(0, 200) || ''}`;
        setMessages((m) => [
          ...m,
          { role: 'assistant', error: true, text, toolCalls: [] },
        ]);
      } else {
        setMessages((m) => [
          ...m,
          {
            role: 'assistant',
            text: data?.answer || '(empty reply)',
            toolCalls: data?.tool_calls || [],
          },
        ]);
      }
    } catch (e) {
      setMessages((m) => [
        ...m,
        {
          role: 'assistant',
          error: true,
          text: `Network error: ${e.message}`,
          toolCalls: [],
        },
      ]);
    } finally {
      setSending(false);
    }
  };

  const onKeyDown = (e) => {
    // Enter to send, Shift+Enter for newline.
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      send();
    }
  };

  return (
    <div className="container">
      <div className="card">
        <div className="ai-chat-header">
          <div>
            <h2 style={{ marginBottom: 4 }}>💬 AI Assistant</h2>
            <div style={{ color: '#666', fontSize: 14 }}>
              Backed by <code>/agent-chat</code> — 3-tool LangGraph agent with multi-turn memory.
              &nbsp;·&nbsp; thread_id: <code>{threadId}</code>
            </div>
          </div>
        </div>

        <div ref={scrollRef} className="ai-chat-scroll">
          {messages.map((m, i) => (
            <Message key={i} m={m} />
          ))}
          {sending && (
            <div className="ai-msg assistant thinking">
              <div className="ai-msg-bubble">Thinking…</div>
            </div>
          )}
        </div>

        <div className="ai-chat-suggestions">
          {SUGGESTIONS.map((s) => (
            <button
              key={s}
              className="ai-chip"
              onClick={() => send(s)}
              disabled={sending}
              title={s}
            >
              {s}
            </button>
          ))}
        </div>

        <div className="ai-chat-input-row">
          <textarea
            className="ai-chat-input"
            rows={2}
            placeholder="Ask me anything about your courses… (Enter to send, Shift+Enter for newline)"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={onKeyDown}
            disabled={sending}
          />
          <button
            className="btn btn-primary"
            onClick={() => send()}
            disabled={sending || !input.trim()}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
};

// One message + its optional tool_calls trace (collapsible).
const Message = ({ m }) => {
  const [showTrace, setShowTrace] = useState(false);
  const hasTrace = m.toolCalls?.length > 0;
  return (
    <div className={`ai-msg ${m.role} ${m.error ? 'error' : ''}`}>
      <div className="ai-msg-bubble">
        <div className="ai-msg-text">{m.text}</div>
        {hasTrace && (
          <div className="ai-msg-trace-wrap">
            <button className="ai-msg-trace-toggle" onClick={() => setShowTrace((s) => !s)}>
              {showTrace ? '▼' : '▶'} {m.toolCalls.length} tool call{m.toolCalls.length > 1 ? 's' : ''}
            </button>
            {showTrace && (
              <ul className="ai-msg-trace">
                {m.toolCalls.map((tc, i) => (
                  <li key={i}>
                    <code>{tc.name}({JSON.stringify(tc.args)})</code>
                    <div className="ai-msg-trace-result">→ {tc.result}</div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default AIChatPage;
