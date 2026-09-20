def test_echo_returns_message(client):
    response = client.post("/echo", json={"message": "hello"})
    assert response.status_code == 200
    assert response.json() == {"echoed": "hello"}


def test_echo_rejects_missing_field(client):
    response = client.post("/echo", json={})
    assert response.status_code == 422  # FastAPI validation error
