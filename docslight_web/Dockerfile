FROM node:lts-alpine AS builder

WORKDIR /app

COPY package.json ./
COPY pnpm*.yaml ./

COPY packages/web/package.json ./packages/web/
COPY packages/admin/package.json ./packages/admin/
COPY package.json pnpm-*.yaml pnpm-workspace.yaml .npmrc* ./

# Install dependencies based on the preferred package manager
RUN npm install -g pnpm && pnpm install
COPY . .
RUN pnpm web:build
RUN pnpm admin:build

FROM nginx:stable-alpine

COPY --from=builder /app/packages/web/dist /app/web/dist
COPY --from=builder /app/packages/web/public/config.js.template /app/web/dist/config.js
COPY --from=builder /app/packages/admin/dist /app/admin/dist
COPY --from=builder /app/packages/admin/public/config.js.template /app/admin/dist/config.js
COPY docker-entrypoint.sh /docker-entrypoint.sh

RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT ["/docker-entrypoint.sh"]
