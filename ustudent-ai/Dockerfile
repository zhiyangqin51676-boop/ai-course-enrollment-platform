# Build for amd64 explicitly so it can ship to AWS ECS from ARM Macs.
# (Lesson 10 pins --platform linux/amd64 on the build command; lesson 11's
# CI/CD pipeline does the same.)
FROM python:3.11-slim

ENV PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    PIP_NO_CACHE_DIR=1

WORKDIR /app

RUN apt-get update && apt-get install -y --no-install-recommends \
        curl \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
# Install CPU-only torch FIRST. sentence-transformers (a RAG dependency) would
# otherwise pull the default CUDA build of torch (~6 GB of nvidia-* libs) —
# Fargate has no GPU, so that's pure bloat that would push the image past 9 GB.
# Pre-installing the CPU wheel keeps the image ~2.5 GB.
RUN pip install --index-url https://download.pytorch.org/whl/cpu torch
RUN pip install -r requirements.txt

# Warm the Chroma default embedding model (~80 MB, all-MiniLM-L6-v2) into the
# image so students' first /rag-ask isn't a 30-60s HuggingFace download that
# often times out on flaky classroom wifi. Runs a throwaway add+query which
# forces the embedding function to fetch + cache the model files.
RUN python -c "import chromadb; c = chromadb.EphemeralClient(); \
    col = c.create_collection('warmup'); \
    col.add(documents=['warmup'], ids=['1']); \
    col.query(query_texts=['warmup'], n_results=1); \
    print('embedding model cached')"

COPY app/ ./app/
COPY data/ ./data/
# Your portfolio-1/2 code lives in scripts/ (scripts/rag.py etc.). The routes
# under app/ import it, so it MUST be in the image or the container crashes
# with "ModuleNotFoundError: No module named 'scripts'".
COPY scripts/ ./scripts/

# Bake the handbook vector index into the image so /rag-ask works on first
# request (the embedding model was already cached above). Skipped gracefully
# if you haven't built the RAG yet (search_handbook.py absent).
RUN if [ -f scripts/search_handbook.py ]; then \
        python scripts/search_handbook.py build ; \
    else \
        echo "no scripts/search_handbook.py — skipping index build" ; \
    fi

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD curl -f http://localhost:8000/health || exit 1

CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
