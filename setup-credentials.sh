#!/bin/bash

echo "=== Simple JSON Query - Maven Central Setup ==="
echo ""

# Check if GPG is installed
if ! command -v gpg &> /dev/null; then
    echo "Error: GPG is not installed. Please install it first:"
    echo "  brew install gnupg"
    exit 1
fi

# List GPG keys
echo "Your GPG keys:"
gpg --list-secret-keys --keyid-format=long
echo ""

# Prompt for key ID
read -p "Enter your GPG key ID (e.g., 6C6D015D60BB43EB): " KEY_ID

if [ -z "$KEY_ID" ]; then
    echo "Error: Key ID is required"
    exit 1
fi

# Export key
echo ""
echo "Exporting GPG key..."
GPG_KEY=$(gpg --export-secret-keys --armor "$KEY_ID" 2>/dev/null)

if [ -z "$GPG_KEY" ]; then
    echo "Error: Failed to export key. Make sure the key ID is correct."
    exit 1
fi

# Convert to single line
GPG_KEY_SINGLE_LINE=$(echo "$GPG_KEY" | sed -e ':a' -e 'N' -e '$!ba' -e 's/\n/\\n/g')

echo "✓ GPG key exported successfully"
echo ""

# Prompt for other credentials
read -p "Enter your Sonatype username: " SONATYPE_USER
read -sp "Enter your Sonatype password: " SONATYPE_PASS
echo ""
read -sp "Enter your GPG key password: " GPG_PASS
echo ""
echo ""

# Create gradle.properties in home directory
GRADLE_PROPS="$HOME/.gradle/gradle.properties"

# Backup existing file
if [ -f "$GRADLE_PROPS" ]; then
    echo "Backing up existing gradle.properties..."
    cp "$GRADLE_PROPS" "$GRADLE_PROPS.backup"
fi

# Write credentials
echo "Writing credentials to $GRADLE_PROPS..."

# Check if file exists and has content
if [ -f "$GRADLE_PROPS" ] && [ -s "$GRADLE_PROPS" ]; then
    # Append to existing file
    echo "" >> "$GRADLE_PROPS"
    echo "# Simple JSON Query Publishing" >> "$GRADLE_PROPS"
else
    # Create new file
    mkdir -p "$HOME/.gradle"
    echo "# Simple JSON Query Publishing" > "$GRADLE_PROPS"
fi

cat >> "$GRADLE_PROPS" << EOF
ossrhUsername=$SONATYPE_USER
ossrhPassword=$SONATYPE_PASS
signingKey=$GPG_KEY_SINGLE_LINE
signingPassword=$GPG_PASS
EOF

echo ""
echo "✓ Credentials saved to $GRADLE_PROPS"
echo ""
echo "Next steps:"
echo "1. Verify your namespace at https://central.sonatype.com/"
echo "2. Test signing: ./gradlew publishToMavenLocal"
echo "3. Publish: ./gradlew publishAllPublicationsToSonatypeRepository"
echo ""
echo "Done! 🚀"
