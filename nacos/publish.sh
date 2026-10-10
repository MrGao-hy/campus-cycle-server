#!/bin/bash
# 一键把 nacos/ 目录下的配置发布到 Nacos 配置中心
#
# 用法：
#   ./nacos/publish.sh                       # 发布全部（公共 + dev + prod 模板）
#   ./nacos/publish.sh dev                   # 只发布公共 + dev
#   NACOS_SERVER_ADDR=192.168.1.10:8848 NACOS_PASSWORD=xxx ./nacos/publish.sh dev
#
# 环境变量：
#   NACOS_SERVER_ADDR  默认 127.0.0.1:8848
#   NACOS_USERNAME     默认 nacos
#   NACOS_PASSWORD     默认 nacos
#   NACOS_NAMESPACE    默认空（public 命名空间）
#   NACOS_GROUP        默认 CAMPUS_CYCLE

set -e

# 必须是服务端端口 8848（Nacos 3.x 控制台是 8080，两者不是一回事）
NACOS_SERVER_ADDR="${NACOS_SERVER_ADDR:-127.0.0.1:8848}"
NACOS_USERNAME="${NACOS_USERNAME:-nacos}"
NACOS_PASSWORD="${NACOS_PASSWORD:-nacos}"
# 显式用 public；与客户端 namespace 留空等价（留空即 public）
NACOS_NAMESPACE="${NACOS_NAMESPACE:-public}"
NACOS_GROUP="${NACOS_GROUP:-CAMPUS_CYCLE}"
DIR="$(cd "$(dirname "$0")" && pwd)"

# 本机地址时绕过可能存在的 http 代理（环境变量方式，避免 --noproxy * 被 shell glob 展开）
case "$NACOS_SERVER_ADDR" in
127.0.0.1* | localhost*)
    export no_proxy="127.0.0.1,localhost"
    export NO_PROXY="$no_proxy"
    ;;
esac

BASE="http://${NACOS_SERVER_ADDR}/nacos"
echo "目标 Nacos: ${BASE}  命名空间:[${NACOS_NAMESPACE:-public}]  分组:${NACOS_GROUP}"

# 1. 登录换取 accessToken（Nacos 3.x 控制台默认开启鉴权）
LOGIN=$(curl -s -X POST "${BASE}/v1/auth/users/login" \
    --data-urlencode "username=${NACOS_USERNAME}" \
    --data-urlencode "password=${NACOS_PASSWORD}")
# 用 sed 抽取 accessToken：不依赖 python，Git Bash / Linux / macOS 都能跑
TOKEN=$(printf '%s' "$LOGIN" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')
if [ -z "$TOKEN" ]; then
    echo "登录失败，请检查 Nacos 地址与账号密码：$LOGIN"
    exit 1
fi

# Nacos 3.x 用 v3 Admin API（2.x 的 /v1/cs/configs 在 3.x 已下线，会返回 404）
publish() {
    local data_id="$1" file="$2"
    if [ ! -f "$file" ]; then
        echo "跳过（文件不存在）：$file"
        return
    fi
    local resp content
    # 先把文件读进变量再发送：Git Bash 下是 Windows 版 curl，
    # 不认 `content@/e/xxx/yyy.yaml` 这种 POSIX 路径（会报 reading a file 错）
    content=$(cat "$file")
    resp=$(curl -s -X POST "${BASE}/v3/admin/cs/config" \
        --data-urlencode "dataId=${data_id}" \
        --data-urlencode "groupName=${NACOS_GROUP}" \
        --data-urlencode "namespaceId=${NACOS_NAMESPACE}" \
        --data-urlencode "type=yaml" \
        --data-urlencode "accessToken=${TOKEN}" \
        --data-urlencode "content=${content}")
    echo "发布 ${data_id} → ${resp}"
}

# 校验：发布后回读一次，确认内容真的进了 Nacos
verify() {
    local data_id="$1"
    local resp
    resp=$(curl -s "${BASE}/v3/admin/cs/config?dataId=${data_id}&groupName=${NACOS_GROUP}&namespaceId=${NACOS_NAMESPACE}&accessToken=${TOKEN}")
    if printf '%s' "$resp" | grep -q '"code":0'; then
        echo "校验 ${data_id} ✓"
    else
        echo "校验 ${data_id} ✗ ${resp}"
    fi
}

publish "campus-cycle-server.yaml" "${DIR}/campus-cycle-server.yaml"
verify "campus-cycle-server.yaml"

case "${1:-all}" in
dev)
    publish "campus-cycle-server-dev.yaml" "${DIR}/campus-cycle-server-dev.yaml"
    verify "campus-cycle-server-dev.yaml"
    ;;
prod)
    publish "campus-cycle-server-prod.yaml" "${DIR}/campus-cycle-server-prod.yaml"
    verify "campus-cycle-server-prod.yaml"
    ;;
all)
    publish "campus-cycle-server-dev.yaml" "${DIR}/campus-cycle-server-dev.yaml"
    publish "campus-cycle-server-prod.yaml" "${DIR}/campus-cycle-server-prod.yaml"
    verify "campus-cycle-server-dev.yaml"
    verify "campus-cycle-server-prod.yaml"
    ;;
*)
    echo "未知环境：$1（可选 dev / prod / all）"
    exit 1
    ;;
esac

CONSOLE_HOST="${NACOS_SERVER_ADDR%:*}"
echo "完成。控制台（Nacos 3.x 新 UI）：http://${CONSOLE_HOST}:8080/next/"
echo "  监听查询：http://${CONSOLE_HOST}:8080/next/#/listeningToQuery?namespace=public&namespaceShowName=public"
echo "  配置列表：http://${CONSOLE_HOST}:8080/next/#/config/list?namespace=public&group=${NACOS_GROUP}"
