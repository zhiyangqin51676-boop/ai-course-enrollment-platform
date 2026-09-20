#!/bin/sh
# Render /etc/nginx/nginx.conf from the template, substituting ONLY the two
# upstream vars (envsubst leaves nginx's own $host/$uri/etc. untouched).
#
#   Local docker-compose:  defaults below resolve the compose service names.
#   AWS ECS:               the frontend task passes the service-discovery FQDNs
#                          (ai.<ns>.local:8000 / backend.<ns>.local:8080).
#
# nginx:alpine runs every /docker-entrypoint.d/*.sh before starting nginx.
set -e

export AI_UPSTREAM="${AI_UPSTREAM:-ustudent-ai:8000}"
export BACKEND_UPSTREAM="${BACKEND_UPSTREAM:-ustudent-backend:8080}"
# DNS server nginx uses to (re-)resolve the upstream names. Compose default is
# Docker's embedded DNS; ECS passes the VPC resolver (169.254.169.253) so nginx
# follows service-discovery IP changes when a Fargate task restarts.
export DNS_RESOLVER="${DNS_RESOLVER:-127.0.0.11}"

envsubst '${AI_UPSTREAM} ${BACKEND_UPSTREAM} ${DNS_RESOLVER}' \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/nginx.conf

echo "[10-render-nginx-conf] AI_UPSTREAM=$AI_UPSTREAM BACKEND_UPSTREAM=$BACKEND_UPSTREAM DNS_RESOLVER=$DNS_RESOLVER"
