import requests
from conftest import BASE_URL

class TestCustomer:
    def test_create_and_get(self, auth_headers):
        r = requests.post(f"{BASE_URL}/api/customers",
                         json={"name": "测试客户", "contact": "张三", "phone": "13800000000"},
                         headers=auth_headers)
        assert r.status_code == 200
        body = r.json()
        assert body["code"] == 0
        cid = body["data"]["id"]

        # 查回来
        r2 = requests.get(f"{BASE_URL}/api/customers/{cid}", headers=auth_headers)
        assert r2.status_code == 200
        assert r2.json()["data"]["name"] == "测试客户"

    def test_list_empty_or_has_data(self, auth_headers):
        r = requests.get(f"{BASE_URL}/api/customers", headers=auth_headers)
        assert r.status_code == 200
        assert r.json()["code"] == 0
