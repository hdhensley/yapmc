# Building YAPMC

## Quick Build Guide

### Prerequisites
- **Recommended**: JDK 21 (LTS) - most stable for jpackage
- **Alternative**: JDK 17, 22, 23, or 24
- **Avoid**: JDK 25 - has known jlink/jpackage bugs

### Standard Build

```bash
# Build the JAR
mvn clean package

# Run the application
java -jar target/yapmc-1.0-SNAPSHOT.jar
```

## Creating Native Installers

### macOS (DMG)

```bash
jpackage \
  --input target \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name YAPMC \
  --type dmg \
  --app-version 1.0.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --mac-package-name YAPMC
```

### Windows (EXE)

```powershell
jpackage `
  --input target `
  --main-jar yapmc-1.0-SNAPSHOT.jar `
  --main-class com.overzealouspelican.Main `
  --name YAPMC `
  --type exe `
  --app-version 1.0.0 `
  --vendor "YAPMC" `
  --description "Privacy-focused desktop API client" `
  --java-options '-Xmx512m' `
  --win-dir-chooser `
  --win-menu `
  --win-shortcut
```

### Linux (DEB)

```bash
jpackage \
  --input target \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name yapmc \
  --type deb \
  --app-version 1.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --linux-package-name yapmc \
  --linux-app-category utils \
  --linux-shortcut
```

## Troubleshooting jpackage/jlink Errors

### Error: "ct.sym has been modified" or jlink fails

This is a known bug in JDK 25 and some JDK installations. Here are the solutions:

#### Solution 1: Use JDK 21 (Recommended)

Switch to JDK 21 LTS, which doesn't have this bug:

```bash
# Check your current Java version
java -version

# Download JDK 21 from:
# - https://adoptium.net/ (Temurin)
# - https://www.oracle.com/java/technologies/downloads/
```

#### Solution 2: Use --runtime-image (Workaround)

If you must use JDK 25 or encounter the error with JDK 21, bypass jlink:

```bash
# Copy your JDK to use as a custom runtime
cp -r $JAVA_HOME custom-runtime

# Use it with jpackage
jpackage \
  --input target \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name yapmc \
  --type deb \
  --app-version 1.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --runtime-image custom-runtime \
  --linux-package-name yapmc \
  --linux-app-category utils \
  --linux-shortcut
```

**Note**: This creates a larger installer since it includes the full JDK instead of a minimal runtime.

#### Solution 3: Create Minimal Runtime (Advanced)

If you want a smaller installer but jlink fails, try creating a minimal runtime with only required modules:

```bash
# List modules your app needs
java --list-modules | grep -E "java.base|java.desktop|java.logging|java.net.http"

# Create minimal runtime (adjust modules as needed)
jlink \
  --add-modules java.base,java.desktop,java.logging,java.net.http,java.sql,java.xml \
  --strip-debug \
  --no-man-pages \
  --no-header-files \
  --compress=2 \
  --output custom-runtime

# If jlink fails, fall back to copying the full JDK
if [ $? -ne 0 ]; then
  echo "jlink failed, using full JDK runtime"
  cp -r $JAVA_HOME custom-runtime
fi

# Then use with jpackage
jpackage \
  --input target \
  --main-jar yapmc-1.0-SNAPSHOT.jar \
  --main-class com.overzealouspelican.Main \
  --name yapmc \
  --type deb \
  --app-version 1.0 \
  --vendor "YAPMC" \
  --description "Privacy-focused desktop API client" \
  --java-options '-Xmx512m' \
  --runtime-image custom-runtime \
  --linux-package-name yapmc \
  --linux-app-category utils \
  --linux-shortcut
```

### Other Common Issues

#### Missing Dependencies on Linux

If you get errors about missing dependencies when building on Linux:

```bash
# Ubuntu/Debian
sudo apt-get install fakeroot

# Fedora/RHEL
sudo dnf install fakeroot
```

#### Permission Issues

If jpackage fails with permission errors:

```bash
# Make sure the target directory is accessible
chmod -R 755 target/
```

## GitHub Actions Builds

The project uses GitHub Actions for automated builds. See `.github/workflows/release.yml` for the configuration.

The workflow:
- Uses JDK 21 for all platforms
- Includes automatic fallback for the jlink bug on Linux
- Creates native installers for Windows, macOS, and Linux
- Attaches installers to GitHub Releases

To trigger a build:
```bash
git tag v1.0.0
git push origin v1.0.0
```

## Testing Installers

### After Building

**macOS:**
```bash
# Open the DMG
open YAPMC-1.0.dmg
```

**Windows:**
```powershell
# Run the installer
.\YAPMC-1.0.exe
```

**Linux:**
```bash
# Install the DEB package
sudo dpkg -i yapmc_1.0-1_amd64.deb

# Run the application
yapmc
```

## Additional Resources

- [jpackage Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jpackage.html)
- [jlink Documentation](https://docs.oracle.com/en/java/javase/21/docs/specs/man/jlink.html)
- [OpenJDK jpackage Guide](https://openjdk.org/jeps/392)

