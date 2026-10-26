#!/usr/bin/env bash
# ============================================================================
# 公告 + 友链 + 文件上传模块 端到端冒烟测试脚本
# ----------------------------------------------------------------------------
# 作用：对**已启动**的 blog-system 服务执行 24 项接口断言（游客只读、鉴权越权、
#       管理员增删改、封面上传成功/非法格式/超大文件），用于回归自测与第 9 周
#       《测试报告》证据采集。配套文档：《公告友链文件上传模块测试用例.md》。
#
# 与 smoke-test.sh 的区别：本脚本【不重启服务、不改数据库结构、不清空数据】，
#   直接对运行中的服务跑断言，新增的公告/友链会在测完后自动清理（删除）。
#
# 前置条件：
#   1. 已执行 sql/init.sql（含种子数据）并启动服务（默认 9090 端口）
#   2. 本机有 curl（Windows 10+ 自带 curl.exe 即可）
#
# 用法示例（Windows Git Bash / Linux / macOS 通用）：
#   bash scripts/smoke-test-extra.sh
#
# 可选环境变量：
#   APP_PORT   服务端口（默认 9090）
#   BASE_URL   服务地址（默认 http://localhost:$APP_PORT）
# ============================================================================
set -u
unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY

APP_PORT="${APP_PORT:-9090}"
BASE_URL="${BASE_URL:-http://localhost:$APP_PORT}"

PASS=0
FAIL=0
TMPDIR_SMOKE="$(mktemp -d)"
CA="$TMPDIR_SMOKE/admin.txt"   # 管理员 cookie
CU="$TMPDIR_SMOKE/user.txt"    # 普通用户 cookie
trap 'rm -rf "$TMPDIR_SMOKE"' EXIT

ck() { # ck 用例名 条件(0为真)
  if [ "$2" -eq 0 ]; then PASS=$((PASS+1)); echo "PASS  $1";
  else FAIL=$((FAIL+1)); echo "FAIL  $1"; fi
}

echo "=============================================="
echo " 目标服务: $BASE_URL"
echo "=============================================="

# ---------- 1. 游客只读接口 ----------
body=$(curl -s "$BASE_URL/announcements")
[ "$(echo "$body" | grep -c '"success":true')" -ge 1 ] && \
  echo "$body" | grep -q '社区评论规范' && c=0 || c=1
ck "AN-01 游客读公告列表(3条种子)" $c

body=$(curl -s "$BASE_URL/announcements/latest?limit=1")
echo "$body" | grep -q '社区评论规范' && \
  ! echo "$body" | grep -q '欢迎使用博客系统' && c=0 || c=1
ck "AN-02 游客读最新公告(limit=1)" $c

body=$(curl -s "$BASE_URL/friend-links")
p1=$(echo "$body" | grep -ob 'spring\.io' | head -1 | cut -d: -f1)
p2=$(echo "$body" | grep -ob 'mysql\.com'  | head -1 | cut -d: -f1)
p3=$(echo "$body" | grep -ob 'vuejs\.org' | head -1 | cut -d: -f1)
[ -n "$p1" ] && [ -n "$p2" ] && [ -n "$p3" ] && \
  [ "$p1" -lt "$p2" ] && [ "$p2" -lt "$p3" ] && c=0 || c=1
ck "FL-01 游客读友链(4条按sort升序)" $c

# ---------- 2. 未登录写操作被拒（拦截器真实 HTTP 401） ----------
code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/admin/announcement" \
  -H "Content-Type: application/json" -d '{"title":"x","content":"y"}')
[ "$code" = "401" ] && c=0 || c=1
ck "AN-03 未登录新增公告被拒401" $c

code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/admin/friend-link" \
  -H "Content-Type: application/json" -d '{"name":"x","url":"https://a.com"}')
[ "$code" = "401" ] && c=0 || c=1
ck "FL-02 未登录新增友链被拒401" $c

code=$(curl -s -o /dev/null -w "%{http_code}" -X POST "$BASE_URL/article/cover")
[ "$code" = "401" ] && c=0 || c=1
ck "FU-01 未登录上传封面被拒401" $c

