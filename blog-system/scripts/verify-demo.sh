#!/bin/bash
# ============================================================
# 答辩前环境自检脚本（启动 -> 全量接口验证 -> 关停）
# 用法：
#   export DB_PASSWORD=你的MySQL密码
#   bash scripts/verify-demo.sh
# 说明：密码只从环境变量读取，脚本内不含任何明文密码。
# ============================================================
set -u

BASE="http://localhost:9090"
JAR="target/blog-system-1.0.0.jar"
JAVA="D:/java/jdk-21/bin/java.exe"
PASS=0
FAIL=0
COOKIE_U="cookie_user.txt"
COOKIE_A="cookie_admin.txt"
LOG="run-verify.log"

rm -f "$COOKIE_U" "$COOKIE_A" "$LOG"

if [ -z "${DB_PASSWORD:-}" ]; then
  echo "❌ 未设置 DB_PASSWORD 环境变量，请先 export DB_PASSWORD=你的密码"
  exit 1
fi

ok()   { PASS=$((PASS+1)); echo "  ✅ $1"; }
bad()  { FAIL=$((FAIL+1)); echo "  ❌ $1"; }

# 断言：响应中包含指定字符串
ck() {  # ck 描述 期望片段 实际内容
  if printf '%s' "$3" | grep -q -- "$2"; then ok "$1"; else bad "$1 (期望含 [$2]，实际: $(printf '%s' "$3" | head -c 160))"; fi
}
# 断言：HTTP 状态码
ckc() { # ckc 描述 期望码 实际码
  if [ "$2" = "$3" ]; then ok "$1 [HTTP $3]"; else bad "$1 (期望 HTTP $2，实际 $3)"; fi
}

echo "=== 1. 启动后端 ==="
"$JAVA" -jar "$JAR" --server.port=9090 > "$LOG" 2>&1 &
APP_PID=$!
echo "  PID=$APP_PID，等待启动..."

READY=0
for i in $(seq 1 40); do
  if ! kill -0 "$APP_PID" 2>/dev/null; then
    echo "  ❌ 进程已退出，最后日志："
    tail -30 "$LOG"
    exit 1
  fi
  CODE=$(curl -s -o /dev/null -w '%{http_code}' --max-time 3 "$BASE/test/health" 2>/dev/null)
  if [ "$CODE" = "200" ]; then READY=1; echo "  ✅ 启动成功（第 ${i} 次探测）"; break; fi
  sleep 1
done

if [ "$READY" != "1" ]; then
  echo "  ❌ 40 秒内未就绪，日志尾部："
  tail -40 "$LOG"
  kill "$APP_PID" 2>/dev/null
  exit 1
fi

cleanup() {
  echo
  echo "=== 关停后端 ==="
  kill "$APP_PID" 2>/dev/null
  sleep 2
  kill -9 "$APP_PID" 2>/dev/null
  rm -f "$COOKIE_U" "$COOKIE_A"
  echo "  已停止 PID=$APP_PID"
}
trap cleanup EXIT

echo
echo "=== 2. 健康检查与鉴权拦截 ==="
BODY=$(curl -s --max-time 5 "$BASE/test/health")
ck "health 返回正常" "ok" "$BODY"

CODE=$(curl -s -o /dev/null -w '%{http_code}' --max-time 5 "$BASE/admin/comments")
ckc "未登录访问管理端被拦截" "401" "$CODE"

echo
echo "=== 3. 分类模块（C1/C3）==="
BODY=$(curl -s --max-time 8 "$BASE/categories")
ck "分类列表：5 个分类" "随笔杂谈" "$BODY"
ck "分类列表：含已发布文章数 articleCount" "articleCount" "$BODY"
BODY=$(curl -s --max-time 8 "$BASE/categories/1/articles")
ck "按分类分页浏览（后端开发）" '"total":2' "$BODY"
BODY=$(curl -s --max-time 8 "$BASE/categories/99999/articles")
ck "不存在的分类返回业务码 404" '"code":404' "$BODY"
ck "不存在的分类提示文案正确" "分类不存在" "$BODY"

echo
echo "=== 4. 标签模块（C2/C4）==="
BODY=$(curl -s --max-time 8 "$BASE/tags")
ck "标签列表：8 个标签" "Spring Boot" "$BODY"
BODY=$(curl -s --max-time 8 "$BASE/tags/1/articles")
ck "按标签分页浏览（Java 标签下 2 篇）" '"total":2' "$BODY"

echo
echo "=== 5. 评论模块（M3 两级树）==="
BODY=$(curl -s --max-time 8 "$BASE/comments/article/1")
ck "评论列表：返回一级评论" "写得非常清晰" "$BODY"
ck "评论列表：二级回复归并进 replies" '"replies"' "$BODY"
ck "评论列表：回复内容在内" "感谢支持" "$BODY"

