import requests
from conftest import BASE_URL

class TestAuth:
    def test_login_success(self):
        r = requests.post(f"{BASE_URL}/api/auth/login", json={
            "username": "qa_user", "password": "qa_pass_123"
        })
        assert r.status_code == 200
        body = r.json()
        assert body["code"] == 0
        assert "token" in body["data"]

    def test_login_wrong_password(self):
        r = requests.post(f"{BASE_URL}/api/auth/login", json={
            "username": "qa_user", "password": "wrong"
        })
        body = r.json()
        assert body["code"] != 0

    def test_no_token_rejected(self):
        r = requests.get(f"{BASE_URL}/api/customers")
        assert r.status_code == 401