# ---------- 3. 登录（种子账号） ----------
body=$(curl -s -c "$CA" -X POST "$BASE_URL/login" -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}')
echo "$body" | grep -q '"role":1' && c=0 || c=1
ck "AUTH-01 管理员 admin/admin123 登录" $c

body=$(curl -s -c "$CU" -X POST "$BASE_URL/login" -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"abc123456"}')
echo "$body" | grep -q '"role":0' && c=0 || c=1
ck "AUTH-02 普通用户 zhangsan/abc123456 登录" $c

body=$(curl -s -X POST "$BASE_URL/login" -H "Content-Type: application/json" \
  -d '{"username":"wangwu","password":"abc123456"}')
echo "$body" | grep -q '"code":403' && echo "$body" | grep -q '账号已被禁用' && c=0 || c=1
ck "AUTH-03 禁用账号 wangwu 登录被拒" $c

# ---------- 4. 越权（AdminInterceptor 真实 HTTP 403） ----------
code=$(curl -s -o /dev/null -w "%{http_code}" -b "$CU" -X POST "$BASE_URL/admin/announcement" \
  -H "Content-Type: application/json" -d '{"title":"x","content":"y"}')
[ "$code" = "403" ] && c=0 || c=1
ck "AN-04 普通用户新增公告被拒403" $c

# ---------- 5. 公告 CRUD（管理员） ----------
body=$(curl -s -b "$CA" -X POST "$BASE_URL/admin/announcement" \
  -H "Content-Type: application/json" -d '{"title":"测试公告-冒烟","content":"冒烟测试内容"}')
echo "$body" | grep -q '"success":true' && c=0 || c=1
ck "AN-05 管理员新增公告" $c
aid=$(echo "$body" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)

body=$(curl -s -b "$CA" -X PUT "$BASE_URL/admin/announcement/$aid" \
  -H "Content-Type: application/json" -d '{"title":"测试公告-修改后","content":"修改后内容"}')
echo "$body" | grep -q '测试公告-修改后' && c=0 || c=1
ck "AN-06 管理员修改公告" $c

body=$(curl -s -b "$CA" -X DELETE "$BASE_URL/admin/announcement/$aid")
echo "$body" | grep -q '删除公告成功' && c=0 || c=1
ck "AN-07 管理员删除公告" $c

body=$(curl -s -b "$CA" -X PUT "$BASE_URL/admin/announcement/99999" \
  -H "Content-Type: application/json" -d '{"title":"x","content":"y"}')
echo "$body" | grep -q '"code":404' && echo "$body" | grep -q '公告不存在' && c=0 || c=1
ck "AN-08 修改不存在公告返回404" $c

# 用文件传 body，避免 shell 转义吃掉空白字符
printf '{"title":"   ","content":"y"}' > "$TMPDIR_SMOKE/blank.json"
body=$(curl -s -b "$CA" -X POST "$BASE_URL/admin/announcement" \
  -H "Content-Type: application/json" --data-binary @"$TMPDIR_SMOKE/blank.json")
echo "$body" | grep -q '"code":400' && echo "$body" | grep -q '标题不能为空' && c=0 || c=1
ck "AN-09 空白标题被拒400" $c

# ---------- 6. 友链 CRUD + URL 校验（管理员） ----------
body=$(curl -s -b "$CA" -X POST "$BASE_URL/admin/friend-link" \
  -H "Content-Type: application/json" -d '{"name":"测试友链","url":"https://example.com","sort":9}')
echo "$body" | grep -q '"success":true' && c=0 || c=1
ck "FL-03 管理员新增友链" $c
fid=$(echo "$body" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)

body=$(curl -s -b "$CA" -X POST "$BASE_URL/admin/friend-link" \
  -H "Content-Type: application/json" -d '{"name":"坏链接","url":"ftp://example.com"}')
echo "$body" | grep -q '必须以 http' && c=0 || c=1
ck "FL-04 非http/https链接被拒" $c