echo
echo "=== 6. 登录与发表评论（M1/M2）==="
BODY=$(curl -s -c "$COOKIE_U" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"username":"zhangsan","password":"abc123456"}' "$BASE/login")
ck "普通用户登录成功" "登录成功" "$BODY"

BODY=$(curl -s -b "$COOKIE_U" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"articleId":1,"content":"答辩现场演示：这条评论由脚本发表"}' "$BASE/comment")
ck "发表一级评论成功" '"code":200' "$BODY"
NEW_ID=$(printf '%s' "$BODY" | grep -oE '"id":[0-9]+' | grep -oE '[0-9]+' | head -1)
echo "  新评论 ID=$NEW_ID"

BODY=$(curl -s -b "$COOKIE_U" --max-time 8 -H 'Content-Type: application/json' \
      -d "{\"articleId\":1,\"parentId\":$NEW_ID,\"content\":\"答辩演示：这是一条回复\"}" "$BASE/comment")
ck "发表回复成功（M2）" '"code":200' "$BODY"

BODY=$(curl -s -b "$COOKIE_U" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"articleId":1,"content":""}' "$BASE/comment")
ck "空内容评论被拒绝" "内容不能为空" "$BODY"

echo
echo "=== 7. 删除评论（M4 级联）==="
BODY=$(curl -s -X DELETE -b "$COOKIE_U" --max-time 8 "$BASE/comment/$NEW_ID")
ck "删除自己的一级评论（级联回复）" '"code":200' "$BODY"
BODY=$(curl -s --max-time 8 "$BASE/comments/article/1")
if printf '%s' "$BODY" | grep -q "答辩现场演示"; then
  bad "评论删除后仍能查到"
else
  ok "评论删除后列表中不再出现"
fi

echo
echo "=== 8. 管理员分类管理（C1）==="
BODY=$(curl -s -c "$COOKIE_A" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"username":"admin","password":"admin123"}' "$BASE/login")
ck "管理员登录成功" "登录成功" "$BODY"

BODY=$(curl -s -b "$COOKIE_A" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"name":"答辩演示分类","sort":99}' "$BASE/admin/category")
ck "新增分类成功" '"code":200' "$BODY"
CAT_ID=$(printf '%s' "$BODY" | grep -oE '"id":[0-9]+' | grep -oE '[0-9]+' | head -1)

BODY=$(curl -s -X PUT -b "$COOKIE_A" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"name":"答辩演示分类-改名","sort":98}' "$BASE/admin/category/$CAT_ID")
ck "修改分类成功" '"code":200' "$BODY"

BODY=$(curl -s -b "$COOKIE_A" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"name":"后端开发","sort":1}' "$BASE/admin/category")
ck "重名分类被拒绝" "已存在" "$BODY"

BODY=$(curl -s -X DELETE -b "$COOKIE_A" --max-time 8 "$BASE/admin/category/1")
ck "删除有文章的分类被拒绝" "文章" "$BODY"

BODY=$(curl -s -X DELETE -b "$COOKIE_A" --max-time 8 "$BASE/admin/category/$CAT_ID")
ck "删除空分类成功" '"code":200' "$BODY"

echo
echo "=== 9. 管理员标签管理（C2）==="
BODY=$(curl -s -b "$COOKIE_A" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"name":"答辩演示标签"}' "$BASE/admin/tag")
ck "新增标签成功" '"code":200' "$BODY"
TAG_ID=$(printf '%s' "$BODY" | grep -oE '"id":[0-9]+' | grep -oE '[0-9]+' | head -1)

BODY=$(curl -s -b "$COOKIE_A" --max-time 8 -H 'Content-Type: application/json' \
      -d '{"name":"Java"}' "$BASE/admin/tag")
ck "重名标签被拒绝" "已存在" "$BODY"

BODY=$(curl -s -X DELETE -b "$COOKIE_A" --max-time 8 "$BASE/admin/tag/$TAG_ID")
ck "删除标签成功" '"code":200' "$BODY"

echo
echo "=== 10. 管理端评论分页 ==="
BODY=$(curl -s -b "$COOKIE_A" --max-time 8 "$BASE/admin/comments?pageNum=1&pageSize=10")
ck "管理端评论分页返回数据" '"list"' "$BODY"
ck "管理端评论含所属文章标题" "articleTitle" "$BODY"

echo
echo "============================================================"
echo "  验证结果：通过 $PASS 项，失败 $FAIL 项"
if [ "$FAIL" = "0" ]; then
  echo "  ✅ 全部通过，环境可用于答辩演示"
else
  echo "  ⚠️  有失败项，请查看上方 ❌ 明细"
fi
echo "============================================================"
echo
echo "说明：本脚本会真实写库（新增评论/分类/标签后再删除）。"
echo "      评论走逻辑删除，跑完后 comment 表会残留 deleted=1 的记录，"
echo "      前台和管理端都查不到，不影响演示。若要让库完全回到种子状态："
echo "        mysql -uroot -p --default-character-set=utf8mb4 < sql/init.sql"
