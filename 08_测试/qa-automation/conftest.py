import pytest
import requests

BASE_URL = "http://localhost:8080"

@pytest.fixture(scope="session")
def auth_token():
    """登录返回 token。qa_user 由 step06 冒烟预置。"""
    r = requests.post(f"{BASE_URL}/api/auth/login", json={
        "username": "qa_user",
        "password": "qa_pass_123"
    }, timeout=5)
    assert r.status_code == 200
    body = r.json()
    assert body["code"] == 0, f"login failed: {body}"
    return body["data"]["token"]

@pytest.fixture
def auth_headers(auth_token):
    return {"Authorization": f"Bearer {auth_token}"}
