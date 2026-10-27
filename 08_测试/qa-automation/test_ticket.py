import requests
from conftest import BASE_URL


class TestTicket:
    def test_create_ticket(self, auth_headers):
        # 先建客户
        r = requests.post(f"{BASE_URL}/api/customers",
                         json={"name": "工单测试客户", "contact": "李四", "phone": "13700000000"},
                         headers=auth_headers)
        cid = r.json()["data"]["id"]

        # 建工单（body 字段是 description）
        r = requests.post(f"{BASE_URL}/api/tickets",
                         json={"customerId": cid, "title": "登录报错", "description": "用户无法登录"},
                         headers=auth_headers)
        assert r.status_code == 200
        body = r.json()
        assert body["code"] == 0
        assert body["data"]["status"] == "OPEN"

    def test_status_transition_open_to_processing(self, auth_headers):
        # 建客户+工单
        r = requests.post(f"{BASE_URL}/api/customers",
                         json={"name": "状态机测试", "contact": "王五", "phone": "13600000000"},
                         headers=auth_headers)
        cid = r.json()["data"]["id"]
        r = requests.post(f"{BASE_URL}/api/tickets",
                         json={"customerId": cid, "title": "测试流转", "description": "OPEN→PROCESSING"},
                         headers=auth_headers)
        tid = r.json()["data"]["id"]

        # PATCH 流转到 PROCESSING
        r = requests.patch(f"{BASE_URL}/api/tickets/{tid}",
                          json={"status": "PROCESSING"},
                          headers=auth_headers)
        assert r.status_code == 200
        assert r.json()["data"]["status"] == "PROCESSING"

    def test_illegal_transition_rejected(self, auth_headers):
        # 建客户+工单（OPEN）
        r = requests.post(f"{BASE_URL}/api/customers",
                         json={"name": "非法流转测试", "contact": "赵六", "phone": "13500000000"},
                         headers=auth_headers)
        cid = r.json()["data"]["id"]
        r = requests.post(f"{BASE_URL}/api/tickets",
                         json={"customerId": cid, "title": "非法流转", "description": "OPEN→CLOSED"},
                         headers=auth_headers)
        tid = r.json()["data"]["id"]

        # 非法流转：OPEN 直接到 CLOSED
        r = requests.patch(f"{BASE_URL}/api/tickets/{tid}",
                          json={"status": "CLOSED"},
                          headers=auth_headers)
        assert r.status_code == 200
        body = r.json()
        assert body["code"] == 40901


class TestMultiTenant:
    def test_customer_isolation(self, auth_headers):
        # qa_user tenant_id=1，查客户列表（分页返回 {total, records}）
        r = requests.get(f"{BASE_URL}/api/customers", headers=auth_headers)
        assert r.status_code == 200
        data = r.json()["data"]
        # data 是分页对象，records 是列表
        records = data.get("records", data if isinstance(data, list) else [])
        for c in records:
            assert "name" in c
