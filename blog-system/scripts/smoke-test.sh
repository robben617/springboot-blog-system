#!/usr/bin/env bash
# ============================================================================
# 分类标签 + 评论模块 端到端冒烟测试脚本
# ----------------------------------------------------------------------------
# 作用：一条命令完成「打包产物启动 → 50 项接口断言 → 关停」，用于回归自测与
#       第 9 周《测试报告》的证据采集。
#
# 【⚠️ 危险提示】脚本会先清空 category / tag / article / article_tag / comment
#   五张表的业务数据（保证可重复运行），**只能对着测试库跑**，切勿对含正式数据
#   的库执行。必须显式设置 CONFIRM_TEST_DB=YES 才会运行。
#
# 前置条件：
#   1. 已执行 sql/init.sql 建库（数据库名默认 blog_db）
#   2. 已打包：mvn -DskipTests package
#   3. 环境变量 DB_PASSWORD 填本机 MySQL 密码
#
# 用法示例（Windows Git Bash / Linux / macOS 通用）：
#   DB_PASSWORD=你的密码 CONFIRM_TEST_DB=YES bash scripts/smoke-test.sh
#
# 可选环境变量：
#   DB_USERNAME   数据库账号（默认 root）
#   DB_NAME       数据库名（默认 blog_db）
#   APP_PORT      服务端口（默认 9090）
#   JAVA_BIN      java 可执行文件（默认 java，可用 JAVA_HOME 指定）
#   MYSQL_CLI     mysql 客户端（默认 mysql，本机未加入 PATH 时需指定绝对路径）
# ============================================================================
set -u
unset http_proxy https_proxy HTTP_PROXY HTTPS_PROXY
unset SERVER_PORT   # 防止环境变量覆盖端口导致探测失败

if [ "${CONFIRM_TEST_DB:-}" != "YES" ]; then
  cat <<'EOF'
!! 已中止：本脚本会清空测试数据，必须显式确认。
   确认这是测试库后，请这样运行：
   DB_PASSWORD=你的密码 CONFIRM_TEST_DB=YES bash scripts/smoke-test.sh
EOF
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJ_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
cd "$PROJ_DIR" || exit 1

DB_PASSWORD="${DB_PASSWORD:?请用环境变量 DB_PASSWORD 提供本机 MySQL 密码}"
DB_USERNAME="${DB_USERNAME:-root}"
DB_NAME="${DB_NAME:-blog_db}"
APP_PORT="${APP_PORT:-9090}"
JAVA_BIN="${JAVA_BIN:-java}"
MYSQL_CLI="${MYSQL_CLI:-mysql}"

JAR=$(ls target/blog-system-*.jar 2>/dev/null | grep -v '\.original$' | head -1)
if [ -z "$JAR" ]; then
  echo "!! 未找到 target/blog-system-*.jar，请先执行：mvn -DskipTests package"
  exit 1
fi

B="http://127.0.0.1:$APP_PORT"
# 注意：这里刻意使用「相对路径」而不是 $PROJ_DIR 拼出的绝对路径。
# 原因：Windows 版 curl 不认识 Git Bash 的 POSIX 路径（/d/xxx），会导致 cookie 文件
# 写不出来、后续所有需登录的请求都变成 401（本项目真实踩过的坑，详见
# 《开发踩坑与避错记录.md》）。相对路径对 curl 与 bash 都安全。
W=".workbuddy"
mkdir -p "$W"
APP_LOG="$W/smoke-app.log"
rm -f "$W"/cookies-*.txt "$APP_LOG"
cd "$PROJ_DIR" || exit 1

PASS=0; FAIL=0
ck() { # 断言：描述 / 实际 / 期望子串
  if printf '%s' "$2" | grep -q -- "$3"; then
    printf '  [OK]   %s\n' "$1"; PASS=$((PASS+1))
  else
    printf '  [FAIL] %s\n         期望含: %s\n         实际: %s\n' "$1" "$3" "$(printf '%s' "$2" | head -c 260)"; FAIL=$((FAIL+1))
  fi
}
id_of() { printf '%s' "$1" | grep -o '"id":[0-9]*' | head -1 | cut -d: -f2; }
q() { "$MYSQL_CLI" -u"$DB_USERNAME" -p"$DB_PASSWORD" --default-character-set=utf8mb4 -N -e "$1" 2>/dev/null | grep -v "Using a password"; }