body=$(curl -s -b "$CA" -X PUT "$BASE_URL/admin/friend-link/$fid" \
  -H "Content-Type: application/json" -d '{"name":"测试友链改","url":"https://example.com/x","sort":8}')
echo "$body" | grep -q '测试友链改' && c=0 || c=1
ck "FL-05 管理员修改友链" $c

body=$(curl -s -b "$CA" -X DELETE "$BASE_URL/admin/friend-link/$fid")
echo "$body" | grep -q '删除友链成功' && c=0 || c=1
ck "FL-06 管理员删除友链" $c

# ---------- 7. 文件上传（封面上传） ----------
# 生成 1x1 PNG（67 字节）
printf '\x89PNG\r\n\x1a\n\x00\x00\x00\rIHDR\x00\x00\x00\x01\x00\x00\x00\x01\x08\x06\x00\x00\x00\x1f\x15\xc4\x89\x00\x00\x00\nIDATx\x9cc\x00\x01\x00\x00\x05\x00\x01\r\n\x2d\xb4\x00\x00\x00\x00IEND\xaeB\x60\x82' \
  > "$TMPDIR_SMOKE/px.png"
echo "not an image" > "$TMPDIR_SMOKE/note.txt"
dd if=/dev/zero of="$TMPDIR_SMOKE/big.png" bs=1M count=6 2>/dev/null

body=$(curl -s -b "$CA" -F "file=@$TMPDIR_SMOKE/px.png;type=image/png" "$BASE_URL/article/cover")
echo "$body" | grep -q '"success":true' && \
  echo "$body" | grep -Eq '/upload/[0-9]{4}/[0-9]{2}/[0-9]{2}/.+\.png' && c=0 || c=1
ck "FU-02 管理员上传PNG封面成功" $c
cover_url=$(echo "$body" | sed -n 's/.*\("\/upload\/[^"]*"\).*/\1/p' | tr -d '"')

code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL$cover_url")
[ "$code" = "200" ] && c=0 || c=1
ck "FU-06 封面URL可通过/upload访问" $c

body=$(curl -s -b "$CA" -F "file=@$TMPDIR_SMOKE/note.txt;type=text/plain" "$BASE_URL/article/cover")
echo "$body" | grep -q '不支持的文件类型' && c=0 || c=1
ck "FU-04 非图片格式被拒" $c

code=$(curl -s -b "$CA" -F "file=@$TMPDIR_SMOKE/big.png;type=image/png" "$BASE_URL/article/cover" \
  -o "$TMPDIR_SMOKE/big-resp.json" -w "%{http_code}")
grep -q '"code":413' "$TMPDIR_SMOKE/big-resp.json" && \
  grep -q '上传文件过大' "$TMPDIR_SMOKE/big-resp.json" && c=0 || c=1
ck "FU-05 超大图片被拒(code=413)" $c

body=$(curl -s -b "$CU" -F "file=@$TMPDIR_SMOKE/px.png;type=image/png" "$BASE_URL/article/cover")
echo "$body" | grep -q '"success":true' && c=0 || c=1
ck "FU-03 普通用户登录后也可上传封面" $c

# FU-07 封面可回填文章（创建后删除，不残留数据）
printf '{"title":"封面测试文章-冒烟","content":"测试正文","cover":"%s"}' "$cover_url" \
  > "$TMPDIR_SMOKE/art.json"
body=$(curl -s -b "$CA" -X POST "$BASE_URL/article" \
  -H "Content-Type: application/json" --data-binary @"$TMPDIR_SMOKE/art.json")
echo "$body" | grep -q '"success":true' && \
  echo "$body" | grep -q "$cover_url" && c=0 || c=1
ck "FU-07 封面URL可回填到文章cover字段" $c
artid=$(echo "$body" | sed -n 's/.*"id":\([0-9]*\).*/\1/p' | head -1)
[ -n "$artid" ] && curl -s -b "$CA" -X DELETE "$BASE_URL/article/$artid" > /dev/null

# ---------- 汇总 ----------
echo "=============================================="
echo " RESULT: PASS=$PASS FAIL=$FAIL"
echo "=============================================="
[ "$FAIL" -eq 0 ] || exit 1
