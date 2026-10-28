#!/bin/bash
# ============================================================
# 前后端联调自检：后端 jar + 前端 vite dev，验证 5173 代理链路
# 用法：
#   export DB_PASSWORD=你的MySQL密码
#   bash scripts/verify-fullstack.sh
# ============================================================
set -u

BACKEND_DIR="/d/javacode/shixun/blog-system"
FRONTEND_DIR="/d/javacode/shixun/blog-frontend"
JAVA="D:/java/jdk-21/bin/java.exe"
NODE="D:/AI/nodejs/node.exe"
PASS=0
FAIL=0
BE_LOG="/tmp/be-fullstack.log"
FE_LOG="/tmp/fe-fullstack.log"

ok()  { PASS=$((PASS+1)); echo "  ✅ $1"; }
bad() { FAIL=$((FAIL+1)); echo "  ❌ $1"; }

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "❌ 未设置 DB_PASSWORD，请先 export DB_PASSWORD=你的密码"
  exit 1
fi

BE_PID=""
FE_PID=""
cleanup() {
  echo
  echo "=== 关停 ==="
  [ -n "$FE_PID" ] && kill "$FE_PID" 2>/dev/null && echo "  前端已停 PID=$FE_PID"
  [ -n "$BE_PID" ] && kill "$BE_PID" 2>/dev/null && echo "  后端已停 PID=$BE_PID"
  sleep 2
  [ -n "$FE_PID" ] && kill -9 "$FE_PID" 2>/dev/null
  [ -n "$BE_PID" ] && kill -9 "$BE_PID" 2>/dev/null
}
trap cleanup EXIT

echo "=== 1. 启动后端（9090）==="
cd "$BACKEND_DIR" || exit 1
"$JAVA" -jar target/blog-system-1.0.0.jar --server.port=9090 > "$BE_LOG" 2>&1 &
BE_PID=$!
for i in $(seq 1 40); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 http://localhost:9090/test/health 2>/dev/null)
  [ "$CODE" = "200" ] && { ok "后端就绪（${i}s）"; break; }
  sleep 1
  [ "$i" = "40" ] && { bad "后端 40 秒未就绪"; tail -20 "$BE_LOG"; exit 1; }
done

echo
echo "=== 2. 启动前端（5173）==="
cd "$FRONTEND_DIR" || exit 1
"$NODE" node_modules/vite/bin/vite.js --port 5173 > "$FE_LOG" 2>&1 &
FE_PID=$!
FE_OK=0
for i in $(seq 1 40); do
  CODE=$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 http://localhost:5173/ 2>/dev/null)
  [ "$CODE" = "200" ] && { FE_OK=1; ok "前端就绪（${i}s）"; break; }
  sleep 1
done
[ "$FE_OK" = "0" ] && { bad "前端 40 秒未就绪"; tail -25 "$FE_LOG"; exit 1; }

echo
echo "=== 3. 首页 HTML ==="
BODY=$(curl -s --max-time 8 http://localhost:5173/)
if printf '%s' "$BODY" | grep -q 'id="app"'; then ok "首页返回 Vue 挂载点"; else bad "首页异常"; fi

echo
echo "=== 4. 代理链路：5173 /api → 9090（我的接口）==="
BODY=$(curl -s --max-time 8 "http://localhost:5173/api/categories")
if printf '%s' "$BODY" | grep -q '后端开发'; then ok "分类列表经代理正常返回"; else bad "分类列表代理失败: $(printf '%s' "$BODY" | head -c 150)"; fi

BODY=$(curl -s --max-time 8 "http://localhost:5173/api/tags")
if printf '%s' "$BODY" | grep -q 'Spring Boot'; then ok "标签列表经代理正常返回"; else bad "标签列表代理失败"; fi

BODY=$(curl -s --max-time 8 "http://localhost:5173/api/comments/article/1")
if printf '%s' "$BODY" | grep -q 'replies'; then ok "评论两级结构经代理正常返回"; else bad "评论接口代理失败"; fi

BODY=$(curl -s --max-time 8 "http://localhost:5173/api/categories/1/articles")
if printf '%s' "$BODY" | grep -q '"total":2'; then ok "按分类浏览文章经代理正常返回"; else bad "按分类浏览代理失败"; fi

echo
echo "=== 5. 前端登录页路由 ==="
CODE=$(curl -s -o /dev/null -w '%{http_code}' --max-time 8 "http://localhost:5173/login")
if [ "$CODE" = "200" ]; then ok "登录页可访问 [HTTP 200]"; else bad "登录页异常 [HTTP $CODE]"; fi

echo
echo "============================================================"
echo "  前后端联调结果：通过 $PASS 项，失败 $FAIL 项"
[ "$FAIL" = "0" ] && echo "  ✅ 浏览器打开 http://localhost:5173 即可演示" || echo "  ⚠️  有失败项，见上方明细"
echo "============================================================"
