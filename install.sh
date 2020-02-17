#!/usr/bin/env bash
set -e

mkdir -p ~/.recordsets

echo "Building cli uberjar."
LEIN_SILENT=true lein build-cli
cp target/recordsets-cli.jar ~/.recordsets/recordsets-cli.jar
echo "Installed cli uberjar at ~/.recordsets/recordsets-cli.jar"

echo "Building server uberjar."
LEIN_SILENT=true lein build-server
cp target/recordsets-server.jar ~/.recordsets/recordsets-server.jar
echo "Installed server uberjar at ~/.recordsets/recordsets-server.jar"

cat <<'EOF' >/usr/local/bin/recordsets
#!/usr/bin/env bash
set -e
case "$1" in
  latest)
    git clone --depth=1 git@github.com:RutledgePaulV/recordsets.git recordsets && cd recordsets && ./install.sh && cd .. && rm -rf recordsets
    echo "recordsets successfully updated."
    ;;
  uninstall)
    rm -rf ~/.recordsets && rm -rf /usr/local/bin/recordsets
    echo "recordsets successfully uninstalled."
    ;;
  server)
    java -jar ~/.recordsets/recordsets-server.jar $@
    ;;
  *)
    java -jar ~/.recordsets/recordsets-cli.jar $@
esac
EOF

chmod +x /usr/local/bin/recordsets
echo "Created executable script for easy access at /usr/local/bin/recordsets"