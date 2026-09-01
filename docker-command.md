docker run -d \
  --name case-viewer-postgres \
  -e POSTGRES_DB=cases \
  -e POSTGRES_USER=caseuser \
  -e POSTGRES_PASSWORD=casepassword \
  -p 5432:5432 \
  postgres:16


 docker run -d \
  --name prometheus \
  -p 9090:9090 \
  -v "$(pwd)/prometheus/prometheus.yml:/etc/prometheus/prometheus.yml" \
  prom/prometheus


docker run -d \
  --name redis \
  -p 6379:6379 \
  redis:latest


docker run -d \
  --name grafana \
  -p 3000:3000 \
  grafana/grafana   