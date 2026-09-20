# Build stage
FROM node:18-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm install

# Copy source code
COPY . .

# Build the app
RUN npm run build

# Production stage
FROM nginx:alpine

# Copy built app to nginx
COPY --from=build /app/build /usr/share/nginx/html

# nginx.conf is rendered from a template at container start (upstream hosts via
# env vars) so ONE image works both locally (docker-compose service names) and
# on AWS ECS (service-discovery FQDNs). Without this the deployed frontend has
# no /api or /ai proxy and the chat section can't reach the AI service.
COPY nginx.conf.template /etc/nginx/nginx.conf.template
COPY docker-entrypoint.d/10-render-nginx-conf.sh /docker-entrypoint.d/10-render-nginx-conf.sh
RUN chmod +x /docker-entrypoint.d/10-render-nginx-conf.sh

# Expose port 80
EXPOSE 80

# nginx:alpine's default ENTRYPOINT runs /docker-entrypoint.d/*.sh (which renders
# our config) then execs this CMD.
CMD ["nginx", "-g", "daemon off;"]