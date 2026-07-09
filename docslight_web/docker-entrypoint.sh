#!/bin/sh

set -e

# 判断端口是否应被附加（不为空且不为 80/443）
format_host() {
  host=$1
  port=$2

  if [ -z "$port" ] || [ "$port" = "80" ] || [ "$port" = "443" ]; then
    echo "$host"
  else
    echo "${host}:${port}"
  fi
}

# 构建完整 HOST
admin_full_host=$(format_host "$ADMIN_HOST" "$ADMIN_PORT")
comidp_full_host=$(format_host "$COMIDP_HOST" "$COMIDP_PORT")

envsubst < /app/web/dist/config.js.template > /app/web/dist/config.js
envsubst < /app/admin/dist/config.js.template > /app/admin/dist/config.js

# 替换环境变量占位符
sed -i "s|__ADMIN_HOST__|${admin_full_host}|g" /app/web/dist/config.js
sed -i "s|__LICENSE_KEY__|${LICENSE_KEY}|g" /app/web/dist/config.js
sed -i "s|__COMIDP_HOST__|${comidp_full_host}|g" /app/admin/dist/config.js

# 启动 Nginx
exec nginx -g 'daemon off;'
