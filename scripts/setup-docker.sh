#!/usr/bin/env bash
set -e

echo "=== Adding Docker GPG Key ==="
install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
chmod a+r /etc/apt/keyrings/docker.asc

echo "=== Adding Docker Apt Repository ==="
echo "deb [arch=amd64 signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu noble stable" > /etc/apt/sources.list.d/docker.list

echo "=== Updating Apt Cache ==="
apt-get update

echo "=== Installing Docker Engine, CLI, Containerd & Plugins ==="
apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

echo "=== Enabling & Starting Docker Service via Systemd ==="
systemctl enable docker
systemctl restart docker

echo "=== Docker Status ==="
docker --version
docker compose version