echo "==================== 启动应用 ===================="
export DB_PASSWORD
nohup "$JAVA_BIN" -jar "$JAR" --server.port="$APP_PORT" > "$APP_LOG" 2>&1 &
for i in $(seq 1 40); do
  R=$(curl -s -m 3 "$B/test/health" 2>/dev/null)
  [ -n "$R" ] && { echo "启动成功（第 ${i} 次探测）"; break; }
  sleep 3
done
if [ -z "${R:-}" ]; then echo "启动失败，日志尾部："; tail -30 "$APP_LOG"; exit 1; fi

echo
echo "==================== 准备测试账号与数据 ===================="
q "DELETE FROM \`$DB_NAME\`.article_tag; DELETE FROM \`$DB_NAME\`.comment; DELETE FROM \`$DB_NAME\`.article;
   DELETE FROM \`$DB_NAME\`.category; DELETE FROM \`$DB_NAME\`.tag;"
for u in admin01 user01 user02; do
  curl -s -m 8 -X POST -H "Content-Type: application/json" \
    -d "{\"username\":\"$u\",\"password\":\"123456\",\"email\":\"$u@test.com\"}" "$B/register" >/dev/null
done
q "UPDATE \`$DB_NAME\`.user SET role=1 WHERE username='admin01';"
login() { curl -s -m 8 -c "$W/cookies-$1.txt" -X POST -H "Content-Type: application/json" \
            -d "{\"username\":\"$1\",\"password\":\"123456\"}" "$B/login"; }
ck "管理员登录" "$(login admin01)" '"success":true'
ck "作者登录"   "$(login user01)"  '"success":true'
ck "第三方登录" "$(login user02)"  '"success":true'
A="$W/cookies-admin01.txt"; U1="$W/cookies-user01.txt"; U2="$W/cookies-user02.txt"
ck "错误密码被拒" "$(curl -s -m 8 -X POST -H "Content-Type: application/json" -d '{"username":"admin01","password":"bad"}' "$B/login")" '用户名或密码错误'

echo
echo "==================== C1 分类管理 ===================="
C1=$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"技术分享","sort":1}' "$B/admin/category")
C2=$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"生活随笔","sort":2}' "$B/admin/category")
C3=$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"空分类待删","sort":9}' "$B/admin/category")
ck "新增分类成功" "$C1" '新增分类成功'
CID1=$(id_of "$C1"); CID2=$(id_of "$C2"); CID3=$(id_of "$C3")
if [ -z "$CID1" ]; then
  echo "!! 管理员登录或新增分类失败，后续测试无意义，提前终止。实际响应：$C1"
  tail -20 "$APP_LOG"; exit 1
fi
ck "重复分类名被拒" "$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"技术分享"}' "$B/admin/category")" '已存在'
ck "空白分类名被拒" "$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"   "}' "$B/admin/category")" '不能为空'
ck "普通用户越权被拦" "$(curl -s -m 8 -b "$U1" -X POST -H "Content-Type: application/json" -d '{"name":"越权"}' "$B/admin/category")" '无权限'
ck "修改分类名" "$(curl -s -m 8 -b "$A" -X PUT -H "Content-Type: application/json" -d '{"name":"技术分享","sort":5}' "$B/admin/category/$CID1")" '修改分类成功'

echo
echo "==================== C2 标签管理 ===================="
T1=$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"Spring Boot"}' "$B/admin/tag")
T2=$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"MySQL"}' "$B/admin/tag")
T3=$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"Vue3"}' "$B/admin/tag")
ck "新增标签成功" "$T1" '新增标签成功'
TID1=$(id_of "$T1"); TID2=$(id_of "$T2"); TID3=$(id_of "$T3")
ck "重复标签名被拒" "$(curl -s -m 8 -b "$A" -X POST -H "Content-Type: application/json" -d '{"name":"MySQL"}' "$B/admin/tag")" '已存在'
ck "修改标签名" "$(curl -s -m 8 -b "$A" -X PUT -H "Content-Type: application/json" -d '{"name":"MySQL 8"}' "$B/admin/tag/$TID2")" '修改标签成功'
ck "未登录不能建分类" "$(curl -s -m 8 -X POST -H "Content-Type: application/json" -d '{"name":"x"}' "$B/admin/category")" '未登录'

echo
echo "==================== 造文章数据（文章模块由他人负责，直接写库）===================="
q "INSERT INTO \`$DB_NAME\`.article (id,user_id,category_id,title,summary,content,status) VALUES
   (1,2,$CID1,'Spring Boot 入门','第一篇','正文内容',1),
   (2,2,$CID1,'MyBatis-Plus 分页实践','第二篇','正文内容',1),
   (3,2,$CID1,'草稿：《待发布的文章》','不该出现','正文',0),
   (4,3,$CID2,'周末随笔','生活','正文',1);"
q "INSERT INTO \`$DB_NAME\`.article_tag (article_id,tag_id) VALUES (1,$TID1),(1,$TID2),(2,$TID2),(3,$TID1),(4,$TID3);"
echo "文章 4 篇（含 1 篇草稿）、标签关联 5 条 已写入"

echo
echo "==================== C3 按分类浏览（公开）===================="
ck "分类列表公开可读" "$(curl -s -m 8 "$B/categories")" '技术分享'
R=$(curl -s -m 8 "$B/categories/$CID1/articles")
ck "按分类查文章(仅已发布)" "$R" 'Spring Boot 入门'
ck "草稿不出现在前台" "$(printf '%s' "$R" | grep -c '草稿')" '^0$'
ck "分页 total 正确(2篇)" "$R" '"total":2'
ck "不存在的分类报错" "$(curl -s -m 8 "$B/categories/9999/articles")" '不存在'
ck "pageNum=0 兜底" "$(curl -s -m 8 "$B/categories/$CID1/articles?pageNum=0&pageSize=1000")" '"success":true'

echo
echo "==================== C4 按标签浏览（公开）===================="
ck "标签列表公开可读" "$(curl -s -m 8 "$B/tags")" 'Spring Boot'
R=$(curl -s -m 8 "$B/tags/$TID2/articles")
ck "按标签查文章(M:N 生效)" "$R" 'MyBatis-Plus 分页实践'
ck "标签分页 total=2" "$R" '"total":2'
ck "不存在的标签报错" "$(curl -s -m 8 "$B/tags/9999/articles")" '不存在'
ck "id 非数字统一异常" "$(curl -s -m 8 "$B/tags/abc/articles")" 'success":false'

echo
echo "==================== M1/M2 发表评论与回复 ===================="
ck "未登录不能评论" "$(curl -s -m 8 -X POST -H "Content-Type: application/json" -d '{"articleId":1,"content":"x"}' "$B/comment")" '未登录'
R1=$(curl -s -m 8 -b "$U1" -X POST -H "Content-Type: application/json" -d '{"articleId":1,"content":"写得很清楚，收藏了"}' "$B/comment")
ck "发表一级评论" "$R1" '评论成功'
MID1=$(id_of "$R1")
ck "一级评论 parentId 为 null" "$R1" '"parentId":null'
R2=$(curl -s -m 8 -b "$U2" -X POST -H "Content-Type: application/json" -d "{\"articleId\":1,\"content\":\"同意楼上\",\"parentId\":$MID1}" "$B/comment")
ck "回复一级评论" "$R2" '评论成功'
MID2=$(id_of "$R2")
ck "回复的 parentId 指向一级评论" "$R2" "\"parentId\":$MID1"
R3=$(curl -s -m 8 -b "$U1" -X POST -H "Content-Type: application/json" -d "{\"articleId\":1,\"content\":\"回复楼中楼（应归并到一级）\",\"parentId\":$MID2}" "$B/comment")
ck "回复的回复自动归并为一级" "$R3" "\"parentId\":$MID1"
MID3=$(id_of "$R3")
ck "内容为空被拒" "$(curl -s -m 8 -b "$U1" -X POST -H "Content-Type: application/json" -d '{"articleId":1,"content":"  "}' "$B/comment")" '不能为空'
ck "超长内容被拒(>500)" "$(curl -s -m 8 -b "$U1" -X POST -H "Content-Type: application/json" -d "{\"articleId\":1,\"content\":\"$(printf 'a%.0s' $(seq 1 520))\"}" "$B/comment")" '500'
ck "评论不存在的文章被拒" "$(curl -s -m 8 -b "$U1" -X POST -H "Content-Type: application/json" -d '{"articleId":9999,"content":"x"}' "$B/comment")" '不存在'
ck "跨文章回复被拒" "$(curl -s -m 8 -b "$U2" -X POST -H "Content-Type: application/json" -d "{\"articleId\":4,\"content\":\"x\",\"parentId\":$MID1}" "$B/comment")" '不属于'

echo
echo "==================== M3 评论展示（公开）===================="
R=$(curl -s -m 8 "$B/comments/article/1")
ck "公开可看评论" "$R" '写得很清楚'
ck "含评论人昵称" "$R" '"userNickname"'
ck "两级结构 replies 就位" "$R" '"replies"'
ck "一级评论数=1" "$(printf '%s' "$R" | grep -c '同意楼上')" '^1$'
ck "空文章返回空数组" "$(curl -s -m 8 "$B/comments/article/4")" '"data":\[\]'

echo
echo "==================== M4 删除评论与权限 ===================="
ck "无关用户不能删他人评论" "$(curl -s -m 8 -b "$U2" -X DELETE "$B/comment/$MID1")" '无权限'
ck "删除不存在的评论" "$(curl -s -m 8 -b "$U1" -X DELETE "$B/comment/99999")" '不存在'
ck "文章作者可删评论" "$(curl -s -m 8 -b "$U1" -X DELETE "$B/comment/$MID1")" '删除评论成功'
ck "级联删除其下回复(库里0条)" "$(q "SELECT COUNT(*) FROM \`$DB_NAME\`.comment WHERE id IN ($MID1,$MID2,$MID3) AND deleted=0;")" '^0$'
MID4=$(id_of "$(curl -s -m 8 -b "$U2" -X POST -H "Content-Type: application/json" -d '{"articleId":1,"content":"管理员可删的评论"}' "$B/comment")")
ck "管理员可删任意评论" "$(curl -s -m 8 -b "$A" -X DELETE "$B/comment/$MID4")" '删除评论成功'

echo
echo "==================== 分类/标签删除的业务规则 ===================="
ck "有文章的分类不能删" "$(curl -s -m 8 -b "$A" -X DELETE "$B/admin/category/$CID1")" '无法删除'
ck "空分类可删" "$(curl -s -m 8 -b "$A" -X DELETE "$B/admin/category/$CID3")" '删除分类成功'
q "INSERT INTO \`$DB_NAME\`.article_tag (article_id,tag_id) VALUES (2,$TID3);"
ck "删标签成功" "$(curl -s -m 8 -b "$A" -X DELETE "$B/admin/tag/$TID3")" '删除标签成功'
ck "删标签后关联已解除(0条)" "$(q "SELECT COUNT(*) FROM \`$DB_NAME\`.article_tag WHERE tag_id=$TID3;")" '^0$'
ck "文章未被连带删除" "$(q "SELECT COUNT(*) FROM \`$DB_NAME\`.article WHERE deleted=0;")" '^4$'
ck "不存在的标签报错" "$(curl -s -m 8 -b "$A" -X DELETE "$B/admin/tag/9999")" '不存在'

echo
echo "==================== 收尾：关停应用 ===================="
PID=$(netstat -ano 2>/dev/null | grep LISTENING | grep ":$APP_PORT" | awk '{print $NF}' | head -1)
if [ -n "$PID" ]; then
  taskkill //F //PID "$PID" >/dev/null 2>&1 || kill "$PID" 2>/dev/null
fi
sleep 2
curl -s -m 3 "$B/test/health" >/dev/null 2>&1 && echo "仍在运行（可手动关闭）" || echo "应用已关停 ✓"

echo
echo "==================== 结果汇总 ===================="
echo "通过: $PASS    失败: $FAIL"
if [ "$FAIL" -eq 0 ]; then echo "全部通过 ✅（共 $PASS 项断言）"; else echo "存在失败项，请查看上方 [FAIL]"; fi
exit "$FAIL"
