import requests

APP_KEY = "PSN6mVDA0UCw8BZm2oYwfust4FHaguFOYaB3"
APP_SECRET = "53VbYt828WHuevvOEQHxfNEO0sivsKuU"



# =========================================================
# 1. OAuth 토큰 발급
# =========================================================

token_res = requests.post(
    "https://openapi.ls-sec.co.kr:8080/oauth2/token",
    headers={
        "content-type": "application/x-www-form-urlencoded"
    },
    data={
        "grant_type": "client_credentials",
        "appkey": APP_KEY,
        "appsecretkey": APP_SECRET,
        "scope": "oob"
    }
)

print("토큰 발급 상태:", token_res.status_code)

if token_res.status_code != 200:
    print("토큰 발급 실패")
    print(token_res.text)
    exit()

token_data = token_res.json()

access_token = token_data["access_token"]

print("토큰 앞부분:", access_token[:20], "...")


# =========================================================
# 2. 해외선물 마스터 조회
#    TR: o3101
# =========================================================

master_res = requests.post(
    "https://openapi.ls-sec.co.kr:8080/overseas-futureoption/market-data",

    headers={
        "content-type": "application/json;charset=UTF-8",
        "authorization": f"Bearer {access_token}",
        "tr_cd": "o3101",
        "tr_cont": "N",
        "tr_cont_key": ""
    },

    json={
        "o3101InBlock": {
            "gubun": ""
        }
    }
)

print("마스터 조회 상태:", master_res.status_code)

if master_res.status_code != 200:
    print("마스터 조회 실패")
    print(master_res.text)
    exit()

data = master_res.json()

# 응답 코드 확인
print("응답 코드:", data.get("rsp_cd"))
print("응답 메시지:", data.get("rsp_msg"))


# =========================================================
# 3. 전체 종목 가져오기
# =========================================================

items = data.get("o3101OutBlock", [])

print()
print("========================================")
print("전체 종목 수:", len(items))
print("========================================")


# =========================================================
# 4. 거래소별 종목 개수
# =========================================================

print()
print("=== 거래소별 종목 수 ===")

exchanges = {}

for item in items:
    exch = item.get("ExchCd", "UNKNOWN")

    if exch not in exchanges:
        exchanges[exch] = 0

    exchanges[exch] += 1

for exch, count in exchanges.items():
    print(f"{exch}: {count}")


# =========================================================
# 5. 전체 종목 출력
# =========================================================

print()
print("========================================")
print("=== 전체 종목 ===")
print("========================================")

for item in items:

    print(
        item.get("Symbol"),
        "-",
        item.get("SymbolNm"),
        "| 거래소:",
        item.get("ExchCd"),
        "| 거래소명:",
        item.get("ExchNm"),
        "| 상품코드:",
        item.get("BscGdsCd"),
        "| 상품명:",
        item.get("BscGdsNm")
    )


# =========================================================
# 6. CME 상품만 출력
# =========================================================

print()
print("========================================")
print("=== CME 상품 ===")
print("========================================")

cme_count = 0

for item in items:

    if item.get("ExchCd") == "CME":

        cme_count += 1

        print(
            item.get("Symbol"),
            "-",
            item.get("SymbolNm"),
            "| 상품코드:",
            item.get("BscGdsCd"),
            "| 상품명:",
            item.get("BscGdsNm")
        )

print("CME 종목 수:", cme_count)


# =========================================================
# 7. NASDAQ / GOLD / OIL 검색
# =========================================================

print()
print("========================================")
print("=== NASDAQ / GOLD / OIL 검색 ===")
print("========================================")

keyword_count = 0

keywords = [
    "nasdaq",
    "gold",
    "crude",
    "oil",
    "e-mini",
    "micro"
]

for item in items:

    symbol = str(item.get("Symbol", ""))
    symbol_nm = str(item.get("SymbolNm", ""))
    bsc_gds_cd = str(item.get("BscGdsCd", ""))
    bsc_gds_nm = str(item.get("BscGdsNm", ""))

    text = (
            symbol + " "
            + symbol_nm + " "
            + bsc_gds_cd + " "
            + bsc_gds_nm
    ).lower()

    if any(keyword in text for keyword in keywords):

        keyword_count += 1

        print(
            symbol,
            "-",
            symbol_nm,
            "| 거래소:",
            item.get("ExchCd"),
            "| 상품코드:",
            bsc_gds_cd,
            "| 상품명:",
            bsc_gds_nm
        )

print("검색 결과:", keyword_count)