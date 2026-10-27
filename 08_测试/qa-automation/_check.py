import requests, sys
try:
    r = requests.post('http://localhost:8080/api/auth/login',
                      json={'username':'qa_user','password':'qa_pass_123'}, timeout=5)
    print(f"STATUS={r.status_code} BODY={r.json()}")
except Exception as e:
    print(f"ERROR: {e}")
    sys.exit(1)
