# macOS Installation Guide

## Gatekeeper Warning

When you download and try to open YAPMC on macOS, you may see a warning:

> **"Apple could not verify "YAPMC" is free of malware that may harm your Mac or compromise your privacy."**

This is normal for open-source applications that aren't distributed through the Mac App Store or signed with an Apple Developer certificate (which costs $99/year). **YAPMC is completely safe** - it's open source and you can review all the code yourself.

## How to Install YAPMC on macOS

### Method 1: Right-Click to Open (Easiest)

1. Download `YAPMC-1.0.0.dmg` from the [Releases page](https://github.com/hdhensley/yapmc/releases)
2. Double-click the DMG to mount it
3. **Right-click** (or Control+click) on the YAPMC app
4. Select **"Open"** from the context menu
5. Click **"Open"** in the dialog that appears
6. macOS will remember your choice and allow the app to run

### Method 2: System Settings Override

1. Try to open YAPMC normally (it will be blocked)
2. Open **System Settings** → **Privacy & Security**
3. Scroll down to the **Security** section
4. You'll see a message: *"YAPMC was blocked from use because it is not from an identified developer"*
5. Click **"Open Anyway"**
6. Click **"Open"** in the confirmation dialog

### Method 3: Remove Quarantine Flag (Advanced)

If you're comfortable with the Terminal:

```bash
# Navigate to where you downloaded the DMG
cd ~/Downloads

# Remove the quarantine attribute
xattr -d com.apple.quarantine YAPMC-1.0.0.dmg

# Now open the DMG normally
open YAPMC-1.0.0.dmg
```

Or after copying the app to your Applications folder:

```bash
# Remove quarantine from the app
sudo xattr -dr com.apple.quarantine /Applications/YAPMC.app

# Now you can open it normally
open /Applications/YAPMC.app
```

## Why This Happens

macOS Gatekeeper blocks applications that aren't:
1. **Notarized** by Apple (requires Apple Developer Program membership - $99/year)
2. **Code-signed** with an Apple Developer certificate
3. **Distributed through** the Mac App Store

YAPMC is an open-source, privacy-focused project. To keep it completely free and open:
- We don't charge for the app
- We don't collect your data
- We don't require you to create accounts
- Therefore, we can't justify the $99/year Apple Developer fee

## Is YAPMC Safe?

**Yes!** YAPMC is:
- ✅ **Open Source** - All code is visible on GitHub
- ✅ **Privacy-Focused** - All data stays on your local machine
- ✅ **No Network Tracking** - Only makes API calls YOU configure
- ✅ **Community Built** - Built by developers, for developers
- ✅ **Reproducible Builds** - You can build it yourself from source

You can verify the safety by:
1. **Reviewing the source code** on GitHub
2. **Building it yourself** from source (see README.md)
3. **Checking the build process** in `.github/workflows/release.yml`

## Alternative: Build from Source

If you prefer to build YAPMC yourself:

```bash
# Clone the repository
git clone https://github.com/hdhensley/yapmc.git
cd yapmc

# Build with Maven
./mvnw clean package

# Run directly
java -jar target/yapmc-1.0-SNAPSHOT.jar

# Or create your own DMG
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

# Sign it with your own ad-hoc signature
codesign --force --deep --sign - YAPMC.app
```

## Need Help?

If you're still having issues:
1. Check [GitHub Issues](https://github.com/hdhensley/yapmc/issues)
2. Start a [Discussion](https://github.com/hdhensley/yapmc/discussions)
3. The community is here to help!

---

**Remember**: This warning appears for ALL open-source apps not distributed through the App Store. It doesn't mean the app is unsafe - it just means Apple hasn't verified it (which requires paying them $99/year).

